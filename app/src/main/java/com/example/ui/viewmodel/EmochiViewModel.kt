package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import org.json.JSONObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import java.util.UUID

sealed class UiState {
    object Loading : UiState()
    object Menu : UiState()
    object SetupWizard : UiState()
    data class Chat(val botId: String) : UiState()
}

class EmochiViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = EmochiRepository(db, application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Menu)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.deleteEmptyMessages()
            } catch (_: Exception) {}
        }
    }

    val botList: StateFlow<List<BotEntity>> = repository.allBots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val providerFallbackLog = repository.providerFallbackLog

    suspend fun testLlm7Connection(): EmochiRepository.ProviderTestResult {
        return repository.testLlm7Connection()
    }

    suspend fun testPollinationsConnection(modelName: String = "openai"): EmochiRepository.ProviderTestResult {
        return repository.testPollinationsConnection(modelName)
    }

    suspend fun fetchNvidiaModels(apiKey: String): List<String> {
        return repository.fetchNvidiaModels(apiKey)
    }

    suspend fun simulateNvidiaRateLimitTest(apiKey: String, modelName: String = ""): EmochiRepository.ProviderTestResult {
        return repository.simulateNvidiaRateLimitTest(apiKey, modelName)
    }

    private val _activeBotId = MutableStateFlow<String?>(null)
    val activeBotId: StateFlow<String?> = _activeBotId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBot: StateFlow<BotEntity?> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getBotFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeCharacterEmotions: StateFlow<List<CharacterEmotionEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getCharacterEmotionsFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAffectionEvents: StateFlow<List<com.example.data.local.AffectionEventEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAffectionEventsFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val customProviders: StateFlow<List<com.example.data.local.CustomProviderEntity>> = db.customProviderDao().getAllProvidersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    suspend fun testCustomProviderConnection(
        baseUrl: String,
        apiKey: String,
        modelName: String,
        apiFormat: String
    ): EmochiRepository.ProviderTestResult {
        return repository.testCustomProviderConnection(baseUrl, apiKey, modelName, apiFormat)
    }

    fun saveCustomProvider(
        label: String,
        baseUrl: String,
        apiKey: String,
        modelName: String,
        apiFormat: String,
        supportsFunctionCalling: Boolean
    ) {
        viewModelScope.launch {
            val encryptedKey = com.example.util.KeystoreEncryptionManager.encrypt(apiKey.trim())
            val entity = com.example.data.local.CustomProviderEntity(
                label = label.ifBlank { modelName },
                baseUrl = baseUrl.trim(),
                apiKeyEncrypted = encryptedKey,
                modelName = modelName.trim(),
                apiFormat = apiFormat,
                supportsFunctionCalling = supportsFunctionCalling
            )
            val newId = db.customProviderDao().insertProvider(entity)
            val currentSettings = repository.getOrCreateSettings()
            updateSettings(currentSettings.copy(selectedProvider = "custom_$newId", selectedModel = modelName.trim()))
        }
    }

    fun updateCustomProviderModel(id: Long, newModelName: String) {
        viewModelScope.launch {
            val existing = db.customProviderDao().getProviderById(id) ?: return@launch
            val updated = existing.copy(modelName = newModelName.trim())
            db.customProviderDao().updateProvider(updated)
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedModel = newModelName.trim()))
            }
        }
    }

    fun updateCustomProviderFull(
        id: Long,
        label: String,
        baseUrl: String,
        apiKey: String?,
        modelName: String,
        apiFormat: String,
        supportsFunctionCalling: Boolean
    ) {
        viewModelScope.launch {
            val existing = db.customProviderDao().getProviderById(id) ?: return@launch
            val finalEncryptedKey = if (!apiKey.isNullOrBlank()) {
                com.example.util.KeystoreEncryptionManager.encrypt(apiKey.trim())
            } else {
                existing.apiKeyEncrypted
            }
            val updated = existing.copy(
                label = label.ifBlank { modelName },
                baseUrl = baseUrl.trim(),
                apiKeyEncrypted = finalEncryptedKey,
                modelName = modelName.trim(),
                apiFormat = apiFormat,
                supportsFunctionCalling = supportsFunctionCalling
            )
            db.customProviderDao().updateProvider(updated)
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedModel = modelName.trim()))
            }
        }
    }

    fun deleteCustomProvider(id: Long) {
        viewModelScope.launch {
            db.customProviderDao().deleteProvider(id)
            val currentSettings = repository.getOrCreateSettings()
            if (currentSettings.selectedProvider == "custom_$id") {
                updateSettings(currentSettings.copy(selectedProvider = "gemini"))
            }
        }
    }

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        viewModelScope.launch {
            repository.getOrCreateSettings()
            repository.initStarterBotsIfEmpty()
        }
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                    val result = tts?.setLanguage(Locale.forLanguageTag("tr-TR"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speakText(text: String, speed: Float? = null, pitch: Float? = null) {
        val settings = userSettings.value
        val effSpeed = speed ?: settings?.ttsSpeed ?: 1.0f
        val effPitch = pitch ?: settings?.ttsPitch ?: 1.0f
        tts?.setSpeechRate(effSpeed)
        tts?.setPitch(effPitch)

        val voiceName = settings?.selectedVoiceName ?: ""
        if (voiceName.isNotBlank()) {
            try {
                tts?.voices?.firstOrNull { it.name == voiceName }?.let { voice ->
                    tts?.voice = voice
                }
            } catch (_: Exception) {}
        }

        val cleanText = text.replace(Regex("\\*.*?\\*"), "").trim()
        if (cleanText.isNotBlank()) {
            _isSpeaking.value = true
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "EmochiTTS")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeaking()
        tts?.shutdown()
    }

    fun openMenu() {
        stopSpeaking()
        _errorMessage.value = null
        _activeBotId.value = null
        EmochiRepository.activeBotId = null
        _uiState.value = UiState.Menu
    }

    fun startWizard() {
        stopSpeaking()
        _errorMessage.value = null
        _uiState.value = UiState.SetupWizard
    }

    fun openBot(botId: String) {
        stopSpeaking()
        _errorMessage.value = null
        if (_activeBotId.value != botId) {
            _activeBotId.value = botId
        }
        EmochiRepository.activeBotId = botId
        _uiState.value = UiState.Chat(botId)

        viewModelScope.launch {
            ensureOpeningMessage(botId)
        }
    }

    fun ensureOpeningMessageForActiveBot() {
        val botId = _activeBotId.value ?: return
        viewModelScope.launch {
            ensureOpeningMessage(botId)
        }
    }

    private val openingMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun ensureOpeningMessage(botId: String) {
        openingMutex.withLock {
            var bot = repository.getBot(botId)
            var retryCount = 0
            while (bot == null && retryCount < 5) {
                kotlinx.coroutines.delay(100)
                bot = repository.getBot(botId)
                retryCount++
            }

            if (bot != null) {
                val existingMsgs = repository.getMessageListForBot(botId)
                if (existingMsgs.isEmpty()) {
                    val openingText = bot.openingMessage.ifBlank {
                        if (bot.mode == "universe") {
                            "*Sahne başlar. Çevre sakin ve atmosferik bir havaya bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
                        } else {
                            "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
                        }
                    }
                    val openingMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = bot.id,
                        role = "assistant",
                        text = openingText,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveMessage(openingMsg)
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createBotFromWizard(bot: BotEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                repository.saveBot(bot)

                val openingText = bot.openingMessage.ifBlank {
                    if (bot.mode == "universe") {
                        "*Sahne başlar. Çevre sakin ve atmosferik bir hava bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
                    } else {
                        "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
                    }
                }

                val openingMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot.id,
                    role = "assistant",
                    text = openingText,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(openingMsg)

                _activeBotId.value = bot.id
                EmochiRepository.activeBotId = bot.id
                _uiState.value = UiState.Chat(bot.id)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "Bot oluşturulamadı: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    private val sendMutex = kotlinx.coroutines.sync.Mutex()

    fun sendMessage(text: String) {
        val botId = _activeBotId.value ?: return
        val sanitizedText = repository.sanitizeUserInput(text)
        if (sanitizedText.isBlank() || _isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            if (!sendMutex.tryLock()) {
                _isSending.value = false
                return@launch
            }
            val currentBot = activeBot.value ?: repository.getBot(botId) ?: run {
                sendMutex.unlock()
                _isSending.value = false
                return@launch
            }
            try {
                _errorMessage.value = null
                repository.resetRegenerateCount(botId)

                val now = System.currentTimeMillis()
                val userMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "user",
                    text = sanitizedText,
                    timestamp = now
                )
                repository.saveMessage(userMsg)

                val currentMsgs = repository.getMessageListForBot(botId)

                val replyResult = repository.generateAiReply(currentBot, currentMsgs)
                val replyText = replyResult.replyText
                if (replyText.isBlank()) {
                    throw IllegalStateException("Model boş yanıt döndürdü.")
                }

                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = (now + 10L).coerceAtLeast(System.currentTimeMillis()),
                    provider = replyResult.usedProvider
                )
                repository.saveMessage(aiMsg)

                repository.saveBot(currentBot.copy(updatedAt = System.currentTimeMillis()))

                val finalMsgs = currentMsgs + aiMsg
                if (finalMsgs.size % 6 == 0) {
                    repository.updateMemorySummaries(currentBot, finalMsgs)
                }
            } catch (e: Exception) {
                val now = System.currentTimeMillis()
                val friendlyMsg = extractUserFriendlyErrorMessage(e)
                val failedAiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = "⚠️ $friendlyMsg",
                    timestamp = (now + 10L).coerceAtLeast(System.currentTimeMillis()),
                    status = "failed"
                )
                repository.saveMessage(failedAiMsg)
                _errorMessage.value = friendlyMsg
            } finally {
                if (sendMutex.isLocked) {
                    sendMutex.unlock()
                }
                _isSending.value = false
            }
        }
    }

    fun retryMessage(msgId: String) {
        val botId = _activeBotId.value ?: return
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            if (!sendMutex.tryLock()) {
                _isSending.value = false
                return@launch
            }
            val currentBot = activeBot.value ?: repository.getBot(botId) ?: run {
                sendMutex.unlock()
                _isSending.value = false
                return@launch
            }
            try {
                _errorMessage.value = null
                // Delete failed message entity
                repository.deleteMessage(msgId)

                val currentMsgs = repository.getMessageListForBot(botId)
                val replyResult = repository.generateAiReply(currentBot, currentMsgs)
                val replyText = replyResult.replyText
                if (replyText.isBlank()) {
                    throw IllegalStateException("Model boş yanıt döndürdü.")
                }

                val now = System.currentTimeMillis()
                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = now,
                    status = "success",
                    provider = replyResult.usedProvider
                )
                repository.saveMessage(aiMsg)
                repository.saveBot(currentBot.copy(updatedAt = now))

                val finalMsgs = currentMsgs + aiMsg
                if (finalMsgs.size % 6 == 0) {
                    repository.updateMemorySummaries(currentBot, finalMsgs)
                }
            } catch (e: Exception) {
                val now = System.currentTimeMillis()
                val friendlyMsg = extractUserFriendlyErrorMessage(e)
                val failedAiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = "⚠️ $friendlyMsg",
                    timestamp = now,
                    status = "failed"
                )
                repository.saveMessage(failedAiMsg)
                _errorMessage.value = friendlyMsg
            } finally {
                if (sendMutex.isLocked) {
                    sendMutex.unlock()
                }
                _isSending.value = false
            }
        }
    }

    fun regenerateLastResponse() {
        val botId = _activeBotId.value ?: return
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            if (!sendMutex.tryLock()) {
                _isSending.value = false
                return@launch
            }
            val currentBot = activeBot.value ?: repository.getBot(botId) ?: run {
                sendMutex.unlock()
                _isSending.value = false
                return@launch
            }
            try {
                _errorMessage.value = null

                val msgs = repository.getMessageListForBot(botId)
                if (msgs.isEmpty()) {
                    return@launch
                }

                val lastIsUser = msgs.lastOrNull()?.role == "user"
                val (remainingMsgs, targetMsg) = if (lastIsUser) {
                    Pair(msgs, null)
                } else {
                    val lastAssistantIndex = msgs.indexOfLast { it.role == "assistant" }
                    if (lastAssistantIndex == -1) Pair(msgs, null)
                    else Pair(msgs.subList(0, lastAssistantIndex), msgs[lastAssistantIndex])
                }

                val botToUse = if (currentBot.previousEmotionState.isNotBlank()) {
                    val reverted = currentBot.copy(emotionState = currentBot.previousEmotionState)
                    repository.saveBot(reverted)
                    reverted
                } else currentBot

                // Generate first before deleting targetMsg to prevent wiping message on network error
                repository.incrementRegenerateCount(botId)
                val replyResult = repository.generateAiReply(botToUse, remainingMsgs)
                val replyText = replyResult.replyText
                if (replyText.isBlank()) {
                    throw IllegalStateException("Model boş yanıt döndürdü.")
                }

                if (targetMsg != null) {
                    repository.deleteMessage(targetMsg.id)
                }

                val newAiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = System.currentTimeMillis(),
                    provider = replyResult.usedProvider
                )
                repository.saveMessage(newAiMsg)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Yeniden oluşturulamadı."
            } finally {
                if (sendMutex.isLocked) {
                    sendMutex.unlock()
                }
                _isSending.value = false
            }
        }
    }

    fun editMessage(msgId: String, newText: String) {
        val botId = _activeBotId.value ?: return
        val trimmedNewText = newText.trim()
        if (trimmedNewText.isBlank() || _isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            if (!sendMutex.tryLock()) {
                _isSending.value = false
                return@launch
            }
            val currentBot = activeBot.value ?: repository.getBot(botId) ?: run {
                sendMutex.unlock()
                _isSending.value = false
                return@launch
            }
            try {
                _errorMessage.value = null
                val msgs = repository.getMessageListForBot(botId)
                val idx = msgs.indexOfFirst { it.id == msgId }
                if (idx == -1) return@launch

                val isUserMsg = msgs[idx].role == "user"
                val editedMsg = msgs[idx].copy(text = trimmedNewText, timestamp = System.currentTimeMillis())

                if (isUserMsg) {
                    val botToUse = if (currentBot.previousEmotionState.isNotBlank()) {
                        val reverted = currentBot.copy(emotionState = currentBot.previousEmotionState)
                        repository.saveBot(reverted)
                        reverted
                    } else currentBot

                    val truncatedList = msgs.subList(0, idx) + editedMsg
                    val replyResult = repository.generateAiReply(botToUse, truncatedList)
                    val replyText = replyResult.replyText
                    if (replyText.isBlank()) {
                        throw IllegalStateException("Model boş yanıt döndürdü.")
                    }

                    // Delete old trailing messages ONLY after generation succeeds
                    for (i in (idx + 1) until msgs.size) {
                        repository.deleteMessage(msgs[i].id)
                    }
                    repository.saveMessage(editedMsg)

                    val newAiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = (editedMsg.timestamp + 10L).coerceAtLeast(System.currentTimeMillis()),
                        provider = replyResult.usedProvider
                    )
                    repository.saveMessage(newAiMsg)
                } else {
                    // Directly edit assistant message
                    repository.saveMessage(editedMsg)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Mesaj düzenlenemedi."
            } finally {
                if (sendMutex.isLocked) {
                    sendMutex.unlock()
                }
                _isSending.value = false
            }
        }
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch {
            repository.deleteMessage(msgId)
        }
    }

    fun resetChat(botId: String, fullReset: Boolean = false) {
        viewModelScope.launch {
            val currentBot = repository.getBot(botId) ?: return@launch
            repository.resetMessagesForBot(botId)
            if (fullReset) {
                repository.resetBotMemoryAndEmotion(botId)
            }

            val updatedBot = repository.getBot(botId) ?: currentBot
            val openingText = updatedBot.openingMessage.ifBlank {
                if (updatedBot.mode == "universe") {
                    "*Sahne başlar. Çevre sakin ve atmosferik bir havaya bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
                } else {
                    "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
                }
            }

            val opening = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = botId,
                role = "assistant",
                text = openingText,
                timestamp = System.currentTimeMillis()
            )
            repository.saveMessage(opening)
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            repository.deleteBot(botId)
            if (_activeBotId.value == botId) {
                openMenu()
            }
        }
    }

    suspend fun getContentFilterCountForBot(botId: String): Int {
        return repository.getContentFilterCountForBot(botId)
    }

    fun retryWithSoftenedPrompt(botId: String) {
        if (_isSending.value) return
        _isSending.value = true

        viewModelScope.launch {
            if (!sendMutex.tryLock()) {
                _isSending.value = false
                return@launch
            }
            val currentBot = activeBot.value ?: repository.getBot(botId) ?: run {
                sendMutex.unlock()
                _isSending.value = false
                return@launch
            }
            try {
                _errorMessage.value = null
                val msgs = repository.getMessageListForBot(botId)
                if (msgs.isEmpty()) return@launch

                if (msgs.last().role == "assistant" && msgs.last().status == "failed") {
                    repository.deleteMessage(msgs.last().id)
                }

                val currentMsgs = repository.getMessageListForBot(botId)
                if (currentMsgs.isEmpty() || currentMsgs.last().role != "user") return@launch

                val replyResult = repository.generateAiReply(
                    currentBot,
                    currentMsgs
                )
                val replyText = replyResult.replyText
                if (replyText.isBlank()) {
                    throw IllegalStateException("Model boş yanıt döndürdü.")
                }

                val now = System.currentTimeMillis()
                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = now,
                    status = "success",
                    provider = replyResult.usedProvider
                )
                repository.saveMessage(aiMsg)
                repository.saveBot(currentBot.copy(updatedAt = now))
            } catch (e: Exception) {
                val now = System.currentTimeMillis()
                val friendlyMsg = extractUserFriendlyErrorMessage(e)
                val failedAiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = "⚠️ $friendlyMsg",
                    timestamp = now,
                    status = "failed"
                )
                repository.saveMessage(failedAiMsg)
                _errorMessage.value = friendlyMsg
            } finally {
                if (sendMutex.isLocked) {
                    sendMutex.unlock()
                }
                _isSending.value = false
            }
        }
    }

    fun createPresetBot(preset: BotEntity) {
        viewModelScope.launch {
            val botToSave = preset.copy(
                id = UUID.randomUUID().toString(),
                isPublic = false,
                isTemplate = false,
                totalPromptTokens = 0L,
                totalCandidateTokens = 0L,
                needsSummarization = false,
                storyNotes = "",
                memoryNotes = "",
                emotionState = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}""",
                previousEmotionState = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}""",
                worldAtmosphere = """{"mood":"sakin","intensity":5,"currentEvent":""}""",
                updatedAt = System.currentTimeMillis()
            )
            repository.saveBot(botToSave)
            openBot(botToSave.id)
        }
    }

    fun updateBotProfile(updatedBot: BotEntity) {
        viewModelScope.launch {
            repository.saveBot(updatedBot)
        }
    }

    fun updateCharacterEmotion(botId: String, characterName: String, newMood: String, affection: Int, trust: Int, tension: Int, intensity: Int = 5) {
        viewModelScope.launch {
            val existing = db.characterEmotionDao().getEmotionForCharacter(botId, characterName)
            val updatedStateJson = JSONObject().apply {
                put("mood", newMood)
                put("intensity", intensity)
                put("affection", affection)
                put("trust", trust)
                put("tension", tension)
            }.toString()

            val entity = existing?.copy(emotionState = updatedStateJson)
                ?: CharacterEmotionEntity(botId = botId, characterName = characterName, emotionState = updatedStateJson)
            db.characterEmotionDao().insertOrUpdate(entity)
        }
    }

    fun updateCharacterEmotionState(botId: String, characterName: String, emotionStateJson: String) {
        viewModelScope.launch {
            val existing = db.characterEmotionDao().getEmotionForCharacter(botId, characterName)
            val entity = existing?.copy(emotionState = emotionStateJson)
                ?: CharacterEmotionEntity(botId = botId, characterName = characterName, emotionState = emotionStateJson)
            db.characterEmotionDao().insertOrUpdate(entity)
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            val sanitized = settings.copy(
                customApiKey = settings.customApiKey.trim(),
                groqApiKey = settings.groqApiKey.trim(),
                claudeApiKey = settings.claudeApiKey.trim(),
                openaiApiKey = settings.openaiApiKey.trim(),
                openRouterApiKey = settings.openRouterApiKey.trim(),
                openRouterModel = settings.openRouterModel.trim(),
                nvidiaApiKey = settings.nvidiaApiKey.trim(),
                nvidiaModel = settings.nvidiaModel.trim(),
                mistralApiKey = settings.mistralApiKey.trim(),
                mistralModel = settings.mistralModel.trim(),
                backupApiKey = settings.backupApiKey.trim(),
                ttsSpeed = settings.ttsSpeed.coerceIn(0.5f, 2.0f),
                ttsPitch = settings.ttsPitch.coerceIn(0.5f, 2.0f)
            )
            repository.updateUserSettings(sanitized)
        }
    }

    suspend fun testOpenRouterConnection(apiKey: String, modelName: String = "deepseek/deepseek-chat"): EmochiRepository.ProviderTestResult {
        return repository.testOpenRouterConnection(apiKey, modelName)
    }

    suspend fun testNvidiaConnection(apiKey: String, modelName: String = ""): EmochiRepository.ProviderTestResult {
        return repository.testNvidiaConnection(apiKey, modelName)
    }

    suspend fun testMistralConnection(apiKey: String, modelName: String = "mistral-large-latest"): EmochiRepository.ProviderTestResult {
        return repository.testMistralConnection(apiKey, modelName)
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDataSnapshot()
    }

    suspend fun importBackupJson(jsonStr: String) {
        repository.importDataSnapshot(jsonStr)
    }

    suspend fun generateOpeningForWizard(botDraft: BotEntity): String {
        return repository.generateOpeningMessage(botDraft)
    }

    private fun extractUserFriendlyErrorMessage(e: Exception): String {
        val rawMsg = e.message ?: ""
        return when {
            rawMsg.contains("içerik kısıtlaması", ignoreCase = true) ||
            rawMsg.contains("içerik filtresi", ignoreCase = true) ||
            rawMsg.contains("güvenlik filtresine", ignoreCase = true) ||
            rawMsg.contains("politikası", ignoreCase = true) ||
            rawMsg.contains("refusal", ignoreCase = true) ||
            rawMsg.contains("content_filter", ignoreCase = true) -> {
                rawMsg.ifBlank { "Seçili AI sağlayıcısı içerik kısıtlaması politikası gereği bu yanıtı süzdü. Lütfen Ayarlar -> AI Model Ayarları menüsünden farklı bir model (ör. Groq veya Gemini) seçin." }
            }
            rawMsg.contains("429", ignoreCase = true) || rawMsg.contains("kotası", ignoreCase = true) || rawMsg.contains("rate limit", ignoreCase = true) -> {
                "API kullanım kotası doldu (429 Rate Limit). Lütfen Ayarlar -> AI Model Ayarları menüsünden API Key'inizi ekleyin veya modelinizi değiştirin."
            }
            else -> if (rawMsg.isNotBlank()) rawMsg else "Yanıt alınamadı. Lütfen ağ bağlantınızı veya API ayarlarınızı kontrol edin."
        }
    }
}
