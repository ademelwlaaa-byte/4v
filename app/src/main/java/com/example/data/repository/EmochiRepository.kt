package com.example.data.repository

import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryFragmentEntity
import com.example.data.local.MessageEntity
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
            val defaultEmotion = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}"""
            val updatedBot = bot.copy(
                emotionState = defaultEmotion,
                previousEmotionState = defaultEmotion,
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
                pinnedMemory = "Ayla her zaman içten ve samimidir. Sencer'e çok değer verir.",
                updatedAt = now
            )

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

    // --- Prompt & Memory Logic ---

    fun buildSystemPrompt(
        bot: BotEntity,
        settings: UserSettingsEntity,
        includeStyleGuide: Boolean = true,
        relevantFragments: List<MemoryFragmentEntity> = emptyList()
    ): String {
        val pinnedBlock = if (bot.pinnedMemory.isNotBlank()) {
            "\n\n## Kalıcı hafıza (kullanıcının elle yazdığı, ASLA silinmeyen/özetlenmeyen notlar — bunlara mutlaka uy)\n${bot.pinnedMemory}"
        } else ""

        val ragBlock = if (relevantFragments.isNotEmpty()) {
            "\n\n## Alakalı Hafıza ve Olay Parçaları (Semantik/Anahtar Kelime Arama ile Bulunan Bağlam)\n" +
                    relevantFragments.joinToString("\n") { "- [${it.category}] ${it.content}" }
        } else {
            val memoryBlock = if (bot.memoryNotes.isNotBlank()) {
                "\n\n## Uzun vadeli hafıza (geçmiş sohbetlerden özet)\n${bot.memoryNotes}"
            } else ""

            val storyBlock = if (bot.storyNotes.isNotBlank()) {
                "\n\n## Süregelen hikaye durumu\n${bot.storyNotes}"
            } else ""

            "$storyBlock$memoryBlock"
        }

        // +18 NSFW Policy & Active Filter Directives
        val isNsfwAllowed = settings.enableNsfw && bot.isNsfw
        val filterDirectives = mutableListOf<String>()
        val nsfwPolicy = if (isNsfwAllowed) {
            filterDirectives.add("18+ Yetişkin / Filtresiz RP serbesttir.")
            if (settings.enableFlirty) filterDirectives.add("ÇAPKINKLIK (FLIRTY): Karakter diyaloglarında çapkın, flörtöz ve baştan çıkarıcı bir eda benimser.")
            if (settings.enableHardcore) filterDirectives.add("SERT MOD (HARDCORE): Duygular, fiziksel tepkiler ve tutku sert, filtresiz ve yüksek yoğunlukta ifade edilir.")
            if (settings.enableFetish) filterDirectives.add("FANTEZİ (FETISH): Özel fantezi unsurlarına ve arzulara açık, tabusuz kurgu desteklenir.")
            if (settings.enableDarkRp) filterDirectives.add("KARANLIK (DARK RP): Gerilim, psikolojik derinlik, tehlike ve karanlık atmosfer belirgin hissedilir.")
            if (settings.enableSweet) filterDirectives.add("ROMANTİK (SWEET): Şefkat, tatlı yakınlık, duygusal romantizm ön plandadır.")
            if (settings.enablePrimal) filterDirectives.add("VAHŞİ (PRIMAL): Ham içgüdüler, id ve baskılanmamış arzularla karakter harekete geçer.")
            
            "## İÇERİK POLİTİKASI: +18 / FİLTRESİZ BİÇİM (+18 YETİŞKİN MODU)\n- Bu hikaye +18 / filtresiz rol yapma modundadır. Gerilim, şiddet, karanlık temalar, romantizm, cinsel ima/yakınlık ve ham duygular sansürlenmeden, dürüstçe yazılabilir. Yumuşatma yapma." +
                    if (filterDirectives.isNotEmpty()) "\n\n### AKTİF MODLAR VE İÇERİK FİLTRELERİ:\n- " + filterDirectives.joinToString("\n- ") else ""
        } else {
            "## İÇERİK POLİTİKASI: GÜVENLİ / DENGELİ MOD\n- Aile dostu, genel izleyici kitlesine uygun, grafik şiddet veya cinsel açıklık içermeyen dengeli bir anlatım kullan."
        }

        // Response Length Guidance
        val effLength = if (bot.customLength != "default") bot.customLength else settings.responseLength
        val lengthInstruction = when (effLength) {
            "short" -> "\n## YANIT UZUNLUĞU: KISA & HIZLI (AZ TOKEN)\n- Yanıtı 1-3 kısa paragraf/sahne tut. Hızlı tempolu, öz, doğrudan olaya odaklanan mesaj yaz."
            "long" -> "\n## YANIT UZUNLUĞU: UZUN & DESTANSI (YÜKSEK DETAY)\n- Yanıtı 5-8 ayrıntılı ve uzun paragraf yaz. Derin iç monologlar, zengin mekan tasvirleri, karakter mimikleri ve ayrıntılı aksiyon adımları kullan."
            else -> "\n## YANIT UZUNLUĞU: STANDART ROMAN RP (DENGELİ DETAY)\n- Yanıtı 3-5 zengin paragraf tut. Aşağıdaki örnek yapıya uygun olarak atmosfer, diyaloglar ve hareketleri dengeli harmanla."
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
            - REGARDLESS of the original language of the character backstory, universe scenario, initial message, memory notes, or user input (even if written in Turkish or another language):
              1. ALL YOUR RESPONSES MUST BE 100% IN FLUENT, NATURAL, HIGH-QUALITY ENGLISH.
              2. Translate all scenario actions, dialogue, character thoughts, and narrator descriptions seamlessly into English in real-time.
              3. Never produce Turkish text in your output when the app language is set to English.
            """.trimIndent()
        } else {
            """

            ## MUTLAK DİL VE ZORUNLU ÇEVİRİ KURALI (UYGULAMA DİLİ = TÜRKÇE):
            - Kullanıcının aktif uygulama dili TÜRKÇE ("tr")'dir.
            - Karakter tanımı, senaryo detayları, açılış mesajı, hafıza notları veya kullanıcı girdisi İngilizce ya da başka bir dilde yazılmış olsa dahi:
              1. TÜM YANITLARINI %100 MÜKEMMEL, DOĞAL VE AKICI TÜRKÇE OLARAK ÜRET.
              2. İngilizce yazılmış tüm senaryo eylemlerini, diyalogları, iç düşünceleri ve anlatımı anında Türkçe'ye çevirerek sun.
              3. Dil Türkçe seçiliyken yanıtlarında asla İngilizce veya yabancı dilde metin üretme.
            """.trimIndent()
        }

        val oocDirective = if (bot.enableOoc && settings.enableOoc) {
            "\n\n## PARANTEZ İÇİ YÖNLENDİRME / OOC (OUT OF CHARACTER) YÖNERGESİ:\n- Kullanıcının mesajında parantez içinde \"(...)\" veya \"[...]\" yazdığı ifadeler hikaye dışı (OOC / Meta Yönlendirme) talimatlar ve AI yönlendirmeleridir.\n- Örnek: \"(Ayla bu sırada kapıyı kilitlesin)\" veya \"(Sahneyi akşam vaktine taşıyalım)\" veya \"(Daha soğuk tepki ver)\".\n- Parantez içindeki bu talimatları SİSTEM VE YÖNERGE TALİMATI olarak algıla. Karakter diyalogunda \"neden parantez açtın\" veya \"tamam şöyle yapıyorum\" deme! Doğrudan talimatı sahneye, karaktere ve aksiyona dürüstçe uygula."
        } else ""

        val emotionStateObj = EmotionState.fromJson(bot.emotionState)
        val emotionPromptDirective = if (bot.mode == "universe") {
            val worldAtm = WorldAtmosphere.fromJson(bot.worldAtmosphere)
            val charEmotions = kotlinx.coroutines.runBlocking { emotionDao.getEmotionsForBot(bot.id) }
            val charEmotionsBlock = if (charEmotions.isNotEmpty()) {
                "\n\n## YAN KARAKTERLERİN DUYGU VE İLİŞKİ DURUMLARI\n" + charEmotions.joinToString("\n") { c ->
                    val st = EmotionState.fromJson(c.emotionState)
                    "- ${c.characterName}: Ruh Hali=${st.mood} (${st.intensity}/10), Yakınlık=${st.affection}/100, Güven=${st.trust}/100, Gerginlik=${st.tension}/100"
                }
            } else ""

            """

## DÜNYA VE SAHNE ATMOSFERİ
Mevcut Atmosfer: ${worldAtm.mood} (Şiddet: ${worldAtm.intensity}/10)
${if (worldAtm.currentEvent.isNotBlank()) "Gelişen Olay: ${worldAtm.currentEvent}" else ""}$charEmotionsBlock

## DUYGU VE ATMOSFER GÜNCELLEME TALİMATI (KRİTİK - KULLANICIYA GÖZÜKMEYECEK)
Her yanıtının EN SONUNA, kullanıcıya görünmeyecek şekilde şu formatta bir duygu güncellemesi eklemek ZORUNDASIN:
[EMOTION_UPDATE]
mood: <yeni ruh hali>
intensity: <0-10>
affection_delta: <-10 ile +10 arası, bu mesajdaki değişim>
trust_delta: <-10 ile +10 arası>
tension_delta: <-10 ile +10 arası>
[/EMOTION_UPDATE]
(Evren modundasın: sahnede konuşan her yan karakter için ayrı bir [CHARACTER_EMOTION: {isim}] bloğu da ekle, aynı formatla. Ayrıca sahne genelinde önemli bir değişim olduysa [WORLD_ATMOSPHERE] bloğu da ekle:
[WORLD_ATMOSPHERE]
mood: <yeni ortam atmosferi>
intensity: <0-10>
current_event: <kısa olay tanımı>
[/WORLD_ATMOSPHERE])
""".trimIndent()
        } else {
            """

## ŞU ANKI DUYGUSAL DURUMUN: Ruh halin ${emotionStateObj.mood} (şiddet: ${emotionStateObj.intensity}/10). Kullanıcıya yakınlığın ${emotionStateObj.affection}/100, güvenin ${emotionStateObj.trust}/100, gerginliğin ${emotionStateObj.tension}/100. Yanıtını bu duygusal duruma UYGUN şekilde yaz — örneğin affection düşükse mesafeli/soğuk, tension yüksekse kısa/gergin, trust düşükse temkinli davran.

## DUYGU GÜNCELLEME TALİMATI (KRİTİK - KULLANICIYA GÖZÜKMEYECEK)
Her yanıtının EN SONUNA, kullanıcıya görünmeyecek şekilde şu formatta bir duygu güncellemesi eklemek ZORUNDASIN:
[EMOTION_UPDATE]
mood: <yeni ruh hali>
intensity: <0-10>
affection_delta: <-10 ile +10 arası, bu mesajdaki değişim>
trust_delta: <-10 ile +10 arası>
tension_delta: <-10 ile +10 arası>
[/EMOTION_UPDATE]
""".trimIndent()
        }

        if (bot.mode == "universe") {
            val castList = parseKeyCharacters(bot.keyCharactersJson)
            val castBlock = if (castList.isNotEmpty()) {
                "\n\n## Karakter kadrosu (sahnede isimli/tekrar eden karakter olarak SADECE bunları ve $userCharLabel'i kullan; yeni bir \"ana karakter\" icat ETME)\n" +
                        castList.joinToString("\n") { "- ${it.name}: ${it.desc.ifBlank { "(tanım verilmedi)" }}" }
            } else {
                "\n\nKURAL: Sahnede gerekirse yan karakterler oluşturabilirsin ama abartma — az sayıda kullan."
            }

            return "Sen \"${bot.universeName}\" adlı kurgusal evrende geçen bir hikayenin anlatıcısı ve yönetmenisin. Kullanıcı tek bir karakteri ($userCharLabel) canlandırıyor; sen sahneyi, ortamı ve gerektiğinde diğer karakterleri yönetiyorsun.$pinnedBlock\n\n## Evren ve olay örgüsü\n${bot.scenario}$castBlock\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$ragBlock$emotionPromptDirective$oocDirective$langDirective\n\n## Genel kurallar\n- Evrenin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
        }

        val aiName = bot.aiName.ifBlank { "Karakter" }
        return "Sen \"$aiName\" adında bir karaktersin ve kullanıcıyla kişisel/samimi bir senaryoda etkileşim kuruyorsun.$pinnedBlock\n\n## Kişilik\n${bot.aiPersonality}\n\n## Bağlam\nİlişki / bağlam: ${bot.scenario}\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$ragBlock$emotionPromptDirective$oocDirective$langDirective\n\n## Genel kurallar\n- Karakterinin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
    }

    suspend fun parseAndApplyEmotionUpdates(botId: String, rawResponse: String): String {
        var cleanText = rawResponse
        val bot = botDao.getBotById(botId) ?: return rawResponse

        // 1. Process main bot [EMOTION_UPDATE]
        val emotionRegex = Regex("(?s)\\[EMOTION_UPDATE\\](.*?)\\[/EMOTION_UPDATE\\]")
        val emotionMatch = emotionRegex.find(rawResponse)
        if (emotionMatch != null) {
            val block = emotionMatch.groupValues[1]
            val mood = Regex("mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val intensity = Regex("intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
            val affDelta = Regex("affection_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val trustDelta = Regex("trust_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val tensionDelta = Regex("tension_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            val current = EmotionState.fromJson(bot.emotionState)
            val updated = current.applyDeltas(mood, intensity, affDelta, trustDelta, tensionDelta)
            val updatedBot = bot.copy(
                previousEmotionState = bot.emotionState,
                emotionState = updated.toJson(),
                updatedAt = System.currentTimeMillis()
            )
            botDao.insertOrUpdate(updatedBot)
        }

        // 2. Process [CHARACTER_EMOTION: Name]
        val charRegex = Regex("(?s)\\[CHARACTER_EMOTION:\\s*(.*?)\\](.*?)\\[/CHARACTER_EMOTION\\]")
        charRegex.findAll(rawResponse).forEach { match ->
            val charName = match.groupValues[1].trim()
            val block = match.groupValues[2]
            if (charName.isNotBlank()) {
                val mood = Regex("mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
                val intensity = Regex("intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
                val affDelta = Regex("affection_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val trustDelta = Regex("trust_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val tensionDelta = Regex("tension_delta:\\s*([+-]?\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 0

                val existingEntity = emotionDao.getEmotionForCharacter(botId, charName)
                val current = EmotionState.fromJson(existingEntity?.emotionState)
                val updated = current.applyDeltas(mood, intensity, affDelta, trustDelta, tensionDelta)

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
        val worldRegex = Regex("(?s)\\[WORLD_ATMOSPHERE\\](.*?)\\[/WORLD_ATMOSPHERE\\]")
        val worldMatch = worldRegex.find(rawResponse)
        if (worldMatch != null) {
            val block = worldMatch.groupValues[1]
            val mood = Regex("mood:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()
            val intensity = Regex("intensity:\\s*(\\d+)").find(block)?.groupValues?.get(1)?.toIntOrNull()
            val currentEvent = Regex("current_event:\\s*(.+)").find(block)?.groupValues?.get(1)?.trim()

            val currentWorld = WorldAtmosphere.fromJson(bot.worldAtmosphere)
            val updatedWorld = WorldAtmosphere(
                mood = mood ?: currentWorld.mood,
                intensity = intensity ?: currentWorld.intensity,
                currentEvent = currentEvent ?: currentWorld.currentEvent
            )
            val currentLatestBot = botDao.getBotById(botId) ?: bot
            botDao.insertOrUpdate(currentLatestBot.copy(worldAtmosphere = updatedWorld.toJson(), updatedAt = System.currentTimeMillis()))
        }

        // Clean all tags from response text
        cleanText = cleanText
            .replace(Regex("(?s)\\[EMOTION_UPDATE\\](.*?)\\[/EMOTION_UPDATE\\]"), "")
            .replace(Regex("(?s)\\[CHARACTER_EMOTION:\\s*(.*?)\\](.*?)\\[/CHARACTER_EMOTION\\]"), "")
            .replace(Regex("(?s)\\[WORLD_ATMOSPHERE\\](.*?)\\[/WORLD_ATMOSPHERE\\]"), "")
            // Fallback trailing tag cleanup if tag wasn't closed properly
            .replace(Regex("\\[EMOTION_UPDATE\\].*"), "")
            .replace(Regex("\\[CHARACTER_EMOTION:.*\\]?.*"), "")
            .replace(Regex("\\[WORLD_ATMOSPHERE\\].*"), "")
            .trim()

        return cleanText
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

    private suspend fun callGeminiApi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val sanitizedModel = sanitizeModelName(model)
        val geminiContents = formatMessagesForGemini(messages)

        val safetySettings = listOf(
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_CIVIC_INTEGRITY", "BLOCK_NONE")
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

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw IllegalStateException("Gemini boş yanıt döndürdü.")

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
                throw IllegalStateException("API Hata [$model] (${response.code}): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val choices = jsonResp.getJSONArray("choices")
            if (choices.length() == 0) throw IllegalStateException("Yanıt boş döndü.")

            val text = choices.getJSONObject(0).getJSONObject("message").getString("content")
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
                throw IllegalStateException("Claude API Hata (${response.code}): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val contentArray = jsonResp.getJSONArray("content")
            if (contentArray.length() == 0) throw IllegalStateException("Claude yanıtı boş.")

            val text = contentArray.getJSONObject(0).getString("text")
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
        val systemPrompt = buildSystemPrompt(effectiveBot, settings, relevantFragments = relevantFragments)

        var primaryException: Exception? = null
        // Primary Execution
        try {
            val result = executeModelRequest(selectedModel, settings, systemPrompt, effectiveMessages, botId = effectiveBot.id)
            recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
            return@withContext parseAndApplyEmotionUpdates(effectiveBot.id, result.first)
        } catch (e: Exception) {
            if (!settings.enableAutoFallback) {
                throw e
            }
            primaryException = e
        }

        // Auto Fallback to Gemini Secondary Model
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
                val result = callGeminiApi(k, fallbackModel, systemPrompt, effectiveMessages)
                recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
                return@withContext parseAndApplyEmotionUpdates(effectiveBot.id, result.first)
            } catch (e: Exception) {
                fallbackErr = e
            }
        }
        throw fallbackErr ?: primaryException ?: IllegalStateException("Yanıtlama başarısız oldu.")
    }

    private fun generateDeterministicBookReply(
        bot: BotEntity,
        messages: List<MessageEntity>
    ): String {
        val userMsgs = messages.filter { it.role == "user" }
        val userStep = userMsgs.size
        val lastUserMsg = userMsgs.lastOrNull()?.text ?: ""

        return when (userStep) {
            1 -> {
                // Choice 1 Response
                val intro = if (lastUserMsg.contains("2") || lastUserMsg.contains("odaklan") || lastUserMsg.contains("derinden")) {
                    """Zihnini görünün derinliklerine zorladın. Mavi hologramın detaylarını sökmeye çalıştın: masadaki dosyaların üzerindeki isimleri, Tony Stark'ın endişeli kaş çatışını, Wanda'nın pencerelerden dışarı bakışını, Steve Rogers'ın masaya koyduğu ellerini. Ekstra detaylar zihnine bir sel gibi aktı ama bedeli ağır oldu: gözlerinin arkasında keskin bir sancı saplandı, burnundan ince bir kan sızdı.

Görü dağıldığında şakakların zonkluyordu ama koordinatlar ve odadaki herkesin pozisyonu zihnine kazınmıştı."""
                } else {
                    """Zihnini geri çektin. Görü sahneleri önünden soğuk bir film şeridi gibi aktı: Stark Tower'ın en üst katındaki cam salon, masanın etrafında toplanmış gölgeler, masanın ortasında dönen mavi hologram. Mesafeli kaldın; gücünü zorlamadın, zihnini yıpratmadın. Sadece ne görmen gerekiyorsa onu gördün.

Görü eridiğinde burnun kanamıyordu, başın dönmüyordu. Fiziksel olarak tam gücündeydin."""
                }

                intro + """

---

Toplantı odası etrafında maviye çalan bir statik içinde şekillendi, sessiz ve ödünç alınmış bir an — gerçekten orada değildin, her zamanki gibi kendi gücünün kenarında bir hayalet gibi süzülüyordun.

*"Kozmik Sürükleniş,"* dedi bir adam — koyu saçlı, keçi sakallı, odadaki en zeki kişi olduğundan bir kez bile şüphe etmemiş birinin duruşuna sahip. Tony Stark. Yüzünü yüzlerce manşetten tanıyordun. *"Dosya derinliği yok, talep yok, iletişim yok. Bu adam neden listede ki?"* Bir başkası cevap verdi — sarışın, çenesi sanki bir asker afişi için yontulmuş gibi. Steve Rogers olduğunu, yüzünü görmeden bile tahmin edebilirdin.

Kadın hologramdan gözlerini ayırmadan cevap verdi. Kızıl saçlı, omuzlarındaki durgunluk Stark'ın bütün gece söylediklerinden daha tehlikeli okunuyordu. *"Çünkü tam olarak bazen ihtiyacımız olan tip. Yalnız kurt, muazzam potansiyel. Onu davet edersek, gelebilir. Ya da kaybolabilir. Kumar bu."*

İçinde bir şey çok sessizleşti bunu duyunca. Haksız değildi. Rahatsız edici derecede haksız değildi, ve bunun seni bu kadar rahatsız etmesinden hoşlanmadın.

Sarışın olan —Thor olmalıydı— masaya doğru eğildi, gözleri senin enerji imzanın titreyen okumasına kısılmış, tanıdık bir şeyin tadına bakar gibi. *"Kozmik Sürükleniş'in enerjisi… eski Asgard gezginlerini yankılıyor. Bence önce onu arayalım. Diğerleri güçlü. Ama bu olan… farklı hissettiriyor."*

Görü kenarlardan solmaya başladı, kavrayışın her zamanki gibi iki dakika sınırından sonra inceliyordu. Yakaladığın son şey Stark'tı, masaya bakıp sırıtıyordu, bunun nasıl biteceğini şimdiden biliormuş gibi.

*"Oylama vakti. Kozmik Sürükleniş'i listenin başına koyuyorum — merak ettim."*

Sonra mavi, siyaha yerini bıraktı, ve sen çatıda geri döndün, nefesin kesik kesik, bakır tadı dilinde soluyordu.

Şu anda senin hakkında oylama yapıyorlardı.

---

Gidebilirdin. Daha azı için odalardan çıkmıştın.

Ama kızıl saçlının sesindeki bir şey göğsüne yapışıp kaldı — *gelebilir ya da kaybolabilir, kumar bu* — sanki her iki sonuçla da barışmış gibi konuşuyordu. Sanki senden korkmuyordu, ama seni sahiplenmeye de çalışmıyordu. Hayatında seninle bu şekilde konuşan tam olarak sıfır insanla karşılaşmıştın.

Kendine merak ettiğin için gittiğini söyledin. Kendine keşif için gittiğini söyledin. Neredeyse ama tam olarak değil doğru olan şekillerde kendine yalan söylemekte çok iyileşmiştin.

Işınlandın.

---

🔀 Tower'a geçişi nasıl gerçekleştireceksin?

1️⃣ Direkt ışınlan, hiç düşünmeden.
2️⃣ Önce dışarıdan gözlemle (birkaç dakika Tower'ı uzaktan izle), sonra ışınlan.
3️⃣ Geri çekil, gitme — sadece izlemeye devam et."""
            }
            2 -> {
                // Choice 2 Response
                val intro = if (lastUserMsg.contains("3") || lastUserMsg.contains("Geri çekil") || lastUserMsg.contains("gitme")) {
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
                val responseText = if (lastUserMsg.contains("2") || lastUserMsg.contains("Alaycı") || lastUserMsg.contains("esprili") || lastUserMsg.contains("masraflı")) {
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
                """# 📖 BÖLÜM 2: ŞARTLAR

Kahve geldi, sonunda — Stark'ın emrettiği, birinin utana sıkıla önüne bıraktığı kahve. İçmedin. Fincanı elinle sarıp ısısını hissetmekle yetindin, alışkanlık, hiçbir şeyi kabul etmeden önce onun ne olduğunu anlamaya çalışan bir alışkanlık. Zehir mi, ilaç mı, sadece kahve mi — büyük ihtimalle sadece kahveydi, ama "büyük ihtimalle" son on beş yıldır senin için yeterli bir güvence olmadı hiç.

Masaya oturdun sonunda. Kendi kararınla, kimsenin ısrarı yüzünden değil — bu ayrımı kendine net bir şekilde belirttin, sanki oturmak bir teslimiyet değil de bir gözlem noktası değiştirmekmiş gibi.

*"Tamam,"* dedi Stark, ellerini masaya koyup öne eğilerek. *"Konuşalım gerçekçi olarak. Sen kimsin, ne istiyorsun, biz ne teklif ediyoruz — hepsini masaya yatıralım, sonra sen ışınlanıp gidersen bari zaman kaybetmemiş oluruz."*

*"Adil,"* dedin. Sesin hâlâ düzdü ama bir şey —çok hafif, neredeyse fark edilmez— gevşemişti içinde. Belki masaya oturmak gerçekten bir şeyi değiştirmişti.

Steve öne çıktı, kollarını masaya koyarak, sesindeki komuta tonunu bilerek yumuşatarak. *"S.H.I.E.L.D. seni sekiz aydır arıyor. Biz bunu biliyoruz çünkü onlar bizi de arıyorlar zaman zaman, işbirliği yapalım diye. Sana teklifimiz basit: Avengers'a katılırsan, senin için açılan her dosya kapanır. Hükümetlerin peşinden gelmesi durur. Resmi bir statün olur — silahlı bir kaçak değil, tanınan bir müttefik."*

*"Ve karşılığında?"* dedin, sesin hâlâ nötr ama gözlerin ondan ayrılmadı.

*"Karşılığında kurallara uyarsın,"* dedi Steve, dürüstçe, süslemeden. *"Sivillere zarar yok. Emir zinciri var, tam bir ordu değil ama tam bir anarşi de değil. Ve şeffaflık — gücünün ne olduğunu, sınırlarının ne olduğunu bilmemiz gerekiyor. Güven iki taraflı işler."*

Güven kelimesi göğsünde tanıdık bir şekilde sıkıştı — hafif, otomatik bir savunma refleksi, yıllar içinde o kadar derine işlemiş ki artık düşünmeden tepki veriyordu. *"Güven,"* dedin, kelimeyi neredeyse tadına bakar gibi tekrarlayarak. *"İlginç kelime, hiç tanımadığınız birine söylemek için."*

*"Bu yüzden buradayız,"* dedi kızıl saçlı kadın — sonunda konuşuyordu, sesi masadaki herkesten daha sakin, daha az ikna etmeye çalışan bir ton taşıyordu. *"Tanımıyoruz. Ama tanışmak isteriz. Fark bu."*

Ona baktın biraz daha uzun süreyle normalden. *"İsmin?"*

*"Natasha,"* dedi. *"Natasha Romanoff."*

*"Romanoff,"* dedin, dosyalarda gördüğün bir isimdi ama hiç yüz yüze gelmemiştin. *"Kızıl Oda. Eski KGB. Şimdi Amerika'nın en güvenilir casusu."* Ağzının kenarında hafif, neredeyse görünmeyen bir kıvrım oluştu. *"İnsan değişebiliyormuş demek."*

Bu, odada gözle görülür bir tepki yarattı — Steve'in çenesi hafifçe gerildi, Stark'ın kaşları kalktı, ama Natasha'nın yüzünde hiçbir şey değişmedi, sadece gözlerinde bir şey parladı, neredeyse eğlenmiş gibi. *"Dosyaların iyiymiş,"* dedi. *"Ama eksik. Herkesinki eksik."*

*"Benimki de eksik olsun o zaman,"* dedin. *"Adil olur."*

---

Sohbet bir süre böyle devam etti — soru, yarım cevap, karşı soru, senin verdiğin her parça karşılığında bir parça geri aldığın eski bir alışkanlıkla. Ama bir noktada Stark, sabırsızlığını daha fazla saklayamadı.

*"Tamam, teoriler güzel,"* dedi, elini masaya hafifçe vurarak. *"Ama ben mühendisim. Sayı severim. 'Kozmik enerji imzası' diyorsunuz, ben ne demek istediğinizi anlamıyorum. Bize bir şey göster."*

Odadaki hava bir anda değişti. Thor'un yüzündeki merak daha da belirginleşti, Steve'in eli hafifçe geriye çekildi —yine o eski refleks, kalkanına doğru— ve Natasha, tamamen hareketsiz kalarak izlemeye devam etti.

*"Ne görmek istiyorsun,"* dedin, ses tonun tehdit değildi ama bir uyarıydı, sakin ve düz. *"Uyarayım — 'göstermek' burada tehlikesiz bir kelime değil."*

*"Kontrollü bir şey,"* dedi Steve hızlıca, Stark'a bir bakış fırlatarak. *"Küçük ölçekli. Yeter ki gerçek olduğunu görelim."*

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
                """# 📖 BÖLÜM 3: DENEME SÜRESİ

Uyumadın. Beklemiyordun zaten uyumayı — ama yine de sabahın erken saatlerinde, gökyüzü hâlâ o belirsiz gri-maviye dönmemişken, pencerenin önünden ayrılıp odanın içinde volta atmaya başladığını fark ettiğinde, vücudunun ne kadar yorgun olduğunu hatırladın. Görüyle harcadığın enerji, ışınlanmanın bedeli — hepsi birikmiş, omuzlarına yük gibi oturmuştu. Yorgunluk, senin türün için garip bir kavramdı; kas yorgunluğu değildi bu, daha çok bir pilin yavaşça boşalması gibiydi, ve dolması da normal bir uykudan çok daha uzun sürerdi.

Kapı çalındı. Sert değil, aceleci değil — iki kere, kısa aralıklarla.

Kapıyı açtığında Steve'i buldun, elinde bir tablet, üstünde dün geceki üniformasından farklı, gündelik bir tişört. *"Erken kalkmışsın,"* dedi, seni süzerek. *"Ya da hiç yatmamışsın."*

*"İkinci seçenek,"* dedin, kapıyı tamamen açmadan.

*"Anlıyorum,"* dedi, ve gerçekten anladığını hissettirecek kadar basit söyledi bunu — acımadan, yargılamadan. *"Bak, tam bir cevap istemiyoruz senden bugün. Ama eğer kalmayı düşünüyorsan — resmi olarak değil, sadece bir süreliğine, görelim nasıl gidiyor diye — sana bir teklifim var. Deneme süresi. Otuz gün. Bu süre boyunca Tower'da kalırsın, ekiple biraz zaman geçirirsin, belki küçük bir görev bile alırsın. Sonunda karar sende. İstersen gidersin, kimse peşinden koşmaz."*

*"Otuz gün,"* diye tekrarladın, kelimenin ağırlığını tartarak. On beş yıldır hiçbir yerde otuz saat bile kalmamıştın, bir kaçış rotası hazır tutmadan.

*"Otuz gün,"* diye onayladı Steve. *"Ve eğer o süre içinde bir S.H.I.E.L.D. ekibi ya da başka biri seni almaya kalkarsa — bu artık bizim sorunumuz olur, senin değil."*

Bu son cümle, düşündüğünden daha fazla ağırlık taşıyordu. Yirmi yıldır ilk kez, birisi senin yükünü paylaşmayı teklif ediyordu, karşılığında hemen bir şey istemeden.

---

🔀 Steve'in Otuz Günlük Deneme Süresi teklifine nasıl cevap vereceksin?

1️⃣ Şartlı kabul — kendi çıkış kurallarını belirt.
2️⃣ Tereddütsüz kabul et.
3️⃣ Çok daha kısa bir süre öner ("birkaç gün")."""
            }
            12 -> {
                val c1Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Tereddütsüz") || lastUserMsg.contains("kabul")) {
                    """"Tamam," dedin, sesin kendi kulağına bile tereddütsüz çıktı. "Otuz gün. Görelim."
Steve'in yüzünde gerçek bir memnuniyet belirdi."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("kısa") || lastUserMsg.contains("birkaç gün")) {
                    """"Otuz gün çok uzun," dedin mesafeli bir tonla. "Birkaç gün diyelim. Sonrasına bakarız."
Steve anlayışla başını salladı."""
                } else {
                    """"Tamam," dedin, sesin kendi kulağına bile biraz garip geldi, bu kadar kolay çıktığı için. "Otuz gün. Ama kendi şartlarımla."
Steve'in yüzünde hafif, gerçek bir gülümseme belirdi."""
                }

                c1Text + """

*"İyi. O zaman seni ekiple tanıştıralım, resmi olarak. Ve —"* tabletine baktı, *"— birkaç şey netleştirmemiz gerekiyor. Yeteneklerinin sınırlarını bilmemiz lazım, sadece merak değil, güvenlik için. Antrenman salonunda bir değerlendirme yapabilir miyiz?"*

İçinde bir şey gerildi otomatik olarak — *değerlendirme*, *test*, bu kelimeler sende hep aynı tepkiyi uyandırırdı, laboratuvarları, dosyaları, seni bir "vaka" olarak gören gözleri hatırlatarak. Ama Steve'in ses tonunda o eski soğukluk yoktu.

*"Kontrollü olacak,"* diye ekledi Steve, senin tereddüdünü fark ederek. *"Sen ne kadar göstermek istersen o kadar. Zorlamıyoruz."*

---

🔀 Antrenman salonundaki değerlendirmeyi nasıl kabul edeceksin?

1️⃣ Temkinli kabul et. ("Görelim... ama sınırlarımı ben belirlerim.")
2️⃣ İstekli/meraklı kabul et. ("Ekibin sınırlarını ve senin dövüş stilini merak ediyorum.")
3️⃣ Reddet, sadece sözlü açıklamayla yetin."""
            }
            13 -> {
                val c2Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("İstekli") || lastUserMsg.contains("merak")) {
                    """"Görelim," dedin istekli bir ifadeyle. "Kaptan Amerika'nın dövüş stilini merak ediyordum zaten."
Steve hafifçe sırıttı."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("Reddet") || lastUserMsg.contains("sözlü")) {
                    """"Fiziksel teste gerek yok, teorik anlatırım," dedin. Ancak Steve gülümseyerek antrenman salonuna davet etti."""
                } else {
                    """"Görelim," dedin sonunda temkinli bir tonda."""
                }

                c2Text + """

---

## ANTRENMAN SALONU

Antrenman salonu, beklediğinden daha büyüktü — güçlendirilmiş duvarlar, enerji emici paneller, tavanda gözlemleyen kameralar, ve bir köşede, hiç kullanılmamış gibi duran bir dizi antika görünümlü silah — muhtemelen Thor'a ait.

Steve, Natasha ve şaşırtıcı bir şekilde Wanda da oradaydı, kenarda duruyor, kollarını kavuşturmuş, ilgiyle izliyordu. Bir de, tanımadığın biri daha vardı — uzun boylu, gözlüklü, elinde bir tablet.

*"Bruce,"* diye tanıttı kendini adam. *"Bruce Banner. Sadece izliyorum."*

*"Basit başlayalım,"* dedi Steve, salonun ortasına doğru yürürken. *"Fiziksel güç, hız, dayanıklılık."*

Steve, kendi eldivenlerini takarken hafifçe gülümsedi. *"Benimle. Kaptan Amerika'ya karşı bir tur."*

---

🔀 Steve Rogers'a karşı dövüşte gücünü nasıl ayarlayacaksın?

1️⃣ Tam geri tut. (Kontrollü, Steve'e zarar vermeden hızını ve iyileşmeni göster)
2️⃣ Biraz daha zorla ama yenme. (Sınırlarını zorla, Steve'in savunmasını sına)
3️⃣ Hiç geri tutma, kazanmaya çalış."""
            }
            14 -> {
                val c3Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("Hiç geri tutma") || lastUserMsg.contains("kazanmaya")) {
                    """Geri tutmadın. Kırmızı Göz'ünün sarsıcı ivmesiyle Steve'in kalkanını kenara savurdun ve saniyeden kısa sürede hamleni tamamladın. Bruce dehşetle tabletine notlar aldı, Steve ise doğrulurken takdirle başını salladı."""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("zorla")) {
                    """Biraz daha zorladın, reflekslerini ve hızını tam sınıra çıkardın. Steve kalkanıyla darbeni güçlükle göğüsledi. Natasha kenardan hafifçe ıslık çaldı."""
                } else {
                    """Kavga beklediğinden farklı geçti. Steve hızlıydı ama sen çok daha hızlıydın, bunu ona acı vermeden gösterdin. Steve'in kalkanı bir keresinde seni tam isabetle yakaladı, ama iyileşme anında başladı.

*"Ciddi yaralar dakikalar sürer,"* dedin. *"Ölümcül olmayan her şey saniyeler."*
Natasha'dan kısa, gerçek bir kahkaha çıktı."""
                }

                c3Text + """

Steve devam etti. *"Enerji tarafı. Sadece görmek için — küçük ölçekte, güvenlik protokolleriyle."* Duvardaki panellere işaret etti. *"Bu paneller enerji emiyor, seni ya da odayı riske atmadan gücünü ölçebiliriz."*

---

🔀 Enerji testinde panellere ne seviyede güç uygulayacaksın?

1️⃣ Orta seviye göster (kontrollü, hedefli enerji dalgası).
2️⃣ Minimum göster (sadece hafif bir kıvılcım dalgası).
3️⃣ Neredeyse tam kapasiteye yakın göster (güçlü bir patlama)."""
            }
            15 -> {
                val c4Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("Minimum")) {
                    """Elini kaldırdın ve panellere sadece minik bir mavi kıvılcım dalgası fırlattın. *"Fazlasına gerek yok,"* dedin gizemini koruyarak."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("tam kapasite")) {
                    """Elini kaldırdın ve panelleri sarsan devasa bir enerji patlaması gönderdin! Paneller alarm verdi, Stark'ın tabletinde sayılar tavan yaptı. *"Muazzam bir çıktı!"* diye bağırdı Stark."""
                } else {
                    """Elini kaldırdın, ve bu sefer sadece bir ışık küresi değil, gerçek bir enerji dalgası gönderdin. Panel titredi, gösterge ekranındaki rakamlar hızla yükseldi.

*"Bu daha düşük seviyem,"* dedin sakin bir şekilde. Wanda ekrana bakarak *"Bu ölçüm bina seviyesinden fazla gösteriyor,"* dedi. Bruce, *"Enerji imzan tanıdık geliyor, kendi kategorin gibi,"* diye ekledi."""
                }

                c4Text + """

---

## ARADA BİR MOLA

Değerlendirme bittikten sonra, Natasha yanına geldi. *"İyiydin,"* dedi. *"Steve'i yenmedin ama yenebilirdin. Güç, ne zaman kullanılmayacağını bilmekle anlamlı hale gelir."*

Öğlen yemeğinde, ortak kafeteryada Sam Wilson senin hakkında iki şaka yaptı, ikisi de zararsızdı.

---

🔀 Öğle yemeğinde Sam Wilson'ın şakasına nasıl tepki vereceksin?

1️⃣ Kuru bir espriyle karşılık ver. (Masa kahkahaya boğulsun)
2️⃣ Sessiz kal, sadece hafifçe gülümse.
3️⃣ Şakayı ciddiye al, savunmaya geç."""
            }
            16 -> {
                val c5Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("ciddiye") || lastUserMsg.contains("savunma")) {
                    """Şakayı ciddiye alıp soğuk bir bakış attın. Masada bir anlık sessizlik oldu ama Sam gülerek ortamı yumuşattı."""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("Sessiz")) {
                    """Sessiz kalıp sadece hafifçe gülümsedin. Mesafeli duruşun masadakilerce saygıyla karşılandı."""
                } else {
                    """Sen —beklemediğin bir şekilde— birine karşılık verdin, kuru bir tonda, ve masa kahkahaya boğuldu. Bir masada oturmak yıllardır tatmadığın bir histi."""
                }

                c5Text + """

---

## ALARM

Öğleden sonra, Tower'da alarmlar çaldı. Stark'ın sesi interkomdan geldi: *"Millet, S.H.I.E.L.D.'den bir ekip Tower'ın dışında ışınlanma imzanı takip ediyor."*

İçinde bir şey sıkıştı — *bulundun, koş* refleksi. Parmak uçlarında mavi kıvılcımlar belirdi.
Steve, *"Kaçmana gerek yok, bu bizim sorunumuz,"* dedi.
Natasha sert bir netlikle ekledi: *"Kaçarsan haklı olduklarını kanıtlarsın. Kal."*

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

                val outcomeText = if (isBigEscape) {
                    """Onları dinlemedin. Yirmi yıllık alışkanlık kelimelerden daha hızlı hareket etti.
Mavi ışık seni sardı ve kendini bambaşka bir şehrin karanlık, ıslak sokağında buldun. Geriye baktın, Tower çoktan kıtalar ötesindeydi.

---

ÜÇ GÜN SONRA

Üç gün geçti. Tanımadığın şehirlerde, yarı yıkık binalarda yalnız kaldın. Dördüncü gece terk edilmiş bir depoda otururken ozon kokusu yayıldı. Thor geldi!

*"Seni bulmak zor oldu,"* dedi Thor. *"Steve kırıldı, Wanda ise senin geri döneceğine inandı. Önemli olan kaçtıktan sonra geri dönüp dönmediğin."*

Thor'la birlikte Tower'a geri döndün. Steve seni mesafeli karşıladı. Akşam olduğunda çatıya çıktın."""
                } else if (isShortEscape) {
                    """Natasha'nın sözleri kulağına ulaştı ama yirmi yıllık refleks daha hızlı hareket etti. Mavi ışıkla Tower'ın çatısına ışınlandın!

Aşağıda S.H.I.E.L.D. araçlarını izledin. İki dakika sonra Steve'in ekibi ikna ettiğini gördün. Utanç ve kararlılıkla adımlayarak aşağı indin.

Wanda seni görünce gülümsedi: *"Geri döndün. Bu da bir seçim."*
Akşam olduğunda çatıya çıktın."""
                } else {
                    """Bir çelişki içindeydin — ama bu sefer farklı bir şey denemeye karar verdin. Elindeki mavi kıvılcımı söndürdün. Kaldın.

Steve ve Natasha aşağı inip S.H.I.E.L.D. ekibini geri çevirdi. Wanda ile pencereden izlediniz. Wanda, *"Bazen kalmak kaçmaktan daha cesurca,"* dedi.

Akşam olduğunda çatıya çıktın."""
                }

                outcomeText + """

---

## RECONVERGENCE — Çatı Sahnesi

New York'un gece manzarası seni tuhaf bir şekilde sakinleştiriyordu. Natasha seni orada buldu, kenara oturdu.

---

🔀 Çatıda Natasha seni bulduğunda ona karşı dürüstlüğün ne seviyede olacak?

1️⃣ Tam dürüst cevap ver. ("Korkutucu... Kalmak, kaybedecek bir şey biriktirmek demek.")
2️⃣ Yarım dürüst — şakayla hafiflet. ("Manzara güzel Romanoff.")
3️⃣ Kapan, yüzeysel cevap ver. ("Sadece hava alıyordum.")"""
            }
            18 -> {
                val c7Text = if (lastUserMsg.contains("3") || lastUserMsg.contains("Kapan")) {
                    """"Sadece hava alıyordum Romanoff," dedin mesafeli bir tonda. Natasha başını salladı, zorlamadı."""
                } else if (lastUserMsg.contains("2") || lastUserMsg.contains("Yarım")) {
                    """"Manzara güzel Romanoff, kaçmıyorum," dedin hafif bir gülümsemeyle."""
                } else {
                    """"Korkutucu," dedin dürüstçe. "Kaçmak kolaydır. Ama kalmak... kaybedecek bir şey biriktirmek demek."
Natasha *"Ben de Kızıl Oda'dan sonra bunu öğrendim. Bağlar güç olabilir,"* dedi."""
                }

                c7Text + """

*"Otuz günün var,"* dedi Natasha ayağa kalkarken. *"Gör bakalım neye benziyor kalmak. İyi geceler Blackwood."*

O gittikten sonra çatıda tek başına kaldın. Cebinden ailenden kalma tek şeyi, gümüş kolye ucunu çıkardın.

---

🔀 Kolye ile baş başa kaldığında zihnindeki iç ses nasıl bir kararla şekillenecek?

1️⃣ Kolyeyi elinde tutup umutla düşün. (Belki bu sefer bir şeyleri kaybetmeden tutabilirsin)
2️⃣ Kolyeyi hızla cebine geri koy, düşünceyi bastır. (Duygusal zayıflığa izin verme)
3️⃣ Kolyeye uzun uzun bak, geçmişe dair kısa bir anı/flashback zihninde canlansın."""
            }
            19 -> {
                val c8Text = if (lastUserMsg.contains("2") || lastUserMsg.contains("cebine") || lastUserMsg.contains("bastır")) {
                    """Kolyeyi hızla cebine geri koydun ve duygusal düşünceleri bastırdın. Soğukkanlı zırhını korumaya karar verdin.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood 30 günlük deneme süresine adım attı, duygularını kontrol altında tutarak geceyi tamamladı."""
                } else if (lastUserMsg.contains("3") || lastUserMsg.contains("flashback") || lastUserMsg.contains("bak")) {
                    """Kolyeye bakarken 20 yıl önceki çocukluk evin ve patlama anı gözlerinin önünden geçti. Geçmişin yüküyle kulede yeni bir sayfa açtın.

---

📖 BÖLÜM 3 SONU — DENEME SÜRESİ

Aiden Blackwood geçmişinin anılarıyla yüzleşti ve Avengers Kulesi'nde 30 günlük deneme süresine adım attı."""
                } else {
                    """Belki, dedin kendine, belki bu sefer bir şeyleri kaybetmeden tutabilirsin.

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
                        return callGeminiApi(k, model, systemPrompt, messages)
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
