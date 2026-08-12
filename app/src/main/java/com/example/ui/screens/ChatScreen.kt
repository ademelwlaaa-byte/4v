package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.components.BotQuickProfileSheet
import com.example.ui.components.BotSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.components.TypingDots
import com.example.ui.components.customTextFieldColors
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiCoralEnd
import com.example.ui.theme.EmochiCoralStart
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import com.example.ui.theme.EmochiUserBubbleText

import androidx.compose.runtime.DisposableEffect

@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    userSettings: UserSettingsEntity?,
    isSending: Boolean,
    errorMessage: String?,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRegenerate: () -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onSaveBotProfile: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit,
    onDeleteBot: () -> Unit,
    onSpeakText: ((String) -> Unit)? = null,
    isSpeaking: Boolean = false,
    onStopSpeaking: () -> Unit = {},
    onEnsureOpeningMessage: () -> Unit = {},
    onClearError: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var showBotSettings by remember { mutableStateOf(false) }
    var showQuickProfile by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // STOP SPEAKING IMMEDIATELY WHEN EXITING CHAT
    DisposableEffect(Unit) {
        onDispose {
            onStopSpeaking()
        }
    }

    // Automatically ensure opening message exists if list is empty
    LaunchedEffect(bot.id, messages.isEmpty()) {
        if (messages.isEmpty() && !isSending) {
            onEnsureOpeningMessage()
        }
    }

    // Scroll to bottom on new messages, typing state change, or keyboard open
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(messages.size, isSending, keyboardHeight) {
        val totalItems = messages.size + if (isSending) 1 else 0
        if (totalItems > 0) {
            try {
                listState.animateScrollToItem(totalItems - 1)
            } catch (_: Exception) {}
        }
    }

    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }

    // Mood detection based on last AI text
    val lastAiText = messages.lastOrNull { it.role == "assistant" }?.text ?: ""
    val detectedMood = remember(lastAiText) {
        val lower = lastAiText.lowercase()
        when {
            listOf("harika", "süper", "mutlu", "güldü", "🎉", "😊").any { lower.contains(it) } -> "joy"
            listOf("canım", "değerlisin", "sarıl", "güzelim", "sevgi", "💕", "❤️").any { lower.contains(it) } -> "warm"
            listOf("üzgün", "üzülüyorum", "kötü", "yalnız", "😢").any { lower.contains(it) } -> "sad"
            listOf("tehlike", "sinirli", "öfke", "korktu").any { lower.contains(it) } -> "tense"
            listOf("merak", "acaba", "ilginç").any { lower.contains(it) } -> "curious"
            else -> "calm"
        }
    }

    val hue = MoodColors.getMoodHue(detectedMood)
    val moodLabel = MoodColors.getMoodLabel(detectedMood)
    val emotionState = remember(bot.emotionState) { com.example.data.local.EmotionState.fromJson(bot.emotionState) }
    val realMoodDisplay = "${emotionState.getMoodEmoji()} ${emotionState.mood.replaceFirstChar { it.uppercase() }}"

    val approxTokens = remember(bot.memoryNotes, bot.storyNotes, bot.pinnedMemory) {
        val chars = bot.memoryNotes.length + bot.storyNotes.length + bot.pinnedMemory.length
        val tok = chars / 4
        if (tok < 1000) "$tok token" else "${"%.1f".format(tok / 1000f)}K"
    }

    val lastAiIndex = messages.indexOfLast { it.role == "assistant" }

    Scaffold(
        containerColor = EmochiBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.EmochiGradients.heroHeader)
                    .border(
                        width = 1.dp,
                        brush = com.example.ui.theme.EmochiGradients.glassBorder,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = EmochiTextPrimary)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EmochiCard.copy(alpha = 0.5f))
                            .clickable { showQuickProfile = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (bot.avatarUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = bot.avatarUrl,
                                    contentDescription = displayName,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, EmochiPrimary, CircleShape)
                                )
                            } else {
                                OrbView(hue = hue, size = 36.dp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = displayName,
                                color = EmochiTextPrimary,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSending) "✨ Düşünüyor..." else realMoodDisplay,
                                color = if (isSending) EmochiPrimary else EmochiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = { showBotSettings = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmochiCard)
                            .testTag("bot_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = EmochiPrimary)
                    }
                }

                // Sub-header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B1A30))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buildString {
                                append(userSettings?.selectedModel?.ifBlank { "gemini-2.5-flash" } ?: "gemini-2.5-flash")
                                val effLen = if (bot.customLength != "default") bot.customLength else userSettings?.responseLength
                                when (effLen) {
                                    "short" -> append(" (Kısa)")
                                    "long" -> append(" (Uzun)")
                                    else -> append(" (RP)")
                                }
                                if ((userSettings?.enableNsfw == true) && bot.isNsfw) {
                                    append(" 🔥")
                                }
                            },
                            color = EmochiTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF381D2A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("❤️ ${emotionState.affection}%", color = Color(0xFFFF6B81), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A2D4A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🛡️ ${emotionState.trust}%", color = Color(0xFF4D96FF), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        if (emotionState.tension > 20) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF3D2C12))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("⚡ ${emotionState.tension}%", color = Color(0xFFFFB302), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (isSpeaking) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A1C38))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔊 Sesli okunuyor...",
                                color = EmochiPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = onStopSpeaking,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("⏹️ Okumayı Durdur", color = EmochiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            val clipboardManager = LocalClipboardManager.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmochiSurface)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                // Quick prompt chips
                val quickPrompts = listOf(
                    "🎭 Sahneyi derinleştir" to "Lütfen şu anki sahneyi ve karakterin iç dünyasını daha detaylı, atmosferik bir şekilde betimleyerek yanıt ver.",
                    "💡 Ne yapmalıyım?" to "Karakter bana bakıp şu anda ne yapmam gerektiğiyle ilgili imalı bir öneride bulunsun.",
                    "🔥 Duyguyu yükselt" to "Aramızdaki duygusal çekimi ve gerilimi hissettirecek şekilde davran.",
                    "🎲 Sürpriz hamle" to "Karakter beklenmedik, şaşırtıcı bir tepki versin veya yeni bir olay başlatsın."
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { (label, promptText) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(EmochiCard)
                                .border(1.dp, EmochiBorder, RoundedCornerShape(16.dp))
                                .clickable(enabled = !isSending) {
                                    onSendMessage(promptText)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                color = EmochiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val trimmed = inputText.trim()
                            if (trimmed.isBlank()) {
                                inputText = "*...*"
                            } else if (trimmed.startsWith("*") && trimmed.endsWith("*") && trimmed.length >= 2) {
                                inputText = trimmed.substring(1, trimmed.length - 1)
                            } else {
                                inputText = "*$trimmed*"
                            }
                        },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmochiCard)
                            .border(1.dp, com.example.ui.theme.EmochiBorderFocused, CircleShape)
                    ) {
                        Text("*RP*", color = EmochiPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("${bot.userCharName.ifBlank { "Kullanıcı" }} olarak yaz...", color = EmochiTextMuted, fontSize = 13.5.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(22.dp),
                        colors = customTextFieldColors(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isSending) com.example.ui.theme.EmochiGradients.primaryButton
                                else Brush.linearGradient(listOf(EmochiCard, EmochiCard))
                            )
                            .clickable(enabled = inputText.isNotBlank() && !isSending) {
                                val text = inputText
                                inputText = ""
                                onSendMessage(text)
                            }
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Gönder",
                            tint = if (inputText.isNotBlank() && !isSending) Color(0xFF151428) else EmochiTextMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (bot.chatBgUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = bot.chatBgUrl,
                    contentDescription = "Sohbet Arka Planı",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                errorMessage?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF3A1414))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(err, color = EmochiError, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hatayı Kapat", tint = EmochiError, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty() && !isSending) {
                        item(key = "empty_placeholder_card") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "✨ $displayName henüz ilk mesajını göndermedi",
                                            color = EmochiTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = "Aşağıdaki butona dokunarak karakterinizin selamlama mesajını başlatabilir veya hemen mesaj yazmaya başlayabilirsiniz.",
                                            color = EmochiTextSecondary,
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Button(
                                            onClick = onEnsureOpeningMessage,
                                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1A1B2E))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("👋 Karakter İlk Mesajını Yükle", color = Color(0xFF1A1B2E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(messages, key = { _, msg -> msg.id }) { idx, msg ->
                        val isUser = msg.role == "user"
                        val isLastAi = !isUser && idx == lastAiIndex
                        val isLastUserUnanswered = isUser && idx == messages.lastIndex

                        val timeFormatted = remember(msg.timestamp) {
                            if (msg.timestamp > 0L) {
                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(msg.timestamp))
                            } else ""
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            if (editingMessageId == msg.id) {
                                // Edit box
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        OutlinedTextField(
                                            value = editingText,
                                            onValueChange = { editingText = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = customTextFieldColors()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { editingMessageId = null }) {
                                                Text("Vazgeç", color = EmochiTextMuted, fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    val text = editingText
                                                    editingMessageId = null
                                                    onEditMessage(msg.id, text)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                            ) {
                                                Text("Kaydet", color = Color(0xFF1A1B2E), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Bubble
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 70.dp, max = 300.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isUser) 18.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 18.dp
                                            )
                                        )
                                        .background(
                                            if (isUser) com.example.ui.theme.EmochiGradients.userBubble
                                            else com.example.ui.theme.EmochiGradients.cardBackground
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = if (isUser) Brush.horizontalGradient(listOf(Color(0xFFFFA07A), Color(0xFFFF6B81)))
                                                    else com.example.ui.theme.EmochiGradients.glassBorder,
                                            shape = RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isUser) 18.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 18.dp
                                            )
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.text,
                                            color = if (isUser) EmochiUserBubbleText else EmochiTextPrimary,
                                            fontSize = 14.5.sp,
                                            lineHeight = 21.sp
                                        )
                                        if (timeFormatted.isNotBlank()) {
                                            Text(
                                                text = timeFormatted,
                                                color = if (isUser) EmochiUserBubbleText.copy(alpha = 0.8f) else EmochiTextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Message actions row
                                val clipboardManager = LocalClipboardManager.current
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                                ) {
                                    Text(
                                        text = "Kopyala",
                                        color = EmochiTextMuted,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                        }
                                    )
                                    if (isUser) {
                                        Text(
                                            text = "Düzenle",
                                            color = EmochiTextMuted,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.clickable {
                                                editingMessageId = msg.id
                                                editingText = msg.text
                                            }
                                        )
                                    } else if (userSettings?.enableTts == true && onSpeakText != null) {
                                        Text(
                                            text = if (isSpeaking) "⏹️ Durdur" else "🔊 Sesli Oku",
                                            color = EmochiPrimary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                if (isSpeaking) onStopSpeaking() else onSpeakText(msg.text)
                                            }
                                        )
                                    }
                                    Text(
                                        text = "Sil",
                                        color = EmochiTextMuted,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.clickable { onDeleteMessage(msg.id) }
                                    )
                                    if ((isLastAi || isLastUserUnanswered) && !isSending) {
                                        Text(
                                            text = "Yeniden Oluştur",
                                            color = EmochiPrimary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { onRegenerate() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSending) {
                    item(key = "typing_dots_indicator") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(com.example.ui.theme.EmochiGradients.cardBackground)
                                    .border(
                                        width = 1.dp,
                                        brush = com.example.ui.theme.EmochiGradients.glassBorder,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (bot.avatarUrl.isNotBlank()) {
                                        coil.compose.AsyncImage(
                                            model = bot.avatarUrl,
                                            contentDescription = displayName,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        OrbView(hue = hue, size = 24.dp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TypingDots(dotColor = EmochiPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$displayName düşünüyor...",
                                        color = EmochiTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scroll to bottom floating button when scrolled up
            val showScrollToBottom = remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex > 2
                }
            }

            if (showScrollToBottom.value) {
                val totalCount = messages.size + if (isSending) 1 else 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmochiPrimary)
                            .clickable {
                                coroutineScope.launch {
                                    if (totalCount > 0) {
                                        listState.animateScrollToItem(totalCount - 1)
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "En aşağı in",
                                tint = Color(0xFF1A1B2E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "En Aşağı İn",
                                color = Color(0xFF1A1B2E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

    if (showBotSettings) {
        BotSettingsModal(
            bot = bot,
            keyCharacters = keyCharacters,
            characterEmotions = characterEmotions,
            onDismiss = { showBotSettings = false },
            onSave = onSaveBotProfile,
            onResetChat = onResetChat,
            onDeleteBot = onDeleteBot
        )
    }

    if (showQuickProfile) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        BotQuickProfileSheet(
            bot = bot,
            keyCharacters = keyCharacters,
            characterEmotions = characterEmotions,
            onDismiss = { showQuickProfile = false },
            onSaveBot = { updatedBot ->
                onSaveBotProfile(updatedBot, keyCharacters)
            },
            onOpenFullSettings = { showBotSettings = true },
            onResetChat = { fullReset ->
                showQuickProfile = false
                onResetChat(fullReset)
            },
            onExportChat = {
                val fullText = messages.joinToString("\n\n") { "${if (it.role == "user") bot.userCharName.ifBlank { "Kullanıcı" } else displayName}: ${it.text}" }
                clipboardManager.setText(AnnotatedString(fullText))
                android.widget.Toast.makeText(context, "Tüm sohbet metni panoya kopyalandı!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
