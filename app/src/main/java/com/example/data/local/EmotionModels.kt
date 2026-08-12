package com.example.data.local

import org.json.JSONObject

data class EmotionState(
    val mood: String = "nötr",
    val intensity: Int = 5,
    val affection: Int = 50,
    val trust: Int = 50,
    val tension: Int = 10
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("mood", mood.ifBlank { "nötr" })
        json.put("intensity", intensity.coerceIn(0, 10))
        json.put("affection", affection.coerceIn(0, 100))
        json.put("trust", trust.coerceIn(0, 100))
        json.put("tension", tension.coerceIn(0, 100))
        return json.toString()
    }

    fun applyDeltas(
        newMood: String?,
        newIntensity: Int?,
        affectionDelta: Int,
        trustDelta: Int,
        tensionDelta: Int
    ): EmotionState {
        return EmotionState(
            mood = if (!newMood.isNullOrBlank()) newMood.trim() else mood,
            intensity = (newIntensity ?: intensity).coerceIn(0, 10),
            affection = (affection + affectionDelta).coerceIn(0, 100),
            trust = (trust + trustDelta).coerceIn(0, 100),
            tension = (tension + tensionDelta).coerceIn(0, 100)
        )
    }

    fun getMoodEmoji(): String {
        val m = mood.lowercase().trim()
        return when {
            m.contains("mutlu") || m.contains("neşeli") || m.contains("happy") || m.contains("joy") || m.contains("sevinç") -> "😊"
            m.contains("üzgün") || m.contains("kırgın") || m.contains("sad") || m.contains("keder") -> "🥺"
            m.contains("kızgın") || m.contains("öfkeli") || m.contains("sinirli") || m.contains("angry") -> "😠"
            m.contains("heyecan") || m.contains("coşku") || m.contains("excited") -> "🤩"
            m.contains("kıskan") || m.contains("jealous") -> "😒"
            m.contains("şüphe") || m.contains("temkin") || m.contains("suspicious") -> "🤨"
            m.contains("romantik") || m.contains("aşık") || m.contains("sevgi") || m.contains("love") -> "🥰"
            m.contains("sakin") || m.contains("huzur") || m.contains("calm") -> "😌"
            m.contains("gergin") || m.contains("stres") || m.contains("tense") -> "😬"
            m.contains("kork") || m.contains("endişe") || m.contains("scared") || m.contains("fear") -> "😨"
            m.contains("soğuk") || m.contains("mesafe") || m.contains("cold") -> "🧊"
            m.contains("utangaç") || m.contains("mahcup") || m.contains("shy") -> "😳"
            m.contains("alay") || m.contains("müstehzi") || m.contains("sarkastik") -> "😏"
            else -> "🙂"
        }
    }

    companion object {
        val DEFAULT = EmotionState()

        fun fromJson(jsonStr: String?): EmotionState {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)
                EmotionState(
                    mood = json.optString("mood", "nötr").ifBlank { "nötr" },
                    intensity = json.optInt("intensity", 5).coerceIn(0, 10),
                    affection = json.optInt("affection", 50).coerceIn(0, 100),
                    trust = json.optInt("trust", 50).coerceIn(0, 100),
                    tension = json.optInt("tension", 10).coerceIn(0, 100)
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}

data class WorldAtmosphere(
    val mood: String = "sakin",
    val intensity: Int = 5,
    val currentEvent: String = ""
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("mood", mood.ifBlank { "sakin" })
        json.put("intensity", intensity.coerceIn(0, 10))
        json.put("currentEvent", currentEvent)
        return json.toString()
    }

    fun getMoodEmoji(): String {
        val m = mood.lowercase().trim()
        return when {
            m.contains("sakin") || m.contains("huzur") -> "🌌"
            m.contains("gergin") || m.contains("tehlike") -> "⚠️"
            m.contains("karanlık") || m.contains("kasvet") -> "🌧️"
            m.contains("kaotik") || m.contains("savaş") -> "🔥"
            m.contains("coşkulu") || m.contains("neşeli") -> "✨"
            else -> "🌐"
        }
    }

    companion object {
        val DEFAULT = WorldAtmosphere()

        fun fromJson(jsonStr: String?): WorldAtmosphere {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)
                WorldAtmosphere(
                    mood = json.optString("mood", "sakin").ifBlank { "sakin" },
                    intensity = json.optInt("intensity", 5).coerceIn(0, 10),
                    currentEvent = json.optString("currentEvent", "")
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}
