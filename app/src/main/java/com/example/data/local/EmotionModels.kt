package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

// ==========================================
// BÖLÜM C: Personality Profile (Big Five + Attachment Style)
// ==========================================
data class PersonalityProfile(
    val openness: Int = 50,
    val conscientiousness: Int = 50,
    val extraversion: Int = 50,
    val agreeableness: Int = 50,
    val neuroticism: Int = 50,
    val attachmentStyle: String = "secure" // "secure", "anxious", "avoidant", "fearful_avoidant"
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("openness", openness.coerceIn(0, 100))
        json.put("conscientiousness", conscientiousness.coerceIn(0, 100))
        json.put("extraversion", extraversion.coerceIn(0, 100))
        json.put("agreeableness", agreeableness.coerceIn(0, 100))
        json.put("neuroticism", neuroticism.coerceIn(0, 100))
        json.put("attachment_style", attachmentStyle)
        return json.toString()
    }

    companion object {
        val DEFAULT = PersonalityProfile()

        fun fromJson(jsonStr: String?): PersonalityProfile {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)
                PersonalityProfile(
                    openness = json.optInt("openness", 50).coerceIn(0, 100),
                    conscientiousness = json.optInt("conscientiousness", 50).coerceIn(0, 100),
                    extraversion = json.optInt("extraversion", 50).coerceIn(0, 100),
                    agreeableness = json.optInt("agreeableness", 50).coerceIn(0, 100),
                    neuroticism = json.optInt("neuroticism", 50).coerceIn(0, 100),
                    attachmentStyle = json.optString("attachment_style", "secure")
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }

        fun deriveFromPersonality(aiName: String, personalityText: String, scenarioText: String): PersonalityProfile {
            val text = "$aiName $personalityText $scenarioText".lowercase()

            var open = 50
            var cons = 50
            var extra = 50
            var agree = 50
            var neur = 50
            var attach = "secure"

            if (text.contains("sanatçı") || text.contains("filozof") || text.contains("hayal") || text.contains("merak")) open += 25
            if (text.contains("disiplin") || text.contains("düzen") || text.contains("ciddi") || text.contains("kuralcı")) cons += 25
            if (text.contains("dışa dönük") || text.contains("sosyal") || text.contains("coşkulu") || text.contains("neşeli")) extra += 25
            if (text.contains("içe dönük") || text.contains("sessiz") || text.contains("utangaç") || text.contains("mesafeli")) extra -= 20
            if (text.contains("sevecen") || text.contains("nazik") || text.contains("şefkat") || text.contains("yardımsever")) agree += 25
            if (text.contains("asi") || text.contains("soğuk") || text.contains("sert") || text.contains("saldırgan") || text.contains("rakip")) agree -= 25
            if (text.contains("stresli") || text.contains("kaygılı") || text.contains("öfkeli") || text.contains("duygusal") || text.contains("travma")) neur += 25
            if (text.contains("sakin") || text.contains("dengeli") || text.contains("soğukkanlı")) neur -= 20

            if (text.contains("kaçınan") || text.contains("mesafeli") || text.contains("bağlanamayan")) {
                attach = if (neur > 60) "fearful_avoidant" else "avoidant"
            } else if (text.contains("kaygılı") || text.contains("terk edilme") || text.contains("bağımlı")) {
                attach = "anxious"
            }

            return PersonalityProfile(
                openness = open.coerceIn(0, 100),
                conscientiousness = cons.coerceIn(0, 100),
                extraversion = extra.coerceIn(0, 100),
                agreeableness = agree.coerceIn(0, 100),
                neuroticism = neur.coerceIn(0, 100),
                attachmentStyle = attach
            )
        }

        fun deriveEmotionalRegulationCapacity(aiName: String, personalityText: String, scenarioText: String): Int {
            val text = "$aiName $personalityText $scenarioText".lowercase()
            var capacity = 50

            if (text.contains("yaşlı") || text.contains("deneyimli") || text.contains("olgun") ||
                text.contains("sakin") || text.contains("bilge") || text.contains("mantıklı") ||
                text.contains("profesör") || text.contains("doktor") || text.contains("yetişkin") ||
                text.contains("uzman") || text.contains("komutan") || text.contains("kıdemli") || text.contains("ağırbaşlı")) {
                capacity += 30
            }
            if (text.contains("genç") || text.contains("çocuk") || text.contains("ergen") ||
                text.contains("toy") || text.contains("deneyimsiz") || text.contains("dürtüsel") ||
                text.contains("hızlı öfkelenen") || text.contains("heyecanlı") || text.contains("sabırsız") ||
                text.contains("dramatik") || text.contains("öfkeli")) {
                capacity -= 25
            }
            return capacity.coerceIn(0, 100)
        }
    }
}

// ==========================================
// BÖLÜM A: Primary Emotions (Plutchik 8)
// ==========================================
data class PrimaryEmotions(
    val joy: Int = 0,
    val trust: Int = 50,
    val fear: Int = 0,
    val anger: Int = 0,
    val sadness: Int = 0,
    val anticipation: Int = 0,
    val surprise: Int = 0,
    val disgust: Int = 0
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("joy", joy.coerceIn(0, 100))
        json.put("trust", trust.coerceIn(0, 100))
        json.put("fear", fear.coerceIn(0, 100))
        json.put("anger", anger.coerceIn(0, 100))
        json.put("sadness", sadness.coerceIn(0, 100))
        json.put("anticipation", anticipation.coerceIn(0, 100))
        json.put("surprise", surprise.coerceIn(0, 100))
        json.put("disgust", disgust.coerceIn(0, 100))
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): PrimaryEmotions {
            if (json == null) return PrimaryEmotions()
            return PrimaryEmotions(
                joy = json.optInt("joy", 0).coerceIn(0, 100),
                trust = json.optInt("trust", 50).coerceIn(0, 100),
                fear = json.optInt("fear", 0).coerceIn(0, 100),
                anger = json.optInt("anger", 0).coerceIn(0, 100),
                sadness = json.optInt("sadness", 0).coerceIn(0, 100),
                anticipation = json.optInt("anticipation", 0).coerceIn(0, 100),
                surprise = json.optInt("surprise", 0).coerceIn(0, 100),
                disgust = json.optInt("disgust", 0).coerceIn(0, 100)
            )
        }
    }
}

// ==========================================
// BÖLÜM A: Relationship Axes
// ==========================================
data class RelationshipAxes(
    val affectionScore: Int = 50,
    val respectScore: Int = 50,
    val comfortScore: Int = 50,
    val resentmentScore: Int = 0
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("affectionScore", affectionScore.coerceIn(0, 100))
        json.put("respectScore", respectScore.coerceIn(0, 100))
        json.put("comfortScore", comfortScore.coerceIn(0, 100))
        json.put("resentmentScore", resentmentScore.coerceIn(0, 100))
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): RelationshipAxes {
            if (json == null) return RelationshipAxes()
            return RelationshipAxes(
                affectionScore = json.optInt("affectionScore", 50).coerceIn(0, 100),
                respectScore = json.optInt("respectScore", 50).coerceIn(0, 100),
                comfortScore = json.optInt("comfortScore", 50).coerceIn(0, 100),
                resentmentScore = json.optInt("resentmentScore", 0).coerceIn(0, 100)
            )
        }
    }
}

// ==========================================
// Delta & Context Data
// ==========================================
data class DeltaContext(
    val setting: String = "private", // "public" or "private"
    val mode: String = "casual", // "formal" or "casual"
    val tension: String = "none" // "none", "conflict", "crisis"
)

data class DeltaData(
    val axis: String = "affectionScore",
    val value: Int = 0,
    val reason: String = "",
    val context: DeltaContext = DeltaContext()
)

// ==========================================
// BÖLÜM E: Self Check Data
// ==========================================
data class SelfCheckData(
    val isDeltaJustifiedByScene: Boolean = true,
    val isExpressionConsistentWithAttachmentStyle: Boolean = true,
    val didISkipAStage: Boolean = false,
    val didIContradictRecentEmotionalState: Boolean = false
) {
    val isValid: Boolean
        get() = isDeltaJustifiedByScene && isExpressionConsistentWithAttachmentStyle && !didISkipAStage && !didIContradictRecentEmotionalState

    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("is_delta_justified_by_scene", isDeltaJustifiedByScene)
        json.put("is_expression_consistent_with_attachment_style", isExpressionConsistentWithAttachmentStyle)
        json.put("did_i_skip_a_stage", didISkipAStage)
        json.put("did_i_contradict_recent_emotional_state", didIContradictRecentEmotionalState)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): SelfCheckData {
            if (json == null) return SelfCheckData()
            return SelfCheckData(
                isDeltaJustifiedByScene = json.optBoolean("is_delta_justified_by_scene", true),
                isExpressionConsistentWithAttachmentStyle = json.optBoolean("is_expression_consistent_with_attachment_style", true),
                didISkipAStage = json.optBoolean("did_i_skip_a_stage", false),
                didIContradictRecentEmotionalState = json.optBoolean("did_i_contradict_recent_emotional_state", false)
            )
        }
    }
}

// ==========================================
// BÖLÜM B: Hierarchical World State (Macro, Meso, Micro)
// ==========================================
data class WorldEvent(
    val id: String = "",
    val name: String = "",
    val addedTimestamp: Long = System.currentTimeMillis(),
    val durationDays: Int = 3,
    val isActive: Boolean = true
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("added_timestamp", addedTimestamp)
        json.put("duration_days", durationDays)
        json.put("is_active", isActive)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject): WorldEvent {
            return WorldEvent(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                addedTimestamp = json.optLong("added_timestamp", System.currentTimeMillis()),
                durationDays = json.optInt("duration_days", 3),
                isActive = json.optBoolean("is_active", true)
            )
        }
    }
}

data class MacroWorldState(
    val eraRules: String = "Standart dönem kuralları",
    val factionsHierarchy: String = "Sabit hiyerarşi",
    val globalTensionLevel: Int = 10,
    val activeWorldEvents: List<WorldEvent> = emptyList()
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("era_rules", eraRules)
        json.put("factions_hierarchy", factionsHierarchy)
        json.put("global_tension_level", globalTensionLevel.coerceIn(0, 100))
        val arr = JSONArray()
        activeWorldEvents.forEach { arr.put(it.toJsonObject()) }
        json.put("active_world_events", arr)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): MacroWorldState {
            if (json == null) return MacroWorldState()
            val eventsList = mutableListOf<WorldEvent>()
            val arr = json.optJSONArray("active_world_events")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i)
                    if (item != null) eventsList.add(WorldEvent.fromJsonObject(item))
                }
            }
            return MacroWorldState(
                eraRules = json.optString("era_rules", "Standart dönem kuralları"),
                factionsHierarchy = json.optString("factions_hierarchy", "Sabit hiyerarşi"),
                globalTensionLevel = json.optInt("global_tension_level", 10).coerceIn(0, 100),
                activeWorldEvents = eventsList
            )
        }
    }
}

data class MesoWorldState(
    val currentLocation: String = "Oda",
    val timeOfDay: String = "Gündüz",
    val weather: String = "Açık",
    val whoIsPresent: List<String> = emptyList(),
    val locationPersistentNotes: String = ""
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("current_location", currentLocation)
        json.put("time_of_day", timeOfDay)
        json.put("weather", weather)
        val arr = JSONArray()
        whoIsPresent.forEach { arr.put(it) }
        json.put("who_is_present", arr)
        json.put("location_persistent_notes", locationPersistentNotes)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): MesoWorldState {
            if (json == null) return MesoWorldState()
            val presentList = mutableListOf<String>()
            val arr = json.optJSONArray("who_is_present")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)
                    if (!item.isNullOrBlank()) presentList.add(item)
                }
            }
            return MesoWorldState(
                currentLocation = json.optString("current_location", "Oda"),
                timeOfDay = json.optString("time_of_day", "Gündüz"),
                weather = json.optString("weather", "Açık"),
                whoIsPresent = presentList,
                locationPersistentNotes = json.optString("location_persistent_notes", "")
            )
        }
    }
}

data class MicroWorldState(
    val sceneTension: String = "none", // "none", "conflict", "crisis"
    val sceneMood: String = "sakin",
    val recentTriggerEvent: String? = null
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("scene_tension", sceneTension)
        json.put("scene_mood", sceneMood)
        json.put("recent_trigger_event", recentTriggerEvent ?: JSONObject.NULL)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): MicroWorldState {
            if (json == null) return MicroWorldState()
            return MicroWorldState(
                sceneTension = json.optString("scene_tension", "none"),
                sceneMood = json.optString("scene_mood", "sakin"),
                recentTriggerEvent = if (json.isNull("recent_trigger_event")) null else json.optString("recent_trigger_event")
            )
        }
    }
}

data class WorldState(
    val macro: MacroWorldState = MacroWorldState(),
    val meso: MesoWorldState = MesoWorldState(),
    val micro: MicroWorldState = MicroWorldState(),
    val lastUpdatedMessageIndex: Int = 0,
    val schemaVersion: Int = 3
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("macro", macro.toJsonObject())
        json.put("meso", meso.toJsonObject())
        json.put("micro", micro.toJsonObject())
        json.put("last_updated_message_index", lastUpdatedMessageIndex)
        json.put("schemaVersion", schemaVersion)
        return json.toString()
    }

    companion object {
        val DEFAULT = WorldState()

        fun fromJson(jsonStr: String?): WorldState {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)
                WorldState(
                    macro = MacroWorldState.fromJsonObject(json.optJSONObject("macro")),
                    meso = MesoWorldState.fromJsonObject(json.optJSONObject("meso")),
                    micro = MicroWorldState.fromJsonObject(json.optJSONObject("micro")),
                    lastUpdatedMessageIndex = json.optInt("last_updated_message_index", 0),
                    schemaVersion = json.optInt("schemaVersion", 3)
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}

// ==========================================
// Backward-compatible EmotionState
// ==========================================
data class EmotionState(
    val primaryEmotions: PrimaryEmotions = PrimaryEmotions(),
    val relationshipAxes: RelationshipAxes = RelationshipAxes(),
    val physicalComfortScore: Int = 30,
    val obsessionScore: Int = 0,
    val emotionalResidue: Int = 0,
    val dominantEmotion: String = "nötr",
    val suppressedEmotion: String = "",
    val computedSecondaryEmotion: String? = null,
    val defenseMechanism: String = "none",
    val deltaAxis: String = "affectionScore",
    val deltaValue: Int = 0,
    val deltaReason: String = "",
    val setting: String = "private",
    val mode: String = "casual",
    val tension: String = "none",
    val schemaVersion: Int = 3,
    // Tracking fields
    val highAffectionStreak: Int = 0,
    val consecutivePositiveCount: Int = 0,
    val dailyAffectionGain: Int = 0,
    val lastGainResetDate: String = "",
    val peakAffectionScore: Int = 0,
    val recoveryLockUntilMessageCount: Int = 0,
    val userToneHistoryJson: String = "[]",
    val userInconsistencyFlag: Boolean = false,
    val resilience: Int = 5,
    val speechPattern: String = ""
) {
    // Backwards compatibility properties
    val affection: Int get() = relationshipAxes.affectionScore
    val trust: Int get() = primaryEmotions.trust
    val mood: String get() = dominantEmotion
    val secondaryMood: String get() = computedSecondaryEmotion ?: ""
    val intensity: Int get() = ((primaryEmotions.joy + primaryEmotions.anger + primaryEmotions.fear + primaryEmotions.sadness) / 40).coerceIn(1, 10)
    val hurt: Int get() = relationshipAxes.resentmentScore
    val obsession: Int get() = obsessionScore

    fun toJson(): String {
        val json = JSONObject()
        json.put("primary_emotions", primaryEmotions.toJsonObject())
        json.put("relationship_axes", relationshipAxes.toJsonObject())
        json.put("physicalComfortScore", physicalComfortScore.coerceIn(0, 100))
        json.put("obsessionScore", obsessionScore.coerceIn(0, 100))
        json.put("emotionalResidue", emotionalResidue.coerceIn(0, 100))
        json.put("dominant_emotion", dominantEmotion.ifBlank { "nötr" })
        json.put("suppressed_emotion", if (suppressedEmotion.isBlank()) JSONObject.NULL else suppressedEmotion)
        json.put("computed_secondary_emotion", computedSecondaryEmotion ?: JSONObject.NULL)
        json.put("defense_mechanism", defenseMechanism)

        val deltaObj = JSONObject()
        deltaObj.put("axis", deltaAxis)
        deltaObj.put("value", deltaValue)
        deltaObj.put("reason", deltaReason)
        val ctxObj = JSONObject()
        ctxObj.put("setting", setting)
        ctxObj.put("mode", mode)
        ctxObj.put("tension", tension)
        deltaObj.put("context", ctxObj)
        json.put("delta", deltaObj)

        json.put("schemaVersion", schemaVersion)

        // Internal fields for legacy tracking
        json.put("highAffectionStreak", highAffectionStreak)
        json.put("consecutivePositiveCount", consecutivePositiveCount)
        json.put("dailyAffectionGain", dailyAffectionGain)
        json.put("lastGainResetDate", lastGainResetDate)
        json.put("peakAffectionScore", peakAffectionScore)
        json.put("recoveryLockUntilMessageCount", recoveryLockUntilMessageCount)
        json.put("userToneHistoryJson", userToneHistoryJson)
        json.put("userInconsistencyFlag", userInconsistencyFlag)
        json.put("resilience", resilience)
        json.put("speechPattern", speechPattern)

        // Legacy fields for direct UI access
        json.put("mood", mood)
        json.put("secondaryMood", secondaryMood)
        json.put("suppressedEmotion", suppressedEmotion)
        json.put("intensity", intensity)
        json.put("affection", affection)
        json.put("trust", trust)
        json.put("tension", if (tension == "crisis") 80 else if (tension == "conflict") 50 else 10)
        json.put("hurt", hurt)
        json.put("obsession", obsession)

        return json.toString()
    }

    fun getAffectionTierLabel(): String {
        return when (affection) {
            in 0..20 -> "Yabancı / Mesafeli"
            in 21..40 -> "Tanıdık"
            in 41..60 -> "Yakın Tanıdık / Arkadaşlık"
            in 61..80 -> "Duygusal Bağ"
            else -> "Derin Bağ / Aşk"
        }
    }

    fun getPhysicalComfortTierLabel(): String {
        return when (physicalComfortScore) {
            in 0..30 -> "Mesafeli"
            in 31..60 -> "Ilımlı"
            else -> "Yakın / Rahat"
        }
    }

    fun getMoodEmoji(): String {
        val combined = "$dominantEmotion $computedSecondaryEmotion $suppressedEmotion".lowercase().trim()
        return when {
            combined.contains("mutlu") || combined.contains("joy") || combined.contains("neşeli") -> "😊"
            combined.contains("kırgın") || combined.contains("hurt") || combined.contains("sadness") || combined.contains("üzgün") -> "🥺"
            combined.contains("öfkeli") || combined.contains("anger") || combined.contains("kızgın") -> "😠"
            combined.contains("koruma") || combined.contains("love") || combined.contains("şefkat") -> "🤗"
            combined.contains("heyecan") || combined.contains("surprise") -> "🤩"
            combined.contains("şüphe") || combined.contains("fear") || combined.contains("temkin") -> "🤨"
            combined.contains("romantik") || combined.contains("aşık") -> "🥰"
            combined.contains("sakin") || combined.contains("neutral") -> "😌"
            else -> "🙂"
        }
    }

    companion object {
        val DEFAULT = EmotionState()

        fun fromJson(jsonStr: String?): EmotionState {
            if (jsonStr.isNullOrBlank()) return DEFAULT
            return try {
                val json = JSONObject(jsonStr)

                val primObj = json.optJSONObject("primary_emotions")
                val relObj = json.optJSONObject("relationship_axes")

                val primary = if (primObj != null) {
                    PrimaryEmotions.fromJsonObject(primObj)
                } else {
                    PrimaryEmotions(
                        trust = json.optInt("trust", 50),
                        anger = json.optInt("hurt", 0)
                    )
                }

                val rel = if (relObj != null) {
                    RelationshipAxes.fromJsonObject(relObj)
                } else {
                    RelationshipAxes(
                        affectionScore = json.optInt("affection", 50),
                        respectScore = 50,
                        comfortScore = 50,
                        resentmentScore = json.optInt("hurt", 0)
                    )
                }

                val deltaObj = json.optJSONObject("delta")
                val ctxObj = deltaObj?.optJSONObject("context")

                EmotionState(
                    primaryEmotions = primary,
                    relationshipAxes = rel,
                    physicalComfortScore = json.optInt("physicalComfortScore", 30).coerceIn(0, 100),
                    obsessionScore = json.optInt("obsessionScore", json.optInt("obsession", 0)).coerceIn(0, 100),
                    emotionalResidue = json.optInt("emotionalResidue", 0).coerceIn(0, 100),
                    dominantEmotion = json.optString("dominant_emotion", json.optString("mood", "nötr")),
                    suppressedEmotion = json.optString("suppressed_emotion", json.optString("suppressedEmotion", "")),
                    computedSecondaryEmotion = if (json.isNull("computed_secondary_emotion")) null else json.optString("computed_secondary_emotion"),
                    defenseMechanism = json.optString("defense_mechanism", "none"),
                    deltaAxis = deltaObj?.optString("axis", "affectionScore") ?: "affectionScore",
                    deltaValue = deltaObj?.optInt("value", 0) ?: 0,
                    deltaReason = deltaObj?.optString("reason", "") ?: "",
                    setting = ctxObj?.optString("setting", "private") ?: "private",
                    mode = ctxObj?.optString("mode", "casual") ?: "casual",
                    tension = ctxObj?.optString("tension", "none") ?: "none",
                    schemaVersion = json.optInt("schemaVersion", 2),
                    highAffectionStreak = json.optInt("highAffectionStreak", 0),
                    consecutivePositiveCount = json.optInt("consecutivePositiveCount", 0),
                    dailyAffectionGain = json.optInt("dailyAffectionGain", 0),
                    lastGainResetDate = json.optString("lastGainResetDate", ""),
                    peakAffectionScore = json.optInt("peakAffectionScore", 0),
                    recoveryLockUntilMessageCount = json.optInt("recoveryLockUntilMessageCount", 0),
                    userToneHistoryJson = json.optString("userToneHistoryJson", "[]"),
                    userInconsistencyFlag = json.optBoolean("userInconsistencyFlag", false),
                    resilience = json.optInt("resilience", 5),
                    speechPattern = json.optString("speechPattern", "")
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }

        fun calculateBaselineEmotionState(
            aiName: String,
            personality: String,
            scenario: String,
            userCharName: String = "",
            userCharDesc: String = ""
        ): EmotionState {
            val combinedText = "$aiName $personality $scenario $userCharName $userCharDesc".lowercase()

            val isMother = combinedText.contains("anne") || combinedText.contains("valide")
            val isFather = combinedText.contains("baba")
            val isSibling = combinedText.contains("kardeş") || combinedText.contains("abla") || combinedText.contains("abi")
            val isBestFriend = combinedText.contains("yakın dost") || combinedText.contains("sırdaş") || combinedText.contains("çocukluk arkadaşı")
            val isLover = combinedText.contains("sevgili") || combinedText.contains("eş ") || combinedText.contains("aşık") || combinedText.contains("partner")
            val isRival = combinedText.contains("düşman") || combinedText.contains("rakip") || combinedText.contains("hasım")
            val isStranger = combinedText.contains("yabancı") || combinedText.contains("tanımıyor")

            val hasConflict = combinedText.contains("kavga") || combinedText.contains("küs") ||
                    combinedText.contains("tartışma") || combinedText.contains("soğukluk")

            return when {
                (isMother || isFather) && hasConflict -> EmotionState(
                    primaryEmotions = PrimaryEmotions(trust = 45, sadness = 40),
                    relationshipAxes = RelationshipAxes(affectionScore = 75, respectScore = 60, comfortScore = 40, resentmentScore = 60),
                    dominantEmotion = "kırgın ve mesafeli",
                    suppressedEmotion = "hüzünlü sevgi"
                )
                (isMother || isFather) -> EmotionState(
                    primaryEmotions = PrimaryEmotions(joy = 40, trust = 88),
                    relationshipAxes = RelationshipAxes(affectionScore = 92, respectScore = 80, comfortScore = 85, resentmentScore = 0),
                    dominantEmotion = "korumacı ve şefkatli"
                )
                isLover && hasConflict -> EmotionState(
                    primaryEmotions = PrimaryEmotions(trust = 50, sadness = 50, anger = 30),
                    relationshipAxes = RelationshipAxes(affectionScore = 80, respectScore = 60, comfortScore = 45, resentmentScore = 55),
                    dominantEmotion = "buruk ve tereddütlü"
                )
                isLover -> EmotionState(
                    primaryEmotions = PrimaryEmotions(joy = 60, trust = 85),
                    relationshipAxes = RelationshipAxes(affectionScore = 90, respectScore = 80, comfortScore = 85, resentmentScore = 0),
                    dominantEmotion = "sevecen ve tutkulu"
                )
                isBestFriend -> EmotionState(
                    primaryEmotions = PrimaryEmotions(joy = 50, trust = 85),
                    relationshipAxes = RelationshipAxes(affectionScore = 85, respectScore = 75, comfortScore = 80, resentmentScore = 0),
                    dominantEmotion = "sıcak ve samimi"
                )
                isSibling -> EmotionState(
                    primaryEmotions = PrimaryEmotions(joy = 40, trust = 80),
                    relationshipAxes = RelationshipAxes(affectionScore = 82, respectScore = 70, comfortScore = 75, resentmentScore = 0),
                    dominantEmotion = "samimi ve alaycı"
                )
                isRival -> EmotionState(
                    primaryEmotions = PrimaryEmotions(trust = 10, fear = 30, anger = 50),
                    relationshipAxes = RelationshipAxes(affectionScore = 15, respectScore = 40, comfortScore = 10, resentmentScore = 75),
                    dominantEmotion = "şüpheci ve tetikte"
                )
                isStranger -> EmotionState(
                    primaryEmotions = PrimaryEmotions(trust = 25, anticipation = 40),
                    relationshipAxes = RelationshipAxes(affectionScore = 30, respectScore = 50, comfortScore = 30, resentmentScore = 0),
                    dominantEmotion = "temkinli ve meraklı"
                )
                else -> EmotionState(
                    primaryEmotions = PrimaryEmotions(trust = 50, anticipation = 30),
                    relationshipAxes = RelationshipAxes(affectionScore = 50, respectScore = 50, comfortScore = 50, resentmentScore = 0),
                    dominantEmotion = "nötr ve ilgili"
                )
            }
        }
    }
}

// Legacy wrapper
data class WorldAtmosphere(
    val mood: String = "sakin",
    val intensity: Int = 5,
    val currentEvent: String = "",
    val macroAtmosphere: String = "",
    val microAtmosphere: String = ""
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("mood", mood.ifBlank { "sakin" })
        json.put("intensity", intensity.coerceIn(0, 10))
        json.put("currentEvent", currentEvent)
        json.put("macroAtmosphere", macroAtmosphere)
        json.put("microAtmosphere", microAtmosphere)
        return json.toString()
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
                    currentEvent = json.optString("currentEvent", ""),
                    macroAtmosphere = json.optString("macroAtmosphere", ""),
                    microAtmosphere = json.optString("microAtmosphere", "")
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}
