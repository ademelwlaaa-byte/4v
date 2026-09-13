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

    /**
     * Detects degenerate repetition loops where sentences or n-gram patterns are repeated
     * with >= 75% similarity or exact phrase duplication.
     */
    fun hasRepetitiveLoops(text: String): Boolean {
        if (text.isBlank() || text.length < 50) return false

        // 1. Sentence-level similarity check
        val sentences = splitIntoSentences(text)
        if (sentences.size >= 2) {
            for (i in 0 until sentences.size - 1) {
                val s1 = sentences[i].trim()
                val s2 = sentences[i + 1].trim()
                if (s1.length >= 12 && s2.length >= 12) {
                    val sim = calculateSimilarity(s1, s2)
                    if (sim >= 0.75) {
                        return true
                    }
                }
            }
        }

        // 2. Substring n-gram repetition check (e.g. 25+ char string appearing 3+ times)
        val cleanText = text.lowercase()
        val minChunkLen = 25
        if (cleanText.length >= minChunkLen * 3) {
            for (i in 0..(cleanText.length - minChunkLen)) {
                val chunk = cleanText.substring(i, i + minChunkLen)
                if (chunk.isBlank()) continue
                val occurrences = countOccurrences(cleanText, chunk)
                if (occurrences >= 3) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Truncates text right at the beginning of the first detected repetitive sentence/phrase,
     * returning the clean initial portion.
     */
    fun truncateAtRepetition(text: String): String {
        if (text.isBlank() || text.length < 50) return text.trim()

        val sentences = splitIntoSentences(text)
        if (sentences.size < 2) return text.trim()

        var cutoffSentenceIndex = sentences.size

        for (i in 0 until sentences.size - 1) {
            val s1 = sentences[i].trim()
            val s2 = sentences[i + 1].trim()
            if (s1.length >= 12 && s2.length >= 12) {
                val sim = calculateSimilarity(s1, s2)
                if (sim >= 0.75) {
                    cutoffSentenceIndex = i + 1
                    break
                }
            }
        }

        if (cutoffSentenceIndex < sentences.size) {
            val validSentences = sentences.subList(0, cutoffSentenceIndex)
            return validSentences.joinToString(" ").trim()
        }

        return text.trim()
    }

    /**
     * Strict length limit enforcement for secondary models (e.g. 1.3x max limit).
     * Truncates at the last completed sentence boundary.
     */
    fun enforceLengthLimits(text: String, responseLengthSetting: String): String {
        if (text.isBlank()) return text

        val maxAllowedChars = when (responseLengthSetting) {
            "short" -> 600
            "long", "detailed" -> 3200
            else -> 1600 // standard
        }

        if (text.length <= maxAllowedChars) return text.trim()

        // Crop at maxAllowedChars, then scan backwards for sentence end
        val croppedRaw = text.substring(0, maxAllowedChars)
        val lastSentenceEnd = croppedRaw.indexOfLast { it == '.' || it == '!' || it == '?' || it == '\n' }

        return if (lastSentenceEnd > 100) {
            croppedRaw.substring(0, lastSentenceEnd + 1).trim()
        } else {
            croppedRaw.trim() + "..."
        }
    }

    fun buildClichePromptInstruction(): String {
        return "\n\n[SİSTEM DİREKTİFİ: Önceki yanıtlarında tekrarladığın klişe ifadeleri kullanma. Özgün ve farklı ifadeler seç.]"
    }

    fun buildElaboratePromptInstruction(): String {
        return "\n\n[SİSTEM DİREKTİFİ: Yanıtın çok kısa kaldı. Lütfen yanıtını daha detaylı ve açıklayıcı yaz.]"
    }

    fun buildRepetitionRetryInstruction(): String {
        return "\n\n[SİSTEM DİREKTİFİ: Önceki yanıtında aynı cümleleri tekrar ettin. Cümleleri ve kelimeleri tekrar etme, hikayeyi yeni ve dinamik olaylarla ileriye taşı.]"
    }

    fun buildSystemPromptGrammarInstruction(): String {
        return "\n[DİL VE UZUNLUK TALİMATI: Yanıtını göndermeden önce yazım ve dilbilgisi hatası olmadığından emin ol. Cümleleri kısa ve net tut. Tekrar eden cümle veya paragraf kalıplarından kesinlikle kaçın.]"
    }

    private fun splitIntoSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?\\n])\\s+")).filter { it.isNotBlank() }
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        val t1 = s1.lowercase().replace(Regex("[^a-z0-9çğıöşü ]"), "").split(Regex("\\s+")).filter { it.isNotBlank() }
        val t2 = s2.lowercase().replace(Regex("[^a-z0-9çğıöşü ]"), "").split(Regex("\\s+")).filter { it.isNotBlank() }
        if (t1.isEmpty() || t2.isEmpty()) return 0.0
        val intersection = t1.intersect(t2.toSet()).size
        val union = (t1.toSet() + t2.toSet()).size
        return if (union > 0) intersection.toDouble() / union else 0.0
    }

    private fun countOccurrences(text: String, target: String): Int {
        var count = 0
        var idx = 0
        while (idx != -1) {
            idx = text.indexOf(target, idx)
            if (idx != -1) {
                count++
                idx += target.length
            }
        }
        return count
    }
}
