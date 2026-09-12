package com.example.util

import com.example.data.local.MessageEntity

object OutputQualityValidator {

    fun checkForRecentCliches(messages: List<MessageEntity>): Boolean {
        val assistantMsgs = messages.filter { it.role == "assistant" }.takeLast(5)
        if (assistantMsgs.isEmpty()) return false
        val cliches = listOf(
            "gülümsedi", "gözlerinin içine baktı", "derin bir nefes",
            "hafifçe tebessüm", "yavaşça sordu", "fısıldadı", "gözlerini devirdi"
        )
        var totalMatches = 0
        for (m in assistantMsgs) {
            val txt = m.text.lowercase()
            for (c in cliches) {
                if (txt.contains(c)) totalMatches++
            }
        }
        return totalMatches >= 2
    }

    fun isResponseTooShort(text: String, responseLengthSetting: String, attempt: Int): Boolean {
        if (attempt > 1) return false
        if (text.isBlank()) return false
        val minCharCount = when (responseLengthSetting) {
            "short" -> 0
            "detailed", "long" -> 120
            else -> 70 // standard
        }
        return minCharCount > 0 && text.trim().length < minCharCount
    }

    fun buildClichePromptInstruction(): String {
        return "\n\n[SİSTEM DİREKTİFİ: Önceki yanıtlarında tekrarladığın klişe ifadeleri (gülümsedi, gözlerinin içine baktı vb.) kullanma. Özgün ve farklı ifadeler seç.]"
    }

    fun buildElaboratePromptInstruction(): String {
        return "\n\n[SİSTEM DİREKTİFİ: Yanıtın çok kısa kaldı. Lütfen yanıtını daha detaylı ve açıklayıcı yaz.]"
    }
}
