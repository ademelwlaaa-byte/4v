package com.example.data.api

import org.json.JSONArray
import org.json.JSONObject

data class MemoryToolProperty(
    val type: String = "string",
    val description: String,
    val enum: List<String>? = null
)

data class MemoryToolDefinition(
    val name: String,
    val description: String,
    val properties: Map<String, MemoryToolProperty>,
    val required: List<String>
)

data class ParsedMemoryToolCall(
    val name: String,
    val arguments: Map<String, Any>
)

data class ModelResponseResult(
    val text: String,
    val toolCalls: List<ParsedMemoryToolCall> = emptyList(),
    val promptTokens: Long = 0L,
    val candidateTokens: Long = 0L
)

object MemoryToolRegistry {
    val CENTRAL_TOOLS: List<MemoryToolDefinition> = listOf(
        MemoryToolDefinition(
            name = "save_memory",
            description = "Konuşma sırasında önemli, kalıcı bir bilgi veya olay fark ettiğinde bunu hafızaya kaydeder.",
            properties = mapOf(
                "content" to MemoryToolProperty(
                    type = "string",
                    description = "Hatırlanacak bilgi veya olay açıklaması"
                ),
                "category" to MemoryToolProperty(
                    type = "string",
                    description = "Bilgi türü: fact (sabit bilgi) veya event (yaşanan olay)",
                    enum = listOf("fact", "event")
                ),
                "importance" to MemoryToolProperty(
                    type = "string",
                    description = "Önem derecesi 1-10"
                )
            ),
            required = listOf("content", "category")
        ),
        MemoryToolDefinition(
            name = "update_memory",
            description = "Var olan bir hafıza kaydını günceller.",
            properties = mapOf(
                "memoryId" to MemoryToolProperty(
                    type = "string",
                    description = "Güncellenecek hafıza kaydının ID'si"
                ),
                "newContent" to MemoryToolProperty(
                    type = "string",
                    description = "Yeni içeriği"
                )
            ),
            required = listOf("memoryId", "newContent")
        ),
        MemoryToolDefinition(
            name = "delete_memory",
            description = "Geçersiz veya silinmesi gereken bir hafıza kaydını siler.",
            properties = mapOf(
                "memoryId" to MemoryToolProperty(
                    type = "string",
                    description = "Silinecek hafıza kaydının ID'si"
                ),
                "reason" to MemoryToolProperty(
                    type = "string",
                    description = "Silme nedeni"
                )
            ),
            required = listOf("memoryId")
        )
    )

    // 1) Gemini Mapper (GeminiTool format)
    fun toGeminiTools(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): List<GeminiTool> {
        val decls = definitions.map { def ->
            val props = def.properties.mapValues { (_, prop) ->
                GeminiFunctionProperty(
                    type = prop.type.uppercase(),
                    description = prop.description,
                    enum = prop.enum
                )
            }
            GeminiFunctionDeclaration(
                name = def.name,
                description = def.description,
                parameters = GeminiFunctionParameters(
                    type = "OBJECT",
                    properties = props,
                    required = def.required
                )
            )
        }
        return listOf(GeminiTool(functionDeclarations = decls))
    }

    // 2) Claude (Anthropic) Mapper: input_schema + lowercase "object" / "string"
    fun toClaudeToolsJsonArray(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): JSONArray {
        val array = JSONArray()
        for (def in definitions) {
            val toolObj = JSONObject()
            toolObj.put("name", def.name)
            toolObj.put("description", def.description)

            val inputSchema = JSONObject()
            inputSchema.put("type", "object")

            val propsObj = JSONObject()
            for ((key, prop) in def.properties) {
                val propObj = JSONObject()
                propObj.put("type", prop.type.lowercase())
                propObj.put("description", prop.description)
                if (!prop.enum.isNullOrEmpty()) {
                    val enumArr = JSONArray()
                    prop.enum.forEach { enumArr.put(it) }
                    propObj.put("enum", enumArr)
                }
                propsObj.put(key, propObj)
            }
            inputSchema.put("properties", propsObj)

            val reqArr = JSONArray()
            def.required.forEach { reqArr.put(it) }
            inputSchema.put("required", reqArr)

            toolObj.put("input_schema", inputSchema)
            array.put(toolObj)
        }
        return array
    }

    // 3) OpenAI / Groq Mapper: type: "function" + parameters: { type: "object", ... }
    fun toOpenAiToolsJsonArray(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): JSONArray {
        val array = JSONArray()
        for (def in definitions) {
            val item = JSONObject()
            item.put("type", "function")

            val funcObj = JSONObject()
            funcObj.put("name", def.name)
            funcObj.put("description", def.description)

            val paramsObj = JSONObject()
            paramsObj.put("type", "object")

            val propsObj = JSONObject()
            for ((key, prop) in def.properties) {
                val propObj = JSONObject()
                propObj.put("type", prop.type.lowercase())
                propObj.put("description", prop.description)
                if (!prop.enum.isNullOrEmpty()) {
                    val enumArr = JSONArray()
                    prop.enum.forEach { enumArr.put(it) }
                    propObj.put("enum", enumArr)
                }
                propsObj.put(key, propObj)
            }
            paramsObj.put("properties", propsObj)

            val reqArr = JSONArray()
            def.required.forEach { reqArr.put(it) }
            paramsObj.put("required", reqArr)

            funcObj.put("parameters", paramsObj)
            item.put("function", funcObj)

            array.put(item)
        }
        return array
    }

    // Response Parsers

    // Claude (Anthropic) Response Parser
    fun parseClaudeToolCalls(jsonResponse: JSONObject): Pair<String, List<ParsedMemoryToolCall>> {
        val contentArray = jsonResponse.optJSONArray("content") ?: return Pair("", emptyList())
        val textParts = mutableListOf<String>()
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        for (i in 0 until contentArray.length()) {
            val item = contentArray.optJSONObject(i) ?: continue
            val type = item.optString("type")
            if (type == "text") {
                val txt = item.optString("text")
                if (txt.isNotBlank()) textParts.add(txt)
            } else if (type == "tool_use") {
                val name = item.optString("name")
                val inputObj = item.optJSONObject("input")
                val argsMap = mutableMapOf<String, Any>()
                if (inputObj != null) {
                    val keys = inputObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        argsMap[k] = inputObj.opt(k)?.toString() ?: ""
                    }
                }
                if (name.isNotBlank()) {
                    toolCalls.add(ParsedMemoryToolCall(name = name, arguments = argsMap))
                }
            }
        }
        return Pair(textParts.joinToString("\n").trim(), toolCalls)
    }

    // OpenAI / Groq Response Parser
    fun parseOpenAiToolCalls(messageObj: JSONObject): Pair<String, List<ParsedMemoryToolCall>> {
        val text = messageObj.optString("content", "") ?: ""
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        val toolCallsArray = messageObj.optJSONArray("tool_calls")
        if (toolCallsArray != null) {
            for (i in 0 until toolCallsArray.length()) {
                val callObj = toolCallsArray.optJSONObject(i) ?: continue
                val funcObj = callObj.optJSONObject("function") ?: continue
                val name = funcObj.optString("name")
                val argsStr = funcObj.optString("arguments", "{}")

                val argsMap = mutableMapOf<String, Any>()
                try {
                    val argsJson = JSONObject(argsStr)
                    val keys = argsJson.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        argsMap[k] = argsJson.opt(k)?.toString() ?: ""
                    }
                } catch (_: Exception) {}

                if (name.isNotBlank()) {
                    toolCalls.add(ParsedMemoryToolCall(name = name, arguments = argsMap))
                }
            }
        }
        return Pair(text.trim(), toolCalls)
    }

    // Gemini Response Parser
    fun parseGeminiToolCalls(candidate: GeminiCandidate?): Pair<String, List<ParsedMemoryToolCall>> {
        val parts = candidate?.content?.parts ?: emptyList()
        val textParts = mutableListOf<String>()
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        for (p in parts) {
            if (!p.text.isNullOrBlank()) {
                textParts.add(p.text)
            }
            if (p.functionCall != null) {
                val argsMap = p.functionCall.args?.mapValues { it.value as Any } ?: emptyMap()
                toolCalls.add(ParsedMemoryToolCall(name = p.functionCall.name, arguments = argsMap))
            }
        }
        return Pair(textParts.joinToString("\n").trim(), toolCalls)
    }
}
