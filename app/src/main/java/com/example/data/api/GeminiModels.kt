package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiFunctionProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiFunctionParameters(
    val type: String = "OBJECT",
    val properties: Map<String, GeminiFunctionProperty>? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiFunctionParameters? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiFunctionCall(
    val name: String,
    val args: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val functionCall: GeminiFunctionCall? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.85f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GeminiSafetySetting(
    val category: String,
    val threshold: String
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null,
    val safetySettings: List<GeminiSafetySetting>? = null,
    val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
    val promptTokenCount: Int? = 0,
    val candidatesTokenCount: Int? = 0,
    val totalTokenCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiEmbedContentRequest(
    val model: String = "models/text-embedding-004",
    val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiEmbeddingValues(
    val values: List<Float>
)

@JsonClass(generateAdapter = true)
data class GeminiEmbedContentResponse(
    val embedding: GeminiEmbeddingValues? = null
)
