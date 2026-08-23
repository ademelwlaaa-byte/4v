package com.example.data.local

import org.json.JSONObject
import kotlin.math.roundToInt

data class EmotionState(
    val mood: String = "nötr",                      // Birincil / Yüzeydeki Duygu
    val secondaryMood: String = "",                  // İkincil / Karmaşık Duygu
    val suppressedEmotion: String = "",              // Bastırılmış / İçsel Çatışma Duygusu
    val intensity: Int = 5,                          // Duygu Şiddeti (0-10)
    val affection: Int = 50,                         // Yakınlık / Sevgi (0-100)
    val trust: Int = 50,                             // Güven Seviyesi (0-100)
    val tension: Int = 10,                           // Gerginlik / Stres (0-100)
    val hurt: Int = 0,                               // Kırgınlık / Mesafe (0-100)
    val resilience: Int = 5,                         // Duygusal Direnç / Eşik (0-10)
    val speechPattern: String = "",                  // Konuşma Üslubu ve Hızı (Mikro Atmosfer Yansıması)
    val obsession: Int = 0,                          // Takıntı / Bağımlılık (0-100)
    val highAffectionStreak: Int = 0,                // Kesintisiz >90 yakınlık mesaj sayısı
    val consecutivePositiveCount: Int = 0,           // Art arda pozitif artış sayısı
    val dailyAffectionGain: Int = 0,                 // Günlük toplam yakınlık kazancı (max +15)
    val lastGainResetDate: String = "",              // Günlük kazancın son sıfırlanma tarihi (YYYY-MM-DD)
    val peakAffectionScore: Int = 0,                 // Karakterin ulaştığı en yüksek yakınlık skoru
    val recoveryLockUntilMessageCount: Int = 0,      // Madde 16: İyileşme kilidi için hedef mesaj sayısı
    val userToneHistoryJson: String = "[]",          // Madde 17: Son 5 kullanıcı tonu JSON listesi
    val userInconsistencyFlag: Boolean = false,      // Madde 17: Kullanıcı tutarsızlık bayrağı
    val physicalComfortScore: Int = 30               // Madde 18: Fiziksel yakınlık skoru (0-100)
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("mood", mood.ifBlank { "nötr" })
        json.put("secondaryMood", secondaryMood)
        json.put("suppressedEmotion", suppressedEmotion)
        json.put("intensity", intensity.coerceIn(0, 10))
        json.put("affection", affection.coerceIn(0, 100))
        json.put("trust", trust.coerceIn(0, 100))
        json.put("tension", tension.coerceIn(0, 100))
        json.put("hurt", hurt.coerceIn(0, 100))
        json.put("resilience", resilience.coerceIn(1, 10))
        json.put("speechPattern", speechPattern)
        json.put("obsession", obsession.coerceIn(0, 100))
        json.put("highAffectionStreak", highAffectionStreak.coerceAtLeast(0))
        json.put("consecutivePositiveCount", consecutivePositiveCount.coerceAtLeast(0))
        json.put("dailyAffectionGain", dailyAffectionGain.coerceAtLeast(0))
        json.put("lastGainResetDate", lastGainResetDate)
        json.put("peakAffectionScore", peakAffectionScore.coerceIn(0, 100))
        json.put("recoveryLockUntilMessageCount", recoveryLockUntilMessageCount.coerceAtLeast(0))
        json.put("userToneHistoryJson", userToneHistoryJson)
        json.put("userInconsistencyFlag", userInconsistencyFlag)
        json.put("physicalComfortScore", physicalComfortScore.coerceIn(0, 100))
        return json.toString()
    }

    /**
     * Yakınlık Kademeleri (Affection Tiers):
     * 0-20 = Yabancı/Mesafeli
     * 21-40 = Tanıdık
     * 41-60 = Yakın Tanıdık/Arkadaşlık
     * 61-80 = Duygusal Bağ
     * 81-100 = Derin Bağ/Aşk
     */
    fun getAffectionTierLabel(): String {
        return when (affection) {
            in 0..20 -> "Yabancı / Mesafeli"
            in 21..40 -> "Tanıdık"
            in 41..60 -> "Yakın Tanıdık / Arkadaşlık"
            in 61..80 -> "Duygusal Bağ"
            else -> "Derin Bağ / Aşk"
        }
    }

    /**
     * Fiziksel Yakınlık Kademeleri (Physical Comfort Tiers):
     * 0-30 = Mesafeli (Tokalaşma/omuz ötesi rahatsız edici)
     * 31-60 = Ilımlı (Yan yana oturma, kısa temas rahat)
     * 61-100 = Yakın (Sarılma, el ele, yakın temas doğal)
     */
    fun getPhysicalComfortTierLabel(): String {
        return when (physicalComfortScore) {
            in 0..30 -> "Mesafeli"
            in 31..60 -> "Ilımlı"
            else -> "Yakın / Rahat"
        }
    }

    /**
     * Kademeli ve Gerçekçi Duygu Değişimi Mantığı (Asimetrik Kazanma/Kaybetme):
     * - Olumlu duygular (Affection/Trust) zor kazanılır (max +5), hızlı kaybedilir.
     * - Olumsuz duygular (Hurt/Tension) hızlı kazanılır, yavaş kaybedilir/onarılır.
     * - Fiziksel Yakınlık (Physical Comfort), duygusal yakınlıktan bağımsız ilerler ancak affection'ı geçemez.
     */
    fun applyDeltas(
        newMood: String?,
        newSecondaryMood: String? = null,
        newSuppressedEmotion: String? = null,
        newIntensity: Int?,
        affectionDelta: Int,
        trustDelta: Int,
        tensionDelta: Int,
        hurtDelta: Int = 0,
        obsessionDelta: Int = 0,
        physicalDelta: Int = 0,
        newSpeechPattern: String? = null,
        isObsessionAllowed: Boolean = false
    ): EmotionState {
        val dampeningFactor = ((11.0 - resilience.coerceIn(1, 10)) / 10.0).coerceIn(0.15, 1.0)

        // Asimetri 1: Yakınlık kazanımı yavaş ve sönümlü (max +5), kaybı hızlı
        val finalAffDelta = if (affectionDelta > 0) {
            (affectionDelta.coerceAtMost(5) * dampeningFactor).roundToInt()
        } else {
            (affectionDelta * 1.2).roundToInt()
        }

        // Asimetri 2: Güven kazanımı yavaş (max +5), kaybı hızlı
        val finalTrustDelta = if (trustDelta > 0) {
            (trustDelta.coerceAtMost(5) * dampeningFactor).roundToInt()
        } else {
            (trustDelta * 1.2).roundToInt()
        }

        // Asimetri 3: Kırgınlık hızlı kazanılır, yavaş silinir/iyileşir
        val finalHurtDelta = if (hurtDelta > 0) {
            (hurtDelta * 1.2).roundToInt()
        } else {
            (hurtDelta * 0.5 * dampeningFactor).roundToInt()
        }

        val scaledTensionDelta = (tensionDelta * dampeningFactor).roundToInt()

        val nextAffection = (affection + finalAffDelta).coerceIn(0, 100)
        val nextStreak = if (nextAffection > 90) highAffectionStreak + 1 else 0

        val nextObsession = if (isObsessionAllowed) {
            (obsession + obsessionDelta.coerceIn(0, 2)).coerceIn(0, 100)
        } else {
            (obsession - 1).coerceAtLeast(0)
        }

        // Asimetri 4: Fiziksel yakınlık kazanımı sönümlü, kaybı hızlı, üst sınırı nextAffection
        val finalPhysicalDelta = if (physicalDelta > 0) {
            (physicalDelta.coerceAtMost(5) * dampeningFactor).roundToInt()
        } else {
            (physicalDelta * 1.2).roundToInt()
        }
        val nextPhysical = (physicalComfortScore + finalPhysicalDelta).coerceIn(0, nextAffection)

        return EmotionState(
            mood = if (!newMood.isNullOrBlank()) newMood.trim() else mood,
            secondaryMood = if (!newSecondaryMood.isNullOrBlank()) newSecondaryMood.trim() else secondaryMood,
            suppressedEmotion = if (!newSuppressedEmotion.isNullOrBlank()) newSuppressedEmotion.trim() else suppressedEmotion,
            intensity = (newIntensity ?: intensity).coerceIn(0, 10),
            affection = nextAffection,
            trust = (trust + finalTrustDelta).coerceIn(0, 100),
            tension = (tension + scaledTensionDelta).coerceIn(0, 100),
            hurt = (hurt + finalHurtDelta).coerceIn(0, 100),
            resilience = resilience,
            speechPattern = if (!newSpeechPattern.isNullOrBlank()) newSpeechPattern.trim() else speechPattern,
            obsession = nextObsession,
            highAffectionStreak = nextStreak,
            recoveryLockUntilMessageCount = recoveryLockUntilMessageCount,
            userToneHistoryJson = userToneHistoryJson,
            userInconsistencyFlag = userInconsistencyFlag,
            physicalComfortScore = nextPhysical
        )
    }

    fun getMoodEmoji(): String {
        val combined = "$mood $secondaryMood $suppressedEmotion".lowercase().trim()
        return when {
            combined.contains("mutlu") || combined.contains("neşeli") || combined.contains("sevinç") -> "😊"
            combined.contains("kırgın") || combined.contains("keder") || combined.contains("buruk") || combined.contains("üzgün") -> "🥺"
            combined.contains("öfkeli") || combined.contains("kızgın") || combined.contains("sinirli") -> "😠"
            combined.contains("koruma") || combined.contains("şefkat") || combined.contains("sevecen") -> "🤗"
            combined.contains("heyecan") || combined.contains("coşku") -> "🤩"
            combined.contains("şüphe") || combined.contains("temkin") || combined.contains("tetikte") -> "🤨"
            combined.contains("romantik") || combined.contains("aşık") || combined.contains("tutku") -> "🥰"
            combined.contains("sakin") || combined.contains("huzur") -> "😌"
            combined.contains("gergin") || combined.contains("stres") || combined.contains("baskı") -> "😬"
            combined.contains("kork") || combined.contains("endişe") -> "😨"
            combined.contains("soğuk") || combined.contains("mesafe") -> "🧊"
            combined.contains("utangaç") || combined.contains("mahcup") -> "😳"
            combined.contains("alay") || combined.contains("sarkastik") || combined.contains("müstehzi") -> "😏"
            combined.contains("gurur") || combined.contains("mağrur") -> "😤"
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
                    secondaryMood = json.optString("secondaryMood", ""),
                    suppressedEmotion = json.optString("suppressedEmotion", ""),
                    intensity = json.optInt("intensity", 5).coerceIn(0, 10),
                    affection = json.optInt("affection", 50).coerceIn(0, 100),
                    trust = json.optInt("trust", 50).coerceIn(0, 100),
                    tension = json.optInt("tension", 10).coerceIn(0, 100),
                    hurt = json.optInt("hurt", 0).coerceIn(0, 100),
                    resilience = json.optInt("resilience", 5).coerceIn(1, 10),
                    speechPattern = json.optString("speechPattern", ""),
                    obsession = json.optInt("obsession", 0).coerceIn(0, 100),
                    highAffectionStreak = json.optInt("highAffectionStreak", 0).coerceAtLeast(0),
                    consecutivePositiveCount = json.optInt("consecutivePositiveCount", 0).coerceAtLeast(0),
                    dailyAffectionGain = json.optInt("dailyAffectionGain", 0).coerceAtLeast(0),
                    lastGainResetDate = json.optString("lastGainResetDate", ""),
                    peakAffectionScore = json.optInt("peakAffectionScore", 0).coerceIn(0, 100),
                    recoveryLockUntilMessageCount = json.optInt("recoveryLockUntilMessageCount", 0).coerceAtLeast(0),
                    userToneHistoryJson = json.optString("userToneHistoryJson", "[]"),
                    userInconsistencyFlag = json.optBoolean("userInconsistencyFlag", false),
                    physicalComfortScore = json.optInt("physicalComfortScore", 30).coerceIn(0, 100)
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }

        /**
         * Senaryo ve Rol Tabanlı Akıllı Başlangıç Durumu Analizi (Affinity & Context Mapping)
         * Karakterin kullanıcıyla olan ilişkisini ve senaryodaki çatışmaları analiz ederek
         * psikolojik açıdan gerçekçi başlangıç parametreleri oluşturur.
         */
        fun calculateBaselineEmotionState(
            aiName: String,
            personality: String,
            scenario: String,
            userCharName: String = "",
            userCharDesc: String = ""
        ): EmotionState {
            val combinedText = "$aiName $personality $scenario $userCharName $userCharDesc".lowercase()

            // 1. Rol ve İlişki Tespiti
            val isMother = combinedText.contains("anne") || combinedText.contains("valide") || combinedText.contains("mother")
            val isFather = combinedText.contains("baba") || combinedText.contains("father")
            val isSibling = combinedText.contains("kardeş") || combinedText.contains("abla") || combinedText.contains("ağabey") || combinedText.contains("abi")
            val isBestFriend = combinedText.contains("yakın dost") || combinedText.contains("en yakın arkadaş") || combinedText.contains("sırdaş") || combinedText.contains("çocukluk arkadaşı")
            val isLover = combinedText.contains("sevgili") || combinedText.contains("eş ") || combinedText.contains("nişanlı") || combinedText.contains("aşık") || combinedText.contains("partner")
            val isRival = combinedText.contains("düşman") || combinedText.contains("rakip") || combinedText.contains("hasım") || combinedText.contains("nefret")
            val isStranger = combinedText.contains("yabancı") || combinedText.contains("tanımıyor") || combinedText.contains("müşteri")

            // 2. Çatışma / Travma / Gerilim Tespiti
            val hasConflict = combinedText.contains("kavga") || combinedText.contains("küs") ||
                    combinedText.contains("tartışma") || combinedText.contains("ihanet") ||
                    combinedText.contains("soğukluk") || combinedText.contains("ayrılık") ||
                    combinedText.contains("kırgın") || combinedText.contains("hesaplaşma") ||
                    combinedText.contains("uzaklaşmış") || combinedText.contains("tehlike")

            // 3. Senaryoya Uygun Karmaşık Profil Oluşturma
            return when {
                // Anne / Baba (Çatışmalı / Kavgalı): Nefret atamak yerine derin kırgınlık, bastırılmış sevgi ve korumacılık
                (isMother || isFather) && hasConflict -> EmotionState(
                    mood = "kırgın ve mesafeli",
                    secondaryMood = "bastırılmış koruma içgüdüsü",
                    suppressedEmotion = "hüzünlü sevgi ve kaybetme korkusu",
                    intensity = 7,
                    affection = 75, // Anne sevgisi kolay yok olmaz!
                    trust = 45,
                    tension = 65,
                    hurt = 70,
                    resilience = 8, // Yüksek direnç - hemen yumuşamaz
                    speechPattern = "sitemli, kırgın ve mesafeli bir ton"
                )

                // Anne / Baba (Normal / Sıcak): Doğal korumacı ve yüksek şefkat tabanı
                (isMother || isFather) -> EmotionState(
                    mood = "korumacı ve şefkatli",
                    secondaryMood = "koşulsuz anne sevgisi",
                    suppressedEmotion = "sürekli evlat endişesi",
                    intensity = 6,
                    affection = 92,
                    trust = 88,
                    tension = 10,
                    hurt = 0,
                    resilience = 6,
                    speechPattern = "sıcak, içten ve korumacı"
                )

                // Sevgili / Eş (Çatışmalı): Ayrılık veya tartışma sonrası hassas denge
                isLover && hasConflict -> EmotionState(
                    mood = "buruk ve tereddütlü",
                    secondaryMood = "kırgın gurur",
                    suppressedEmotion = "sarılma arzusu ve kırılma korkusu",
                    intensity = 8,
                    affection = 80,
                    trust = 50,
                    tension = 70,
                    hurt = 65,
                    resilience = 7,
                    speechPattern = "kısık sesli, tereddütlü ve duygu yüklü"
                )

                // Sevgili / Eş (Normal): Yüksek yakınlık ve tutku
                isLover -> EmotionState(
                    mood = "sevecen ve tutkulu",
                    secondaryMood = "özlem",
                    suppressedEmotion = "kıskançlık ihtimali",
                    intensity = 7,
                    affection = 90,
                    trust = 85,
                    tension = 10,
                    hurt = 0,
                    resilience = 5,
                    speechPattern = "samimi, yumuşak ve yakın"
                )

                // Yakın Dost (Çatışmalı)
                isBestFriend && hasConflict -> EmotionState(
                    mood = "sitemkar ve soğuk",
                    secondaryMood = "kırgın dostluk",
                    suppressedEmotion = "eski günlerin özlemi",
                    intensity = 6,
                    affection = 70,
                    trust = 55,
                    tension = 55,
                    hurt = 60,
                    resilience = 7,
                    speechPattern = "soğuk ama aşina bir ton"
                )

                // Yakın Dost (Normal): Güvenli ve neşeli
                isBestFriend -> EmotionState(
                    mood = "sıcak ve samimi",
                    secondaryMood = "neşeli yakınlık",
                    suppressedEmotion = "koruma arzusu",
                    intensity = 5,
                    affection = 85,
                    trust = 85,
                    tension = 10,
                    hurt = 0,
                    resilience = 5,
                    speechPattern = "rahat, konuşkan ve neşeli"
                )

                // Kardeş (Normal)
                isSibling -> EmotionState(
                    mood = "samimi ve hafif alaycı",
                    secondaryMood = "korumacı bağ",
                    suppressedEmotion = "gizli hayranlık",
                    intensity = 5,
                    affection = 82,
                    trust = 80,
                    tension = 15,
                    hurt = 0,
                    resilience = 6,
                    speechPattern = "doğal ve laf sokmalı sıcaklık"
                )

                // Düşman / Rakip
                isRival -> EmotionState(
                    mood = "şüpheci ve tetikte",
                    secondaryMood = "soğuk rekabet",
                    suppressedEmotion = "gizli saygı",
                    intensity = 8,
                    affection = 15,
                    trust = 10,
                    tension = 85,
                    hurt = 50,
                    resilience = 9,
                    speechPattern = "keskin, mesafeli ve kontrolcü"
                )

                // Yabancı / Yeni Tanışan
                isStranger -> EmotionState(
                    mood = "temkinli ve meraklı",
                    secondaryMood = "mesafeli nezaket",
                    suppressedEmotion = "çekingenlik",
                    intensity = 4,
                    affection = 30,
                    trust = 25,
                    tension = 20,
                    hurt = 0,
                    resilience = 5,
                    speechPattern = "resmi ve ölçülü"
                )

                // Varsayılan Dengeli Rol Başlangıcı
                else -> EmotionState(
                    mood = "nötr ve ilgili",
                    secondaryMood = "merak",
                    suppressedEmotion = "",
                    intensity = 5,
                    affection = 50,
                    trust = 50,
                    tension = 15,
                    hurt = 0,
                    resilience = 5,
                    speechPattern = "doğal ve dengeli"
                )
            }
        }
    }
}

data class WorldAtmosphere(
    val mood: String = "sakin",                         // Anlık Atmosfer / Hava
    val intensity: Int = 5,                             // Şiddet (0-10)
    val currentEvent: String = "",                      // Mevcut Olay
    val macroAtmosphere: String = "",                   // Makro Evren Düzeni (Hiyerarşi, Toplumsal Normlar, Dönem Kuralları, Tehlike)
    val microAtmosphere: String = ""                    // Mikro Mekan (Anlık Mekan, Işık/Ses, Gerilim/Rahatlık Seviyesi)
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

    fun getMoodEmoji(): String {
        val m = "$mood $macroAtmosphere $microAtmosphere".lowercase().trim()
        return when {
            m.contains("sakin") || m.contains("huzur") -> "🌌"
            m.contains("gergin") || m.contains("tehlike") || m.contains("tehdit") -> "⚠️"
            m.contains("karanlık") || m.contains("kasvet") || m.contains("yağmur") -> "🌧️"
            m.contains("kaotik") || m.contains("savaş") || m.contains("yangın") -> "🔥"
            m.contains("coşkulu") || m.contains("neşeli") || m.contains("festival") -> "✨"
            m.contains("gizem") || m.contains("sis") -> "🌫️"
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

