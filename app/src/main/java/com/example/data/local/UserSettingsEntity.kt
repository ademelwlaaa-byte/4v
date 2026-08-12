package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val customApiKey: String = "", // Gemini Key
    val groqApiKey: String = "",
    val claudeApiKey: String = "",
    val openaiApiKey: String = "",
    val backupApiKey: String = "",
    val selectedProvider: String = "gemini", // "gemini", "groq", "claude", "openai"
    val selectedModel: String = "gemini-2.5-flash",
    val fallbackModel: String = "gemini-2.5-flash",
    val responseLength: String = "standard", // "short", "standard", "long"
    val enableNsfw: Boolean = true, // +18 / Filtresiz RP modu
    val enableOoc: Boolean = true, // Parantez İçi Yönlendirme Modu (... Bu böyle olmalı)
    val enableFlirty: Boolean = true, // Çapkınlık (Flirty)
    val enableHardcore: Boolean = true, // Sert Mod (Hardcore)
    val enableFetish: Boolean = false, // Fantezi (Fetish)
    val enableDarkRp: Boolean = false, // Karanlık (Dark RP)
    val enableSweet: Boolean = false, // Romantik (Sweet)
    val enablePrimal: Boolean = false, // Vahşi (Primal)
    val enableAutoFallback: Boolean = true,
    val enableTts: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val selectedVoiceName: String = "",
    val appLanguage: String = "tr", // "tr" (Türkçe), "en" (English)
    val totalPromptTokens: Long = 0L,
    val totalCandidateTokens: Long = 0L
)

