package com.example.util

data class Llm7UsageStats(
    val requestsLastMinute: Int,
    val requestsLastHour: Int,
    val tokensUsed24h: Long,
    val tokenLimit24h: Long = 500_000L,
    val isMinLimitReached: Boolean,
    val isHourLimitReached: Boolean,
    val isTokenLimitReached: Boolean,
    val isSecLimitReached: Boolean,
    val averageResponseTimeMs: Long = 0L,
    val malformedCount: Int = 0,
    val fallbackCount: Int = 0
)

object Llm7RateLimitTracker {

    fun canSendRequest(estimatedTokens: Int = 1000): Pair<Boolean, String> {
        return ProviderRateLimitTracker.canSendRequest("llm7", estimatedTokens)
    }

    fun recordRequest(tokensUsed: Long, responseTimeMs: Long = 0L) {
        ProviderRateLimitTracker.recordRequest("llm7", tokensUsed, responseTimeMs)
    }

    fun recordMalformedOutput() {
        ProviderRateLimitTracker.recordMalformedOutput("llm7")
    }

    fun recordFallbackTrigger() {
        ProviderRateLimitTracker.recordFallbackTrigger("llm7")
    }

    fun getUsageStats(): Llm7UsageStats {
        val stats = ProviderRateLimitTracker.getUsageStats("llm7")
        return Llm7UsageStats(
            requestsLastMinute = stats.requestsLastMinute,
            requestsLastHour = stats.requestsLastHour,
            tokensUsed24h = stats.tokensUsed24h,
            tokenLimit24h = stats.tokenLimit24h,
            isMinLimitReached = stats.requestsLastMinute >= 9,
            isHourLimitReached = stats.requestsLastHour >= 54,
            isTokenLimitReached = stats.tokensUsed24h >= 450_000L,
            isSecLimitReached = stats.isLimitApproaching,
            averageResponseTimeMs = stats.averageResponseTimeMs,
            malformedCount = stats.malformedCount,
            fallbackCount = stats.fallbackCount
        )
    }
}
