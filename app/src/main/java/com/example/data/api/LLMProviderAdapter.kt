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

interface LLMProviderAdapter {
    val providerName: String
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
    private val supportsFC: Boolean = true
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
                    if (code == 402 || bodyStr.contains("quota", ignoreCase = true) || bodyStr.contains("credit", ignoreCase = true) || bodyStr.contains("insufficient", ignoreCase = true) || bodyStr.contains("balance", ignoreCase = true)) {
                        throw IllegalStateException("NVIDIA ücretsiz krediniz tükenmiş olabilir (HTTP $code). Lütfen build.nvidia.com adresinden yeni bir API Key alın.")
                    } else if (code == 429) {
                        throw IllegalStateException("NVIDIA 429 Rate Limit sınırı aşıldı (Dakikada 40 istek sınırı).")
                    }
                } else if (endpointUrl.contains("github.ai") || providerName.contains("github")) {
                    if (code == 401 || bodyStr.contains("bad credentials", ignoreCase = true) || bodyStr.contains("expired", ignoreCase = true) || bodyStr.contains("unauthorized", ignoreCase = true)) {
                        throw IllegalStateException("GitHub token'ınızın süresi dolmuş veya geçersiz olabilir (HTTP 401). Yeni bir Personal Access Token (PAT) oluşturun.")
                    }
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
            root.put("system", systemPrompt)
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
            model = model,
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
                    supportsFC = true
                )
            }
            providerKey == "pollinations" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://text.pollinations.ai/openai/chat/completions",
                    apiKey = "unused",
                    model = modelName.ifBlank { "openai" },
                    providerName = "pollinations",
                    supportsFC = false
                )
            }
            providerKey == "opencode_zen" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://opencode.ai/zen/v1/chat/completions",
                    apiKey = "unused",
                    model = modelName.ifBlank { "deepseek-v4-flash-free" },
                    providerName = "opencode_zen",
                    supportsFC = true
                )
            }
            providerKey == "ovh" -> {
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://oai.endpoints.kepler.ai.cloud.ovh.net/v1/chat/completions",
                    apiKey = "unused",
                    model = modelName.ifBlank { "meta-llama/Meta-Llama-3-70B-Instruct" },
                    providerName = "ovh",
                    supportsFC = false
                )
            }
            providerKey == "openrouter" -> {
                val apiKey = settings.openRouterApiKey
                if (apiKey.isBlank()) throw IllegalStateException("OpenRouter API Key eksik.")
                val mName = modelName.ifBlank { settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" } }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "openrouter",
                    supportsFC = true
                )
            }
            providerKey == "nvidia" -> {
                val apiKey = settings.nvidiaApiKey
                if (apiKey.isBlank()) throw IllegalStateException("NVIDIA NIM API Key eksik.")
                val mName = modelName.ifBlank { settings.nvidiaModel.ifBlank { "deepseek-ai/deepseek-v4-flash" } }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://integrate.api.nvidia.com/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "nvidia",
                    supportsFC = true
                )
            }
            providerKey == "github" -> {
                val apiKey = settings.githubPatToken
                if (apiKey.isBlank()) throw IllegalStateException("GitHub Personal Access Token (PAT) eksik.")
                val mName = modelName.ifBlank { settings.githubModel.ifBlank { "openai/gpt-4o" } }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://models.inference.ai.azure.com/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "github",
                    supportsFC = true
                )
            }
            providerKey == "mistral" -> {
                val apiKey = settings.mistralApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Mistral API Key eksik.")
                val mName = modelName.ifBlank { settings.mistralModel.ifBlank { "mistral-large-latest" } }
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.mistral.ai/v1/chat/completions",
                    apiKey = apiKey,
                    model = mName,
                    providerName = "mistral",
                    supportsFC = true
                )
            }
            providerKey.startsWith("custom_") -> {
                if (customProvider != null) {
                    val apiKey = com.example.util.KeystoreEncryptionManager.decrypt(customProvider.apiKeyEncrypted)
                    if (customProvider.apiFormat == "anthropic") {
                        GenericAnthropicCompatibleAdapter(
                            apiKey = apiKey,
                            model = customProvider.modelName,
                            providerName = "custom_${customProvider.id}",
                            baseUrl = customProvider.baseUrl,
                            supportsFC = customProvider.supportsFunctionCalling
                        )
                    } else {
                        val endpointUrl = when {
                            customProvider.baseUrl.endsWith("/chat/completions") -> customProvider.baseUrl
                            customProvider.baseUrl.endsWith("/") -> "${customProvider.baseUrl}chat/completions"
                            else -> "${customProvider.baseUrl}/chat/completions"
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
            providerKey == "groq" || modelName.contains("llama") || modelName.contains("groq") || modelName.contains("mixtral") -> {
                val apiKey = settings.groqApiKey.ifBlank { settings.customApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("Groq API Key eksik.")
                GenericOpenAICompatibleAdapter(
                    endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    model = modelName,
                    providerName = "groq",
                    supportsFC = true
                )
            }
            providerKey == "claude" || modelName.contains("claude") -> {
                val apiKey = settings.claudeApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Claude API Key eksik.")
                GenericAnthropicCompatibleAdapter(
                    apiKey = apiKey,
                    model = modelName,
                    providerName = "claude",
                    supportsFC = true
                )
            }
            providerKey == "openai" || providerKey == "deepseek" || modelName.contains("gpt") || modelName.contains("deepseek") -> {
                val isDeepseek = modelName.contains("deepseek") || providerKey == "deepseek"
                val url = if (isDeepseek) "https://api.deepseek.com/chat/completions" else "https://api.openai.com/v1/chat/completions"
                val apiKey = settings.openaiApiKey.ifBlank { settings.groqApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("OpenAI/DeepSeek API Key eksik.")
                GenericOpenAICompatibleAdapter(
                    endpointUrl = url,
                    apiKey = apiKey,
                    model = modelName,
                    providerName = if (isDeepseek) "deepseek" else "openai",
                    supportsFC = true
                )
            }
            else -> { // Default Gemini
                val customKey = settings.customApiKey.trim()
                val primaryKey = if (customKey.isNotBlank()) customKey else buildConfigGeminiKey
                if (primaryKey.isBlank() || primaryKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("Gemini API Key eksik.")
                }
                GeminiAdapter(
                    apiKey = primaryKey,
                    model = modelName,
                    providerName = "gemini",
                    enableNsfw = settings.enableNsfw,
                    supportsFC = true
                )
            }
        }
    }
}
