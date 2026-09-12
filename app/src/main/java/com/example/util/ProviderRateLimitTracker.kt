package com.example.util

import com.example.data.api.ReliabilityTier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

enum class QuotaType { RENEWING, EXHAUSTIBLE }

data class ProviderLimitConfig(
    val maxReqPerSec: Int = 2,
    val maxReqPerMin: Int = 25,
    val maxReqPerHour: Int = 200,
    val maxTokens24h: Long = 500_000L,
    val quotaType: QuotaType = QuotaType.RENEWING,
    val totalCreditsMax: Long = 1000L
)

data class ProviderUsageStats(
    val providerKey: String,
    val displayName: String,
    val reliabilityTier: ReliabilityTier,
    val requestsLastMinute: Int,
    val requestsLastHour: Int,
    val tokensUsed24h: Long,
    val tokenLimit24h: Long,
    val isLimitApproaching: Boolean,
    val limitReason: String = "",
    val averageResponseTimeMs: Long = 0L,
    val malformedCount: Int = 0,
    val fallbackCount: Int = 0,
    val totalRequestsCount: Int = 0,
    val quotaType: QuotaType = QuotaType.RENEWING,
    val creditsRemaining: Long = 1000L
)

private class ProviderTrackerState(
    val config: ProviderLimitConfig
) {
    val requestTimestamps = ConcurrentLinkedQueue<Long>()
    val tokenUsageLog = ConcurrentLinkedQueue<Pair<Long, Long>>() // timestamp -> token count
    val responseTimeLog = ConcurrentLinkedQueue<Long>()

    @Volatile var lastRequestTime: Long = 0L
    @Volatile var malformedOutputCount: Int = 0
    @Volatile var fallbackTriggerCount: Int = 0
    @Volatile var totalRequestsCount: Int = 0
    @Volatile var creditsUsed: Long = 0L

    fun pruneExpiredLogs(now: Long) {
        requestTimestamps.removeIf { now - it > 3_600_000L }
        tokenUsageLog.removeIf { now - it.first > 86_400_000L }
    }

    fun getTokensUsedIn24h(now: Long): Long {
        return tokenUsageLog.filter { now - it.first <= 86_400_000L }.sumOf { it.second }
    }
}

object ProviderRateLimitTracker {

    private val trackerMap = ConcurrentHashMap<String, ProviderTrackerState>()

    private val providerConfigs = mapOf(
        "llm7" to ProviderLimitConfig(maxReqPerSec = 1, maxReqPerMin = 10, maxReqPerHour = 60, maxTokens24h = 500_000L),
        "pollinations" to ProviderLimitConfig(maxReqPerSec = 2, maxReqPerMin = 20, maxReqPerHour = 120, maxTokens24h = 500_000L),
        "opencode_zen" to ProviderLimitConfig(maxReqPerSec = 2, maxReqPerMin = 20, maxReqPerHour = 120, maxTokens24h = 500_000L),
        "openrouter" to ProviderLimitConfig(maxReqPerSec = 2, maxReqPerMin = 30, maxReqPerHour = 200, maxTokens24h = 300_000L),
        "nvidia" to ProviderLimitConfig(maxReqPerSec = 2, maxReqPerMin = 40, maxReqPerHour = 300, maxTokens24h = 1_000_000L, quotaType = QuotaType.EXHAUSTIBLE, totalCreditsMax = 1000L),
        "mistral" to ProviderLimitConfig(maxReqPerSec = 3, maxReqPerMin = 30, maxReqPerHour = 300, maxTokens24h = 1_000_000L)
    )

    private fun getOrCreateState(providerKey: String): ProviderTrackerState {
        return trackerMap.computeIfAbsent(providerKey) { key ->
            val cfg = providerConfigs[key] ?: ProviderLimitConfig()
            ProviderTrackerState(cfg)
        }
    }

    @Synchronized
    fun canSendRequest(providerKey: String, estimatedTokens: Int = 1000): Pair<Boolean, String> {
        val state = getOrCreateState(providerKey)
        val cfg = state.config
        val now = System.currentTimeMillis()

        // 1. Check min gap between requests (e.g. 1s for llm7, 500ms for others)
        val minGapMs = if (cfg.maxReqPerSec == 1) 1000L else 400L
        if (now - state.lastRequestTime < minGapMs) {
            return Pair(false, "Saniye sınırı ($providerKey) - İki istek arası çok hızlı")
        }

        state.pruneExpiredLogs(now)

        // 2. Check 1-minute limit (threshold 90%)
        val safeMinLimit = (cfg.maxReqPerMin * 0.9).toInt().coerceAtLeast(1)
        val countMin = state.requestTimestamps.count { now - it <= 60_000L }
        if (countMin >= safeMinLimit) {
            return Pair(false, "Dakikalık kota dolmak üzere ($countMin/${cfg.maxReqPerMin} req/min)")
        }

        // 3. Check 1-hour limit (threshold 90%)
        val safeHourLimit = (cfg.maxReqPerHour * 0.9).toInt().coerceAtLeast(1)
        val countHour = state.requestTimestamps.count { now - it <= 3_600_000L }
        if (countHour >= safeHourLimit) {
            return Pair(false, "Saatlik kota dolmak üzere ($countHour/${cfg.maxReqPerHour} req/hour)")
        }

        // 4. Check 24-hour token limit
        val safeTokenLimit = (cfg.maxTokens24h * 0.9).toLong()
        val current24hTokens = state.getTokensUsedIn24h(now)
        if (current24hTokens + estimatedTokens > safeTokenLimit) {
            return Pair(false, "24 Saatlik token kotası dolmak üzere ($current24hTokens/${cfg.maxTokens24h} token)")
        }

        // 5. Check Exhaustible credits limit (if applicable like NVIDIA)
        if (cfg.quotaType == QuotaType.EXHAUSTIBLE) {
            if (state.creditsUsed >= cfg.totalCreditsMax) {
                return Pair(false, "Toplam NVIDIA kredileri tükendi (${state.creditsUsed}/${cfg.totalCreditsMax})")
            }
        }

        return Pair(true, "OK")
    }

    @Synchronized
    fun recordRequest(providerKey: String, tokensUsed: Long, responseTimeMs: Long = 0L) {
        val state = getOrCreateState(providerKey)
        val now = System.currentTimeMillis()
        state.lastRequestTime = now
        state.totalRequestsCount++
        state.requestTimestamps.add(now)
        if (tokensUsed > 0) {
            state.tokenUsageLog.add(Pair(now, tokensUsed))
            if (state.config.quotaType == QuotaType.EXHAUSTIBLE) {
                state.creditsUsed += (tokensUsed / 200L).coerceAtLeast(1L)
            }
        }
        if (responseTimeMs > 0) {
            state.responseTimeLog.add(responseTimeMs)
            if (state.responseTimeLog.size > 100) state.responseTimeLog.poll()
        }
        state.pruneExpiredLogs(now)
    }

    @Synchronized
    fun recordMalformedOutput(providerKey: String) {
        val state = getOrCreateState(providerKey)
        state.malformedOutputCount++
    }

    @Synchronized
    fun recordFallbackTrigger(providerKey: String) {
        val state = getOrCreateState(providerKey)
        state.fallbackTriggerCount++
    }

    fun getDisplayName(providerKey: String): String {
        return when (providerKey) {
            "llm7" -> "LLM7 (Ücretsiz)"
            "pollinations" -> "Pollinations AI"
            "opencode_zen" -> "OpenCode Zen"
            "openrouter" -> "OpenRouter Free"
            "nvidia" -> "NVIDIA NIM"
            "mistral" -> "Mistral AI Free"
            else -> providerKey.replaceFirstChar { it.uppercase() }
        }
    }

    fun getUsageStats(providerKey: String): ProviderUsageStats {
        val state = getOrCreateState(providerKey)
        val cfg = state.config
        val now = System.currentTimeMillis()
        val countMin = state.requestTimestamps.count { now - it <= 60_000L }
        val countHour = state.requestTimestamps.count { now - it <= 3_600_000L }
        val tokens24h = state.getTokensUsedIn24h(now)
        val avgResp = if (state.responseTimeLog.isNotEmpty()) state.responseTimeLog.average().toLong() else 0L

        val (canSend, reason) = canSendRequest(providerKey, 0)

        return ProviderUsageStats(
            providerKey = providerKey,
            displayName = getDisplayName(providerKey),
            reliabilityTier = ReliabilityTier.SECONDARY,
            requestsLastMinute = countMin,
            requestsLastHour = countHour,
            tokensUsed24h = tokens24h,
            tokenLimit24h = cfg.maxTokens24h,
            isLimitApproaching = !canSend,
            limitReason = reason,
            averageResponseTimeMs = avgResp,
            malformedCount = state.malformedOutputCount,
            fallbackCount = state.fallbackTriggerCount,
            totalRequestsCount = state.totalRequestsCount,
            quotaType = cfg.quotaType,
            creditsRemaining = (cfg.totalCreditsMax - state.creditsUsed).coerceAtLeast(0L)
        )
    }

    fun getAllSecondaryStats(): List<ProviderUsageStats> {
        val secondaryKeys = listOf("llm7", "pollinations", "opencode_zen", "openrouter", "nvidia", "mistral")
        return secondaryKeys.map { getUsageStats(it) }
    }
}
