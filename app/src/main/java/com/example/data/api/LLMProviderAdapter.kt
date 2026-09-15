package com.example.data.api

import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class TokenUsage(
    val promptTokens: Long = 0L,
    val candidateTokens: Long = 0L
)

data class LLMResponse(
    val text: String,
    val toolCalls: List<ParsedMemoryToolCall>? = null,
    val rawStateBlock: String? = null,
    val usage: TokenUsage? = null,
    val usedProvider: String
)

enum class ReliabilityTier {
    PRIMARY,
    SECONDARY,
    STABLE_FREE,
    STABLE_KEY
}

interface LLMProviderAdapter {
    val providerName: String
    val reliabilityTier: ReliabilityTier get() = ReliabilityTier.PRIMARY
    fun supportsFunctionCalling(): Boolean
    fun supportsStreaming(): Boolean
    suspend fun sendMessage(
        systemPrompt: String,
        messages: List<MessageEntity>,
        tools: List<MemoryToolDefinition>?
    ): LLMResponse
}

// Regex to extract STATE blocks when function calling is not supported or not triggered
val STATE_BLOCK_REGEX = Regex("\\[\\[STATE(?:_JSON)?\\s*(\\{.*?\\}|[^\\]]+)\\]\\]", RegexOption.DOT_MATCHES_ALL)

fun extractRawStateBlock(text: String): String? {
    val match = STATE_BLOCK_REGEX.find(text)
    return match?.value
}

// 1. Generic OpenAI Compatible Adapter (Works for OpenAI, DeepSeek, Groq, LLM7, and OpenAI-formatted Custom Providers)
class GenericOpenAICompatibleAdapter(
    private val endpointUrl: String,
    private val apiKey: String,
    private val model: String,
    override val providerName: String,
    private val supportsFC: Boolean = true,
    override val reliabilityTier: ReliabilityTier = ReliabilityTier.PRIMARY
) : LLMProviderAdapter {

    override fun supportsFunctionCalling(): Boolean = supportsFC
    override fun supportsStreaming(): Boolean = false

    override suspend fun sendMessage(
        systemPrompt: String,
        messages: List<MessageEntity>,
        tools: List<MemoryToolDefinition>?
    ): LLMResponse {
        val root = JSONObject()
        root.put("model", model)
        root.put("max_tokens", 4096)

        val messagesArr = JSONArray()
        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            }
            messagesArr.put(sysObj)
        }

        for (msg in messages) {
            val role = if (msg.role == "assistant") "assistant" else "user"
            val msgObj = JSONObject().apply {
                put("role", role)
                put("content", msg.text)
            }
            messagesArr.put(msgObj)
        }
        root.put("messages", messagesArr)

        if (supportsFunctionCalling() && !tools.isNullOrEmpty()) {
            root.put("tools", MemoryToolRegistry.toOpenAiToolsJsonArray(tools))
        }

        val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val reqBuilder = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)

        if (apiKey.isNotBlank() && apiKey != "unused") {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = reqBuilder.build()

        return RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val code = response.code
                if (endpointUrl.contains("nvidia.com") || providerName.contains("nvidia")) {
                    if (code == 410 || code == 404 || bodyStr.contains("gone", ignoreCase = true) || bodyStr.contains("not found", ignoreCase = true)) {
                        throw IllegalStateException("Bu model artık kullanılamıyor, güncel model listesini görmek için 'Modelleri Yenile'ye bas.")
                    } else if (code == 402 || bodyStr.contains("quota", ignoreCase = true) || bodyStr.contains("credit", ignoreCase = true) || bodyStr.contains("insufficient", ignoreCase = true) || bodyStr.contains("balance", ignoreCase = true)) {
                        throw IllegalStateException("NVIDIA ücretsiz krediniz tükenmiş olabilir (HTTP $code). Lütfen build.nvidia.com adresinden yeni bir API Key alın.")
                    } else if (code == 429) {
                        throw IllegalStateException("NVIDIA 429 Rate Limit sınırı aşıldı (Dakikada 40 istek sınırı).")
                    }
                } else if (endpointUrl.contains("pollinations") || providerName.contains("pollinations")) {
                    throw IllegalStateException("Pollinations API Hatası (HTTP $code): ${bodyStr.take(250)}")
                }
                throw IllegalStateException("OpenAI Compatible API Hatası ($code): $bodyStr")
            }

            val respJson = JSONObject(bodyStr)
            val choices = respJson.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                throw IllegalStateException("API boş yanıt döndürdü (choices boş).")
            }

            val firstChoice = choices.getJSONObject(0)
            val finishReason = firstChoice.optString("finish_reason", "")
            val messageObj = firstChoice.optJSONObject("message") ?: JSONObject()
            val refusal = messageObj.optString("refusal", "")

            if (finishReason == "content_filter" || refusal.isNotBlank()) {
                val detail = if (refusal.isNotBlank()) refusal else "içerik filtresi (content_filter)"
                throw IllegalStateException("İçerik kısıtlaması politikası nedeniyle yanıt engellendi: $detail")
            }

            val (text, parsedTools) = MemoryToolRegistry.parseOpenAiToolCalls(messageObj)

            if (providerName == "pollinations" || endpointUrl.contains("pollinations")) {
                if (text.contains("budget", ignoreCase = true) ||
                    text.contains("queue full", ignoreCase = true) ||
                    text.contains("rate limit", ignoreCase = true) ||
                    text.contains("unauthorized", ignoreCase = true) ||
                    text.contains("get unlimited access", ignoreCase = true) ||
                    text.contains("reached its budget", ignoreCase = true)
                ) {
                    throw IllegalStateException("Pollinations Bütçe/Sıra Sınırı Hatası: ${text.take(200)}")
                }
            }

            val usageObj = respJson.optJSONObject("usage")
            val pTokens = usageObj?.optLong("prompt_tokens", 0L) ?: 0L
            val cTokens = usageObj?.optLong("completion_tokens", 0L) ?: 0L

            val rawState = extractRawStateBlock(text)

            LLMResponse(
                text = text,
                toolCalls = parsedTools.ifEmpty { null },
                rawStateBlock = rawState,
                usage = TokenUsage(pTokens, cTokens),
                usedProvider = providerName
            )
        }
    }
}

// 2. Generic Anthropic (Claude) Compatible Adapter
class GenericAnthropicCompatibleAdapter(
    private val apiKey: String,
    private val model: String,
    override val providerName: String = "claude",
    private val baseUrl: String = "https://api.anthropic.com/v1",
    private val supportsFC: Boolean = true
) : LLMProviderAdapter {

    override fun supportsFunctionCalling(): Boolean = supportsFC
    override fun supportsStreaming(): Boolean = false

    override suspend fun sendMessage(
        systemPrompt: String,
        messages: List<MessageEntity>,
        tools: List<MemoryToolDefinition>?
    ): LLMResponse {
        val formattedUrl = when {
            baseUrl.endsWith("/messages") -> baseUrl
            baseUrl.endsWith("/") -> "${baseUrl}messages"
            else -> "$baseUrl/messages"
        }

        val root = JSONObject()
        root.put("model", model)
        root.put("max_tokens", 4096)

        if (systemPrompt.isNotBlank()) {
            val sysArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", systemPrompt)
                    put("cache_control", JSONObject().put("type", "ephemeral"))
                })
            }
            root.put("system", sysArray)
        }

        val msgsArr = JSONArray()
        for (m in messages) {
            val role = if (m.role == "assistant") "assistant" else "user"
            val mObj = JSONObject().apply {
                put("role", role)
                put("content", m.text)
            }
            msgsArr.put(mObj)
        }
        root.put("messages", msgsArr)

        if (supportsFunctionCalling() && !tools.isNullOrEmpty()) {
            root.put("tools", MemoryToolRegistry.toClaudeToolsJsonArray(tools))
        }

        val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val reqBuilder = Request.Builder()
            .url(formattedUrl)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)

        if (!apiKey.startsWith("sk-ant")) {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val request = reqBuilder.build()

        return RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw IllegalStateException("Claude API Hatası (${response.code}): $bodyStr")
            }

            val jsonResp = JSONObject(bodyStr)
            val (text, parsedTools) = MemoryToolRegistry.parseClaudeToolCalls(jsonResp)

            val usageObj = jsonResp.optJSONObject("usage")
            val pTokens = usageObj?.optLong("input_tokens", 0L) ?: 0L
            val cTokens = usageObj?.optLong("output_tokens", 0L) ?: 0L

            val rawState = extractRawStateBlock(text)

            LLMResponse(
                text = text,
                toolCalls = parsedTools.ifEmpty { null },
                rawStateBlock = rawState,
                usage = TokenUsage(pTokens, cTokens),
                usedProvider = providerName
            )
        }
    }
}

// 3. Native Gemini Adapter
class GeminiAdapter(
    private val apiKey: String,
    private val model: String,
    override val providerName: String = "gemini",
    private val enableNsfw: Boolean = true,
    private val supportsFC: Boolean = true
) : LLMProviderAdapter {

    override fun supportsFunctionCalling(): Boolean = supportsFC
    override fun supportsStreaming(): Boolean = false

    override suspend fun sendMessage(
        systemPrompt: String,
        messages: List<MessageEntity>,
        tools: List<MemoryToolDefinition>?
    ): LLMResponse {
        val geminiContents = messages.map { m ->
            val role = if (m.role == "assistant") "model" else "user"
            GeminiContent(role = role, parts = listOf(GeminiPart(text = m.text)))
        }

        val sysInstruction = if (systemPrompt.isNotBlank()) {
            GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        } else null

        val safetySettings = if (enableNsfw) {
            listOf(
                GeminiSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
                GeminiSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
                GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
                GeminiSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE")
            )
        } else null

        val geminiTools = if (supportsFunctionCalling() && !tools.isNullOrEmpty()) {
            MemoryToolRegistry.toGeminiTools(tools)
        } else null

        val request = GeminiRequest(
            contents = geminiContents,
            systemInstruction = sysInstruction,
            generationConfig = GeminiGenerationConfig(temperature = 0.85f),
            safetySettings = safetySettings,
            tools = geminiTools
        )

        val response = RetrofitClient.service.generateContent(
            model = model.ifBlank { "gemini-2.5-flash" },
            apiKey = apiKey,
            request = request
        )

        val candidate = response.candidates?.firstOrNull()
            ?: throw IllegalStateException("Gemini API yanıt döndürmedi (candidates boş veya filtre engelledi).")

        val finishReason = candidate.finishReason ?: ""
        if (finishReason == "SAFETY" || finishReason == "RECITATION" || finishReason == "BLOCKLIST") {
            throw IllegalStateException("Gemini yanıtı güvenlik filtresi ($finishReason) nedeniyle engellendi.")
        }

        val (text, parsedTools) = MemoryToolRegistry.parseGeminiToolCalls(candidate)

        val pTokens = response.usageMetadata?.promptTokenCount?.toLong() ?: 0L
        val cTokens = response.usageMetadata?.candidatesTokenCount?.toLong() ?: 0L

        val rawState = extractRawStateBlock(text)

        return LLMResponse(
            text = text,
            toolCalls = parsedTools.ifEmpty { null },
            rawStateBlock = rawState,
            usage = TokenUsage(pTokens, cTokens),
            usedProvider = providerName
        )
    }
}

// Factory helper to construct LLMProviderAdapter instance dynamically
object LLMAdapterFactory {
    fun createAdapter(
        providerKey: String,
        modelName: String,
        settings: com.example.data.local.UserSettingsEntity,
        customProvider: com.example.data.local.CustomProviderEntity? = null,
        buildConfigGeminiKey: String = ""
    ): LLMProviderAdapter {
        return when {
            providerKey == "llm7" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.llm7.io/v1/chat/completions",
                    apiKey = "unused",
                    model = "default",
                    providerName = "llm7",
                    supportsFC = false,
                    reliabilityTier = ReliabilityTier.SECONDARY
                )
            }
            providerKey == "pollinations" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://text.pollinations.ai/openai/chat/completions",
                    apiKey = "unused",
                    model = "openai",
                    providerName = "pollinations",
                    supportsFC = false,
                    reliabilityTier = ReliabilityTier.SECONDARY
                )
            }
            providerKey == "ovh" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://llama-3-70b-instruct.endpoints.kepler.ai.cloud.ovh.net/v1/chat/completions",
                    apiKey = "unused",
                    model = "meta-llama/Meta-Llama-3-70B-Instruct",
                    providerName = "ovh",
                    supportsFC = false,
                    reliabilityTier = ReliabilityTier.SECONDARY
                )
            }
            providerKey == "openrouter" -> {
                val apiKey = settings.openRouterApiKey
                if (apiKey.isBlank()) throw IllegalStateException("OpenRouter API Key eksik.")
                val mName = when {
                    modelName.contains("/") -> modelName
                    settings.openRouterModel.contains("/") -> settings.openRouterModel
                    else -> "deepseek/deepseek-chat"
                }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "openrouter",
                    supportsFC = true,
                    reliabilityTier = ReliabilityTier.STABLE_FREE
                )
            }
            providerKey == "nvidia" -> {
                val apiKey = settings.nvidiaApiKey
                if (apiKey.isBlank()) throw IllegalStateException("NVIDIA NIM API Key eksik.")
                val mName = when {
                    modelName.contains("/") -> modelName
                    settings.nvidiaModel.contains("/") -> settings.nvidiaModel
                    else -> "meta/llama-3.3-70b-instruct"
                }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://integrate.api.nvidia.com/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "nvidia",
                    supportsFC = true,
                    reliabilityTier = ReliabilityTier.STABLE_FREE
                )
            }
            providerKey == "mistral" -> {
                val apiKey = settings.mistralApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Mistral API Key eksik.")
                val mName = when {
                    modelName.contains("mistral") || modelName.contains("pixtral") -> modelName
                    settings.mistralModel.isNotBlank() -> settings.mistralModel
                    else -> "mistral-large-latest"
                }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.mistral.ai/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "mistral",
                    supportsFC = true,
                    reliabilityTier = ReliabilityTier.STABLE_FREE
                )
            }
            providerKey.startsWith("custom_") -> {
                if (customProvider != null) {
                    val apiKey = com.example.util.KeystoreEncryptionManager.decrypt(customProvider.apiKeyEncrypted)
                    val rawUrl = customProvider.baseUrl.trim()
                    val baseUrlWithScheme = when {
                        rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
                        else -> "https://$rawUrl"
                    }
                    if (customProvider.apiFormat == "anthropic") {
                        GenericAnthropicCompatibleAdapter(
                            apiKey = apiKey,
                            model = customProvider.modelName,
                            providerName = "custom_${customProvider.id}",
                            baseUrl = baseUrlWithScheme,
                            supportsFC = customProvider.supportsFunctionCalling
                        )
                    } else {
                        val endpointUrl = when {
                            baseUrlWithScheme.endsWith("/chat/completions") -> baseUrlWithScheme
                            baseUrlWithScheme.endsWith("/") -> "${baseUrlWithScheme}chat/completions"
                            else -> "${baseUrlWithScheme}/chat/completions"
                        }
                        GenericOpenAICompatibleAdapter(
                            endpointUrl = endpointUrl,
                            apiKey = apiKey,
                            model = customProvider.modelName,
                            providerName = "custom_${customProvider.id}",
                            supportsFC = customProvider.supportsFunctionCalling
                        )
                    }
                } else {
                    throw IllegalStateException("Seçili Özel Sağlayıcı (Custom Provider) veritabanında bulunamadı.")
                }
            }
            providerKey == "groq" -> {
                val apiKey = settings.groqApiKey.ifBlank { settings.customApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("Groq API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden Groq API Key girin.")
                val mName = when {
                    modelName.startsWith("openai/") || modelName.startsWith("qwen/") -> modelName
                    modelName.contains("mixtral") -> "mixtral-8x7b-32768"
                    modelName.contains("gemma") -> "gemma2-9b-it"
                    settings.groqModel.isNotBlank() && (settings.groqModel.startsWith("openai/") || settings.groqModel.startsWith("qwen/")) -> settings.groqModel
                    else -> "openai/gpt-oss-120b"
                }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "groq",
                    supportsFC = true
                )
            }
            providerKey == "claude" -> {
                val apiKey = settings.claudeApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Claude API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden Claude API Key girin.")
                val mName = when {
                    modelName.contains("claude") -> modelName
                    settings.claudeModel.contains("claude") -> settings.claudeModel
                    else -> "claude-3-5-sonnet-20241022"
                }
                GenericAnthropicCompatibleAdapter(
                    apiKey = apiKey,
                    model = mName,
                    providerName = "claude",
                    supportsFC = true
                )
            }
            providerKey == "openai" -> {
                val apiKey = settings.openaiApiKey
                if (apiKey.isBlank()) throw IllegalStateException("OpenAI API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden OpenAI API Key girin.")
                val mName = when {
                    modelName.startsWith("gpt-") || modelName.startsWith("o1") || modelName.startsWith("o3") -> modelName
                    settings.openaiModel.startsWith("gpt-") || settings.openaiModel.startsWith("o1") || settings.openaiModel.startsWith("o3") -> settings.openaiModel
                    else -> "gpt-4o"
                }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.openai.com/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "openai",
                    supportsFC = true
                )
            }
            providerKey == "deepseek" -> {
                val apiKey = settings.openaiApiKey.ifBlank { settings.groqApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("DeepSeek API Key (OpenAI API Key alanına yazılan) eksik.")
                val mName = if (modelName.contains("reasoner") || modelName.contains("r1")) "deepseek-reasoner" else "deepseek-chat"
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.deepseek.com/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "deepseek",
                    supportsFC = true
                )
            }
            else -> { // Default Gemini
                val customKey = settings.customApiKey.trim()
                val buildKey = buildConfigGeminiKey.trim()
                val primaryKey = when {
                    customKey.isNotBlank() -> customKey
                    buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY" -> buildKey
                    else -> ""
                }
                if (primaryKey.isBlank()) {
                    throw IllegalStateException("Gemini API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden Gemini API Key girin.")
                }
                val rawGModel = if (modelName.contains("gemini")) modelName else settings.geminiModel.ifBlank { "gemini-2.0-flash" }
                val gModelName = when {
                    rawGModel.contains("2.5") || rawGModel.contains("3.5") || rawGModel == "gemini-1.5-flash" -> "gemini-2.0-flash"
                    rawGModel.contains("pro") -> "gemini-1.5-pro"
                    else -> "gemini-2.0-flash"
                }
                GeminiAdapter(
                    apiKey = primaryKey,
                    model = gModelName,
                    providerName = "gemini",
                    enableNsfw = settings.enableNsfw,
                    supportsFC = true
                )
            }
        }
    }
}
