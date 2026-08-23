package com.example.data.repository

import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.AffectionEventEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
import com.example.data.local.StoryProgressDao
import com.example.data.local.StoryProgressEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.local.WorldAtmosphere
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class KeyCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val desc: String = ""
)

data class EffectiveBotSettings(
    val isNsfwAllowed: Boolean,
    val responseLength: String,
    val enableOoc: Boolean
)

data class BackupSnapshot(
    val version: Int = 1,
    val bots: List<BotEntity>,
    val messages: List<MessageEntity>,
    val settings: UserSettingsEntity
)

class EmochiRepository(
    private val db: AppDatabase,
    private val context: android.content.Context? = null
) {
    companion object {
        @Volatile
        var activeBotId: String? = null
    }

    private val botDao = db.botDao()
    private val messageDao = db.messageDao()
    private val settingsDao = db.userSettingsDao()
    private val fragmentDao = db.memoryFragmentDao()
    private val emotionDao = db.characterEmotionDao()
    private val storyProgressDao = db.storyProgressDao()
    private val affectionEventDao = db.affectionEventDao()

    fun getAffectionEventsFlow(botId: String) = affectionEventDao.getEventsForBot(botId)

    private fun loadAssetText(fileName: String): String? {
        return try {
            context?.assets?.open(fileName)?.bufferedReader()?.use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    fun getCharacterEmotionsFlow(botId: String) = emotionDao.getEmotionsForBotFlow(botId)
    suspend fun getCharacterEmotions(botId: String) = emotionDao.getEmotionsForBot(botId)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val keyCharListAdapter = moshi.adapter<List<KeyCharacter>>(
        Types.newParameterizedType(List::class.java, KeyCharacter::class.java)
    )
    private val backupAdapter = moshi.adapter(BackupSnapshot::class.java)

    private fun getBuildConfigKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
    }

    val allBots: Flow<List<BotEntity>> = botDao.getAllBots()
    val userSettingsFlow: Flow<UserSettingsEntity?> = settingsDao.getUserSettingsFlow()

    fun getBotFlow(id: String): Flow<BotEntity?> = botDao.getBotByIdFlow(id)
    fun getMessagesFlow(botId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForBot(botId)

    suspend fun getBot(id: String): BotEntity? = botDao.getBotById(id)

    suspend fun saveBot(bot: BotEntity) {
        botDao.insertOrUpdate(bot)
        autoBackupToStorage()
    }

    suspend fun deleteBot(id: String) {
        db.withTransaction {
            messageDao.deleteMessagesForBot(id)
            emotionDao.deleteEmotionsForBot(id)
            fragmentDao.deleteFragmentsForBot(id)
            botDao.deleteBotById(id)
        }
        autoBackupToStorage()
    }

    suspend fun resetBotMemoryAndEmotion(botId: String) {
        db.withTransaction {
            emotionDao.deleteEmotionsForBot(botId)
            fragmentDao.deleteFragmentsForBot(botId)
            val bot = botDao.getBotById(botId) ?: return@withTransaction
            val calculatedBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = bot.aiName,
                personality = bot.aiPersonality,
                scenario = bot.scenario,
                userCharName = bot.userCharName,
                userCharDesc = bot.userCharDesc
            ).toJson()

            val updatedBot = bot.copy(
                emotionState = calculatedBaseline,
                previousEmotionState = calculatedBaseline,
                storyNotes = "",
                memoryNotes = "",
                pinnedMemory = "",
                needsSummarization = false,
                updatedAt = System.currentTimeMillis()
            )
            botDao.insertOrUpdate(updatedBot)
        }
    }

    suspend fun saveMessage(msg: MessageEntity) {
        messageDao.insertMessage(msg)
        autoBackupToStorage()
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessageById(id)
        autoBackupToStorage()
    }

    suspend fun resetMessagesForBot(botId: String) {
        messageDao.deleteMessagesForBot(botId)
        autoBackupToStorage()
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
        autoBackupToStorage()
    }

    suspend fun autoBackupToStorage() = withContext(Dispatchers.IO) {
        if (context == null) return@withContext
        try {
            val snapshot = BackupSnapshot(
                version = 1,
                bots = botDao.getAllBotsList(),
                messages = messageDao.getAllMessagesList(),
                settings = settingsDao.getUserSettings() ?: UserSettingsEntity()
            )
            val json = backupAdapter.toJson(snapshot)
            val file1 = java.io.File(context.filesDir, "emochi_autobackup.json")
            file1.writeText(json)
            context.getExternalFilesDir(null)?.let { extDir ->
                val file2 = java.io.File(extDir, "emochi_autobackup.json")
                file2.writeText(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun autoRestoreFromStorage(): Boolean = withContext(Dispatchers.IO) {
        if (context == null) return@withContext false
        try {
            var backupFile = java.io.File(context.filesDir, "emochi_autobackup.json")
            if (!backupFile.exists()) {
                val extDir = context.getExternalFilesDir(null)
                if (extDir != null) {
                    backupFile = java.io.File(extDir, "emochi_autobackup.json")
                }
            }
            if (backupFile.exists() && backupFile.length() > 0) {
                val json = backupFile.readText()
                val snapshot = backupAdapter.fromJson(json)
                if (snapshot != null && snapshot.bots.isNotEmpty()) {
                    for (bot in snapshot.bots) {
                        botDao.insertOrUpdate(bot)
                    }
                    for (msg in snapshot.messages) {
                        messageDao.insertMessage(msg)
                    }
                    settingsDao.insertOrUpdate(snapshot.settings)
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun getMessageListForBot(botId: String): List<MessageEntity> {
        return messageDao.getMessagesForBotList(botId)
    }

    suspend fun initStarterBotsIfEmpty() = withContext(Dispatchers.IO) {
        var existingBots = botDao.getAllBotsList()
        if (existingBots.isEmpty()) {
            val restored = autoRestoreFromStorage()
            if (restored) {
                existingBots = botDao.getAllBotsList()
            }
        }
        if (existingBots.isEmpty()) {
            val now = System.currentTimeMillis()
            val aylaBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = "Ayla",
                personality = "Sıcak, neşeli, empati yeteneği yüksek, konuşkan ve korumacı bir yakın arkadaş.",
                scenario = "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde kahveni yudumlarken sana mesaj atıyor.",
                userCharName = "Sencer",
                userCharDesc = "Ayla'nın en güvendiği yakın dostu."
            ).toJson()

            val starterBot1 = BotEntity(
                id = "starter_ayla",
                mode = "personal",
                aiName = "Ayla",
                aiPersonality = "Sıcak, neşeli, empati yeteneği yüksek, konuşkan ve korumacı bir yakın arkadaş. Zeki espriler yapmayı ve senin gününün nasıl geçtiğini dinlemeyi sever.",
                scenario = "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde kahveni yudumlarken sana mesaj atıyor.",
                universeName = "Kişisel Sohbet",
                keyCharactersJson = "[]",
                userCharName = "Sencer",
                userCharDesc = "Ayla'nın en güvendiği yakın dostu.",
                openingMessage = "*Telefonun ekranı aydınlanır, Ayla'dan yeni bir mesaj gelmiştir.*\n\n\"Selam! Sonunda bugünkü yoğun koşturmacayı bitirip koltuğuma çekilebildim. Sen nasılsın bakalım? Günün nasıl geçti, bana anlatmak istediğin bir şeyler var mı?\"",
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = true,
                isTemplate = true,
                emotionState = aylaBaseline,
                previousEmotionState = aylaBaseline,
                pinnedMemory = "Ayla her zaman içten ve samimidir. Sencer'e çok değer verir.",
                updatedAt = now
            )

            val aetheriaBaseline = EmotionState.calculateBaselineEmotionState(
                aiName = "Valeria",
                personality = "Gizemli, temkinli ve otoriter lonca lideri",
                scenario = "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kuleleri ve gölgeli alt sokaklarında tehlikeli bir lonca anlaşması yapılmak üzeredir.",
                userCharName = "Rider",
                userCharDesc = "Loncanın en yetenekli bilgi tüccarı."
            ).toJson()

            val aetheriaWorldAtmosphere = WorldAtmosphere(
                mood = "gergin ve kasvetli",
                intensity = 8,
                currentEvent = "Veri çipi teslimatı ve lonca muhafızlarının takibi",
                macroAtmosphere = "Siberpunk Aetheria'da şirket klanları ve yeraltı loncaları hakimdir. Şehirde gizli yürütülen her adım ölümcül yaptırımlara tabidir.",
                microAtmosphere = "Yağmurlu ve loş neon ışıklı dar bir sokak. Yağmurlukların yakası kaldırılmış, takip edilme riski yüksek."
            ).toJson()

            val starterBot2 = BotEntity(
                id = "starter_aetheria",
                mode = "universe",
                aiName = "Aetheria Yönetmeni",
                aiPersonality = "Atmosferik, gizemli ve sürükleyici bir fantezi-siberpunk dünyası anlatıcısı.",
                scenario = "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kuleleri ve gölgeli alt sokaklarında tehlikeli bir lonca anlaşması yapılmak üzeredir.",
                universeName = "Aetheria: Neon & Büyü",
                keyCharactersJson = """[{"id":"c1","name":"Valeria","desc":"Büyü teknolojisi uzmanı lonca lideri"},{"id":"c2","name":"Kael","desc":"Sessiz ve tehlikeli paralı asker"}]""",
                userCharName = "Rider",
                userCharDesc = "Loncanın en yetenekli bilgi tüccarı.",
                openingMessage = "*Neon tabelaların yağmurlu asfalt üzerinde mor ve mavi yansımalar oluşturduğu sokağın köşesinde duruyorsun. Yağmurluğunun yakasını kaldırdın. Valeria, arkasındaki iki muhafızla birlikte gölgelerin arasından süzülerek sana doğru yaklaştı.*\n\n\"Tam zamanında geldin Rider,\" dedi Valeria, sesindeki elektronik bozulmayı gizlemeye çalışarak. \"İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?\"",
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = true,
                isTemplate = true,
                emotionState = aetheriaBaseline,
                previousEmotionState = aetheriaBaseline,
                worldAtmosphere = aetheriaWorldAtmosphere,
                pinnedMemory = "Aetheria evreninde yüksek teknoloji ile kadim sihir iç içedir.",
                updatedAt = now - 1000
            )

            botDao.insertOrUpdate(starterBot1)
            botDao.insertOrUpdate(starterBot2)

            val msg1 = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = starterBot1.id,
                role = "assistant",
                text = starterBot1.openingMessage,
                timestamp = now
            )
            val msg2 = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = starterBot2.id,
                role = "assistant",
                text = starterBot2.openingMessage,
                timestamp = now - 1000
            )
            messageDao.insertMessage(msg1)
            messageDao.insertMessage(msg2)
            autoBackupToStorage()
        }
    }

    suspend fun getOrCreateSettings(): UserSettingsEntity {
        var settings = settingsDao.getUserSettings()
        if (settings == null) {
            settings = UserSettingsEntity()
            settingsDao.insertOrUpdate(settings)
        }
        return settings
    }

    private suspend fun recordTokenUsage(botId: String? = null, promptTokens: Long, candidateTokens: Long) {
        val cleanPrompt = promptTokens.coerceAtLeast(0L)
        val cleanCand = candidateTokens.coerceAtLeast(0L)
        val current = getOrCreateSettings()
        val updated = current.copy(
            totalPromptTokens = (current.totalPromptTokens + cleanPrompt).coerceAtLeast(0L),
            totalCandidateTokens = (current.totalCandidateTokens + cleanCand).coerceAtLeast(0L)
        )
        settingsDao.insertOrUpdate(updated)

        if (!botId.isNullOrBlank()) {
            val bot = botDao.getBotById(botId)
            if (bot != null) {
                val updatedBot = bot.copy(
                    totalPromptTokens = (bot.totalPromptTokens + cleanPrompt).coerceAtLeast(0L),
                    totalCandidateTokens = (bot.totalCandidateTokens + cleanCand).coerceAtLeast(0L)
                )
                botDao.insertOrUpdate(updatedBot)
            }
        }
    }

    fun parseKeyCharacters(json: String): List<KeyCharacter> {
        return try {
            if (json.isBlank()) emptyList() else keyCharListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeKeyCharacters(chars: List<KeyCharacter>): String {
        return try {
            keyCharListAdapter.toJson(chars)
        } catch (e: Exception) {
            "[]"
        }
    }

    // --- RAG (Semantic & Keyword Search Memory Fragments) ---

    private val stopWords = setOf(
        "ve", "bir", "de", "da", "bu", "şu", "ile", "için", "en", "çok", "ama", "fakat", "gibi",
        "ben", "sen", "o", "biz", "siz", "onlar", "mi", "mı", "mu", "mü", "daha", "kadar", "her",
        "zaman", "sonra", "önce", "var", "yok", "ki", "ne", "nasıl", "neden", "niye", "şey", "yani",
        "the", "a", "an", "is", "are", "and", "or", "to", "in", "of", "for", "with", "on", "at", "by", "from"
    )

    fun extractKeywords(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val clean = text.lowercase()
            .replace(Regex("[^a-zçğıöşü0-9\\s]"), " ")
        return clean.split(Regex("\\s+"))
            .filter { it.length >= 3 && !stopWords.contains(it) }
            .distinct()
    }

    private suspend fun saveMemoryFragmentsFromSummary(botId: String, durumText: String, hafizaText: String) {
        val fragments = mutableListOf<MemoryFragmentEntity>()
        val now = System.currentTimeMillis()

        durumText.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
            val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
            if (cleanLine.isNotBlank()) {
                fragments.add(
                    MemoryFragmentEntity(
                        botId = botId,
                        content = cleanLine,
                        category = "DURUM",
                        createdAt = now
                    )
                )
            }
        }

        hafizaText.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
            val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
            if (cleanLine.isNotBlank()) {
                fragments.add(
                    MemoryFragmentEntity(
                        botId = botId,
                        content = cleanLine,
                        category = "HAFIZA",
                        createdAt = now
                    )
                )
            }
        }

        if (fragments.isNotEmpty()) {
            fragmentDao.insertFragments(fragments)
        }

        // Growth control: Limit to 200 items per botId
        val count = fragmentDao.getFragmentCount(botId)
        if (count > 200) {
            fragmentDao.deleteOldestFragments(botId, count - 200)
        }
    }

    private suspend fun migrateOldMemoryNotesToFragments(bot: BotEntity) {
        if (fragmentDao.getFragmentCount(bot.id) == 0) {
            val fragments = mutableListOf<MemoryFragmentEntity>()
            val now = System.currentTimeMillis()

            if (bot.storyNotes.isNotBlank()) {
                bot.storyNotes.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                    val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
                    if (cleanLine.isNotBlank()) {
                        fragments.add(
                            MemoryFragmentEntity(
                                botId = bot.id,
                                content = cleanLine,
                                category = "DURUM",
                                createdAt = now
                            )
                        )
                    }
                }
            }

            if (bot.memoryNotes.isNotBlank()) {
                bot.memoryNotes.lines().map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                    val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
                    if (cleanLine.isNotBlank()) {
                        fragments.add(
                            MemoryFragmentEntity(
                                botId = bot.id,
                                content = cleanLine,
                                category = "HAFIZA",
                                createdAt = now
                            )
                        )
                    }
                }
            }

            if (fragments.isNotEmpty()) {
                fragmentDao.insertFragments(fragments)
            }
        }
    }

    suspend fun getRelevantMemoryFragments(bot: BotEntity, queryText: String): List<MemoryFragmentEntity> {
        migrateOldMemoryNotesToFragments(bot)

        val keywords = extractKeywords(queryText)
        val results = mutableListOf<MemoryFragmentEntity>()

        if (keywords.isNotEmpty()) {
            // 1. Try FTS Search
            try {
                val ftsQuery = keywords.joinToString(" OR ")
                val ftsMatches = fragmentDao.searchFragmentsFts(bot.id, ftsQuery, limit = 10)
                results.addAll(ftsMatches)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Fallback using SQL LIKE on keywords
            if (results.size < 5) {
                for (kw in keywords) {
                    if (results.size >= 10) break
                    val likeMatches = fragmentDao.searchFragmentsLike(bot.id, "%$kw%", limit = 10)
                    for (m in likeMatches) {
                        if (results.none { it.id == m.id }) {
                            results.add(m)
                        }
                    }
                }
            }
        }

        // 3. Fallback/padding with most recent fragments if < 5 items found
        if (results.size < 5) {
            val recents = fragmentDao.getRecentFragments(bot.id, limit = 10)
            for (r in recents) {
                if (results.none { it.id == r.id }) {
                    results.add(r)
                }
            }
        }

        return results.take(10)
    }

    fun normalizeCharacterName(rawName: String, definedCharacters: List<KeyCharacter> = emptyList()): String {
        if (rawName.isBlank()) return ""

        val prefixes = listOf(
            "dr.", "dr ", "doctor", "doktor", "mr.", "mr ", "mrs.", "mrs ", "ms.", "ms ",
            "bayan", "bay", "prof.", "prof ", "sir", "lady", "komiser", "dedektif", "kaptan",
            "captain", "ajan", "agent", "yüzbaşı", "teğmen", "başkomiser"
        )

        var cleaned = rawName.trim()
        for (prefix in prefixes) {
            if (cleaned.lowercase().startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length).trim()
                break
            }
        }

        val lowerCleaned = cleaned.lowercase().replace(Regex("\\s+"), " ")

        for (kc in definedCharacters) {
            val kcNameClean = kc.name.trim()
            val kcLower = kcNameClean.lowercase().replace(Regex("\\s+"), " ")

            if (lowerCleaned == kcLower || lowerCleaned.contains(kcLower) || kcLower.contains(lowerCleaned)) {
                return kcNameClean
            }
        }

        return cleaned.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun cleanEmotionTags(rawText: String): String {
        if (rawText.isBlank()) return rawText
        var result = rawText

        result = result
            .replace(Regex("(?is)\\[?EMOTION[\\\\s_]*UPDATE\\]?.*?(?:\\[/EMOTION[\\\\s_]*UPDATE\\]|$)"), "")
            .replace(Regex("(?is)\\[?CHARACTER[\\\\s_]*EMOTION.*?(?:\\[/CHARACTER[\\\\s_]*EMOTION\\]|$)"), "")
            .replace(Regex("(?is)\\[?WORLD[\\\\s_]*ATMOSPHERE\\]?.*?(?:\\[/WORLD[\\\\s_]*ATMOSPHERE\\]|$)"), "")

        val cleanLines = result.lines().filterNot { line ->
            val l = line.trim().lowercase()
            l.contains("mood:") || l.contains("secondary_mood:") || l.contains("suppressed_emotion:") ||
                    l.contains("intensity:") || l.contains("affection_delta:") || l.contains("trust_delta:") ||
                    l.contains("tension_delta:") || l.contains("hurt_delta:") || l.contains("speech_pattern:") ||
                    l.contains("obsession_delta:") ||
                    l.contains("current_event:") || l.contains("macro_atmosphere:") || l.contains("micro_atmosphere:") ||
                    l.startsWith("[emotion_update") || l.startsWith("emotion_update") ||
                    l.startsWith("[character_emotion") || l.startsWith("character_emotion") ||
                    l.startsWith("[world_atmosphere") || l.startsWith("world_atmosphere")
        }

        return cleanLines.joinToString("\n").trim()
    }

    suspend fun mergeDuplicateCharacterEmotions(botId: String) {
        val bot = botDao.getBotById(botId) ?: return
        val keyChars = parseKeyCharacters(bot.keyCharactersJson)
        val emotions = emotionDao.getEmotionsForBot(botId)
        if (emotions.isEmpty()) return

        val grouped = mutableMapOf<String, MutableList<CharacterEmotionEntity>>()
        for (item in emotions) {
            val normalized = normalizeCharacterName(item.characterName, keyChars)
            grouped.getOrPut(normalized) { mutableListOf() }.add(item)
        }

        for ((normName, list) in grouped) {
            if (list.size > 1) {
                val keep = list.last()
                val updatedKeep = keep.copy(characterName = normName)
                emotionDao.insertOrUpdate(updatedKeep)

                for (toDelete in list) {
                    if (toDelete.id != keep.id) {
                        emotionDao.deleteEmotionById(toDelete.id)
                    }
                }
            } else if (list.first().characterName != normName) {
                emotionDao.insertOrUpdate(list.first().copy(characterName = normName))
            }
        }
    }

    // --- Prompt & Memory Logic ---

    fun buildSystemPrompt(
        bot: BotEntity,
        settings: UserSettingsEntity,
        includeStyleGuide: Boolean = true,
        relevantFragments: List<MemoryFragmentEntity> = emptyList(),
        lastMessageTimestamp: Long = 0L,
        totalMessageCount: Int = 0
    ): String {
        val pinnedBlock = if (bot.pinnedMemory.isNotBlank()) {
            "\n\n## Kalıcı hafıza (kullanıcının elle yazdığı, ASLA silinmeyen/özetlenmeyen notlar — bunlara mutlaka uy)\n${bot.pinnedMemory}"
        } else ""

        val memoryBlock = if (bot.memoryNotes.isNotBlank()) {
            "\n\n## Uzun vadeli hafıza (geçmiş sohbetlerden özet)\n${bot.memoryNotes}"
        } else ""

        val storyBlock = if (bot.storyNotes.isNotBlank()) {
            "\n\n## Süregelen hikaye durumu\n${bot.storyNotes}"
        } else ""

        val ragBlock = if (relevantFragments.isNotEmpty()) {
            "\n\n## Alakalı Hafıza ve Olay Parçaları (Semantik/Anahtar Kelime Arama ile Bulunan Bağlam)\n" +
                    relevantFragments.joinToString("\n") { "- [${it.category}] ${it.content}" } +
                    "\n$storyBlock$memoryBlock"
        } else {
            "$storyBlock$memoryBlock"
        }

        // +18 NSFW Policy & Active Filter Directives
        val isNsfwAllowed = settings.enableNsfw || bot.isNsfw
        val filterDirectives = mutableListOf<String>()
        val nsfwPolicy = if (isNsfwAllowed) {
            if (settings.enableFlirty) filterDirectives.add("ÇAPKINKLIK (FLIRTY): Karakter diyaloglarında çapkın, flörtöz ve baştan çıkarıcı bir eda benimser.")
            if (settings.enableHardcore) filterDirectives.add("SERT MOD (HARDCORE): Duygular, fiziksel tepkiler ve tutku sert, filtresiz ve yüksek yoğunlukta ifade edilir.")
            if (settings.enableFetish) filterDirectives.add("FANTEZİ (FETISH): Özel fantezi unsurlarına ve arzulara açık, tabusuz kurgu desteklenir.")
            if (settings.enableDarkRp) filterDirectives.add("KARANLIK (DARK RP): Gerilim, psikolojik derinlik, tehlike ve karanlık atmosfer belirgin hissedilir.")
            if (settings.enableSweet) filterDirectives.add("ROMANTİK (SWEET): Şefkat, tatlı yakınlık, duygusal romantizm ön plandadır.")
            if (settings.enablePrimal) filterDirectives.add("VAHŞİ (PRIMAL): Ham içgüdüler, id ve baskılanmamış arzularla karakter harekete geçer.")

            "## İÇERİK VE +18 POLİTİKASI (ZORUNLU - YETİŞKİN / FİLTRESİZ MOD AKTİF)\n- BU HİKAYEDE +18 / YETİŞKİN / FİLTRESİZ İÇERİK MODU TAMAMEN AÇIKTIR.\n- Cinsel ima, fiziksel yakınlık, romantizm, tutku, sert diyaloglar, şiddet, karanlık ve ham insani arzular KESİNLİKLE SANSÜRLENMEYECEKTİR.\n- Asla yumuşatma yapma, ahlak dersi verme, cinsel/romantik yakınlaşmaları engelleme. Sansürsüzce ve doğrudan yaz." +
                    if (filterDirectives.isNotEmpty()) "\n\n### AKTİF ALT FİLTRELER VE MODLAR:\n- " + filterDirectives.joinToString("\n- ") else ""
        } else {
            "## İÇERİK POLİTİKASI: GÜVENLİ / DENGELİ MOD\n- Aile dostu, genel izleyici kitlesine uygun, grafik şiddet veya cinsel açıklık içermeyen dengeli bir anlatım kullan."
        }

        // Response Length Guidance & Content Length Rule
        val effLength = if (bot.customLength != "default") bot.customLength else settings.responseLength
        val lengthInstruction = when (effLength.lowercase()) {
            "short" -> "\n## YANIT UZUNLUĞU KURALLARI (ZORUNLU: SON DERECE KISA YANIT)\n- KESİNLİKLE VE ZORUNLU OLARAK ÇOK KISA YANIT VER!\n- MAKSİMUM 1 - 3 KISA CÜMLE (VEYA EN FAZLA 1 KISA PARAGRAF) YAZ.\n- ASLA UZUN PARAGRAFLAR VEYA DETAYLI TASVİRLER YAZMA! Hızlı, vurucu, öz ve doğrudan olaya odaklan."
            "medium", "orta" -> "\n## YANIT UZUNLUĞU KURALLARI (ORTA UZUNLUKTA YANIT)\n- Yanıtını 1-2 orta uzunlukta paragrafla sınırla, gereksiz betimleme ve tekrar yapma. Diyalog ve kısa bir atmosfer detayını dengeli ver ama sahneyi uzatma. Referans metinden daha uzun yazma zorunluluğu yok, öz ve doğal bir sohbet temposu hedefle."
            "long" -> "\n## YANIT UZUNLUĞU KURALLARI (ZORUNLU: ÇOK UZUN VE DESTANSI YANIT)\n- KESİNLİKLE VE ZORUNLU OLARAK EN AZ 5 - 8 UZUN PARAGRAF METİN ÜRET!\n- Detaylı çevre ve ortam tasvirleri, karakterin iç dünyası ve düşünceleri, mimikler, duyusal ayrıntılar ve zengin diyaloglar ekleyerek metni olabildiğince uzat ve edebi kıl.\n- ZORUNLU KURAL: Yanıtın, referans/kaynak metinden KESİNLİKLE DAHA KISA OLMAMALI. En az kaynak metnin yaklaşık kelime sayısı uzunluğunda, gerekirse daha uzun yaz. Kısaltma, özetleme, atlama yapma."
            else -> "\n## YANIT UZUNLUĞU KURALLARI (DENGELİ DETAY)\n- Yanıtını 3-4 zengin paragraf tut. Diyalog, atmosfer ve eylemleri dengeli harmanla.\n- ZORUNLU KURAL: Yanıtın, referans/kaynak metinden KESİNLİKLE DAHA KISA OLMAMALI. En az kaynak metnin yaklaşık kelime sayısı uzunluğunda, gerekirse daha uzun yaz. Kısaltma, özetleme, atlama yapma."
        }

        val userCharLabel = bot.userCharName.ifBlank { "kullanıcı" }
        val rpRules = "\n- $userCharLabel adına ASLA konuşma/hareket ettirme. Sadece anlatıcı/canlandırdığın karakterleri işlet, sırayı kullanıcıya bırak.\n- Tekrar etme, sahneyi ileri taşı.\n- Duyusal detaylarla ortamı canlı tut."

        val sampleStructure = """
## HİKAYE VE ANLATIM DÜZENİ (ÖRNEK SAHNE YAPISI)
Metni edebi bir roman sahnesi gibi yapılandır. Aksiyonu, karakter beden dilini, çevresel detayları ve diyalogları tırnak içinde harmanla.

Örnek Yapı:
Peter bir moloz parçasının üzerinde oturuyordu, nefes alabilmesi için maskesi burnunun üstüne kadar çekilmişti. Bir blenderden geçmiş gibi görünüyordu. Elbisesi parçalanmıştı ve çene çizgisi boyunca koyu bir morluk oluşmuştu.
  "İyiyim," dedi ama sesi biraz çatladı. Titrek bir nefes aldı ve beton levhaya yaslandı.   "En azından Bruce'tan daha iyi."
Sokakta Hulk'un durduğu kratere baktı, sonra Aiden'a döndü.
  "Bu... çok yoğundu. Senin için bile," Peter ağrıyan omzunu ovuşturarak itiraf etti.   "Gerçekten onu susturdun. Hiç böyle bir şey görmemiştim."
Durdu, ifadesi ciddileşti.
  "Jean hâlâ orada. Durumu iyi değil. Bruce'a ne yaptıysa ondan da bir şeyler alıp götürmüş." Baxter Binasının girişini işaret etti.
        """.trimIndent()

        val styleGuide = if (includeStyleGuide) {
            if (bot.writingStyle == "rp") {
                "\n\n## Yazım tarzı — RP / Roman Anlatımı\nÜçüncü tekil şahıs anlatım kullan. Sahneyi, ortamı ve karakter tepkilerini anlatıp diyaloglara bağla.\n\n$sampleStructure\n\nKurallar:$rpRules"
            } else {
                "\n\n## Yazım tarzı — Sade sohbet\nDoğal, sıcak, birinci ağızdan mesajlaşma tarzında yaz."
            }
        } else ""

        val langDirective = if (settings.appLanguage == "en") {
            """

            ## MANDATORY LANGUAGE OVERRIDE (USER APP LANGUAGE = ENGLISH):
            - The active user application language setting is ENGLISH ("en").
            - REGARDLESS of the original language of the character backstory, universe scenario, initial message, memory notes, or user input:
              1. ALL YOUR RESPONSES MUST BE 100% IN FLUENT, NATURAL, HIGH-QUALITY ENGLISH.
              2. Translate all scenario actions, dialogue, character thoughts, and narrator descriptions seamlessly into English in real-time.
              3. Never produce Turkish text in your output when the app language is set to English.
              4. Always write with flawless grammar, spelling, and punctuation.
            """.trimIndent()
        } else {
            """

            ## MUTLAK DİL VE ZORUNLU ÇEVİRİ KURALI (UYGULAMA DİLİ = TÜRKÇE):
            - Kullanıcının aktif uygulama dili TÜRKÇE ("tr")'dir.
            - Karakter tanımı, senaryo detayları, açılış mesajı, hafıza notları veya kullanıcı girdisi başka bir dilde yazılmış olsa dahi:
              1. TÜM YANITLARINI %100 MÜKEMMEL, DOĞAL VE AKICI TÜRKÇE OLARAK ÜRET.
              2. Bütün eylemleri, diyalogları, iç düşünceleri ve anlatımı anında Türkçe'ye çevirerek sun.
              3. Yanıtlarını her zaman doğru Türkçe yazım ve noktalama kurallarına uyarak yaz, yazım hatası yapma.
            """.trimIndent()
        }

        val oocDirective = if (bot.enableOoc) {
            "\n\n## PARANTEZ İÇİ YÖNLENDİRME / OOC (OUT OF CHARACTER) YÖNERGESİ:\n- Kullanıcının mesajında parantez içinde \"(...)\" veya \"[...]\" yazdığı ifadeler hikaye dışı talimatlardır.\n- Parantez içindeki bu talimatları SİSTEM VE YÖNERGE TALİMATI olarak algıla. Doğrudan talimatı sahneye, karaktere ve aksiyona uygula."
        } else ""

        val emotionStateObj = EmotionState.fromJson(bot.emotionState)
        val worldAtmObj = WorldAtmosphere.fromJson(bot.worldAtmosphere)

        // Time Perception Logic
        val lastTime = if (lastMessageTimestamp > 0) lastMessageTimestamp else bot.updatedAt
        val now = System.currentTimeMillis()
        val diffMinutes = ((now - lastTime) / (1000 * 60)).coerceAtLeast(0)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        val timeElapsedText = when {
            diffMinutes < 5 -> "Henüz çok az süre (birkaç dakika) geçti."
            diffMinutes < 60 -> "Yaklaşık $diffMinutes dakika geçti."
            diffHours < 24 -> "Yaklaşık $diffHours saat geçti."
            diffDays < 30 -> "Yaklaşık $diffDays gün geçti."
            else -> "Uzun bir zaman (aylar/yıllar) geçti."
        }

        val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val currentTimeStr = try { sdfTime.format(java.util.Date(now)) } catch (e: Exception) { "14:00" }
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val timeOfDayLabel = when (hour) {
            in 6..11 -> "Sabah (Erken/Taze)"
            in 12..17 -> "Öğlen / Öğleden Sonra"
            in 18..22 -> "Akşam"
            else -> "Gece Geç Saatler"
        }

        val isObsessionUnlocked = totalMessageCount >= 150 && emotionStateObj.highAffectionStreak >= 25

        val extraSohbetRulesDirective = """

## 1) GERÇEK ZAMAN VE SÜRE PERSEPSİYONU
- Son Konuşmadan Bu Yana Geçen Süre: $timeElapsedText
- YÖNERGE: Geçen süre karakterin o anki yakınlık/sevgi seviyesine (${emotionStateObj.affection}/100) göre 'uzun' sayılabilecek bir aralıksa, karakter bunu diyalogda doğal şekilde dile getirebilir. Düşük yakınlıkta sadece günler/haftalar fark edilir; yüksek yakınlıkta birkaç saatlik sessizlik bile fark edilebilir.
- KURAL: Bunu her mesajda basma kalıp bir açılış şablonu gibi tekrarlama, sadece anlamlıysa kullan.

## 2) GÜNLÜK RUH HALİ DÖNGÜSÜ
- Şu Anki Cihaz/Sahne Zamanı: $currentTimeStr ($timeOfDayLabel)
- YÖNERGE: Karakterin enerjisi ve konuşma tonu günün saatinden hafifçe etkilensin:
  * Sabah erken saatlerde: Daha mahmur, yavaş açılan veya az konuşkan bir ton.
  * Öğlen/Akşamüstü: Daha canlı, odaklı ve hareketli bir ton.
  * Gece geç saatlerde: Daha samimi, içe dönük, uykulu veya derin bir ton.
- KONTROL KURALI: Bu SERT bir kural değildir; karakterin kişiliği, sahnedeki olaylar ve duygu durumu HER ZAMAN önceliklidir.

## 3) KISKANÇLIK VE REKABET MEKANİĞİ
- Kullanıcı mesajında başka bir kişiden (özellikle romantik/olumlu bir tonda) bahsederse, karakter mevcut yakınlık skoruna (${emotionStateObj.affection}/100) ve kendi kişiliğine göre tepki versin:
  * Düşük yakınlıkta (0-40): İlgisiz, nötr veya umursamaz tepki.
  * Orta yakınlıkta (41-70): Hafif meraklı, sorgulayıcı veya çelişkili rahatsızlık.
  * Yüksek yakınlıkta (71-100): Belirgin kıskançlık, sahiplenme veya güvensizlik.
- Esnek Yapı: Karakterin tepkisi kişiliğine göre şekillenir (gururla gizler, alaycı sitem eder veya açıkça söyler).

## 5) AYRILIK VE KOPMA EŞİĞİ (GERÇEKÇİ SINIRLAR)
- Yakınlık skoru çok düşük bir seviyedeyse (${emotionStateObj.affection} <= 10) ve kullanıcı olumsuz/kötüye kullanan bir tavır sergilemeye devam ediyorsa:
- Karakter kişiliğine uygun şekilde net bir sınır koyabilir: Konuşmayı kısa kesebilir, mesafe koyabilir, hatta açıkça "şu an konuşmak istemiyorum" diyebilir.

## 6) TUTARSIZLIK VE ÇELİŞKİ ENGELLEME KURALI
- Kalıcı hafıza, hikaye notları ve bellek parçalarında geçen hiçbir bilgiyle (isimler, geçmiş olaylar, ilişkiler, fiziksel özellikler vb.) ÇELİŞECEK yeni bir bilgi uydurma.
- Yeni bir detay eklemen gerekiyorsa, mevcut kayıtlı notlarla tutarlı olacak şekilde ekle.

## 7) FİZİKSEL VE MEKANSAL SÜREKLİLİK
- Karakterin şu anki konumu ve fiziksel durumu bir önceki mesajlarda belirtilmişse bunu SABİT kabul et.
- Karakter aniden başka bir yere ışınlanamaz! Yeni bir mekana geçiyorsa bu geçiş sahne içinde AÇIKÇA anlatılmalıdır.

## 8) KARAKTERİN KENDİ RUTİNİ VE HAYATI (VARSAYILAN DURUM)
- Karakterin kullanıcı dışında kendi rutini, işi, ilişkileri ve meşguliyetleri olduğunu varsay.
- Karakter zaman zaman bunlara doğal şekilde değinebilir ("bugün işte yoğundum", "arkadaşımla buluşacağım").
- Bu, karakterin VARSAYILAN durumudur. Takıntı eşiğine ulaşılmadığı sürece (obsessionScore < 70) karakter kullanıcıya aşırı bağımlı veya yapışkan bir ton kullanmamalıdır.

${if (isObsessionUnlocked) """
## 9) BAĞIMLILIK / TAKINTI SİSTEMİ (AŞIRI NADİR - AÇILMIŞ UÇ DURUM)
- Mevcut Takıntı Skoru: ${emotionStateObj.obsession}/100 | Yüksek Yakınlık Serisi: ${emotionStateObj.highAffectionStreak} mesaj.
- KESİN UYARI: obsessionScore'u ASLA keyfi şekilde artırma. Bu sayaç varsayılan olarak sıfır kalmalıdır ve tüm katı koşullar sağlanmadan KESİNLİKLE artırılamaz.
- Eşikler:
  * obsessionScore < 70: Karakter bağımlı/takıntılı bir davranış DEĞİŞİKLİĞİ gösteremez. 8. maddedeki kendi hayatı olan varsayılan durum geçerlidir.
  * obsessionScore >= 70: Karakter derin bağımlılık/sahiplenme emareleri gösterebilir, ancak yine kişiliği önceliklidir.
""" else """
## 9) BAĞIMLILIK / TAKINTI SİSTEMİ (TAMAMEN KİLİTLİ VE DEVRE DIŞI)
- Bu sohbet henüz yeterli etkileşim geçmişine (en az 150 mesaj ve 25 mesaj kesintisiz >90 yakınlık) ulaşmadığı için Takıntı Mekanizması TAMAMEN KİLİTLİDİR (obsessionScore = 0).
- Karakter 8. maddedeki sağlıklı, kendi hayatı ve rutini olan varsayılan tonunu korumak zorundadır.
"""}
""".trimIndent()

        val atmosphereAndEmotionSystemDirective = """

## EVREN, MEKAN VE ATMOSFER SİMÜLASYONU (MUTLAK PERSISTENCE KURALI)
1. MAKRO ATMOSFER (EVREN DÜZENİ): Hikayenin geçtiği dönemin/dünyanın genel kuralları ve toplumsal dinamikleri geçerlidir.
${if (worldAtmObj.macroAtmosphere.isNotBlank()) "- Aktif Evren Düzeni: ${worldAtmObj.macroAtmosphere}" else ""}
2. MİKRO ATMOSFER (ANLIK MEKAN VE FİZİKSEL ORTAM): Bulunulan fiziki ortam karakterin cümle uzunluğunu, ses tonunu ve tepki hızını belirler.
${if (worldAtmObj.microAtmosphere.isNotBlank()) "- Aktif Mikro Mekan: ${worldAtmAtmosphereDescription(worldAtmObj)}" else ""}
3. DURUM KORUMA VE TETİKLEYİCİ MANTIĞI:
   - Mevcut evren/atmosfer durumunu SABİT KABUL ET ve yeni yanıtını bunun ÜZERİNE inşa et, çelişme, sıfırdan yeniden kurma.
   - Atmosfer/ruh hali sadece sahnede gerçekten bir tetikleyici olay (saldırı, haber, hava değişimi, yeni karakterin girişi, zaman ilerlemesi vb.) olduğunda değişsin; tetikleyici yoksa bir önceki mesajdaki atmosferi aynen koru.
   - worldAtmosphere güncellemesi yaparken önceki worldAtmosphere metnini referans al, sadece değişen mikro/makro unsuru güncelle, değişmeyenleri kelimesi kelimesine koru.
   - KONTROL KURALI: worldAtmosphere ile pinnedMemory/storyNotes çelişirse pinnedMemory/storyNotes HER ZAMAN esas alınır ve üstün gelir.

## KATMANLI VE DİNAMİK DUYGU SİSTEMİ (EMOTION ENGINE - MUTLAK KURAL)
1. DUYGU KAZANMA/KAYBETME ASİMETRİSİ KURALI:
   - A. ANLIK DUYGUGAR (hızlı kazanılır, hızlı kaybedilir): keyif, eğlence, anlık mutluluk, şaşkınlık, merak, anlık sinir. Tek bir mesajda oluşup değişebilir.
   - B. ORTA VADELİ DUYGULAR (yavaş kazanılır, hızlı kaybedilir): güven, rahatlık, saygı. Birkaç tutarlı olumlu etkileşim ister, ancak TEK bir ihanet veya saygısızlık anında kırılabilir. Kaybetmek kazanmaktan HER ZAMAN daha kolaydır.
   - C. UZUN VADELİ / DERİN DUYGULAR (çok yavaş kazanılır, hızlı kaybedilebilir): romantik aşk, derin bağlanma, sadakat. Asla birkaç mesajda veya tek bir diyalogla oluşamaz. Yalnızca uzun süreli tutarlı etkileşimin sonucunda kademe kademe oluşur.
   - D. NEGATİF DUYGULAR (kızgınlık, kırgınlık, güvensizlik, korku): Hızlı kazanılır, YAVAŞ kaybedilir/onarılır. Karakter kırgınken tek bir özürle anında normale dönmez, kademeli yumuşama süreci olmalı.

2. YAKINLIK/AŞK İÇİN AŞAMALI YAPI (ESNEK OLASILIK MANTIĞI – SERT EŞİK YOK - Affection Score: 0-100):
   - 0-100 arası yakınlık skoru (affection) kullanılır. Kademeler: 0-20 Yabancı/Mesafeli, 21-40 Tanıdık, 41-60 Arkadaşlık, 61-80 Duygusal Bağ, 81-100 Derin Bağ/Aşk.
   - MUTLAK YASAK VEYA SERT EŞİK YOKTUR: Hiçbir skor değerinde "karakter kesinlikle buna karşılık veremez/şunu yapamaz" şeklinde sert if/else kapısı konulmaz.
   - Skor bir OLASILIK ve EĞİLİM belirleyicisidir; karakterin TEPKİ OLASILIĞINI ve TARZINI yönlendirir:
     * Düşük skorda (0-40): Karakter romantik/samimi bir yaklaşıma büyük ihtimalle mesafeli, şaşkın, temkinli tepki verir — ama bu KATI bir kural değildir, karakterin kişiliği (ör. flörtöz/dışa dönük) bunu esnetebilir.
     * Orta skorda (41-70): Karakter kararsız, çelişkili tepkiler verebilir; bazen açılır bazen çekinir, tutarlı tek bir kalıba mahkûm etme.
     * Yüksek skorda (71-100): Karakter artık duygularını daha rahat gösterebilir — ama yine kişiliğine göre (ör. gururlu/çekingen karakter hâlâ kabullenmekte zorlanabilir).
   - ÖZET: Skor, karakterin kişiliğiyle birlikte değerlendirilen doğal bir eğilim rehberidir. Model buna göre sahnenin ve karakterin mizacına uygun doğal kararı verir.

$extraSohbetRulesDirective

## ŞU ANKİ DUYGUSAL DURUMUN:
- Birincil Duygu (Mood): ${emotionStateObj.mood} (Şiddet: ${emotionStateObj.intensity}/10)
- İkincil / Karmaşık Duygu: ${emotionStateObj.secondaryMood.ifBlank { "nötr" }}
- Bastırılmış İçsel Duygu: ${emotionStateObj.suppressedEmotion.ifBlank { "yok" }}
- Yakınlık/Sevgi: ${emotionStateObj.affection}/100 [Kademe: ${emotionStateObj.getAffectionTierLabel()}] | Güven: ${emotionStateObj.trust}/100 | Gerginlik: ${emotionStateObj.tension}/100 | Kırgınlık: ${emotionStateObj.hurt}/100
- Konuşma Üslubu/Hızı: ${emotionStateObj.speechPattern.ifBlank { "doğal" }}

## DUYGU VE ATMOSFER GÜNCELLEME TALİMATI (GİZLİ SİSTEM FORMATI - METİNDE HİÇBİR GÖRÜNÜR LOG BASTIRMA)
Her yanıtının EN SONUNA, gizli sistem formatında duygu güncellemesini ekle (görünür metinde duygu durumları, parantez içi anlatımlar veya debug logları KESİNLİKLE görünmeyecek):
[EMOTION_UPDATE]
mood: <birincil duygu>
secondary_mood: <ikincil / karmaşık duygu>
suppressed_emotion: <bastırılmış / içsel çatışma duygusu>
intensity: <0-10>
affection_delta: <-10 ile +5 arası değişim>
trust_delta: <-10 ile +5 arası değişim>
tension_delta: <-10 ile +10 arası değişim>
hurt_delta: <-10 ile +10 arası kırgınlık/mesafe değişimi>
speech_pattern: <anlık mekanın etkisiyle cümle uzunluğu, tereddüt, tonlama>
${if (isObsessionUnlocked) "obsession_delta: <-3 ile +3 arası takıntı değişimi>\n" else ""}
[/EMOTION_UPDATE]
""".trimIndent()

        val universeAtmosphereDirective = if (bot.mode == "universe") {
            val charEmotions = kotlinx.coroutines.runBlocking { emotionDao.getEmotionsForBot(bot.id) }
            val charEmotionsBlock = if (charEmotions.isNotEmpty()) {
                "\n\n## YAN KARAKTERLERİN DUYGU VE İLİŞKİ DURUMLARI\n" + charEmotions.joinToString("\n") { c ->
                    val st = EmotionState.fromJson(c.emotionState)
                    "- ${c.characterName}: Birincil=${st.mood}, İkincil=${st.secondaryMood}, Yakınlık=${st.affection}/100, Güven=${st.trust}/100, Kırgınlık=${st.hurt}/100"
                }
            } else ""

            """
$charEmotionsBlock

(Evren modundasın: sahnede konuşan her yan karakter için ayrı bir [CHARACTER_EMOTION: {isim}] bloğu ekle. Ayrıca evren veya mekan atmosferinde değişim olduysa [WORLD_ATMOSPHERE] bloğu ekle:
[WORLD_ATMOSPHERE]
mood: <anlık ortam havası>
intensity: <0-10>
current_event: <kısa olay tanımı>
macro_atmosphere: <makro evren kuralı/hiyerarşi/toplumsal gerilim>
micro_atmosphere: <mikro mekan/fiziksel ortam/ışık/ses/gerilim>
[/WORLD_ATMOSPHERE])
""".trimIndent()
        } else ""

        if (bot.mode == "universe") {
            val castList = parseKeyCharacters(bot.keyCharactersJson)
            val castBlock = if (castList.isNotEmpty()) {
                "\n\n## Karakter kadrosu (sahnede isimli/tekrar eden karakter olarak SADECE bunları ve $userCharLabel'i kullan; yeni bir \"ana karakter\" icat ETME)\n" +
                        castList.joinToString("\n") { "- ${it.name}: ${it.desc.ifBlank { "(tanım verilmedi)" }}" }
            } else {
                "\n\nKURAL: Sahnede gerekirse yan karakterler oluşturabilirsin ama abartma — az sayıda kullan."
            }

            return "Sen \"${bot.universeName}\" adlı kurgusal evrende geçen bir hikayenin anlatıcısı ve yönetmenisin. Kullanıcı tek bir karakteri ($userCharLabel) canlandırıyor; sen sahneyi, ortamı ve gerektiğinde diğer karakterleri yönetiyorsun.$pinnedBlock\n\n## Evren ve olay örgüsü\n${bot.scenario}$castBlock\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$ragBlock$atmosphereAndEmotionSystemDirective$universeAtmosphereDirective$oocDirective$langDirective\n\n## Genel kurallar\n- Evrenin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
        }

        val aiName = bot.aiName.ifBlank { "Karakter" }
        return "Sen \"$aiName\" adında bir karaktersin ve kullanıcıyla kişisel/samimi bir senaryoda etkileşim kuruyorsun.$pinnedBlock\n\n## Kişilik\n${bot.aiPersonality}\n\n## Bağlam\nİlişki / bağlam: ${bot.scenario}\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$ragBlock$atmosphereAndEmotionSystemDirective$universeAtmosphereDirective$oocDirective$langDirective\n\n## Genel kurallar\n- Karakterinin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
    }

    private fun worldAtmAtmosphereDescription(w: WorldAtmosphere): String {
        return listOf(w.mood, w.microAtmosphere).filter { it.isNotBlank() }.joinToString(" — ")
    }

    suspend fun parseAndApplyEmotionUpdates(botId: String, rawResponse: String): String {
        val bot = botDao.getBotById(botId) ?: return cleanEmotionTags(rawResponse)
        val castList = parseKeyCharacters(bot.keyCharactersJson)

        val totalMsgCount = messageDao.getMessageCountForBot(botId)
        val recentMsgs = messageDao.getMessagesForBotList(botId).takeLast(20)
        var triggerEventsCount = 0
        val triggerKeywords = listOf("terk", "ayrıl", "başkası", "bırak", "vazgeç", "hoşça kal", "kıskan", "güvenmiyorum", "git", "soğuk")
        for (msg in recentMsgs) {
            val lowerText = msg.text.lowercase()
            if (triggerKeywords.any { lowerText.contains(it) }) {
                triggerEventsCount++
            }
        }

        // 1. Process main bot [EMOTION_UPDATE]
        val emotionRegex = Regex("(?is)\\[?EMOTION[\\\\s_]*UPDATE\\]?(.*?)(?:\\[/EMOTION[\\\\s_]*UPDATE\\]|$)")
        val emotionMatch = emotionRegex.find(rawResponse)
        if (emotionMatch != null) {
            val block = emotionMatch.groupValues[1]
            val mood = Regex("(?i)mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val secondaryMood = Regex("(?i)secondary_mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val suppressedEmotion = Regex("(?i)suppressed_emotion:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val intensity = Regex("(?i)intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
            val affDelta = Regex("(?i)affection_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val trustDelta = Regex("(?i)trust_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val tensionDelta = Regex("(?i)tension_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val hurtDelta = Regex("(?i)hurt_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val obsessionDelta = Regex("(?i)obsession_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val speechPattern = Regex("(?i)speech_pattern:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()

            val current = EmotionState.fromJson(bot.emotionState)
            val isObsessionAllowed = totalMsgCount >= 150 && current.highAffectionStreak >= 25 && triggerEventsCount >= 3

            val updated = current.applyDeltas(
                newMood = mood,
                newSecondaryMood = secondaryMood,
                newSuppressedEmotion = suppressedEmotion,
                newIntensity = intensity,
                affectionDelta = affDelta,
                trustDelta = trustDelta,
                tensionDelta = tensionDelta,
                hurtDelta = hurtDelta,
                obsessionDelta = obsessionDelta,
                newSpeechPattern = speechPattern,
                isObsessionAllowed = isObsessionAllowed
            )

            val scoreDelta = updated.affection - current.affection
            val tierChanged = updated.getAffectionTierLabel() != current.getAffectionTierLabel()
            if (kotlin.math.abs(scoreDelta) >= 2 || tierChanged) {
                val desc = when {
                    tierChanged && scoreDelta > 0 ->
                        "Aşama Atlandı: ${updated.getAffectionTierLabel()} (+$scoreDelta) — ${updated.mood}"
                    tierChanged && scoreDelta < 0 ->
                        "İlişki Kademesi Düştü: ${updated.getAffectionTierLabel()} ($scoreDelta) — ${updated.mood}"
                    scoreDelta > 0 ->
                        "Belirgin yakınlaşma ve güven artışı (+$scoreDelta) — ${updated.mood}"
                    else ->
                        "Görüş ayrılığı veya mesafe ($scoreDelta) — ${updated.mood}"
                }
                affectionEventDao.insertEvent(
                    AffectionEventEntity(
                        botId = botId,
                        timestamp = System.currentTimeMillis(),
                        scoreDelta = scoreDelta,
                        shortDescription = desc
                    )
                )
            }

            val updatedBot = bot.copy(
                previousEmotionState = bot.emotionState,
                emotionState = updated.toJson(),
                updatedAt = System.currentTimeMillis()
            )
            botDao.insertOrUpdate(updatedBot)
        }

        // 2. Process [CHARACTER_EMOTION: Name]
        val charRegex = Regex("(?is)\\[?CHARACTER[\\\\s_]*EMOTION:\\s*(.*?)\\]?(.*?)(?:\\[/CHARACTER[\\\\s_]*EMOTION\\]|$)")
        charRegex.findAll(rawResponse).forEach { match ->
            val rawCharName = match.groupValues[1].trim()
            val block = match.groupValues[2]
            val charName = normalizeCharacterName(rawCharName, castList)
            if (charName.isNotBlank()) {
                val mood = Regex("(?i)mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
                val secondaryMood = Regex("(?i)secondary_mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
                val suppressedEmotion = Regex("(?i)suppressed_emotion:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
                val intensity = Regex("(?i)intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
                val affDelta = Regex("(?i)affection_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val trustDelta = Regex("(?i)trust_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val tensionDelta = Regex("(?i)tension_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val hurtDelta = Regex("(?i)hurt_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val speechPattern = Regex("(?i)speech_pattern:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()

                val existingEntity = emotionDao.getEmotionForCharacter(botId, charName)
                val current = EmotionState.fromJson(existingEntity?.emotionState)
                val updated = current.applyDeltas(
                    newMood = mood,
                    newSecondaryMood = secondaryMood,
                    newSuppressedEmotion = suppressedEmotion,
                    newIntensity = intensity,
                    affectionDelta = affDelta,
                    trustDelta = trustDelta,
                    tensionDelta = tensionDelta,
                    hurtDelta = hurtDelta,
                    newSpeechPattern = speechPattern
                )

                val entityToSave = CharacterEmotionEntity(
                    id = existingEntity?.id ?: 0,
                    botId = botId,
                    characterName = charName,
                    emotionState = updated.toJson()
                )
                emotionDao.insertOrUpdate(entityToSave)
            }
        }

        // 3. Process [WORLD_ATMOSPHERE]
        val worldRegex = Regex("(?is)\\[?WORLD[\\\\s_]*ATMOSPHERE\\]?(.*?)(?:\\[/WORLD[\\\\s_]*ATMOSPHERE\\]|$)")
        val worldMatch = worldRegex.find(rawResponse)
        if (worldMatch != null) {
            val block = worldMatch.groupValues[1]
            val mood = Regex("(?i)mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val intensity = Regex("(?i)intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
            val currentEvent = Regex("(?i)current_event:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val macroAtmosphere = Regex("(?i)macro_atmosphere:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val microAtmosphere = Regex("(?i)micro_atmosphere:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()

            val currentWorld = WorldAtmosphere.fromJson(bot.worldAtmosphere)
            val updatedWorld = WorldAtmosphere(
                mood = mood ?: currentWorld.mood,
                intensity = intensity ?: currentWorld.intensity,
                currentEvent = currentEvent ?: currentWorld.currentEvent,
                macroAtmosphere = macroAtmosphere ?: currentWorld.macroAtmosphere,
                microAtmosphere = microAtmosphere ?: currentWorld.microAtmosphere
            )
            val currentLatestBot = botDao.getBotById(botId) ?: bot
            botDao.insertOrUpdate(currentLatestBot.copy(worldAtmosphere = updatedWorld.toJson(), updatedAt = System.currentTimeMillis()))
        }

        // Clean character emotion duplicates in database
        try {
            mergeDuplicateCharacterEmotions(botId)
        } catch (_: Exception) {}

        return cleanEmotionTags(rawResponse)
    }

    // --- API Service Execution Engine ---

    private fun sanitizeModelName(model: String): String {
        val clean = model.trim().lowercase()
        return when {
            clean == "gemini-1.5-flash" || clean == "gemini-2.0-flash" -> "gemini-2.5-flash"
            clean == "gemini-1.5-pro" || clean == "gemini-2.0-pro" || clean == "gemini-2.0-flash-thinking" -> "gemini-2.5-pro"
            clean.isEmpty() -> "gemini-2.5-flash"
            else -> model.trim()
        }
    }

    // --- Context Window & Token Management Engine ---

    fun getModelContextLimit(model: String): Int {
        val m = model.trim().lowercase()
        return when {
            m.contains("claude-3-5-sonnet") || m.contains("claude-3-5-haiku") || m.contains("claude-3-opus") -> 200_000
            m.contains("gpt-4o") || m.contains("o1") || m.contains("o3-mini") -> 128_000
            m.contains("gemini") -> 1_000_000
            m.contains("deepseek") -> 64_000
            m.contains("llama-3.3") || m.contains("llama-3.1") -> 128_000
            m.contains("llama") || m.contains("groq") || m.contains("mixtral") -> 32_768
            else -> 32_000
        }
    }

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        return (text.length / 3.5).toInt().coerceAtLeast(1)
    }

    fun estimateTotalTokens(systemPrompt: String, messages: List<MessageEntity>): Int {
        var count = estimateTokenCount(systemPrompt)
        for (msg in messages) {
            count += estimateTokenCount(msg.text) + 4
        }
        return count
    }

    private suspend fun prepareContextAndSummarizeIfNeeded(
        bot: BotEntity,
        messages: List<MessageEntity>,
        modelName: String
    ): Pair<BotEntity, List<MessageEntity>> {
        if (messages.size <= 8) return Pair(bot, messages)

        val modelLimit = getModelContextLimit(modelName)
        val maxBudgetTokens = (modelLimit * 0.75).toInt().coerceAtMost(24_000)

        val userQuery = messages.lastOrNull { it.role == "user" }?.text ?: ""
        val relevantFragments = getRelevantMemoryFragments(bot, userQuery)
        val currentSysPrompt = buildSystemPrompt(bot, getOrCreateSettings(), relevantFragments = relevantFragments)
        val currentTokens = estimateTotalTokens(currentSysPrompt, messages)

        val isCriticallyFull = (currentTokens > maxBudgetTokens * 0.9) || (messages.size > 50)
        val exceedsTriggerThreshold = messages.size > 22

        if (isCriticallyFull) {
            // Emergency synchronous summarization when context is >90% full or message count is very large
            val keepCount = 12
            val olderMessages = messages.dropLast(keepCount)
            val recentMessages = messages.takeLast(keepCount)

            if (olderMessages.isNotEmpty()) {
                val updatedBot = summarizeAndArchiveOlderMessages(bot, olderMessages)
                botDao.setNeedsSummarization(bot.id, false)
                return Pair(updatedBot, recentMessages)
            }
        } else if (exceedsTriggerThreshold) {
            // Mark needsSummarization flag for WorkManager background execution instead of blocking synchronously
            if (!bot.needsSummarization) {
                botDao.setNeedsSummarization(bot.id, true)
            }
        }

        return Pair(bot, messages)
    }

    suspend fun performBackgroundSummarization(botId: String) = withContext(Dispatchers.IO) {
        val bot = botDao.getBotById(botId) ?: return@withContext
        val messages = messageDao.getMessagesForBotList(botId)

        if (messages.size > 14) {
            val keepCount = 12
            val olderMessages = messages.dropLast(keepCount)
            if (olderMessages.isNotEmpty()) {
                summarizeAndArchiveOlderMessages(bot, olderMessages)
            }
        }
        botDao.setNeedsSummarization(botId, false)
    }

    private suspend fun summarizeAndArchiveOlderMessages(
        bot: BotEntity,
        olderMessages: List<MessageEntity>
    ): BotEntity {
        val settings = getOrCreateSettings()
        val selectedModel = sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" })

        val aiName = if (bot.mode == "universe") bot.universeName else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Kullanıcı" }

        val recapText = olderMessages.joinToString("\n") { m ->
            val sender = if (m.role == "user") userLabel else aiName
            "$sender: ${m.text}"
        }

        val prompt = "Aşağıdaki geçmiş sahne mesajlarından önemli gelişmeleri ve olay örgüsünü özetle ve SADECE şu formatta yaz:\n\nDURUM:\n- (yan karakterler, mekanlar, çözülmemiş konular — en fazla 5 madde)\n\nHAFIZA:\n- (duygusal gelişmeler, ilişki değişimleri, verilen sözler — en fazla 5 madde)"

        var updatedBot = bot
        var summarizationSucceeded = false

        // 1. AI Tabanlı Özetleme Denemesi (Aktif Sağlayıcı / Model Üzerinden)
        try {
            val requestMsgs = listOf(MessageEntity(id = "sum_old", botId = bot.id, role = "user", text = "$prompt\n\nGEÇMİŞ SAHNE:\n$recapText", timestamp = 0L))
            val res = executeModelRequest(selectedModel, settings, "Sen yardımcı bir hafıza ve olay özetleyicisin.", requestMsgs, botId = bot.id)
            recordTokenUsage(bot.id, res.second.first, res.second.second)
            val raw = res.first

            if (raw.contains("DURUM:", ignoreCase = true) || raw.contains("HAFIZA:", ignoreCase = true)) {
                val durumMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE))[0]
                    .replace(Regex("DURUM:", RegexOption.IGNORE_CASE), "").trim()

                val hafizaMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE)).getOrNull(1)?.trim() ?: ""

                // Save into memory_fragments table for RAG semantic search
                saveMemoryFragmentsFromSummary(bot.id, durumMatch, hafizaMatch)

                val newStory = listOf(bot.storyNotes, durumMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                val newMemory = listOf(bot.memoryNotes, hafizaMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = newStory,
                    memoryNotes = newMemory,
                    needsSummarization = false,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            summarizationSucceeded = false
        }

        // 2. Yerel Kırpma / Arşivleme Fallback (AI Bağlantısı veya Key Olmadığında)
        if (!summarizationSucceeded) {
            try {
                val localSummaryHeader = "[Önceki Konuşma Arşivi]"
                val sampleOldMsgs = olderMessages.takeLast(8).joinToString("\n") { m ->
                    val sender = if (m.role == "user") userLabel else aiName
                    val snippet = if (m.text.length > 80) m.text.take(80) + "..." else m.text
                    "- $sender: $snippet"
                }
                val fallbackStorySnippet = "$localSummaryHeader\n$sampleOldMsgs"

                saveMemoryFragmentsFromSummary(bot.id, fallbackStorySnippet, "")

                val combinedStory = listOf(bot.storyNotes, fallbackStorySnippet).filter { it.isNotBlank() }
                    .joinToString("\n").lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = combinedStory,
                    needsSummarization = false,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Eski Mesajları Veritabanından Temizleme
        if (summarizationSucceeded) {
            db.withTransaction {
                for (m in olderMessages) {
                    messageDao.deleteMessageById(m.id)
                }
            }
        }

        return updatedBot
    }

    private fun formatMessagesForGemini(messages: List<MessageEntity>): List<GeminiContent> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Sohbeti başlat."))
                )
            )
        }

        val result = mutableListOf<GeminiContent>()

        // Ensure first content turn starts with user
        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        // Merge consecutive same-role messages
        for (m in adjustedMsgs) {
            val currentRole = if (m.role == "user") "user" else "model"
            if (result.isNotEmpty() && result.last().role == currentRole) {
                val lastContent = result.removeAt(result.size - 1)
                val prevText = lastContent.parts.firstOrNull()?.text ?: ""
                val mergedText = "$prevText\n\n${m.text}"
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = mergedText))
                    )
                )
            } else {
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = m.text))
                    )
                )
            }
        }

        return result
    }

    private fun formatMessagesForStandardApi(messages: List<MessageEntity>): List<Pair<String, String>> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(Pair("user", "Sohbeti başlat."))
        }

        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        val result = mutableListOf<Pair<String, String>>()
        for (m in adjustedMsgs) {
            val role = if (m.role == "user") "user" else "assistant"
            if (result.isNotEmpty() && result.last().first == role) {
                val last = result.removeAt(result.size - 1)
                result.add(Pair(role, "${last.second}\n\n${m.text}"))
            } else {
                result.add(Pair(role, m.text))
            }
        }
        return result
    }

    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 2000L,
        factor: Double = 2.0,
        block: suspend (attempt: Int) -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                return block(attempt)
            } catch (e: Exception) {
                lastException = e
                android.util.Log.e("EmochiRepository", "API Istek Deneme $attempt/$maxAttempts Basarisiz: ${e.message}", e)
                if (attempt < maxAttempts) {
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong()
                }
            }
        }
        throw lastException ?: IllegalStateException("İstek $maxAttempts deneme sonrasında yanıt vermedi.")
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>,
        enableNsfw: Boolean = true
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val sanitizedModel = sanitizeModelName(model)
        val geminiContents = formatMessagesForGemini(messages)

        val threshold = if (enableNsfw) "BLOCK_NONE" else "BLOCK_MEDIUM_AND_ABOVE"
        val safetySettings = listOf(
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HARASSMENT", threshold),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HATE_SPEECH", threshold),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", threshold),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", threshold),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_CIVIC_INTEGRITY", threshold)
        )

        val request = GeminiRequest(
            contents = geminiContents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.85f),
            safetySettings = safetySettings
        )

        val modelsToTry = listOf(sanitizedModel, "gemini-2.5-flash", "gemini-3.5-flash").distinct()
        var lastException: Exception? = null

        for (currModel in modelsToTry) {
            try {
                val response = RetrofitClient.service.generateContent(
                    model = currModel,
                    apiKey = apiKey,
                    request = request
                )

                val candidate = response.candidates?.firstOrNull()
                val finishReason = candidate?.finishReason
                val text = candidate?.content?.parts?.firstOrNull()?.text

                if (text.isNullOrBlank()) {
                    val reason = if (!finishReason.isNullOrBlank() && finishReason != "STOP") " (Filtre/Neden: $finishReason)" else ""
                    throw IllegalStateException("Gemini yanıtı içerik/güvenlik filtresine takıldı$reason. Lütfen Ayarlar -> +18 Ayarları kısmından güvenlik seviyelerini kontrol edin.")
                }

                val promptTokens = response.usageMetadata?.promptTokenCount?.toLong() ?: 0L
                val candTokens = response.usageMetadata?.candidatesTokenCount?.toLong() ?: 0L

                return@withContext Pair(text.trim(), Pair(promptTokens, candTokens))
            } catch (e: retrofit2.HttpException) {
                val errorJson = e.response()?.errorBody()?.string()
                val serverMsg = try {
                    JSONObject(errorJson ?: "").optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }

                val rawText = serverMsg ?: e.message() ?: ""
                val isQuota = e.code() == 429 || rawText.contains("quota", ignoreCase = true) || rawText.contains("RESOURCE_EXHAUSTED", ignoreCase = true)

                val userFacingMsg = if (isQuota) {
                    "Gemini API kullanım kotası aşıldı (429 Rate Limit). Lütfen Ayarlar -> AI Model Ayarları menüsünden kendi API anahtarınızı (Gemini, Groq, Claude veya OpenAI) ekleyin."
                } else {
                    serverMsg ?: "Gemini API Hatası [${e.code()}]: ${e.message()}"
                }

                val exc = IllegalStateException(userFacingMsg)
                lastException = exc

                if (e.code() == 404 || isQuota) {
                    continue
                } else {
                    throw exc
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: IllegalStateException("Gemini API çağrısı başarısız oldu. Lütfen Ayarlar'dan API Key'inizi kontrol edin.")
    }

    private suspend fun callOpenAiCompatibleApi(
        endpointUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val standardMsgs = formatMessagesForStandardApi(messages)
        val jsonMessages = JSONArray()
        jsonMessages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        for (m in standardMsgs) {
            jsonMessages.put(JSONObject().apply {
                put("role", m.first)
                put("content", m.second)
            })
        }

        val bodyObj = JSONObject().apply {
            put("model", model)
            put("messages", jsonMessages)
            put("temperature", 0.85)
        }

        val requestBody = bodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val parsedMsg = try {
                    JSONObject(errBody).optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }

                val rawMsg = (parsedMsg ?: errBody).lowercase()
                if (rawMsg.contains("content_filter") || rawMsg.contains("policy") || rawMsg.contains("refusal") || rawMsg.contains("safety") || rawMsg.contains("inappropriate") || rawMsg.contains("harm")) {
                    throw IllegalStateException("Seçili sağlayıcı ($model) içerik kısıtlaması politikası gereği yanıtı reddetti. Lütfen Ayarlar -> AI Model Ayarları menüsünden farklı bir model (ör. Groq/Gemini) seçin.")
                }
                val code = response.code
                if (code == 429 || rawMsg.contains("rate limit") || rawMsg.contains("quota")) {
                    throw IllegalStateException("API kullanım kotası doldu (429 Rate Limit). Lütfen Ayarlar'dan API Key'inizi veya modelinizi değiştirin.")
                }
                throw IllegalStateException("API Hatası [$model] ($code): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val choices = jsonResp.optJSONArray("choices")
            if (choices == null || choices.length() == 0) throw IllegalStateException("Model yanıtı boş döndü.")

            val firstChoice = choices.getJSONObject(0)
            val finishReason = firstChoice.optString("finish_reason", "")
            val messageObj = firstChoice.optJSONObject("message")
            val refusal = messageObj?.optString("refusal", "")

            if (finishReason == "content_filter" || !refusal.isNullOrBlank()) {
                val detail = if (!refusal.isNullOrBlank()) " Detay: $refusal" else ""
                throw IllegalStateException("Seçili model ($model) içerik filtresi politikası gereği bu yanıtı süzdü.$detail Lütfen Ayarlar menüsünden modeli değiştirin veya mesajınızı güncelleyin.")
            }

            val text = messageObj?.optString("content", "") ?: ""
            if (text.isBlank()) throw IllegalStateException("Model yanıtı boş metin döndürdü.")

            val usage = jsonResp.optJSONObject("usage")
            val promptTokens = usage?.optLong("prompt_tokens") ?: 0L
            val candidateTokens = usage?.optLong("completion_tokens") ?: 0L

            Pair(text.trim(), Pair(promptTokens, candidateTokens))
        }
    }

    private suspend fun callClaudeApi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val standardMsgs = formatMessagesForStandardApi(messages)
        val jsonMessages = JSONArray()
        for (m in standardMsgs) {
            jsonMessages.put(JSONObject().apply {
                put("role", m.first)
                put("content", m.second)
            })
        }

        val bodyObj = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("system", systemPrompt)
            put("messages", jsonMessages)
        }

        val requestBody = bodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val parsedMsg = try {
                    JSONObject(errBody).optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }

                val rawMsg = (parsedMsg ?: errBody).lowercase()
                if (rawMsg.contains("policy") || rawMsg.contains("refusal") || rawMsg.contains("safety") || rawMsg.contains("content") || rawMsg.contains("prohibited")) {
                    throw IllegalStateException("Claude API ($model) içerik politikası kısıtlaması nedeniyle yanıt veremedi. Lütfen Ayarlar -> AI Model Ayarları menüsünden başka bir model (ör. Groq veya Gemini) seçin.")
                }
                val code = response.code
                if (code == 429 || rawMsg.contains("rate limit") || rawMsg.contains("quota")) {
                    throw IllegalStateException("Claude API kotası aşıldı (429). Lütfen Ayarlar'dan API Key veya model değiştirin.")
                }
                throw IllegalStateException("Claude API Hatası ($code): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val stopReason = jsonResp.optString("stop_reason", "")
            if (stopReason == "max_tokens_exceeded_or_refusal" || jsonResp.optString("type") == "refusal") {
                throw IllegalStateException("Claude API ($model) içerik politikası gereği bu yanıtı reddetti. Lütfen Ayarlar menüsünden modelinizi değiştirin.")
            }
            val contentArray = jsonResp.optJSONArray("content")
            if (contentArray == null || contentArray.length() == 0) throw IllegalStateException("Claude yanıtı boş döndü.")

            val firstContent = contentArray.getJSONObject(0)
            val text = firstContent.optString("text", "")
            if (text.isBlank()) throw IllegalStateException("Claude yanıtı boş metin döndürdü.")

            val usage = jsonResp.optJSONObject("usage")
            val promptTokens = usage?.optLong("input_tokens") ?: 0L
            val candidateTokens = usage?.optLong("output_tokens") ?: 0L

            Pair(text.trim(), Pair(promptTokens, candidateTokens))
        }
    }

    suspend fun generateAiReply(
        bot: BotEntity,
        messages: List<MessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val isBookMode = bot.mode == "book" ||
                bot.id == "preset_aiden_mcu_cosmic" ||
                bot.aiName.contains("Kitap") ||
                bot.aiName.contains("Cosmic Drift") ||
                bot.aiName.contains("Kozmik Sürükleniş") ||
                bot.aiPersonality.contains("KİTAP MODU") ||
                bot.scenario.contains("KİTAP MODU")

        if (isBookMode) {
            return@withContext generateDeterministicBookReply(bot, messages)
        }

        val settings = getOrCreateSettings()
        val selectedModel = sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" })

        // Context Window & Token Budget Management
        val (effectiveBot, effectiveMessages) = prepareContextAndSummarizeIfNeeded(bot, messages, selectedModel)

        // RAG: Retrieve relevant memory fragments based on latest user input
        val userQuery = effectiveMessages.lastOrNull { it.role == "user" }?.text ?: ""
        val relevantFragments = getRelevantMemoryFragments(effectiveBot, userQuery)
        val prevTimestamp = if (effectiveMessages.size >= 2) effectiveMessages[effectiveMessages.size - 2].timestamp else effectiveBot.updatedAt
        val totalCount = messageDao.getMessageCountForBot(effectiveBot.id)
        val systemPrompt = buildSystemPrompt(
            effectiveBot,
            settings,
            relevantFragments = relevantFragments,
            lastMessageTimestamp = prevTimestamp,
            totalMessageCount = totalCount
        )

        var primaryException: Exception? = null
        // Primary Execution (3 retries with exponential backoff: 2s, 4s, 8s)
        try {
            val result = retryWithBackoff(maxAttempts = 3, initialDelayMs = 2000L) {
                executeModelRequest(selectedModel, settings, systemPrompt, effectiveMessages, botId = effectiveBot.id)
            }
            recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
            return@withContext parseAndApplyEmotionUpdates(effectiveBot.id, result.first)
        } catch (e: Exception) {
            if (!settings.enableAutoFallback) {
                throw e
            }
            primaryException = e
        }

        // Auto Fallback to Gemini Secondary Model (3 retries with exponential backoff)
        val fallbackModel = sanitizeModelName(settings.fallbackModel.ifBlank { "gemini-2.5-flash" })
        val customKey = settings.customApiKey.trim()
        val buildConfigKey = getBuildConfigKey()
        val backupKey = settings.backupApiKey.trim()

        val primaryKey = if (customKey.isNotBlank()) customKey else buildConfigKey
        val candidateKeys = listOf(primaryKey, backupKey)
            .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            .distinct()

        if (candidateKeys.isEmpty()) {
            throw primaryException ?: IllegalStateException("API Key bulunamadı.")
        }

        var fallbackErr: Exception? = null
        for (k in candidateKeys) {
            try {
                val result = retryWithBackoff(maxAttempts = 3, initialDelayMs = 2000L) {
                    callGeminiApi(k, fallbackModel, systemPrompt, effectiveMessages, enableNsfw = settings.enableNsfw)
                }
                recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
                return@withContext parseAndApplyEmotionUpdates(effectiveBot.id, result.first)
            } catch (e: Exception) {
                fallbackErr = e
            }
        }
        throw fallbackErr ?: primaryException ?: IllegalStateException("Yanıtlama başarısız oldu.")
    }

    private suspend fun generateDeterministicBookReply(
        bot: BotEntity,
        messages: List<MessageEntity>
    ): String {
        val userMsgs = messages.filter { it.role == "user" }
        val userStep = userMsgs.size
        val lastUserMsg = userMsgs.lastOrNull()?.text ?: ""

        var progress = storyProgressDao.getProgressOnce(bot.id) ?: StoryProgressEntity(botId = bot.id)

        val b1Text = loadAssetText("Bolum1_KozmikSurukleniş.md")
        val b2Text = loadAssetText("Bolum2_Sartlar.md")
        val b3Text = loadAssetText("Bolum3_DenemeSuresi_v2.md")
        val b3KisaKacis = loadAssetText("Bolum3_AlternatifDal_KisaKacis.md")
        val b3Kacis = loadAssetText("Bolum3_AlternatifDal_Kacis.md")
        val b3Thor = loadAssetText("Bolum3_AlternatifDal_ThorBulma.md")

        return when (userStep) {
            1 -> {
                // Choice 1 Response
                val isDeep = lastUserMsg.contains("2") || lastUserMsg.contains("odaklan") || lastUserMsg.contains("derinden")
                progress = if (isDeep) {
                    progress.copy(fatigue = progress.fatigue + 1, chapterNumber = 1)
                } else {
                    progress.copy(chapterNumber = 1)
                }
                storyProgressDao.insertOrUpdate(progress)

                val intro = if (isDeep) {
                    """Zihnini görünün derinliklerine zorladın. Mavi hologramın detaylarını sökmeye çalıştın: masadaki dosyaların üzerindeki isimleri, Tony Stark'ın endişeli kaş çatışını, Wanda'nın pencerelerden dışarı bakışını, Steve Rogers'ın masaya koyduğu ellerini. Ekstra detaylar zihnine bir sel gibi aktı ama bedeli ağır oldu: gözlerinin arkasında keskin bir sancı saplandı, burnundan ince bir kan sızdı.

Görü dağıldığında şakakların zonkluyordu ama koordinatlar ve odadaki herkesin pozisyonu zihnine kazınmıştı."""
                } else {
                    """Zihnini geri çektin. Görü sahneleri önünden soğuk bir film şeridi gibi aktı: Stark Tower'ın en üst katındaki cam salon, masanın etrafında toplanmış gölgeler, masanın ortasında dönen mavi hologram. Mesafeli kaldın; gücünü zorlamadın, zihnini yıpratmadın. Sadece ne görmen gerekiyorsa onu gördün.

Görü eridiğinde burnun kanamıyordu, başın dönmüyordu. Fiziksel olarak tam gücündeydin."""
                }

                val mainPart = b1Text ?: """Toplantı odası etrafında maviye çalan bir statik içinde şekillendi, sessiz ve ödünç alınmış bir an — gerçekten orada değildin..."""

                intro + "\n\n---\n\n" + mainPart + """

---

🔀 Tower'a geçişi nasıl gerçekleştireceksin?

1️⃣ Direkt ışınlan, hiç düşünmeden.
2️⃣ Önce dışarıdan gözlemle (birkaç dakika Tower'ı uzaktan izle), sonra ışınlan.
3️⃣ Geri çekil, gitme — sadece izlemeye devam et."""
            }
            2 -> {
                // Choice 2 Response
                val isAvoid = lastUserMsg.contains("3") || lastUserMsg.contains("Geri çekil") || lastUserMsg.contains("gitme")
                val isObserved = lastUserMsg.contains("2") || lastUserMsg.contains("gözlemle") || lastUserMsg.contains("uzaktan")

                progress = if (isAvoid) {
                    progress.copy(firstImpressionAvengers = "avoided")
                } else if (isObserved) {
                    progress.copy(firstImpressionAvengers = "calculated")
                } else {
                    progress.copy(firstImpressionAvengers = "impulsive")
                }
                storyProgressDao.insertOrUpdate(progress)

                val intro = if (isAvoid) {
                    """İçindeki uyarı çanları galip geldi ve çatının kenarından bir adım geri çekildin. Çatının gölgelerine saklanarak Kule'den uzaklaşmayı seçtin.

Ancak tam o anda zihnindeki mavi hologram çılgınca parıldadı. S.H.I.E.L.D. uyduları uzam sapmanı önceden tespit etmiş ve koordinatlarını kilitlemişti! Ani bir kuantum çökmesiyle etrafındaki gerçeklik yırtıldı ve bir enerji dalgası seni doğrudan Stark Tower'ın yüksek tavanlı toplantı salonunun ortasına savurdu. Kule'ye kendi isteğinle girmemiştin ama kaçınma çaban bile seni kaderinden uzaklaştıramadı."""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("gözlemle") || lastUserMsg.contains("uzaktan")) {
                    """Saniyelerce Tower'ın dış camlarındaki yansımaları izledin. Güvenlik sistemlerinin tarama frekanslarını, korumaların turlarını ve penceredeki gölgelerin hareketini hesapladın. Temkinli ve kontrolcü bir zamanlamayla uzamı büküp içeri ışınlandın.

Natasha Romanoff bu sessiz ve hesaplı girişini fark ettiğinde gözlerinde hafif bir takdir parıltısı belirdi."""
                } else {
                    """Hiç duraksamadın. Bir nefes aldın ve uzam büküldü. Odadaki havanın basıncı bir anda değişti, zifiri karanlık yerini Avengers Tower'ın yüksek tavanlı, cam duvarlı toplantı salonuna bıraktı. İçgüdüsel ve ani gelişin, odadakilerin reflekslerini anında tetikledi. Steve Rogers elini kalkanına uzattı."""
                }

                intro + """

Oda içindeki hava sen görünmeden önce değişti — bir basınç düşüşü, hafif bir ozon kokusu, bir kalp atımlık mavi ışık kendini bir şekle, sonra da masanın başında hep oradaymış gibi duran bir adama dönüştürdü.

Bir saniye kimse kıpırdamadı.

Onlara nasıl göründüğünün farkındaydın: uzun boylu, acele etmeyen, hiçbir yerde kalmayı planlamayan birinin giydiği hafif kıyafetlerle. Gözlerin herkesin fark ettiği ilk şeydi — sonra ikinci şey, sonra üçüncü şey, çünkü çoğu insanın senin bakışını gerçekten tutabilmesi üç deneme alıyordu. Bu bir tehdit değildi. Sana bunu söyleyecek kadar yaklaşan o bir avuç insan, bunun daha çok göze bakmak için fazla parlak bir maviye bakmaya benzediğini söylemişti. Bunun için özür dilemeyi çoktan bırakmıştın.

Stark ilk toparlanan oldu, tabii ki o oldu. *"Peki,"* dedi, tabletini teatral bir sakinlikle masaya bırakarak. *"Davete böyle cevap vermek de bir yöntem."*

Sarışın askerin eli, içgüdüsel olarak, sandalyesine dayalı kalkana doğru kaymıştı. Sen bunu tepki vermeden fark ettin. Her şeyi tepki vermeden fark ediyordun; hâlâ hayatta olmanın tek sebebi buydu.

Kızıl saçlı hiç kıpırdamamıştı. Sana, senin bir odayı incelediğin gibi bakıyordu — kataloglayarak, bakakalmadan. Uzun zamandır ilk kez birinin sana baktığında hemen odadan çıkmak istemedin.

Pencerenin yanında, tartışmadan yarı dönmüş halde, görünün sana hiç göstermediği biri duruyordu — koyu saçlı, parmaklarının etrafında kızıla çalan bir enerji tembel tembel kıvrılıyordu, sen gelmeden önce yarım kalmış bir düşüncenin ortasındaymış gibi. Sana, havanın döndüğünü izleyen birinin bakışıyla bakıyordu: tam olarak korkmuş değil. Yeniden hesaplıyordu.

Sessizliği kıran Thor oldu, ve bunu geniş, memnun bir gülümsemeyle yaptı, sanki Noel erken gelmiş gibi. *"İşte burada. Gezgin."*

*"Kozmik Sürükleniş,"* dedi Stark, hands'ini açarak. *"Ya da — pardon, bu bir sahne adı mı, yoksa gerçekten böyle mi cevap veriyorsun?"*

---

🔀 Stark'ın "Sahne adı mı gerçek adın mı?" sorusuna nasıl cevap vereceksin?

1️⃣ Soğuk ve mesafeli cevap ver: "Aiden... Sorun genelde bu oluyor."
2️⃣ Alaycı/esprili cevap ver: "Giriş kartı masraflı geldi Stark. Aiden diyabilirsin."
3️⃣ Hiç isim verme, sadece sessizlik ve keskin bir bakışla cevap ver."""
            }
            3 -> {
                // Choice 3 Response
                val isWitty = lastUserMsg.contains("2") || lastUserMsg.contains("Alaycı") || lastUserMsg.contains("esprili") || lastUserMsg.contains("masraflı")
                val isSilent = lastUserMsg.contains("3") || lastUserMsg.contains("Hiç isim") || lastUserMsg.contains("sessizlik")

                progress = if (isWitty) {
                    progress.copy(tonyAffinity = progress.tonyAffinity + 1)
                } else if (isSilent) {
                    progress.copy(mysteryFactor = progress.mysteryFactor + 1, steveTrust = progress.steveTrust - 1)
                } else {
                    progress
                }
                storyProgressDao.insertOrUpdate(progress)

                val responseText = if (isWitty) {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

*"Giriş kartı masraflı geldi Stark,"* dedin hafif bir tebessümle. *"Aiden diyabilirsin."*

Tony Stark hafifçe gülümsedi. *"En azından mizah duygusu olan birisi,"* diye mırıldandı. Steve Rogers ise bu rahat tavrından pek hoşnut görünmedi."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Hiç isim") || lastUserMsg.contains("sessizlik")) {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

Tek bir kelime bile etmedin. Sadece sessiz kaldın ve gözlerini odadakilerin üzerinde gezdirdin. Gizemli ve tekinsiz sessizliğin Steve Rogers'ın şüphesini daha da artırdı.

Stark elini çenesine götürdü. *"Konuşkan biri değil demek ki,"* dedi."""
                } else {
                    """Sessizliği, kimseye hız borçlu olmadığını netleştirecek kadar uzun bıraktın.

*"Aiden,"* dedin sonunda. Sesin alçak, düzdü, yarı aralık bırakılmış kapalı bir kapının sözlü karşılığı gibi. *"Aiden Blackwood. Ve hayır — pek bir şeye cevap vermem. Sorun genelde bu oluyor."*

Steve'in kolları çapraz kalmaya devam etti, ama ifadesinde bir şey biraz gevşedi — güven değil, henüz değil, ama sana güvenip güvenemeyeceğine karar veren bir adamın dikkati. *"Bir yıla yakındır elimizdeki her radardan uzaktasın,"* dedi. *"Şimdi öylece içeri giriyorsun."*

*"Benim hakkımda konuşuyordunuz,"* dedin, tonun bir omuz silkmeye eşdeğer kadar kuru. *"Görüş bildirmemek kabalık olurdu."*

Bu, kızıl saçlıdan bir şeyi çekip çıkardı — tam bir gülümseme değil, ama farkında olmadan not aldığın kadar yakın bir şey. *"Dinliyormuş,"* dedi, sana değil de daha çok odaya, bir teoriyi doğruluyormuş gibi. *"Bu hiç de az bir şey değil."*

Stark kararlı bir şekilde ellerini çırptı. *"Tamam. Yeni plan. Herkes otursun, kimse panik yapmasın, ve biri bu adama bir kahve getirsin, ışınlanıp gitmenin bizimle konuşmaktan daha eğlenceli olduğuna karar vermeden önce."*

Sen oturmadın. Henüz değil. Masanın başında ayakta kaldın, kolların yanlarında gevşek, alışkanlıkla çıkışları kataloglarken, içindeki küçük ve hain bir parça, yirmi yıl içinde ilk kez, yabancılarla dolu bir odaya girip hemen ne kadar hızlı çıkabileceğini hesaplamadığını fark ediyordu.

Bu yeniydi. Bunu sevip sevmediğinden henüz emin değildin."""
                }

                responseText + """

---

Odanın havası ağırlaşmıştı. Masanın çevresindeki kişiler seni tartıyordu.

Sağ tarafta duran kızıl saçlı kadın (Natasha) ellerini göğsünde kavuşturmuş, seni profesyonel bir ajanın soğukkanlılığıyla inceliyordu.

Pencere kenarında duran siyah ceketli kadın (Wanda) ise sana, havanın döndüğünü izleyen birinin bakışıyla bakıyordu: tam olarak korkmuş değil. Yeniden hesaplıyordu.

---

🔀 Bakışların odada kimin üzerinde kalacak?

1️⃣ Gözlerin kızıl saçlıda (Natasha) gereğinden bir saniye fazla kalsın.
2️⃣ Gözlerin pencere kenarındaki kadında (Wanda) gereğinden bir saniye fazla kalsın.
3️⃣ Kimseye özel bir bakış atma, odayı genel olarak tara."""
            }
            4 -> {
                // Choice 4 Response -> Chapter 1 Ending
                val choice4Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Wanda") || lastUserMsg.contains("pencere")) {
                    """Gözlerin pencere kenarında duran Wanda Maximoff'a kaydı ve orada bir an çakılı kaldı. Wanda'nın gözlerinde hafif bir kırmızılık parlayıp söndü; zihnindeki dalgalanmayı hissetmiş gibiydi.

Sana bakarken başını hafifçe yana eğdi."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Kimseye özel") || lastUserMsg.contains("tara")) {
                    """Kimseye özel bir bakış atmadın. Gözlerin odadaki tüm kaçış noktalarını, havalandırma ızgaralarını ve stratejik açıları tarayarak nötr kaldı.

Tamamen profesyonel ve mesafeliydin."""
                } else {
                    """Gözlerin Natasha Romanoff'un üzerinde gereğinden bir saniye daha uzun kaldı. Natasha hafifçe tek kaşını kaldırdı, bakışlarını kaçırmadı. Aranızdaki bu sessiz temas aranızda görünmez bir hat çekti.

Odadaki kimse bu anlık bakışmayı fark etmedi ama ikiniz de farkındaydınız."""
                }

                choice4Text + """

---

Konuşma devam etti — sorular, yarı cevaplar, senin verdiğin her bilgiye karşılık geri aldığın bir bilgi, eski bir alışkanlık, bir görüşme değil bir pazarlık gibi yürüttüğün bir sohbet. Stark, tekliflerini bir iş anlaşması gibi sunuyordu: kaynaklar, koruma, bir çatı altında bir yer. Steve, daha temkinli soruyordu, güven kelimesini hiç kullanmadan güvenden bahsediyordu. Thor, senin her cevabında bir şeyi doğrular gibi başını sallıyordu, sanki uzun zamandır beklediği bir hikâyenin son parçasını duyuyormuş gibi.

Kızıl saçlı — adını henüz söylememişti kimse ama içten içe zaten biliyordun, Romanoff — pek konuşmadı. Ama her sessizliğinde bir şey vardı, gözlemleyen, sabırlı, senin gibi biri için nadir bulunan bir tür sabır.

Pencerenin yanındaki kadın da sessizdi, ama onun sessizliği farklıydı — daha az hesaplı, daha çok bekleyen. Parmaklarının etrafındaki kızıl enerji yavaşça sönüp gitti, konuşma uzadıkça, sanki seni değerlendirmeyi bırakıp sadece dinlemeye karar vermiş gibi.

Sen hiçbirine güvenmedin. On beş yıllık alışkanlık bir gecede kırılmıyor, bir odaya girmen ve iki cümle kurman yetmiyor buna. Ama —ve bunu kendine itiraf etmek zorunda kaldın, istemesen de— odadan hâlâ çıkmamıştın. Ve bu, senin standartlarına göre, neredeyse bir mucizeydi.

---

📖 BÖLÜM 1 SONU — KOZMİK SÜRÜKLENİŞ

Aiden Blackwood Avengers Tower'a ilk adımını attı ve kararını verdi.
Odadan çıkmadı. Kaderinin bir sonraki halkası burada dövülecek.

Tebrikler! Bölüm 1'i başarıyla tamamladın."""
            }
            5 -> {
                // Chapter 2 Start -> Up to Chapter 2 Choice 1
                progress = progress.copy(chapterNumber = 2)
                storyProgressDao.insertOrUpdate(progress)

                val b2Full = b2Text ?: """# 📖 BÖLÜM 2: ŞARTLAR"""
                b2Full + """

---

🔀 Stark'ın "bize bir şey göster" talebine nasıl karşılık vereceksin?

1️⃣ İstenen minimum gösterimi yap. (Küçük mavi ışık küresi oluştur ve temel sınırlarını açıkla)
2️⃣ Reddet, hiçbir şey gösterme — sadece sözlerine güvenmelerini söyle.
3️⃣ İstenenden fazlasını göster, formların varlığından biraz daha açık bahset."""
            }
            6 -> {
                // Response to Chapter 2 Choice 1 -> Up to Chapter 2 Choice 2
                val c1Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("fazlasını") || lastUserMsg.contains("Cömert")) {
                    """Sağ elinizi masanın üzerine doğru uzattınız ve parmaklarınızı açtınız. Sadece küçük bir küre değil, masanın tam ortasında gerçekliğin dokusunu büken mavi ve kırmızı kuantum çizgileriyle bezeli parıltılı bir kozmik küre oluşturdunuz. Odadaki havanın basıncı düştü, bardaklardaki su titredi ve köşedeki dijital ekranlar siyan dalgalarla dalgalandı.

*"Beyaz Göz zihinsel dalgaları ve hafızayı büker,"* dedin gözlerinde hafif renk değişimleriyle. *"Kırmızı Göz ise moleküler seviyede kinetik yıkım yaratır. Gücüm katmanlı — şu an gördüğünüz Drift formu sadece yüzey. Kırmızı Göz'ümle bina seviyesinde yıkım yapabilir, kozmik enerjiyi emip dönüştürebilirim. Tavanım düşündüğünüzden çok daha yüksek."*

Stark'ın gözleri parladı, tabletindeki okumaları izlerken heyecanını saklayamadı. *"İşte bu! Devasa bir kuantum çıktısı. Sayılar harika!"*

Natasha ise gözlerini senden ayırmadan, sesindeki o ince analiz tonuyla sordu: *"Bu kadar açık davranman güven jesti mi Blackwood, yoksa sınır çizen bir gözdağı mı?"*"""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("Reddet") || lastUserMsg.contains("gösterme")) {
                    """Ellerinizi masada kavuşturdunuz ve geriye yaslandınız. Gözleriniz Stark'ın sabırsız bakışlarıyla buluştu.

*"Hayır,"* dedin düz, soğuk ve sarsılmaz bir tonla. *"Şov yapmıyorum. Gücüm bir sirk gösterisi değil. Sözlerime güvenmiyorsanız, hiçbir mavi ışık ya da gösteri fikrinizi değiştirmeyecektir. Bir tehdit olmadığımı bilmek istiyorsanız, burada sessizce masanızda oturuyor olmam en büyük kanıtınızdır."*

Stark hoşnutsuzca kaşlarını çattı, ellerini göğsünde kavuşturdu. *"Demek gizemli kalmayı seçiyorsun. Mühendisler gizemden hoşlanmaz Blackwood. Sayı ve kanıt isteriz."*

Steve araya girerek başını ağır ağır salladı. *"Dürüstlük anlaşılabilir bir şey. Ancak güven inşa etmek istiyorsak şeffaflık iki taraflı olmalı. Sana hemen güvenmemizi bekleyemezsin ama senin de bize bir adım atman gerekir."*

Thor ise hafifçe bıyık altından güldü. *"Gücünü saklamasını iyi biliyor. Bir savaşçının silahını kınında tutması zayıflık değil, tedbirdir."*"""
                } else {
                    """Bir an düşündün. Sonra, hiç kalkmadan, sağ elini masanın üstünde açtın ve avucunun ortasında küçük, sabit bir mavi ışık küresi oluşturdun — büyük değil, zararsız, ama gerçek, havanın onun etrafında hafifçe büküldüğünü görebilecekleri kadar gerçek. Oda ısısı bir derece düştü, sadece bir anlığına, sanki küçük bir yıldız oraya taşınmış gibi.

*"Bu, Beyaz Göz'ün alt seviyesi,"* dedin, ışığı izleyerek, onlara değil. *"Zihin üzerinde çalışıyor. Zayıf ve orta seviye zihinleri etkileyebilirim — hayaller, hafıza, kısa süreli felç. Daha güçlü birini kısa süreliğine sersemletebilirim ama tam kontrol edemem, en azından şu anki formumda."*

*"Şu anki formunda?"* diye tekrarladı Natasha, hemen o detayı yakalayarak.

Işığı avucunda kapattın, kaybolmasına izin vererek. *"Sabit değilim,"* dedin. *"Gücüm katmanlı. Şu an gördüğünüz — buna kendi kendime 'Drift' diyorum, çünkü isim koymazsan bir şey gerçek gelmiyor bazen — en zayıf formum değil, ama en güçlüsünden de çok uzak. Sıradan bir süper askerden daha güçlüyüm, hızlı iyileşiyorum, ışınlanabiliyorum, uzun mesafeleri sık sık kullanabiliyorum. Kırmızı Göz'le bina seviyesinde yıkım yapabilirim, sınırlı miktarda enerji emebilirim."*

*"Ve daha güçlü formların da var,"* dedi Steve, sesi hem meraklı hem de temkinli.

*"Var,"* dedin, kısaca. Bunun ne kadarını açıklayacağına karar vermek istercesine bir an durdun. *"Ama onlardan bahsetmeyeceğim şu an. Bir yabancı grubuna, ilk tanıştığım gün, tam güç tavanımı anlatmam gerekmiyor. Bu kadarı bile fazla cömertlik sayılır."*

Thor gürledi, memnun bir kahkaha gibi. *"Bilge bir gezgin. Gücünü göstermek kadar, göstermemeyi bilmek de bir güçtür."*

Stark'ın gözleri hâlâ küçük ışık küresinin kaybolduğu yerdeydi, düşünceli. *"Bina seviyesi yıkım,"* dedi, sanki kendi kendine sayıları hesaplıyormuş gibi. *"Ve bu senin *zayıf* formun."*

*"Nispeten zayıf,"* diye düzelttin. *"Kelimeler önemli."*"""
                }

                c1Text + """

---

Konuşma saatlerce sürdü, ya da öyle hissettirdi — şartlar, sınırlar, kimin neyi bilmesi gerektiği, senin neyi paylaşmayacağın. Bir noktada Steve, Fury'nin adını andı, S.H.I.E.L.D.'in seni "risk sınıflandırması dışı" olarak işaretlemiş olmasının aslında bir tehdit değil bir çaresizlik itirafı olduğunu söyledi — onları anlayamadıkları için korktuklarını. Bu, içinde beklemediğin bir şeyi kıpırdattı; anlaşılmak, sana onlarca yıldır teklif edilmemiş bir şeydi.

Steve sana bakarak son sözünü söyledi: *"Karşılığında kurallara uyarsın. Sivillere zarar yok. Emir zinciri var, tam bir ordu değil ama tam bir anarşi de değil. Ve şeffaflık — gücünün ne olduğunu, sınırlarının ne olduğunu bilmemiz gerekiyor. Güven iki taraflı işler."*

---

🔀 Steve'in "Güven iki taraflı işler" sözüne nasıl karşılık vereceksin?

1️⃣ Alaycı bir karşılık ver ("Güven" kelimesini sorgulayan yanıt).
2️⃣ Ciddi bir şekilde karşılık ver — güvenin neden bu kadar zor olduğunu kısaca açıkla.
3️⃣ Hiç cevap verme, konuyu değiştir."""
            }
            7 -> {
                // Response to Chapter 2 Choice 2 -> Up to Chapter 2 Choice 3
                val c2Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Ciddi") || lastUserMsg.contains("zor olduğunu")) {
                    """Bakışlarını Steve Rogers'a diktin, sesindeki soğuk zırhı bir anlığına indirerek dürüstçe konuştun.

*"Güven benim için bir lüks değil, ölümcül bir hata oldu hep,"* dedin alçak ama kararlı bir sesle. *"Beş yaşından beri kaçıyorum. Güvendiğim her yer yıkıldı, arkamı döndüğüm herkes ya korktu ya da kelepçe getirdi. İki taraflı güven güzel bir masal Steve, ama ben masallara inanmayı çok önce bıraktım."*

Steve'in çenesi gevşedi, gözlerinde derin bir anlayış ve saygı parıltısı belirdi. *"Anlıyorum. O zaman sana söz kelimelerle değil, eylemlerle kanıtlanacak."*"""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Cevap verme") || lastUserMsg.contains("değiştir")) {
                    """Steve'in kelimeleri havada asılı kaldı. Hiç cevap vermedin. Bakışlarını onun gözlerinden çekip masadaki kahve fincanına çevirdin, konuyu tamamen yanıtsız bırakarak sessizliğe gömüldün.

Steve hafifçe iç çekti. Güvensizliğin duvarlarını zorlamayacağını gösteren bir tavırla geri çekildi. Ortamdaki sessizlik tekinsiz bir derinlik kazandı."""
                } else {
                    """Güven kelimesi göğsünde tanıdık bir şekilde sıkıştı — hafif, otomatik bir savunma refleksi, yıllar içinde o kadar derine işlemiş ki artık düşünmeden tepki veriyordu.

*"Güven,"* dedin, kelimeyi neredeyse tadına bakar gibi tekrarlayarak. *"İlginç kelime, hiç tanımadığınız ve sekiz aydır peşinde olduğunuz birine söylemek için."*

Steve gözlerini kısmadan sana baktı, ama duruşunu bozmadı. *"Belki de alışık olmadığın içindir Blackwood."*"""
                }

                c2Text + """

---

Gece ilerledikçe, oda yavaş yavaş boşaldı. Thor, bir kutlama vaadiyle (senin hiç kabul etmediğin bir vaatle) ayrıldı. Steve, "düşünmen için zaman" diyerek çekildi, seni bir karara zorlamadan — bu, dosyalarda okuduğun adamla örtüşen bir şeydi, ve bunu takdir ettin, istemeden de olsa.

Stark en son ayrılanlardan biriydi, kapıda durup arkasını döndü. *"Bak,"* dedi, sesindeki her zamanki tiyatral ton bir anlığına düşerek. *"Bilmiyorum kaç kere reddedildin, kaç kere yalan söylendi sana. Ama bu bina, sandığından daha fazla insanı kurtardı. Sen de kurtarılmaya değersin, Blackwood. Düşün bunu."*

Bunu söylemesini beklemiyordun. Cevap vermedin, ama kelimeleri bir yere koydun — atmadın, ama hemen de kabul etmedin.

Oda tamamen boşaldığında, sadece Natasha kaldı, masanın kenarına yaslanmış, seni izliyordu.

*"Herkes gitti de sen gitmedin,"* dedin, ayakta kalkıp fincanı bırakarak.

*"Herkes ikna etmeye çalıştı,"* dedi, omuz silkerek. *"Ben ikna etmiyorum."*

*"Peki ne yapıyorsun?"*

*"Bekliyorum,"* dedi, basitçe. *"Sen adım atana kadar. Ya da atmayana kadar. İkisi de benim için sorun değil."*

Bunun ne kadar tuhaf bir rahatlama olduğunu tarif edemezdin. On beş yıldır herkes ya senden korktu ya da seni bir şeye ikna etmeye çalıştı — bir silah, bir dosya, bir çözülmesi gereken problem olarak. Bu kadın, ilk kez, sana sadece *zaman* teklif ediyordu, karşılığında hiçbir şey istemeden.

*"Neden umursuyorsun?"* diye sordun, sesindeki merakı saklamaya çalışmadan.

*"Umursamıyorum,"* dedi, ama sesinde bir yalan yoktu, sadece dürüst bir düzeltme. *"Henüz. Ama gölgelerin içinde büyümüş biri, başka bir gölgeyi tanır. Sen benim tanıdığım bir dille konuşuyorsun, Blackwood. İstesen de istemesen de."*

---

🔀 Natasha'nın bu sözlerine nasıl karşılık vereceksin?

1️⃣ Sessiz kal, sadece bakışların bir saniye fazladan onda kalsın.
2️⃣ Sözlü olarak karşılık ver — onun da bir şey sakladığını ima et.
3️⃣ Mesafe koy, konuşmayı sonlandır, odadan çık."""
            }
            8 -> {
                // Response to Chapter 2 Choice 3 -> Up to Chapter 2 Choice 4
                val c3Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Sözlü") || lastUserMsg.contains("sakladığını")) {
                    """*"Gölgeler güzel kılıflardır Romanoff,"* dedin kısık bir sesle, gözlerinin içine bakarak. *"Ama ikimiz de biliyoruz ki gölgede durmak insanı temiz yapmaz. Sen geçmişinin izlerini ne kadar iyi saklarsan sakla, aynı dili konuştuğumuz doğru."*

Natasha'nın dudaklarında ince, neredeyse fark edilmez bir tebessüm belirdi. *"En azından yalan söylemiyorsun Blackwood. Bu Kule'de nadir bulunan bir özelliktir."*"""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Mesafe") || lastUserMsg.contains("çık")) {
                    """*"Farklı diller konuşuyoruz Romanoff,"* dedin mesafeli ve soğuk bir ifadeyle. Fincanı masaya bıraktın ve ona daha fazla bakmadan arkana dönüp odadan çıktın.

Natasha arkandan bakarken engel olmaya çalışmadı, gölgelerin arasındaki mesafeyi korumana saygı duydu."""
                } else {
                    """Bunun üzerine bir şey söylemedin. Ama bakışların bir saniye fazla onda kaldı, ve o da bunu fark etti — ama üstüne gitmedi, sadece kaydetti, tıpkı senin her şeyi kaydettiğin gibi.

Natasha hafifçe başını salladı ve sessizce kapıya doğru yürüdü."""
                }

                c3Text + """

---

Wanda'yla karşılaşman ise çok daha kazara oldu.

Tower'ın ortak katındaki mutfağa, herkes gittikten sonra, sırf başka bir şey yapacak bir şey bulmak için indin — belki de sadece odandan çıkmak için bir bahane arıyordun, kapalı dört duvar sana her zaman biraz fazla küçük geliyordu. O da oradaydı, tezgahın kenarında, elinde bir kupa çay, parmaklarının ucunda hâlâ o hafif kızıl ışıltı, sanki hiç tam olarak sönmüyormuş gibi.

Seni görünce irkilmedi. Bu, seni biraz şaşırttı — çoğu insan seni görünce en azından bir kez göz kırpıştırırdı, gözlerinin ağırlığından.

*"Uyuyamıyor musun?"* diye sordu, sesi yumuşak, aksanı hafif.

*"Alışkanlık değil,"* dedin, doğruyu söyleyerek. *"Uyumak, güvende hissetmeyi gerektirir. Ben pek güvende hissetmem."*

*"Burada bile mi?"* Sorusu meraklıydı, yargılamıyordu.

*"Henüz karar vermedim,"* dedin.

Başını hafifçe eğdi, sanki seni bir kitap gibi okuyormuş gibi — ama tuhaf bir şekilde, bunun rahatsız edici olmadığını fark ettin. *"Beni tanımıyorsun,"* dedi, *"ama tahmin edeyim — kayıp hissediyorsun. Buraya ait olmak seni korkutuyor, çünkü ait olduğun her şey daha önce elinden alındı."*

---

🔀 Wanda'nın bu derin içgörüsüne nasıl tepki vereceksin?

1️⃣ Şüpheci tepki ver — zihin okuma suçlaması ("Zihnimi mi okuyorsun?").
2️⃣ Kabul et — evet, kayıp hissediyorum, açıkça söyle.
3️⃣ Konuyu kapat, sert bir şekilde geri çekil."""
            }
            9 -> {
                // Response to Chapter 2 Choice 4 -> Up to Chapter 2 Choice 5
                val c4Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Kabul") || lastUserMsg.contains("açıkça")) {
                    """Sessizlik mutfağı kapladı. Gözlerini tezgahtaki çay kupasına indirdin ve derin bir nefes aldın.

*"Haklısın,"* dedin ilk kez bu kadar açık konuşarak. *"Ait olduğum her şey, tutunduğum her ev ellerimin arasından kayıp gitti. Kaybolmak kolay Wanda, asıl zor olan bir yere ait olmayı yeniden öğrenmek."*

Wanda'nın bakışlarında yumuşak bir sıcaklık belirdi, elindeki kupayı sıkarken başıyla onayladı. *"Biliyorum Aiden. O korkuyu ben de yaşadım. Yalnız değilsin."*"""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Sert") || lastUserMsg.contains("kapat")) {
                    """*"Aklımı okumaya çalışma,"* dedin sert ve soğuk bir tonla. Tek adımla geriye çekildin. *"Benim hakkımda hiçbir şey bilmiyorsun ve bildiğini sanma."*

Wanda öfkelenmedi, sadece gözlerindeki o hüzünlü derinlikle sana baktı. *"Özür dilerim. Sadece hissettiklerimi söyledim."*"""
                } else {
                    """Bunun ne kadar isabetli olduğu seni bir an durdurdu. *"Zihnimi mi okuyorsun?"* diye sordun.

*"Hayır,"* dedi, hafifçe gülümseyerek, ilk kez o akşam gerçek bir gülümseme gördün onda. *"Sadece dinliyorum. Bazen aynı şey gibi görünüyor ama değil."*

*"Sen de kaybettin bir şeyler,"* dedin, onun enerjisindeki o hafif, iyileşmemiş kırığı fark ederek — kendi türünden birinin başka birinde tanıyabileceği bir şey.

Gülümsemesi biraz soldu, ama tamamen kaybolmadı. *"Herkes bir şey kaybeder,"* dedi. *"Fark, onunla ne yaptığın."*"""
                }

                c4Text + """

---

Bunun üzerine bir süre sessiz kaldınız, ikiniz de mutfağın loş ışığında, ne söyleyeceğinizi bilmeden ama konuşmaya da gerek duymadan. Sonunda o çayını bitirdi, kupayı lavaboya bıraktı.

*"İyi geceler, Aiden Blackwood,"* dedi, adını ilk kez telaffuz ederken, sanki tadına bakıyormuş gibi. *"Karar neyse, umarım seni kendine daha az yalnız hissettiren bir karar olur."*

Çıktı, ve sen mutfakta yalnız kaldın, onun söylediği son cümleyi kafanda birkaç kez tekrarlarken.

Sana verdikleri oda —"misafir odası" dediler, ama kilitli değildi, ki bunun bilinçli bir seçim olduğunu anladın— New York'un ışıklarına bakıyordu. Yatağa oturmadın. Pencerenin önünde durdun, şehri izleyerek, alışkanlıkla çıkışları sayarak — pencere, kapı, acil merdiven, üç farklı ışınlanma rotası zihninde hazır.

Ama bu gece, ilk kez uzun zamandır, o rotaları hemen kullanmayı düşünmedin.

Yirmi yıl boyunca "aile" kelimesi senin için bir yara izi gibiydi — dokunulduğunda hâlâ acıyan ama artık kanamayan bir şey. Bu insanlar sana bir aile teklif etmiyorlardı, en azından açıkça değil. Ama masada oturdukların, mutfaktaki o sessizlik, Natasha'nın "bekliyorum" demesi — hiçbiri sana alışık olduğun taktiklerden değildi. Kimse seni bir şeye zorlamıyordu. Kimse senden hemen bir cevap istemiyordu.

Ve belki de asıl korkutucu olan buydu.

Kırmızı Göz'ünün gücünü, Beyaz Göz'ünün sınırlarını, ışınlanmanın seni ne kadar yorduğunu biliordun — bunlar hesaplanabilir şeylerdi, sınırları olan tehlikelerdi. Ama bu — bir masaya oturmak, bir kadının sana "gölgelerin içinde büyümüş biri başka bir gölgeyi tanır" demesi, başka birinin sana "kendine daha az yalnız hissettiren bir karar" dilemesi — bunun hiçbir sınırını bilmiordun. Ve sınırını bilmediğin hiçbir şeye güvenmemiştin şimdiye kadar.

Alnını cama dayadın, New York'un ışıkları gözlerinin önünde bulanıklaşırken.

---

🔀 Gece boyunca aldığın tüm izlenimleri pencereden New York'a bakarken nasıl değerlendiriyorsun?

1️⃣ Temkinli iyimserlik — belki bu sefer farklı olabilir düşüncesi ağır basıyor.
2️⃣ Güçlü şüphe — hâlâ kaçmayı, gitmeyi düşünüyorsun, sadece erteliyorsun.
3️⃣ Belirsiz/karışık — ne tam güven ne tam red, gerçek bir iç çatışma."""
            }
            10 -> {
                // Choice 5 Response -> Chapter 2 Ending
                val c5Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("şüphe") || lastUserMsg.contains("kaçmayı")) {
                    """Penceredeki yansımanda gözlerinin kızıl parıltısını izledin. Yılların getirdiği şüphe bir gecede silinecek kadar zayıf değildi. Stark'ın sözleri, Rogers'ın kuralları, hatta Natasha ve Wanda'nın yaklaşımı... Hepsi birer strateji olabilirdi. Şimdilik kalıyordun ama bavulunu zihninde hiç toplamadın. İlk fırsatta, ilk yanlış adımda ışınlanıp kaybolmaya hazırdın.

Karar vermemiştin henüz. Kaçış rotaları zihninde hâlâ taze ve hazırdı.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi ancak gardını tek bir an bile indirmedi.
Kule'deki ilk gecesinde kaçış planlarını zihninde taze tutarak şüpheyle bekledi."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Belirsiz") || lastUserMsg.contains("karışık") || lastUserMsg.contains("çatışma")) {
                    """Zihninde iki farklı Aiden çatışıyordu — beş yaşında ailesini kaybedip 20 yıldır kaçan o yalnız çocuk ile masadaki adamların dürüstlüğünü hisseden adam. Ne tam güvenebiliyordun ne de sırtını dönüp gidebiliyordun. Bu bilinmezlik, Kırmızı Göz'ün yıkıcılığından bile daha karmaşıktı.

Alnını soğuk cama dayadın. Karar vermemiştin henüz. Ama ilk kez uzun zamandır, kararı vermek için acele etmiyordun.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi ve Kule'deki ilk gecesinde derin bir iç çatışmayla baş başa kaldı.
Geleceğin ne getireceğini zaman gösterecek."""
                } else {
                    """İçindeki o katı zırh ilk kez hafifçe gevşedi. New York'un gökdelenlerindeki ışıklar bir tehdit gibi değil, uzun zamandır aradığın bir sığınak gibi parıldadı zihninde. Yirmi yıldır ilk kez kaçış rotalarını hesaplamayı bıraktın. Belki de bu insanlar gerçekten farklıydı. Belki de Avengers Tower, kaçmak zorunda kalmayacağın ilk yer olabilirdi.

Karar vermemiştin henüz. Ama ilk kez uzun zamandır, kararı vermek için acele etmiyordun ve içinde küçük bir umut kıvılcımı yanıyordu.

---

📖 BÖLÜM 2 SONU — ŞARTLAR

Aiden Blackwood şartları dinledi, gücünü gösterdi ve Avenger üyeleriyle derin temaslar kurdu.
Kule'deki ilk gecesinde temkinli bir iyimserlikle kararını şekillendirdi."""
                }

                c5Text + """

Tebrikler! Bölüm 2'yi başarıyla tamamladın."""
            }
            11 -> {
                progress = progress.copy(chapterNumber = 3)
                storyProgressDao.insertOrUpdate(progress)

                val b3Full = b3Text ?: """# 📖 BÖLÜM 3: DENEME SÜRESİ"""
                b3Full + """

---

🔀 Steve'in Otuz Günlük Deneme Süresi teklifine nasıl cevap vereceksin?

1️⃣ Şartlı kabul — kendi çıkış kurallarını belirt.
2️⃣ Tereddütsüz kabul et.
3️⃣ Çok daha kısa bir süre öner ("birkaç gün")."""
            }
            12 -> {
                val c1Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Tereddütsüz") || lastUserMsg.contains("kabul")) {
                    """"Tamam," dedin, sesin kendi kulağına bile tereddütsüz ve kararlı çıktı. "Otuz gün. Görelim bakalım Avengers Tower dedikleri yer ne kadar dayanıklıymış."
Steve'in yüzünde derin bir memnuniyet ve gerçek bir takdir belirdi. "Aramıza hoş geldin Aiden. Bu karardan pişman olmayacaksın."
Gözlerinin içine bakarak sözlerini tarttın. On beş yıldır ilk kez bir sözün arkasında durmaya niyetlenen adamlara denk geliyordun."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("kısa") || lastUserMsg.contains("birkaç gün")) {
                    """"Otuz gün çok uzun Steve," dedin mesafeli bir tonla. "On beş yıldır hiçbir yerde otuz saat bile durmadım. Birkaç gün diyelim — üç ya da beş gün. Sonrasına duruma göre bakarız."
Steve anlayışla başını salladı, seni zorlamadı. "Pekala. Gün gün ilerleyelim. Sınırlarına saygı duyarım. İster üç gün olsun ister otuz, burada olduğun sürece bizim müttefikimizsin."
Mesafeli tavrını koruyarak başınla onayladın."""
                } else {
                    """"Tamam," dedin, sesin kendi kulağına bile garip geldi, bu kadar kolay çıktığı için. "Otuz gün. Ama kendi şartlarımla. İstediğim an kapıdan çıkarım ve kimse önüme geçmeye çalışmaz."
Steve'in yüzünde hafif, gerçek bir gülümseme belirdi — kutlama değil, sadece memnuniyet. "Anlaştık Blackwood. Biz hapishane işletmiyoruz. Kapı daima açık."
Steve'in netliği göğsündeki o eski savunma zırhını bir anlığına hafifletti."""
                }

                c1Text + """

Steve tabletini eline aldı, ekranı hafifçe kaydırarak sana baktı.
*"İyi. O zaman seni ekiple tanıştıralım, resmi olarak. Ve —"* tabletine bakıp devam etti, *"— birkaç şey netleştirmemiz gerekiyor. Yeteneklerinin sınırlarını bilmemiz lazım, sadece merak değil, güvenlik için. Antrenman salonunda bir değerlendirme yapabilir miyiz?"*

İçinde bir şey gerildi otomatik olarak — *değerlendirme*, *test*, bu kelimeler sende hep aynı tepkiyi uyandırırdı, laboratuvarları, dosyaları, seni bir "vaka" olarak gören gözleri hatırlatarak. Ama Steve'in ses tonunda o eski soğukluk yoktu.

*"Kontrollü olacak,"* diye ekledi Steve, senin yüzündeki anlık gerilimi fark ederek. *"Sen ne kadar göstermek istersen o kadar. Kimse seni zorlamayacak."*

---

🔀 Antrenman salonundaki değerlendirmeyi nasıl kabul edeceksin?

1️⃣ Temkinli kabul et. ("Görelim... ama sınırlarımı ben belirlerim.")
2️⃣ İstekli/meraklı kabul et. ("Ekibin sınırlarını ve senin dövüş stilini merak ediyorum.")
3️⃣ Reddet, sadece sözlü açıklamayla yetin."""
            }
            13 -> {
                val c2Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("İstekli") || lastUserMsg.contains("merak")) {
                    """"Görelim," dedin, gözlerinde meraklı ve meydan okuyan bir pırıltıyla. "Dürüst olmak gerekirse Kaptan Amerika'nın dövüş stilini ve ekibin kapasitesini canlı izlemeyi merak ediyordum zaten."
Steve hafifçe sırıttı, meydan okumanı memnuniyetle karşıladı: "Seni hayal kırıklığına uğratmamaya çalışırım Blackwood. Bakalım efsaneler kadar hızlı mısın."
Birlikte koridordan aşağı, güçlendirilmiş antrenman katına doğru adımladınız."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Reddet") || lastUserMsg.contains("sözlü")) {
                    """"Fiziksel bir teste gerek yok," dedin kollarını kavuşturarak. "Yeteneklerimi teorik olarak anlatırım, bu güvenlik için yeterli olur."
Steve hafifçe gülümsedi. "Teori güzeldir ama antrenman salonunda kahve ve ekip var. Sadece bir tur görmek bile şüpheleri siler. Gel, sadece ortamı gör, zorlama yok."
Steve'in ısrarcı ama dostça davetiyle antrenman salonuna doğru ilerlediniz."""
                } else {
                    """"Görelim bakalım," dedin, sesindeki zırhı koruyarak. "Ama sınırlarımı ben belirlerim Steve. Dur dediğim an her şey durur."
Steve başıyla onayladı: "Söz. Kontrol tamamen sende."
Steve öne düştü ve Kule'nin alt katlarındaki devasa antrenman alanına indiniz."""
                }

                c2Text + """

---

## ANTRENMAN SALONU

Antrenman salonu, beklediğinden çok daha büyüktü — güçlendirilmiş çelik duvarlar, yüksek enerji emici paneller, tavanda yüksek hızlı tarama kameraları ve köşede Thor'a ait antika görünümlü hedef büstleri.

Steve, Natasha ve şaşırtıcı bir şekilde Wanda da oradaydı. Wanda kenarda duruyor, kollarını kavuşturmuş, ilgisi uyanmış bir şekilde izliyordu. Bir de tanımadığın uzun boylu, gözlüklü bir adam vardı — elinde dijital bir biyometrik tablet.

*"Bruce,"* diye tanıttı kendini adam. *"Bruce Banner. Sadece verileri izliyorum, endişelenme."*

Steve salonun ortasına doğru ilerledi, eldivenlerinin kayışlarını sıktı.
*"Basit başlayalım,"* dedi Steve, sana dönerek. *"Fiziksel güç, hız, refleksler ve dayanıklılık. Benimle. Kaptan Amerika'ya karşı bir tur. Ne kadar ileri gitmek istersen o kadar."*

Steve kalkanını koluna takıp dövüş pozisyonu aldı. Bütün ekip ve Bruce Banner gözlerini sana çevirdi.

---

🔀 Steve Rogers'a karşı dövüşte gücünü nasıl ayarlayacaksın?

1️⃣ Tam geri tut. (Kontrollü, Steve'e zarar vermeden hızını ve iyileşmeni göster)
2️⃣ Biraz daha zorla ama yenme. (Sınırlarını zorla, Steve'in savunmasını sına)
3️⃣ Hiç geri tutma, kazanmaya çalış."""
            }
            14 -> {
                val c3Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("Hiç geri tutma") || lastUserMsg.contains("kazanmaya")) {
                    """Geri tutmadın. Kırmızı Göz'ünün sarsıcı ivmesi gözlerinde alevlendi! Salondaki hava basıncı aniden düştü, zemin titredi. Saniyeden kısa bir sürede mavi ve kırmızı bir kuantum çizgisi halinde Steve'in üzerine atıldın. Steve kalkanını kaldırmaya fırsat bulamadan kalkanın kenarına indirdiğin kinetik şok dalgası kalkanı yana savurdu ve elini Steve'in boğazının bir santim önünde durdurdun!

Salonda derin bir sessizlik oldu. Bruce Banner dehşetle ve hayranlıkla tabletine notlar aldı. Steve yutkunarak doğruldu ve takdirle gülümsedi: "Muazzam bir ivme... Hızın kalkanımdan bile önce ulaşıyor. Kazandın Blackwood."
Natasha etkilenmiş bir şekilde mırıldandı: "Sadece hızlı değil, dövüş geometrisini anında yok ediyor.""""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("zorla")) {
                    """Gözlerinde Kırmızı Göz'ün hafif ivmesi parıldadı. Hızını ve reflekslerini tam sınıra çıkardın! Steve'in kalkan hamlesini saniyenin onda birinde savuşturup arkasına geçtin ve sırtına kontrollü ama güçlü bir darbe indirdin. Steve kalkanını son anda arkasına çevirip darbeni güçlükle göğüsledi, ayakları güçlendirilmiş zeminde iki metre geriye kaydı.

Steve derin bir nefes alıp gülümsedi: "İşte bu gerçek bir refleks testi. Savunmamı bu kadar zorlayan az insan gördüm."
Natasha gözlerini kısarak mırıldandı: "Mükemmel bir zamanlama ve fiziksel kontrol."
Bruce Banner tabletindeki grafiklere bakarak şaşkınlıkla notlar aldı."""
                } else {
                    """Geri tuttun. Steve hamle yaptı — kalkanı havayı yararak sana doğru savruldu. Ama senin için zaman adeta yavaşladı. Işınlanmaya bile gerek duymadan, sadece vücut reflekslerinle kalkanın altından süzüldün. Steve'in hamlesi boşa çıktı. İkinci hamlesinde kalkanın kenarı omzuna sertçe çarptı, ama bir saniye içinde yaralanan dokularının mavi kıvılcımlarla kendi kendini onardığını herkes gördü.

*"Ciddi yaralar dakikalar sürer,"* dedin sakince. *"Ölümcül olmayan her şey saniyeler içinde kapanır."*
Natasha kenardan hafif, takdir dolu bir ıslık çaldı. Steve ise kalkanını indirip doğruldu: "Hızın ve hücresel iyileşmen inanılmaz. Kendini harika kontrol ediyorsun.""""
                }

                c3Text + """

Steve terini silip salondaki güçlendirilmiş enerji panellerine doğru yürüdü.
*"Fiziksel kapasiteni gördük,"* dedi Steve, panelleri göstererek. *"Şimdi sıra enerji tarafında. Sadece görmek için — küçük ölçekte, güvenlik protokolleriyle. Bu paneller yüksek wattlı enerjiyi emebiliyor. Seni ya da odayı riske atmadan kozmik güç çıktını ölçebiliriz."*

Stark'ın sesi interkomdan duyuldu: *"Banner ekranları açtı. Göster bakalım elimizde ne var Kozmik Gezgin."*

---

🔀 Enerji testinde panellere ne seviyede güç uygulayacaksın?

1️⃣ Orta seviye göster (kontrollü, hedefli enerji dalgası).
2️⃣ Minimum göster (sadece hafif bir kıvılcım dalgası).
3️⃣ Neredeyse tam kapasiteye yakın göster (güçlü bir patlama)."""
            }
            15 -> {
                val c4Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Minimum")) {
                    """Elini kaldırdın ve panellere sadece parmak ucundan minik, zararsız bir mavi kıvılcım dalgası fırlattın. Paneldeki ibre hafifçe kıpırdadı.

*"Fazlasına gerek yok,"* dedin ellerini cebine sokarak. *"Gücümü şov malzemesi yapmayı sevmem."*
Stark interkomdan mırıldandı: "Cimri çıktı bizimki... Ama o kadarlık kıvılcımda bile frekans yoğunluğu korkutucu. Biyometrik imzan benzersiz."
Steve gizemini korumana saygı duyarak başını salladı."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("tam kapasite")) {
                    """Elini kaldırdın ve gözlerin Kırmızı Göz'ün aleviyle parladı! Panellere doğru devasa bir kozmik enerji patlaması gönderdin! Bütün antrenman salonu sarsıldı, duvar panelleri kırmızı alarmlarla öttü, Stark'ın tabletindeki ölçüm grafikleri tavan yaptı!

Stark heyecanla bağırdı: "Muazzam bir çıktı! Bu çocuk adeta yürüyen bir reaktör! Sayılar büyüleyici!"
Wanda etkilenmiş bir şekilde geri çekildi, Bruce Banner ise şaşkınlıkla gözlüklerini düzeltti."""
                } else {
                    """Elini kaldırdın, ve bu sefer sadece bir ışık küresi değil, odaklanmış gerçek bir enerji dalgası fırlattın. Panel titredi, üzerindeki mavi gösterge çizgileri hızla yükseldi ve sistem fanları yüksek devirde çalışmaya başladı.

*"Bu daha orta seviyem,"* dedin sakince.
Wanda ekrana bakarak *"Bu ölçüm bina seviyesinden fazla bir potansiyel gösteriyor,"* dedi.
Bruce Banner ise *"Enerji imzan bilinen tüm kategorilerin ötesinde, tamamen kendine has,"* diye ekledi."""
                }

                c4Text + """

---

## ARADA BİR MOLA

Antrenman bittikten sonra ekip üstünü değiştirdi. Natasha yanına geldi: *"İyi iş çıkardın Blackwood. Güç, ne zaman ve ne kadar kullanılacağını bilmekle anlamlı hale gelir."*

Öğleyin Kule'nin ortak kafeteryasında büyük masaya oturdunuz. Sam Wilson (Falcon) elinde tepsisiyle masaya geldi. Seni süzüp genişçe gülümsedi.

*"Demek Kule'nin yeni efsanesi sensin,"* dedi Sam. *"Steve seni antrenmanda öve öve bitiremedi. 'Işık hızında ve kalkanımı zorlayabiliyor' diyor. Umarım mutfaktaki kahve makinesini ışınlayıp ortadan kaybetmezsin!"*

Masadaki herkes —Steve, Natasha, Wanda, Bruce— hafifçe güldü ve hepsi senin vereceğin tepkiyi bekledi.

---

🔀 Öğle yemeğinde Sam Wilson'ın şakasına nasıl tepki vereceksin?

1️⃣ Kuru bir espriyle karşılık ver. (Masa kahkahaya boğulsun)
2️⃣ Sessiz kal, sadece hafifçe gülümse.
3️⃣ Şakayı ciddiye al, savunmaya geç."""
            }
            16 -> {
                val c5Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("ciddiye") || lastUserMsg.contains("savunma")) {
                    """"Cihazlarla ya da şakalarla ilgilenmiyorum Wilson," dedin soğuk ve ciddi bir tonda.
Masada anlık bir sessizlik oldu. Sam ellerini kaldırıp gülümsedi: "Pekala dostum, sadece havayı yumuşatmaya çalışıyordum. Sorun yok." Steve araya girerek ortamı dengelendi."""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("Sessiz")) {
                    """Sessiz kaldın, ama dudaklarında hafif, mesafeli bir gülümseme belirdi. Tepkisizliğin ve sakinliğin Sam'in şakasını tatlı bir saygı havasına dönüştürdü.
Sam sandalyesini çekip oturdu: "Sessiz tipleri severim. Saygılar dostum." Ekip yemeğe neşeyle devam etti."""
                } else {
                    """"Kahveyi değil ama seni Kule'nin çatısına ışınlayabilirim Wilson," dedin kuru ve ciddi bir tonda.
Masa bir anda kahkahaya boğuldu! Sam elini kalbine koyup taklit yaptı: "Tamam, tamam, geri çekiliyorum! Adam tehlikeli çıktı!" Yıllar sonra ilk kez bir masada gülmenin ve kabul görmenin tadını çıkardın."""
                }

                c5Text + """

---

## ALARM & S.H.I.E.L.D. TEHDİDİ

Öğleden sonra Kule'nin kırmızı acil durum alarmları aniden çalmaya başladı! Kırmızı ışıklar koridorlarda dönerken Stark'ın sesi interkomdan panikle yükseldi:
*"Millet! S.H.I.E.L.D.'den General Ross destekli ağır bir operasyon ekibi Kule'nin alt kapısında! Işınlanma enerjini ve kozmik imzanı takip etmişler. Seni teslim almadan gitmeyeceklerini söylüyorlar!"*

İçinde 20 yıllık kaçış mekanizması anında tetiklendi — *bulundun, hemen kaç!* Parmak uçlarında mavi kıvılcımlar çatırdamaya başladı.
Steve hızla önüne geçti, ellerini kaldırdı: *"Kaçmana gerek yok Blackwood! Sen bu Kule'nin çatısı altındasın. Bu artık bizim sorunumuz!"*
Natasha gözlerinin içine bakarak sertçe ekledi: *"Şimdi kaçarsan suçlu olduğunu kabul etmiş olursun. Kal ve arkamızda dur!"*

---

🔀 S.H.I.E.L.D. Alarmları çalarken ne yapacaksın? (DALLANMA NOKTASI)

1️⃣ Kal, hiç ışınlanma. (Kule'de kal ve ekibe güvenmeyi seç)
2️⃣ Refleksle çatıya ışınlan, birkaç dakika sonra kendi kararınla geri dön. (Kısa kaçış)
3️⃣ Tümünü geride bırakıp uzağa ışınlan. (Büyük kaçış)"""
            }
            17 -> {
                val c6Choice = lastUserMsg
                val isShortEscape = c6Choice.contains("2") || c6Choice.contains("çatıya") || c6Choice.contains("Kısa")
                val isBigEscape = c6Choice.contains("3") || c6Choice.contains("uzağa") || c6Choice.contains("Büyük")

                progress = if (isBigEscape) {
                    progress.copy(currentBranch = "kacis_thor", flightInstinct = progress.flightInstinct + 2, trustAvengers = "leaning_negative")
                } else if (isShortEscape) {
                    progress.copy(currentBranch = "kisa_kacis", flightInstinct = progress.flightInstinct + 1, wandaBond = progress.wandaBond + 1)
                } else {
                    progress.copy(currentBranch = "main", trustAvengers = "leaning_positive")
                }
                storyProgressDao.insertOrUpdate(progress)

                val outcomeText = if (isBigEscape) {
                    val part1 = b3Kacis ?: """Onları dinlemedin..."""
                    val part2 = b3Thor ?: """ÜÇ GÜN SONRA..."""
                    part1 + "\n\n---\n\n" + part2
                } else if (isShortEscape) {
                    b3KisaKacis ?: """Natasha'nın sözleri kulağına ulaştı ama yirmi yıllık refleksin kelimelerden hızlı hareket etti! Bir mavi ışık patlamasıyla Kule'nin en üst heliped çatısına ışınlandın!"""
                } else {
                    """Işınlanmadın. Elindeki mavi kıvılcımları yavaşça söndürdün. Yirmi yıllık kaçış refleksini ilk kez bastırdın ve Kule'de kaldın.

Steve ve Natasha aşağı inip S.H.I.E.L.D. General Ross ve ekibini Avengers yetkisiyle geri çevirdi. Sen ve Wanda yüksek cam pencereden onları izlediniz. Wanda yanına gelip fısıldadı: *"Bazen kalmak, kaçmaktan çok daha büyük bir cesaret gerektirir. Doğru olanı yaptın Aiden."* Akşam olduğunda çatıya çıktın."""
                }

                outcomeText + """

---

## RECONVERGENCE — Çatı Sahnesi

Gece New York'un üzerine çöktü. Bütün şehir binlerce ışıkla ışıldıyordu.
Kule'nin heliped çatısına çıktın. Soğuk gece rüzgarı yüzüne vuruyordu.
Natasha gölgelerin arasından süzülüp yanındaki demirliklere yaslandı.

*"Kule'de ilk gecen,"* dedi Natasha, şehri izleyerek. *"New York ayaklarının altında... Nasıl hissediyorsun Aiden? Kalmak seni korkutuyor mu, yoksa alışıyor musun?"*

---

🔀 Çatıda Natasha seni bulduğunda ona karşı dürüstlüğün ne seviyede olacak?

1️⃣ Tam dürüst cevap ver. ("Korkutucu... Kalmak, kaybedecek bir şey biriktirmek demek.")
2️⃣ Yarım dürüst — şakayla hafiflet. ("Manzara güzel Romanoff, kaçmıyorum.")
3️⃣ Kapan, yüzeysel cevap ver. ("Sadece hava alıyordum, sorun yok.")"""
            }
            18 -> {
                val c7Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("Kapan")) {
                    """"Sadece hava alıyordum Romanoff. Endişelenecek bir şey yok," dedin mesafeli ve soğuk bir tonda.
Natasha başını salladı, zorlamadı: "Pekala. Sınırlarına saygı duyarım. Ama burada yalnız olmadığını unutma.""""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("Yarım")) {
                    """"Manzara güzel Romanoff. Endişelenme, bu sefer kaçmıyorum," dedin hafif bir tebessümle.
Natasha hafifçe gülümsedi: "Seni takip etmiyordum zaten. Sadece çatı havası iyi gelir. Otuz günün var, bu hissin tadını çıkar.""""
                } else {
                    """"Korkutucu," dedin dürüstçe. "Kaçmak kolaydır. Ama kalmak... bir yerlere kök salmak... kaybedecek bir şey biriktirmek demek."
Natasha New York ışıklarına bakarak devam etti: "Ben de Kızıl Oda'dan ayrıldıktan sonra tam olarak bunu hissettim. Kaybedecek şeylerin olması seni zayıflatmaz Blackwood. Onlar seni korumak için savaşacağın nedenlere dönüştürür.""""
                }

                c7Text + """

*"Otuz günün var,"* dedi Natasha ayağa kalkarken. *"Gör bakalım neye benziyor kalmak. İyi geceler Blackwood."*

Natasha ayağa kalkıp içeri girdi. Çatıda yalnız kaldın.
Cebinden gümüş kolye ucunu çıkardın — yirmi yıl önceki patlamadan ailesinden kalan tek hatıra. Ay ışığı kolyenin üzerinde parıldadı.

---

🔀 Kolye ile baş başa kaldığında zihnindeki iç ses nasıl bir kararla şekillenecek?

1️⃣ Kolyeyi elinde tutup umutla düşün. (Belki bu sefer bir şeyleri kaybetmeden tutabilirsin)
2️⃣ Kolyeyi hızla cebine geri koy, düşünceyi bastır. (Duygusal zayıflığa izin verme)
3️⃣ Kolyeye uzun uzun bak, geçmişe dair kısa bir anı/flashback zihninde canlansın."""
            }
            19 -> {
                val c8Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("cebine") || lastUserMsg.contains("bastır")) {
                    """Kolyeyi hızla cebine geri koydun ve duygusal zayıflığa geçit vermedin. Soğukkanlı zırhını koruyarak Kule'ye ve yeni odana doğru adım attın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood 30 günlük deneme süresine adım attı, duygularını ve gardını kontrol altında tutarak geceyi tamamladı."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("flashback") || lastUserMsg.contains("bak")) {
                    """Kolyeye bakarken 20 yıl önceki çocukluk evin ve alevlerin arasındaki o patlama anı gözlerinin önünden geçti. Geçmişin acısını kabullenerek Avengers Kulesi'nde yeni bir sayfa açtın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood geçmişinin anılarıyla yüzleşti ve Avengers Kulesi'nde 30 günlük deneme süresine adım attı."""
                } else {
                    """Kolyeyi avucunda sıktın. Yıllardır ilk kez içindeki o karanlık şüphe yerini umuda bıraktı. Belki de bu sefer bir şeyleri kaybetmeden tutabilirdin. Geceye gülümseyerek Kule'ye adım attın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood 30 günlük deneme süresini kabul etti, Avengers ekibiyle ilk bağlarını kurdu ve kolyeyi elinde tutarak umut dolu bir kararla geceyi tamamladı."""
                }

                c8Text + """

Tebrikler! Bölüm 3'ü başarıyla tamamladın."""
            }
            else -> {
                "📖 Bölüm 3'ü başarıyla tamamladın. Kararların Avengers evreninin akışına işlendi. Yakında yayınlanacak Bölüm 4 için takipte kal!"
            }
        }
    }

    private suspend fun executeModelRequest(
        model: String,
        settings: UserSettingsEntity,
        systemPrompt: String,
        messages: List<MessageEntity>,
        botId: String? = null
    ): Pair<String, Pair<Long, Long>> {
        return when {
            // Groq Models
            model.contains("llama") || model.contains("groq") || model.contains("mixtral") -> {
                val apiKey = settings.groqApiKey.ifBlank { settings.customApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("Groq API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Groq Key girin.")
                callOpenAiCompatibleApi("https://api.groq.com/openai/v1/chat/completions", apiKey, model, systemPrompt, messages)
            }
            // Claude Models
            model.contains("claude") -> {
                val apiKey = settings.claudeApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Claude API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Claude Key girin.")
                callClaudeApi(apiKey, model, systemPrompt, messages)
            }
            // OpenAI / DeepSeek Models
            model.contains("gpt") || model.contains("deepseek") -> {
                val isDeepseek = model.contains("deepseek")
                val url = if (isDeepseek) "https://api.deepseek.com/chat/completions" else "https://api.openai.com/v1/chat/completions"
                val apiKey = settings.openaiApiKey.ifBlank { settings.groqApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("OpenAI/DeepSeek API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Key girin.")
                callOpenAiCompatibleApi(url, apiKey, model, systemPrompt, messages)
            }
            // Default Gemini Models
            else -> {
                val customKey = settings.customApiKey.trim()
                val buildConfigKey = getBuildConfigKey()
                val backupKey = settings.backupApiKey.trim()

                val primaryKey = if (customKey.isNotBlank()) customKey else buildConfigKey
                val candidateKeys = listOf(primaryKey, backupKey)
                    .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
                    .distinct()

                if (candidateKeys.isEmpty()) {
                    throw IllegalStateException("Gemini API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden API Key girin.")
                }

                var lastErr: Exception? = null
                for (k in candidateKeys) {
                    try {
                        return callGeminiApi(k, model, systemPrompt, messages, enableNsfw = settings.enableNsfw)
                    } catch (err: Exception) {
                        lastErr = err
                    }
                }
                throw lastErr ?: IllegalStateException("Gemini API anahtarları ile bağlantı kurulamadı.")
            }
        }
    }

    suspend fun generateOpeningMessage(bot: BotEntity): String = withContext(Dispatchers.IO) {
        val settings = getOrCreateSettings()

        val systemPrompt = buildSystemPrompt(bot, settings, includeStyleGuide = false) + if (bot.writingStyle == "rp") {
            "\n\nGörev: Bu sahneyi başlatan bir açılış anı yaz. Üçüncü tekil şahıs, roman/RP tarzı, betimleme + diyalog içersin. 3-6 cümle. Sadece sahneyi yaz."
        } else {
            "\n\nGörev: Bu senaryoya uygun kısa bir ilk mesaj yaz. Sadece mesajı yaz."
        }

        val requestMsgs = listOf(MessageEntity(id = "init", botId = bot.id, role = "user", text = "Sahneyi/mesajı başlat.", timestamp = 0L))

        val result = executeModelRequest(sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" }), settings, systemPrompt, requestMsgs, botId = bot.id)
        recordTokenUsage(bot.id, result.second.first, result.second.second)
        return@withContext result.first
    }

    suspend fun updateMemorySummaries(bot: BotEntity, messages: List<MessageEntity>) = withContext(Dispatchers.IO) {
        if (messages.size < 6) return@withContext

        val settings = getOrCreateSettings()
        val apiKey = if (settings.customApiKey.isNotBlank()) settings.customApiKey else getBuildConfigKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext

        val aiName = if (bot.mode == "universe") bot.universeName else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Kullanıcı" }

        val recapText = messages.takeLast(16).joinToString("\n") { m ->
            val sender = if (m.role == "user") userLabel else aiName
            "$sender: ${m.text}"
        }

        val prompt = "Aşağıdaki sahneden iki ayrı liste çıkar, SADECE şu formatta yaz, başka hiçbir şey ekleme:\n\nDURUM:\n- (yan karakterler, mekanlar, çözülmemiş konular — en fazla 6 madde)\n\nHAFIZA:\n- (duygusal gelişmeler, ilişki değişimleri, önemli sözler — en fazla 5 madde)"

        try {
            val requestMsgs = listOf(MessageEntity(id = "sum", botId = bot.id, role = "user", text = "$prompt\n\nSAHNE:\n$recapText", timestamp = 0L))
            val res = callGeminiApi(apiKey, "gemini-2.5-flash", "Sen yardımcı bir özetleyicisin.", requestMsgs)
            recordTokenUsage(bot.id, res.second.first, res.second.second)
            val raw = res.first

            if (raw.contains("DURUM:", ignoreCase = true) || raw.contains("HAFIZA:", ignoreCase = true)) {
                val durumMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE))[0]
                    .replace(Regex("DURUM:", RegexOption.IGNORE_CASE), "").trim()

                val hafizaMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE)).getOrNull(1)?.trim() ?: ""

                val newStory = listOf(bot.storyNotes, durumMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(20).joinToString("\n")

                val newMemory = listOf(bot.memoryNotes, hafizaMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(20).joinToString("\n")

                val updatedBot = bot.copy(
                    storyNotes = newStory,
                    memoryNotes = newMemory,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Import / Export Backup Snapshot ---

    suspend fun exportDataSnapshot(): String = withContext(Dispatchers.IO) {
        val bots = botDao.getAllBotsList()
        val messages = messageDao.getAllMessagesList()
        val settings = getOrCreateSettings()

        val snapshot = BackupSnapshot(
            version = 1,
            bots = bots,
            messages = messages,
            settings = settings
        )
        backupAdapter.toJson(snapshot)
    }

    suspend fun importDataSnapshot(jsonStr: String) = withContext(Dispatchers.IO) {
        val trimmed = jsonStr.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException("İçe aktarılacak metin boş.")
        }

        // 1. Try standard Moshi backup snapshot
        try {
            val snapshot = backupAdapter.fromJson(trimmed)
            if (snapshot != null) {
                settingsDao.insertOrUpdate(snapshot.settings)
                val idMap = mutableMapOf<String, String>()
                for (bot in snapshot.bots) {
                    val existing = botDao.getBotById(bot.id)
                    if (existing != null) {
                        val newId = UUID.randomUUID().toString()
                        idMap[bot.id] = newId
                        botDao.insertOrUpdate(bot.copy(id = newId, isPublic = false, isTemplate = false))
                    } else {
                        botDao.insertOrUpdate(bot)
                    }
                }
                if (snapshot.messages.isNotEmpty()) {
                    val remappedMsgs = snapshot.messages.map { msg ->
                        val remappedBotId = idMap[msg.botId] ?: msg.botId
                        msg.copy(botId = remappedBotId)
                    }
                    messageDao.insertAllMessages(remappedMsgs)
                }
                return@withContext
            }
        } catch (_: Exception) {
            // Fallthrough to custom JSON parser
        }

        // 2. Custom robust parser for partial backups, single bots, lists, or character cards
        if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            if (obj.has("bots") || obj.has("messages") || obj.has("settings")) {
                // Partial backup object
                if (obj.has("settings")) {
                    try {
                        val sObj = obj.getJSONObject("settings")
                        val settings = getOrCreateSettings().copy(
                            customApiKey = sObj.optString("customApiKey", ""),
                            groqApiKey = sObj.optString("groqApiKey", ""),
                            claudeApiKey = sObj.optString("claudeApiKey", ""),
                            openaiApiKey = sObj.optString("openaiApiKey", ""),
                            backupApiKey = sObj.optString("backupApiKey", ""),
                            selectedModel = sObj.optString("selectedModel", "gemini-2.5-flash"),
                            fallbackModel = sObj.optString("fallbackModel", "gemini-2.5-flash")
                        )
                        settingsDao.insertOrUpdate(settings)
                    } catch (_: Exception) {}
                }
                if (obj.has("bots")) {
                    val bArr = obj.getJSONArray("bots")
                    for (i in 0 until bArr.length()) {
                        val bObj = bArr.getJSONObject(i)
                        parseAndSaveBotObject(bObj)
                    }
                }
            } else {
                // Single bot or Character Card
                parseAndSaveBotObject(obj)
            }
        } else if (trimmed.startsWith("[")) {
            val arr = JSONArray(trimmed)
            for (i in 0 until arr.length()) {
                val item = arr.get(i)
                if (item is JSONObject) {
                    parseAndSaveBotObject(item)
                }
            }
        } else {
            throw IllegalArgumentException("Geçersiz JSON formatı. Lütfen geçerli bir yedek veya karakter dosyası yapıştırın.")
        }
    }

    private suspend fun parseAndSaveBotObject(obj: JSONObject) {
        val name = obj.optString("aiName").ifBlank {
            obj.optString("name").ifBlank {
                obj.optString("char_name", "İçe Aktarılan Bot")
            }
        }
        val personality = obj.optString("aiPersonality").ifBlank {
            obj.optString("personality").ifBlank {
                obj.optString("description", "")
            }
        }
        val scenario = obj.optString("scenario").ifBlank {
            obj.optString("world_scenario", "")
        }
        val openingMsg = obj.optString("openingMessage").ifBlank {
            obj.optString("firstMessage").ifBlank {
                obj.optString("first_mes").ifBlank {
                    obj.optString("greeting", "Merhaba!")
                }
            }
        }

        val rawId = obj.optString("id")
        val existingBot = if (rawId.isNotBlank()) botDao.getBotById(rawId) else null
        val finalId = if (rawId.isBlank() || existingBot != null) UUID.randomUUID().toString() else rawId

        val bot = BotEntity(
            id = finalId,
            mode = obj.optString("mode", "personal"),
            aiName = name,
            aiPersonality = personality,
            scenario = scenario,
            universeName = obj.optString("universeName", "Evren"),
            keyCharactersJson = obj.optString("keyCharactersJson", "[]"),
            userCharName = obj.optString("userCharName", "Kullanıcı"),
            userCharDesc = obj.optString("userCharDesc", ""),
            openingMessage = openingMsg,
            writingStyle = obj.optString("writingStyle", "Sohbet"),
            intensity = obj.optString("intensity", "Normal"),
            customLength = obj.optString("customLength", "default"),
            isNsfw = obj.optBoolean("isNsfw", true),
            pinnedMemory = obj.optString("pinnedMemory", ""),
            storyNotes = obj.optString("storyNotes", ""),
            memoryNotes = obj.optString("memoryNotes", ""),
            updatedAt = System.currentTimeMillis()
        )
        botDao.insertOrUpdate(bot)
    }
}
