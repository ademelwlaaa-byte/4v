package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.example.ui.theme.EmochiError

// Helper function to format spoken dialogue and narrative actions in AI messages
fun formatNarrativeText(text: String): AnnotatedString {
    return buildAnnotatedString {
        // Regex for quoted dialogue ("..." or “...”) and asterisk actions (*...*)
        val regex = Regex("(\"[^\"]*\")|(“[^”]*”)|(«[^»]*»)|(\\*[^\\*]*\\*)")
        var lastIdx = 0
        val matches = regex.findAll(text)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIdx) {
                append(text.substring(lastIdx, start))
            }
            val matchedStr = text.substring(start, end)
            if (matchedStr.startsWith("*") && matchedStr.endsWith("*")) {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFCBD5E1),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                ) {
                    append(matchedStr)
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFC084FC), // Bright neon purple for dialogue quotes
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(matchedStr)
                }
            }
            lastIdx = end
        }
        if (lastIdx < text.length) {
            append(text.substring(lastIdx))
        }
    }
}

@Composable
fun RpAtmosphericBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Base dark purple/black canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07030C))
        )

        // Generated atmospheric gothic wallpaper image (richer visibility)
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_chat_bg_gothic),
            contentDescription = "Gothic Chat Background",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.65f
        )

        // Canvas for subtle floating glowing particles / star dust
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val random = java.util.Random(42)
            for (i in 0..35) {
                val x = random.nextFloat() * width
                val y = random.nextFloat() * height
                val alpha = random.nextFloat() * 0.35f + 0.1f
                val radius = random.nextFloat() * 1.6f + 0.8f
                drawCircle(
                    color = Color(0xFFD8B4FE).copy(alpha = alpha),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        // Very subtle vignette overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x6005030A),
                            Color(0x1005030A),
                            Color(0x7005030A)
                        )
                    )
                )
        )
    }
}

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
    var showBackgroundPicker by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var expandedMenuMessageId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Stop speaking immediately when exiting chat
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

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size, isSending) {
        val totalItems = messages.size + if (isSending) 1 else 0
        if (totalItems > 0) {
            try {
                listState.animateScrollToItem(totalItems - 1)
            } catch (_: Exception) {}
        }
    }

    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }

    // --- Rich Animations Setup ---
    val infiniteTransition = rememberInfiniteTransition(label = "chatScreenAnimations")

    // 1. Breathing Glow Alpha Pulse (0.35f to 0.90f)
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )

    // 2. Tactile Pulse Scale (0.96f to 1.04f)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 3. Rotating Sweep Angle for Neon Borders (0f to 360f)
    val sweepRotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepRotateAngle"
    )

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
    val emotionState = remember(bot.emotionState) { com.example.data.local.EmotionState.fromJson(bot.emotionState) }
    val realMoodDisplay = "${emotionState.getMoodEmoji()} ${emotionState.mood.replaceFirstChar { it.uppercase() }}"

    val lastAiIndex = messages.indexOfLast { it.role == "assistant" }

    Box(modifier = Modifier.fillMaxSize()) {
        // Base atmospheric wallpaper (full bleed across entire screen)
        RpAtmosphericBackground()

        // Bot background wallpaper model resolution
        val bgModel: Any = remember(bot.chatBgUrl, bot.avatarUrl, bot.id) {
            when {
                bot.chatBgUrl == "USE_AVATAR" -> {
                    when {
                        bot.avatarUrl.isNotBlank() -> bot.avatarUrl
                        bot.id.contains("aiden_zoktay") -> com.example.R.drawable.aiden_zoktay
                        bot.id.contains("aiden_dispatch") -> com.example.R.drawable.aiden_dispatch
                        bot.id.contains("aiden_joker") -> com.example.R.drawable.aiden_joker
                        bot.id.contains("aiden_doctor") -> com.example.R.drawable.aiden_doctor
                        bot.id.contains("aiden_obsidian") -> com.example.R.drawable.aiden_obsidian
                        bot.id == "starter_ayla" || bot.aiName.equals("Ayla", ignoreCase = true) -> com.example.R.drawable.img_ayla_avatar
                        bot.id == "starter_aetheria" || bot.universeName.contains("Aetheria", ignoreCase = true) -> com.example.R.drawable.img_aetheria_universe
                        else -> com.example.R.drawable.img_chat_bg_gothic
                    }
                }
                bot.chatBgUrl.isNotBlank() -> bot.chatBgUrl
                else -> com.example.R.drawable.img_chat_bg_gothic
            }
        }

        coil.compose.AsyncImage(
            model = bgModel,
            contentDescription = "Sohbet Arka Planı",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.65f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x90070512),
                            Color(0x35070512),
                            Color(0x80070512),
                            Color(0xFA070512)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xD0070512), Color(0x50070512))
                            )
                        )
                        .statusBarsPadding()
                ) {
                // Top bar Header matching screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF130F26))
                            .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Center Stack (Avatar + Bot Name + Emotion Badge)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showQuickProfile = true }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.size(54.dp)
                        ) {
                            // Pulsing Outer Aura Ring
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0x30A855F7).copy(alpha = pulseGlowAlpha * 0.5f))
                            )

                            // Avatar with animated gradient border
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.sweepGradient(
                                            listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED), Color(0xFFE9D5FF))
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(2.dp)
                            ) {
                                if (bot.id == "starter_ayla" || bot.aiName.equals("Ayla", ignoreCase = true)) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_ayla_avatar),
                                        contentDescription = displayName,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else if (bot.id == "starter_aetheria" || bot.universeName.contains("Aetheria", ignoreCase = true) || bot.aiName.contains("Aetheria", ignoreCase = true)) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_aetheria_universe),
                                        contentDescription = displayName,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else if (bot.avatarUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = bot.avatarUrl,
                                        contentDescription = displayName,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    OrbView(hue = hue, size = 48.dp)
                                }
                            }

                            // Active green online status dot with pulsing glow ring
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = pulseGlowAlpha))
                                    .padding(1.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .border(1.5.dp, Color(0xFF070512), CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Mood Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF17102C))
                                .border(1.dp, Color(0x408B5CF6), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isSending) "✨ Düşünüyor..." else realMoodDisplay,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wallpaper / Background Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF130F26))
                                .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                .clickable { showBackgroundPicker = true }
                                .testTag("wallpaper_picker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Arka Plan",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Settings Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF130F26))
                                .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                .clickable { showBotSettings = true }
                                .testTag("bot_settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ayarlar",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Sub-header stats row matching screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Tag on Left
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1538))
                            .border(1.dp, Color(0x608B5CF6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ " + (userSettings?.selectedModel?.ifBlank { "Gemini 2.5 Flash" } ?: "Gemini 2.5 Flash") + " (RP) 🔥",
                                color = Color(0xFFD8B4FE),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stats Badges on Right: Red Heart % and Blue Shield %
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF381224))
                                .border(1.dp, Color(0x60FF4D6D), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("❤️ ${emotionState.affection}%", color = Color(0xFFFF4D6D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F253E))
                                .border(1.dp, Color(0x6038BDF8), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🛡️ ${emotionState.trust}%", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        Text(
                            text = "🔊 Sesli okunuyor...",
                            color = Color(0xFFD8B4FE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = onStopSpeaking,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("⏹️ Okumayı Durdur", color = Color(0xFFD8B4FE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD0070512))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                // Quick RP prompt suggestion chips
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
                    val chipBorder = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x90C084FC),
                                Color(0x306B21A8)
                            )
                        )
                    )
                    quickPrompts.forEach { (label, promptText) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x90140A28))
                                .border(chipBorder, RoundedCornerShape(20.dp))
                                .clickable(enabled = !isSending) {
                                    onSendMessage(promptText)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Input bar matching screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // +RP✦ button with glowing aura and sweep border
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF3B1065), Color(0xFF130826))
                                )
                            )
                            .border(
                                BorderStroke(
                                    width = 1.8.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFFE9D5FF),
                                            Color(0xFFC084FC),
                                            Color(0xFF7C3AED),
                                            Color(0xFFE9D5FF)
                                        )
                                    )
                                ),
                                CircleShape
                            )
                            .clickable {
                                val trimmed = inputText.trim()
                                if (trimmed.isBlank()) {
                                    inputText = "*...*"
                                } else if (trimmed.startsWith("*") && trimmed.endsWith("*") && trimmed.length >= 2) {
                                    inputText = trimmed.substring(1, trimmed.length - 1)
                                } else {
                                    inputText = "*$trimmed*"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+RP✦",
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Bir mesaj yazın...", color = Color(0xFF64748B), fontSize = 13.5.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = customTextFieldColors(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send paperplane button with outer pulsing halo
                    val isSendEnabled = inputText.isNotBlank() && !isSending
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSendEnabled) {
                            // Pulsing glowing background halo when typing
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                    .clip(CircleShape)
                                    .background(Color(0xFFC084FC).copy(alpha = pulseGlowAlpha * 0.45f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSendEnabled) {
                                        Brush.linearGradient(
                                            listOf(Color(0xFFD8B4FE), Color(0xFF9333EA), Color(0xFF581C87))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color(0x403B0764), Color(0x401E1035))
                                        )
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        width = 1.3.dp,
                                        brush = if (isSendEnabled) {
                                            Brush.sweepGradient(
                                                listOf(Color(0xFFFFFFFF), Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFFFFFFFF))
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                listOf(Color(0x407C3AED), Color(0x203B0764))
                                            )
                                        }
                                    ),
                                    CircleShape
                                )
                                .clickable(enabled = isSendEnabled) {
                                    val text = inputText
                                    inputText = ""
                                    onSendMessage(text)
                                }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Gönder",
                                tint = if (isSendEnabled) Color.White else Color(0x60A78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130F26)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x408B5CF6)),
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
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = "Aşağıdaki butona dokunarak karakterinizin selamlama mesajını başlatabilir veya hemen mesaj yazmaya başlayabilirsiniz.",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Button(
                                            onClick = onEnsureOpeningMessage,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("👋 Karakter İlk Mesajını Yükle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

                            if (editingMessageId == msg.id) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130F26)),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
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
                                                Text("Vazgeç", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    val text = editingText
                                                    editingMessageId = null
                                                    onEditMessage(msg.id, text)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                                            ) {
                                                Text("Kaydet", color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else if (isUser) {
                                // USER BUBBLE (Right Side) matching screenshot
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // User Name + Avatar Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                                    ) {
                                        Text(
                                            text = bot.userCharName.ifBlank { "Kullanıcı" },
                                            color = Color(0xFFC084FC),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, Color(0xFF8B5CF6), CircleShape)
                                                .background(Color(0xFF1E143B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (bot.userCharName.ifBlank { "K" }).take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // User Message Box with smooth corners, rich glassmorphic gradient fill, and multi-layered purple neon glow
                                    val userShape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 80.dp, max = 310.dp)
                                            // Layer 1: Outer soft glow halo
                                            .border(4.dp, Color(0x30A855F7), userShape)
                                            // Layer 2: Medium glow halo
                                            .border(2.dp, Color(0x60C084FC), userShape)
                                            .clip(userShape)
                                            // Glassmorphic gradient background with depth
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0x8023104D), // Rich semi-transparent dark violet top
                                                        Color(0x950E0620)  // Deep dark purple bottom
                                                    )
                                                )
                                            )
                                            // Layer 3: Crisp glowing gradient border line
                                            .border(
                                                BorderStroke(
                                                    width = 1.3.dp,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFFE9D5FF), // Crisp bright white/lila top-left
                                                            Color(0xFFC084FC), // Glowing neon violet
                                                            Color(0xFF8B5CF6), // Purple
                                                            Color(0xFF5B21B6)  // Deep violet bottom-right
                                                        )
                                                    )
                                                ),
                                                shape = userShape
                                            )
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 14.5.sp,
                                                lineHeight = 21.sp
                                            )
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Row(
                                                modifier = Modifier.align(Alignment.End),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (timeFormatted.isNotBlank()) {
                                                    Text(
                                                        text = timeFormatted,
                                                        color = Color(0xFFA78BFA),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("✓✓", color = Color(0xFFA78BFA), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (timeFormatted.isNotBlank()) "Şimdi" else "",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                            } else {
                                // AI BOT BUBBLE (Left Side) matching screenshot
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (bot.avatarUrl.isNotBlank()) {
                                            coil.compose.AsyncImage(
                                                model = bot.avatarUrl,
                                                contentDescription = displayName,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_cosmic_orb),
                                                contentDescription = displayName,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        // Bot Name Label
                                        Text(
                                            text = displayName,
                                            color = Color(0xFFC084FC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                        )

                                        // AI Bubble Box with smooth corners, rich glassmorphic gradient fill, and multi-layered purple neon glow
                                        val aiShape = RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                // Layer 1: Outer soft glow halo
                                                .border(4.dp, Color(0x30A855F7), aiShape)
                                                // Layer 2: Medium glow halo
                                                .border(2.dp, Color(0x60C084FC), aiShape)
                                                .clip(aiShape)
                                                // Glassmorphic gradient background with depth
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0x8023104D), // Rich semi-transparent dark violet top
                                                            Color(0x950E0620)  // Deep dark purple bottom
                                                        )
                                                    )
                                                )
                                                // Layer 3: Crisp glowing gradient border line
                                                .border(
                                                    BorderStroke(
                                                        width = 1.3.dp,
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFFE9D5FF), // Crisp bright white/lila top-left
                                                                Color(0xFFC084FC), // Glowing neon violet
                                                                Color(0xFF8B5CF6), // Purple
                                                                Color(0xFF5B21B6)  // Deep violet bottom-right
                                                            )
                                                        )
                                                    ),
                                                    shape = aiShape
                                                )
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = formatNarrativeText(msg.text),
                                                    color = Color(0xFFF1F5F9),
                                                    fontSize = 14.5.sp,
                                                    lineHeight = 22.sp
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.align(Alignment.End),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (timeFormatted.isNotBlank()) {
                                                        Text(
                                                            text = timeFormatted,
                                                            color = Color(0xFF94A3B8),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "||ı||",
                                                        color = Color(0xFFA78BFA),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                         // Message Action Pills Row matching screenshot
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .horizontalScroll(rememberScrollState())
                                                .padding(vertical = 2.dp)
                                        ) {
                                            val pillBorder = BorderStroke(
                                                width = 1.dp,
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0x90C084FC),
                                                        Color(0x306B21A8)
                                                    )
                                                )
                                            )
                                            val pillBg = Color(0x90140A28)

                                            // Copy Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(pillBg)
                                                    .border(pillBorder, RoundedCornerShape(14.dp))
                                                    .clickable { clipboardManager.setText(AnnotatedString(msg.text)) }
                                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("📋", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Kopyala", color = Color(0xFFE2E8F0), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            // Read Aloud Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(pillBg)
                                                    .border(pillBorder, RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        if (isSpeaking) onStopSpeaking() else onSpeakText?.invoke(msg.text)
                                                    }
                                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(if (isSpeaking) "⏹️" else "🎙️", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        if (isSpeaking) "Durdur" else "Sesli Oku",
                                                        color = Color(0xFFE2E8F0),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            // Regenerate Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(pillBg)
                                                    .border(pillBorder, RoundedCornerShape(14.dp))
                                                    .clickable(enabled = !isSending) { onRegenerate() }
                                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🔄", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Yeniden Oluştur", color = Color(0xFFE2E8F0), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            // More Actions Pill (•••) matching screenshot
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(pillBg)
                                                    .border(pillBorder, RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        expandedMenuMessageId = if (expandedMenuMessageId == msg.id) null else msg.id
                                                    }
                                                    .padding(horizontal = 13.dp, vertical = 6.dp)
                                            ) {
                                                Text("•••", color = Color(0xFFE2E8F0), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Expanded items (Edit / Delete) when ••• is toggled
                                            if (expandedMenuMessageId == msg.id) {
                                                // Edit Pill
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF160F2C))
                                                        .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            editingMessageId = msg.id
                                                            editingText = msg.text
                                                            expandedMenuMessageId = null
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("✏️", fontSize = 11.sp)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Düzenle", color = Color(0xFFCBD5E1), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }

                                                // Delete Pill
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF160F2C))
                                                        .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            expandedMenuMessageId = null
                                                            onDeleteMessage(msg.id)
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("🗑️", fontSize = 11.sp)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Sil", color = Color(0xFFCBD5E1), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
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
                                        .background(Color(0xFF0F0B21))
                                        .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(20.dp))
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
                                        TypingDots(dotColor = Color(0xFFC084FC))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$displayName düşünüyor...",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                                .background(Color(0xFF8B5CF6))
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
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "En Aşağı İn",
                                    color = Color.White,
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

    if (showBackgroundPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showBackgroundPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130D2A)),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, Color(0xFFA78BFA)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🖼️ Arka Plan Fotoğrafı",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sohbet ekranında görünmesini istediğiniz duvar kağıdı seçeneği:",
                        color = Color(0xFFA5B4FC),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Button 1: Use Avatar
                    Button(
                        onClick = {
                            onSaveBotProfile(bot.copy(chatBgUrl = "USE_AVATAR"), keyCharacters)
                            showBackgroundPicker = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1B4E), contentColor = Color(0xFFC084FC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("👤 Bot Avatarını Arka Plan Yap", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button 2: Default Theme
                    Button(
                        onClick = {
                            onSaveBotProfile(bot.copy(chatBgUrl = ""), keyCharacters)
                            showBackgroundPicker = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1437), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("🌌 Varsayılan Temaya Dön", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button 3: Custom Gallery Photo / Profile Settings
                    Button(
                        onClick = {
                            showBackgroundPicker = false
                            showBotSettings = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA), contentColor = Color(0xFF130D2A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("📸 Galeriden Fotoğraf Seç", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
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
