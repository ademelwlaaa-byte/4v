package com.example.ui.screens

import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.ui.theme.EmochiSurface

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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    userSettings: UserSettingsEntity?,
    isSending: Boolean,
    errorMessage: String?,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<com.example.data.local.AffectionEventEntity> = emptyList(),
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
    onClearError: () -> Unit = {},
    onRetryMessage: ((String) -> Unit)? = null
) {
    var inputText by remember { mutableStateOf("") }
    var showBotSettings by remember { mutableStateOf(false) }
    var showQuickProfile by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var expandedMenuMessageId by remember { mutableStateOf<String?>(null) }
    var isStatsExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Stop speaking immediately when exiting chat
    DisposableEffect(Unit) {
        onDispose {
            onStopSpeaking()
        }
    }

    // Automatically ensure opening message exists if list is empty (except for book mode)
    LaunchedEffect(bot.id, messages.isEmpty()) {
        if (messages.isEmpty() && !isSending && bot.mode != "book") {
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

    val isBookMode = remember(bot) {
        bot.mode == "book" ||
        bot.id == "preset_aiden_mcu_cosmic" ||
        bot.aiName.contains("Kitap") ||
        bot.aiPersonality.contains("KİTAP MODU") ||
        bot.scenario.contains("KİTAP MODU")
    }

    if (isBookMode) {
        BookReaderView(
            bot = bot,
            messages = messages,
            isSending = isSending,
            errorMessage = errorMessage,
            onBack = onBack,
            onSendMessage = onSendMessage,
            onEnsureOpeningMessage = onEnsureOpeningMessage,
            onResetChat = { onResetChat(it) },
            onClearError = onClearError
        )
    } else {
    Box(modifier = Modifier.fillMaxSize()) {
        val isImeVisible = WindowInsets.isImeVisible

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
                        .background(EmochiSurface.copy(alpha = 0.88f))
                        .statusBarsPadding()
                        .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = if (isImeVisible) 4.dp else 6.dp)
                    ) {
                        // Back button (Left)
                        Box(
                            modifier = Modifier
                                .size(if (isImeVisible) 34.dp else 38.dp)
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x90130F26))
                                .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White,
                                modifier = Modifier.size(if (isImeVisible) 17.dp else 19.dp)
                            )
                        }

                        // Truly Centered Stack (Avatar + Bot Name + Mood Emoji) - Hidden when keyboard is open
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isImeVisible,
                            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .then(
                                        if (bot.mode == "personal") {
                                            Modifier.clickable { isStatsExpanded = !isStatsExpanded }
                                        } else Modifier
                                    )
                                    .padding(horizontal = 50.dp)
                            ) {
                                // Avatar Box
                                Box(
                                    contentAlignment = Alignment.BottomEnd,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0x30A855F7).copy(alpha = pulseGlowAlpha * 0.5f))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.8.dp,
                                                brush = Brush.sweepGradient(
                                                    listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED), Color(0xFFE9D5FF))
                                                ),
                                                shape = CircleShape
                                            )
                                            .padding(1.5.dp)
                                    ) {
                                        if (bot.id == "starter_ayla" || bot.aiName.equals("Ayla", ignoreCase = true)) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_ayla_avatar),
                                                contentDescription = displayName,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else if (bot.id == "starter_aetheria" || bot.universeName.contains("Aetheria", ignoreCase = true) || bot.aiName.contains("Aetheria", ignoreCase = true)) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_aetheria_universe),
                                                contentDescription = displayName,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else if (bot.avatarUrl.isNotBlank()) {
                                            coil.compose.AsyncImage(
                                                model = bot.avatarUrl,
                                                contentDescription = displayName,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            OrbView(hue = hue, size = 38.dp)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = pulseGlowAlpha))
                                            .padding(1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                                .border(1.dp, Color(0xFF070512), CircleShape)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Bot Name (+ Mood Emoji on SAME LINE for Personal Mode)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = displayName,
                                        color = Color.White,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    if (bot.mode == "personal") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSending) "✨" else emotionState.getMoodEmoji(),
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Right Action Buttons (Wallpaper + Settings)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            AnimatedVisibility(
                                visible = !isImeVisible,
                                enter = fadeIn(tween(200)),
                                exit = fadeOut(tween(200))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x90130F26))
                                        .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                        .clickable { showBackgroundPicker = true }
                                        .testTag("wallpaper_picker_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallpaper,
                                        contentDescription = "Arka Plan",
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (!isImeVisible) {
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(if (isImeVisible) 34.dp else 38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x90130F26))
                                    .border(1.dp, Color(0x508B5CF6), RoundedCornerShape(12.dp))
                                    .clickable { showBotSettings = true }
                                    .testTag("bot_settings_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ayarlar",
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(if (isImeVisible) 17.dp else 18.dp)
                                )
                            }
                        }
                    }

                    // Expandable Header Stats Banner (Personal Mode Only) - Only when keyboard is closed
                    AnimatedVisibility(
                        visible = !isImeVisible && isStatsExpanded && bot.mode == "personal",
                        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xE6140A28))
                                .border(1.dp, Color(0x608B5CF6), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ " + (userSettings?.selectedModel?.ifBlank { "Gemini 2.5 Flash" } ?: "Gemini 2.5 Flash") + " 🔥",
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF381224))
                                            .border(1.dp, Color(0x60FF4D6D), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("❤️ ${emotionState.affection}%", color = Color(0xFFFF4D6D), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F253E))
                                            .border(1.dp, Color(0x6038BDF8), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("🛡️ ${emotionState.trust}%", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (isSpeaking && !isImeVisible) {
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
                        .background(EmochiSurface.copy(alpha = 0.88f))
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
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

                                                if (msg.status == "failed") {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Surface(
                                                        color = Color(0x30EF4444),
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.dp, Color(0x80EF4444))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Text("⚠️ Mesaj iletilemedi", color = Color(0xFFFCA5A5), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                            Button(
                                                                onClick = { onRetryMessage?.invoke(msg.id) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(26.dp)
                                                            ) {
                                                                Text("🔄 Tekrar Dene", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
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

                                            // 1. Regenerate Pill
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

                                            // 2. Continue Story Pill
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(pillBg)
                                                    .border(pillBorder, RoundedCornerShape(14.dp))
                                                    .clickable(enabled = !isSending) {
                                                        onSendMessage("Devam et, hikayeye kaldığın yerden devam et.")
                                                    }
                                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("▶️", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Devam Ettir", color = Color(0xFFE2E8F0), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }

                                            // 3. Read Aloud Pill
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

                                            // 4. Copy Pill
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

                                            // 5. More Actions Pill (•••)
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
            }

            val showScrollToBottom = remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    if (totalItems == 0) return@derivedStateOf false

                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
                    val itemsFromEnd = totalItems - 1 - lastVisibleItem.index

                    if (itemsFromEnd >= 2) {
                        true
                    } else if (itemsFromEnd == 1) {
                        true
                    } else {
                        // On last visible item: check if scrolled up by at least ~180px
                        val viewportEnd = layoutInfo.viewportEndOffset
                        val itemBottom = lastVisibleItem.offset + lastVisibleItem.size
                        (itemBottom - viewportEnd) > 180
                    }
                }
            }

            if (showScrollToBottom.value) {
                val totalCount = messages.size + if (isSending) 1 else 0
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF9333EA), Color(0xFF6B21A8))
                                )
                            )
                            .border(1.dp, Color(0xFFD8B4FE), CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    if (totalCount > 0) {
                                        listState.animateScrollToItem(totalCount - 1)
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
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
                                fontSize = 11.5.sp,
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
            affectionEvents = affectionEvents,
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
            affectionEvents = affectionEvents,
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
    } // End of else block for non-book mode
}

data class BookChoiceOption(val text: String, val rawLine: String)

fun parseBookChoiceOptions(rawText: String): List<BookChoiceOption> {
    val result = mutableListOf<BookChoiceOption>()
    val lines = rawText.split("\n")
    var inChoiceSection = false

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("🔀")) {
            inChoiceSection = true
            continue
        }

        val isChoice = trimmed.startsWith("1️⃣") || trimmed.startsWith("2️⃣") || trimmed.startsWith("3️⃣") ||
                trimmed.startsWith("4️⃣") || trimmed.startsWith("5️⃣") || trimmed.startsWith("6️⃣") ||
                trimmed.matches(Regex("^[1-9][0-9]*[\\.\\)]\\s*.*")) ||
                (inChoiceSection && (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("*")))

        if (isChoice) {
            val choiceText = trimmed
                .replace(Regex("^(1️⃣|2️⃣|3️⃣|4️⃣|5️⃣|6️⃣|\\d+[\\.\\)]|[-•*])\\s*"), "")
                .trim()
            if (choiceText.length >= 2) {
                result.add(BookChoiceOption(choiceText, trimmed))
            }
        }
    }
    return result
}

enum class SummaryViewMode {
    PROMPT,
    SEQUENTIAL,
    FINAL_GRID
}

enum class StepPhase {
    CARD_ONLY,
    RESULT_REVEALED
}

data class ChapterChoiceItem(
    val id: Int,
    val title: String,
    val selectedOption: String,
    val impactText: String,
    val icon: String, // "🔀", "💫", "💭"
    val isBranching: Boolean = false,
    val isRelationship: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSummaryModal(
    messages: List<com.example.data.local.MessageEntity>,
    onDismiss: () -> Unit,
    onRestartChapter: () -> Unit,
    onNextChapter: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var viewMode by remember { mutableStateOf(SummaryViewMode.PROMPT) }
    var currentIndex by remember { mutableStateOf(0) }
    var stepPhase by remember { mutableStateOf(StepPhase.CARD_ONLY) }
    var isClosing by remember { mutableStateOf(false) }

    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    val completedChapterNumber = remember(messages) {
        val lastAssistant = messages.lastOrNull { it.role == "assistant" || it.role == "model" }
        val txt = lastAssistant?.text ?: ""
        when {
            txt.contains("BÖLÜM 3 SONU") -> 3
            txt.contains("BÖLÜM 2 SONU") -> 2
            else -> 1
        }
    }

    // Audio Fade-out helper (100% -> 0% in ~1.5s)
    val safeExitWithFade = remember {
        { action: () -> Unit ->
            if (!isClosing) {
                isClosing = true
                coroutineScope.launch {
                    mediaPlayer?.let { player ->
                        try {
                            for (i in 100 downTo 0) {
                                val vol = i / 100f
                                if (player.isPlaying) {
                                    player.setVolume(vol, vol)
                                }
                                delay(15) // ~1.5s total fade out
                            }
                            if (player.isPlaying) player.stop()
                            player.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    mediaPlayer = null
                    action()
                }
            }
        }
    }

    // Audio Fade-in effect (0% -> 100% in ~1.5s)
    DisposableEffect(Unit) {
        var player: android.media.MediaPlayer? = null
        try {
            val afd = context.resources.openRawResourceFd(com.example.R.raw.bolumsonu)
            if (afd != null) {
                val p = android.media.MediaPlayer()
                p.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                p.setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                p.isLooping = true
                p.prepare()
                p.setVolume(0.1f, 0.1f)
                p.start()
                player = p
                mediaPlayer = p

                // Rapid Fade-In: 0% -> 100%
                coroutineScope.launch {
                    for (i in 10..100) {
                        val vol = i / 100f
                        try {
                            if (p.isPlaying) p.setVolume(vol, vol)
                        } catch (e: Exception) { e.printStackTrace() }
                        delay(15)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                player?.let { p ->
                    if (p.isPlaying) p.stop()
                    p.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val userMsgs = remember(messages) { messages.filter { it.role == "user" } }
    val lastAssistantMsg = remember(messages) {
        messages.lastOrNull { it.role == "assistant" || it.role == "model" }
    }
    val isChapter3 = remember(messages, lastAssistantMsg) {
        userMsgs.size >= 11 || (lastAssistantMsg?.text?.contains("BÖLÜM 3") == true)
    }
    val isChapter2 = remember(messages, lastAssistantMsg) {
        !isChapter3 && (userMsgs.size >= 5 || (lastAssistantMsg?.text?.contains("BÖLÜM 2") == true))
    }

    val choices = remember(messages, isChapter2, isChapter3) {
        val items = mutableListOf<ChapterChoiceItem>()

        if (isChapter3) {
            if (userMsgs.size >= 11) {
                val c1Msg = userMsgs.getOrNull(10)?.text ?: ""
                val isUnconditional = c1Msg.contains("2") || c1Msg.contains("Tereddütsüz")
                val isShort = c1Msg.contains("3") || c1Msg.contains("kısa")
                items.add(
                    ChapterChoiceItem(
                        id = 1,
                        title = "1. OTUZ GÜNLÜK TEKLİFE CEVAP",
                        selectedOption = if (isUnconditional) "Tereddütsüz 30 günlük deneme süresini kabul ettin."
                                        else if (isShort) "30 günü çok uzun bulup birkaç günlük kısa süre önerdin."
                                        else "30 günlük süreyi kendi çıkış şartlarınla kabul ettin.",
                        impactText = if (isUnconditional) "Steve Rogers memnun kaldı, sana karşı güveni arttı."
                                    else if (isShort) "Steve anlayış gösterdi ama ekibin temkinli yaklaşımı sürdü."
                                    else "Özgürlüğünden ödün vermeden ilk adımı attın.",
                        icon = if (isUnconditional) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isUnconditional
                    )
                )
            }

            if (userMsgs.size >= 12) {
                val c2Msg = userMsgs.getOrNull(11)?.text ?: ""
                val isEager = c2Msg.contains("2") || c2Msg.contains("İstekli")
                val isRefuse = c2Msg.contains("3") || c2Msg.contains("Reddet")
                items.add(
                    ChapterChoiceItem(
                        id = 2,
                        title = "2. ANTRENMAN SALONU DEĞERLENDİRMESİ",
                        selectedOption = if (isEager) "Steve Rogers ile antrenmana istekli katıldın."
                                        else if (isRefuse) "Fiziksel testi reddedip teorik anlatım istedin."
                                        else "Sınırlarını kendin belirlemek şartıyla temkinli kabul ettin.",
                        impactText = if (isEager) "Ekip üyelerinde büyük merak ve olumlu izlenim uyandırdın."
                                    else if (isRefuse) "Gizemli kaldın ama Steve seni yine de antrenman ortamına davet etti."
                                    else "Kontrolü elinde tutarak antrenman alanına adım attın.",
                        icon = if (isEager) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isEager
                    )
                )
            }

            if (userMsgs.size >= 13) {
                val c3Msg = userMsgs.getOrNull(12)?.text ?: ""
                val isFullPower = c3Msg.contains("3") || c3Msg.contains("Hiç geri tutma")
                val isPushHard = c3Msg.contains("2") || c3Msg.contains("zorla")
                items.add(
                    ChapterChoiceItem(
                        id = 3,
                        title = "3. STEVE'E KARŞI GÜÇ GÖSTERİMİ",
                        selectedOption = if (isFullPower) "Hiç geri tutmadın, Kırmızı Göz ivmesiyle kazandın."
                                        else if (isPushHard) "Steve'in savunmasını zorladın ama sınırda durdun."
                                        else "Kendini tamamen kontrol ettin, Steve'e zarar vermedin.",
                        impactText = if (isFullPower) "Bruce Banner dehşete düştü, Steve gücüne takdirle yaklaştı."
                                    else if (isPushHard) "Natasha ve Steve hızından ve kontrolünden etkilendi."
                                    else "Hızlı iyileşme gücün ve kontrolün Natasha'dan takdir gördü.",
                        icon = if (isFullPower) "🔀" else "💫",
                        isBranching = isFullPower,
                        isRelationship = !isFullPower
                    )
                )
            }

            if (userMsgs.size >= 14) {
                val c4Msg = userMsgs.getOrNull(13)?.text ?: ""
                val isMin = c4Msg.contains("2") || c4Msg.contains("Minimum")
                val isMax = c4Msg.contains("3") || c4Msg.contains("tam kapasite")
                items.add(
                    ChapterChoiceItem(
                        id = 4,
                        title = "4. ENERJİ TESTİ GÖSTERİMİ",
                        selectedOption = if (isMin) "Minimum kıvılcım seviyesinde güç uyguladın."
                                        else if (isMax) "Neredeyse tam kapasitede devasa enerji patlaması yarattın."
                                        else "Orta seviyede kontrollü enerji dalgası fırlattın.",
                        impactText = if (isMin) "Sınırlarını gizli tuttun, merak uyandırdın."
                                    else if (isMax) "Stark sayılara hayran kaldı, Wanda bina seviyesi potansiyelini not etti."
                                    else "Bruce Banner senin kozmik imzanın benzersiz olduğunu belirtti.",
                        icon = if (isMax) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isMax
                    )
                )
            }

            if (userMsgs.size >= 15) {
                val c5Msg = userMsgs.getOrNull(14)?.text ?: ""
                val isSerious = c5Msg.contains("3") || c5Msg.contains("ciddiye")
                val isSilent = c5Msg.contains("2") || c5Msg.contains("Sessiz")
                items.add(
                    ChapterChoiceItem(
                        id = 5,
                        title = "5. SAM WILSON'IN ŞAKASINA TEPKİ",
                        selectedOption = if (isSerious) "Şakayı ciddiye alıp soğuk durdun."
                                        else if (isSilent) "Sessiz kalıp sadece gülümsedin."
                                        else "Kuru bir espriyle masayı kahkahaya boğdun.",
                        impactText = if (isSerious) "Mesafeli tavrını korudun."
                                    else if (isSilent) "Saygıyla karşılanan mesafeli bir gülümseme bıraktın."
                                    else "Yıllar sonra ilk kez bir ekiple masada gülmenin tadını çıkardın.",
                        icon = if (!isSerious && !isSilent) "💫" else "💭",
                        isBranching = false,
                        isRelationship = !isSerious && !isSilent
                    )
                )
            }

            if (userMsgs.size >= 16) {
                val c6Msg = userMsgs.getOrNull(15)?.text ?: ""
                val isShort = c6Msg.contains("2") || c6Msg.contains("çatıya") || c6Msg.contains("Kısa")
                val isBig = c6Msg.contains("3") || c6Msg.contains("uzağa") || c6Msg.contains("Büyük")
                items.add(
                    ChapterChoiceItem(
                        id = 6,
                        title = "6. S.H.I.E.L.D. ALARM ALARMINDA KARAR",
                        selectedOption = if (isBig) "Tümünü geride bırakıp başka bir şehre ışınlandın (Büyük Kaçış)."
                                        else if (isShort) "Refleksle çatıya ışınlandın, sonra geri döndün (Kısa Kaçış)."
                                        else "Kule'de kaldın ve ekibe güvenmeyi seçtin (Normal Akış).",
                        impactText = if (isBig) "Thor seni 3 gün sonra buldu, kaçış ve dönüş deneyimin olgunlaştı."
                                    else if (isShort) "Çatıdan izledikten sonra adımlayarak aşağı indin, Wanda cesaretini övdü."
                                    else "Wanda ve Steve ile güven bağını güçlendirdin, kalmanın cesaret olduğunu hissettin.",
                        icon = "🔀",
                        isBranching = true,
                        isRelationship = !isBig
                    )
                )
            }

            if (userMsgs.size >= 17) {
                val c7Msg = userMsgs.getOrNull(16)?.text ?: ""
                val isSincere = c7Msg.contains("1") || c7Msg.contains("Tam dürüst") || c7Msg.contains("Korkutucu")
                val isJoking = c7Msg.contains("2") || c7Msg.contains("Yarım")
                items.add(
                    ChapterChoiceItem(
                        id = 7,
                        title = "7. NATASHA İLE ÇATI DİYALOGU",
                        selectedOption = if (isSincere) "Kalmanın kaybedecek bir şey biriktirmek olduğunu dürüstçe söyledin."
                                        else if (isJoking) "Manzaradan bahsettiğini söyleyerek durumu şakaya vurdun."
                                        else "Kapanıp sadece hava aldığını söyledin.",
                        impactText = if (isSincere) "Natasha Kızıl Oda geçmişini paylaşarak derin bir bağ kurdu."
                                    else if (isJoking) "Natasha hafifçe gülümsedi ve 30 günlük süreni hatırlattı."
                                    else "Natasha mesafene saygı duyarak iyi geceler diledi.",
                        icon = if (isSincere) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isSincere
                    )
                )
            }

            if (userMsgs.size >= 18) {
                val c8Msg = userMsgs.getOrNull(17)?.text ?: ""
                val isSuppress = c8Msg.contains("2") || c8Msg.contains("cebine") || c8Msg.contains("bastır")
                val isFlashback = c8Msg.contains("3") || c8Msg.contains("flashback") || c8Msg.contains("bak")
                items.add(
                    ChapterChoiceItem(
                        id = 8,
                        title = "8. KOLYE ANI & İÇ SES",
                        selectedOption = if (isSuppress) "Kolyeyi cebine koyup duygusal zayıflığı bastırdın."
                                        else if (isFlashback) "Kolyeye bakıp 20 yıl önceki patlamayı hatırladın."
                                        else "Kolyeyi elinde tutup umutla New York ışıklarını izledin.",
                        impactText = if (isSuppress) "Soğukkanlı zırhını korumaya devam ettin."
                                    else if (isFlashback) "Geçmişin yükünü kabullenerek kulede yeni bir sayfa açtın."
                                    else "Yıllar sonra ilk kez bir şeyleri kaybetmeden tutabileceğine inandın.",
                        icon = if (!isSuppress) "💫" else "💭",
                        isBranching = false,
                        isRelationship = false
                    )
                )
            }
        } else if (isChapter2) {
            // Chapter 2 Choices (Indices 4, 5, 6, 7, 8)
            if (userMsgs.size >= 5) {
                val c1Msg = userMsgs.getOrNull(4)?.text ?: ""
                val isGenerous = c1Msg.contains("3") || c1Msg.contains("fazlası") || c1Msg.contains("Cömert")
                val isRefuse = c1Msg.contains("2") || c1Msg.contains("Reddet") || c1Msg.contains("gösterme")
                items.add(
                    ChapterChoiceItem(
                        id = 1,
                        title = "1. GÜÇ GÖSTERİMİ STRATEJİSİ",
                        selectedOption = if (isGenerous) "Güç gösteriminde cömert davrandın ve katmanlı formlarından bahsettin."
                                        else if (isRefuse) "Güç gösterisini tamamen reddettin, sadece sözlerine güvenmelerini istedin."
                                        else "Küçük mavi ışık küresi oluşturdun ve temel sınırlarını açıkladın.",
                        impactText = if (isGenerous) "Tony Stark'ın ilgisini çektin ve bilimsel bir merak uyandırdın."
                                    else if (isRefuse) "Gizemin korundu ancak Steve Rogers ve Tony Stark'ın şüphesi bir nebze arttı."
                                    else "Güç gösterin kontrollü bulundu, Stark ve Rogers güçlerini gerçekçi şekilde değerlendirdi.",
                        icon = if (isGenerous) "💫" else if (isRefuse) "🔀" else "💭",
                        isBranching = isRefuse,
                        isRelationship = isGenerous
                    )
                )
            }

            if (userMsgs.size >= 6) {
                val c2Msg = userMsgs.getOrNull(5)?.text ?: ""
                val isSincere = c2Msg.contains("2") || c2Msg.contains("Ciddi") || c2Msg.contains("zor olduğunu")
                val isSilent = c2Msg.contains("3") || c2Msg.contains("Cevap verme") || c2Msg.contains("değiştir")
                items.add(
                    ChapterChoiceItem(
                        id = 2,
                        title = "2. STEVE'İN GÜVEN SÖZÜNE TEPKİ",
                        selectedOption = if (isSincere) "Güvenin senin için neden bu kadar zor olduğunu dürüstçe açıkladın."
                                        else if (isSilent) "Cevap vermeyip konuyu değiştirdin."
                                        else "Güven kelimesini sorgulayarak alaycı bir üslup takındın.",
                        impactText = if (isSincere) "Steve Rogers'ın saygısını ve güvenini kazandın."
                                    else if (isSilent) "Steve Rogers ayrılırken temkinli ve mesafeli bir tavır sergiledi."
                                    else "Geri çekilmeden sınır çizdin, mesafeli duruşunu korudun.",
                        icon = if (isSincere) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isSincere
                    )
                )
            }

            if (userMsgs.size >= 7) {
                val c3Msg = userMsgs.getOrNull(6)?.text ?: ""
                val isTalk = c3Msg.contains("2") || c3Msg.contains("Sözlü") || c3Msg.contains("sakladığını")
                val isLeave = c3Msg.contains("3") || c3Msg.contains("Mesafe") || c3Msg.contains("çık")
                items.add(
                    ChapterChoiceItem(
                        id = 3,
                        title = "3. NATASHA İLE İLİŞKİ TAVRI",
                        selectedOption = if (isTalk) "Natasha'ya sözlü karşılık verdin, onun geçmişini ima ettin."
                                        else if (isLeave) "Mesafe koyup odadan çıktın."
                                        else "Natasha'ya karşı sessiz kaldın, bakışların onda uzadı.",
                        impactText = if (isTalk) "Natasha ile güçlü ve doğrudan bir diyalog bağı kurdun."
                                    else if (isLeave) "Natasha ile arandaki mesafeyi korudun, gizemini muhafaza ettin."
                                    else "Gölgelerden gelen iki insan arasındaki sessiz anlayış derinleşti.",
                        icon = if (isTalk || !isLeave) "💫" else "💭",
                        isBranching = false,
                        isRelationship = !isLeave
                    )
                )
            }

            if (userMsgs.size >= 8) {
                val c4Msg = userMsgs.getOrNull(7)?.text ?: ""
                val isAccept = c4Msg.contains("2") || c4Msg.contains("Kabul") || c4Msg.contains("açıkça")
                val isReject = c4Msg.contains("3") || c4Msg.contains("Sert") || c4Msg.contains("kapat")
                items.add(
                    ChapterChoiceItem(
                        id = 4,
                        title = "4. WANDA'NIN İÇGÖRÜSÜNE TEPKİ",
                        selectedOption = if (isAccept) "Kayıp hissettiğini Wanda'ya açıkça kabul ettin."
                                        else if (isReject) "Sert bir şekilde konuyu kapatıp geri çekildin."
                                        else "Wanda'ya zihin okuma şüphesiyle karşılık verdin.",
                        impactText = if (isAccept) "Wanda ile aranızda derin bir duygusal empati ve bağ kuruldu."
                                    else if (isReject) "Wanda ile arandaki duygusal teması sınırlandırdın."
                                    else "Wanda anlayışla gülümsedi ve seni içgörüyle dinlemeye devam etti.",
                        icon = if (isAccept) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isAccept || !isReject
                    )
                )
            }

            if (userMsgs.size >= 9) {
                val c5Msg = userMsgs.getOrNull(8)?.text ?: ""
                val isDoubt = c5Msg.contains("2") || c5Msg.contains("şüphe") || c5Msg.contains("kaçmayı")
                val isConflicted = c5Msg.contains("3") || c5Msg.contains("Belirsiz") || c5Msg.contains("karışık") || c5Msg.contains("çatışma")
                items.add(
                    ChapterChoiceItem(
                        id = 5,
                        title = "5. PENCEREDEKİ GECE KARARI",
                        selectedOption = if (isDoubt) "Pencerede kaçış rotalarını düşünmeye devam ettin."
                                        else if (isConflicted) "İç çatışma ve derin kararsızlıkla şehre baktın."
                                        else "Pencerede temkinli bir iyimserlikle New York'u izledin.",
                        impactText = if (isDoubt) "Şüpheci ve kaçış odaklı tutumunu korudun."
                                    else if (isConflicted) "Kararsızlığını korudun, zamanın ne getireceğini beklemeye karar verdin."
                                    else "Avengers'a karşı umut dolu, olumlu bir eğilim kazandın.",
                        icon = if (!isDoubt) "💫" else "🔀",
                        isBranching = isDoubt,
                        isRelationship = false
                    )
                )
            }
        } else {
            // Chapter 1 Choices
            if (userMsgs.isNotEmpty()) {
                val c1Msg = userMsgs.getOrNull(0)?.text ?: ""
                val isC1Deep = c1Msg.contains("odaklan") || c1Msg.contains("derinden") || c1Msg.contains("2")
                items.add(
                    ChapterChoiceItem(
                        id = 1,
                        title = "1. GÖRÜYE YAKLAŞIM",
                        selectedOption = if (isC1Deep) "Görüye derinden odaklandın." else "Görüden zihnini geri çektin ve mesafeli kaldın.",
                        impactText = if (isC1Deep) {
                            "Ekstra detaylar elde ettin ancak bu zihinsel yük Aiden'a fiziksel yorgunluk ve burun kanaması verdi."
                        } else {
                            "Bu seçim Aiden'ın gücünü tasarruflu kullanma ve zihnini yıpratmama kararlılığını yansıttı, hikayeyi doğrudan değiştirmedi."
                        },
                        icon = "💭",
                        isBranching = false,
                        isRelationship = false
                    )
                )
            }

            if (userMsgs.size >= 2) {
                val c2Msg = userMsgs.getOrNull(1)?.text ?: ""
                val isC2Avoid = c2Msg.contains("Geri çekil") || c2Msg.contains("gitme") || c2Msg.contains("3")
                val isC2Observe = c2Msg.contains("gözlemle") || c2Msg.contains("uzaktan") || c2Msg.contains("2")
                val c2Selected = if (isC2Avoid) {
                    "Kule'ye girmemeyi ve geri çekilmeyi tercih ettin."
                } else if (isC2Observe) {
                    "Kule'yi önce dışarıdan gözlemledin, sonra ışınlandın."
                } else {
                    "Kule'ye direkt ışınlandın."
                }
                val c2Impact = if (isC2Avoid) {
                    "Avengers Tower'dan uzak durmayı denedin ancak S.H.I.E.L.D. uyduları uzam sapmanı tespit etti ve zorunlu bir portal dalgasıyla seni kuleye getirdi."
                } else if (isC2Observe) {
                    "Hesaplı ve temkinli bir giriş yaptın; bu tavır Natasha Romanoff'un takdirini kazandı."
                } else {
                    "Aniden ortaya çıkman odadakilerin reflekslerini tetikledi ve daha dürtüsel bir izlenim bıraktın."
                }
                items.add(
                    ChapterChoiceItem(
                        id = 2,
                        title = "2. KULE'YE GİRİŞ STRATEJİSİ",
                        selectedOption = c2Selected,
                        impactText = c2Impact,
                        icon = if (isC2Avoid) "🔀" else if (isC2Observe) "💫" else "💭",
                        isBranching = isC2Avoid,
                        isRelationship = isC2Observe
                    )
                )
            }

            if (userMsgs.size >= 3) {
                val c3Msg = userMsgs.getOrNull(2)?.text ?: ""
                val isC3Witty = c3Msg.contains("Alaycı") || c3Msg.contains("esprili") || c3Msg.contains("masraflı") || c3Msg.contains("2")
                val isC3Silent = c3Msg.contains("Sessizlik") || c3Msg.contains("Hiç isim") || c3Msg.contains("3")
                val c3Selected = if (isC3Witty) {
                    "Stark'a alaycı ve esprili bir yanıt verdin."
                } else if (isC3Silent) {
                    "Hiç isim vermeyip sessiz kaldın."
                } else {
                    "Soğuk ve mesafeli bir cevap verdin."
                }
                val c3Impact = if (isC3Witty) {
                    "Mizahi yaklaşımın Tony Stark'ın sempatisini kazandı ve ortamdaki gerilimi bir nebze düşürdü."
                } else if (isC3Silent) {
                    "Gizemli ve tekinsiz sessizliğin Steve Rogers'ın şüphesini daha da artırdı."
                } else {
                    "Bu seçim Aiden'ın zırhlarını koruma ve mesafeli durma tavrını yansıttı, hikayeyi doğrudan değiştirmedi."
                }
                items.add(
                    ChapterChoiceItem(
                        id = 3,
                        title = "3. TONY STARK İLE DİYALOG",
                        selectedOption = c3Selected,
                        impactText = c3Impact,
                        icon = if (isC3Witty || isC3Silent) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isC3Witty || isC3Silent
                    )
                )
            }

            if (userMsgs.size >= 4) {
                val c4Msg = userMsgs.getOrNull(3)?.text ?: ""
                val isC4Natasha = c4Msg.contains("Natasha") || c4Msg.contains("kızıl") || c4Msg.contains("1")
                val isC4Wanda = c4Msg.contains("Wanda") || c4Msg.contains("pencere") || c4Msg.contains("2")
                val c4Selected = if (isC4Natasha) {
                    "Bakışların Natasha Romanoff'un üzerinde kaldı."
                } else if (isC4Wanda) {
                    "Bakışların Wanda Maximoff'un üzerinde kaldı."
                } else {
                    "Kimseye özel bakış atmayıp odayı genel olarak taradın."
                }
                val c4Impact = if (isC4Natasha) {
                    "Natasha Romanoff ile aranızda sessiz ama güçlü bir diyalog ve görünmez bir bağ kuruldu."
                } else if (isC4Wanda) {
                    "Wanda Maximoff zihinsel dalgalanmanı hissetti ve aranızda ince bir merak filizlendi."
                } else {
                    "Bu seçim Aiden'ın o anki nötr ve profesyonel tavrını yansıttı, hikayeyi doğrudan değiştirmedi."
                }
                items.add(
                    ChapterChoiceItem(
                        id = 4,
                        title = "4. BAKIŞ ODAK NOKTASI",
                        selectedOption = c4Selected,
                        impactText = c4Impact,
                        icon = if (isC4Natasha || isC4Wanda) "💫" else "💭",
                        isBranching = false,
                        isRelationship = isC4Natasha || isC4Wanda
                    )
                )
            }
        }

        items
    }

    val relationshipTease = remember(userMsgs, isChapter2, isChapter3) {
        if (isChapter3) {
            "Otuz günlük deneme süresinde Avengers Tower'da yerini almaya başladın. Steve ile karşılıklı saygı, Natasha ile derin geçmiş anlayışı ve Wanda ile duygusal empati geliştirdin."
        } else if (isChapter2) {
            val c3Msg = userMsgs.getOrNull(6)?.text ?: ""
            val c4Msg = userMsgs.getOrNull(7)?.text ?: ""
            val natashaBond = c3Msg.contains("1") || c3Msg.contains("2") || c3Msg.contains("Sözlü") || c3Msg.contains("Sessiz")
            val wandaBond = c4Msg.contains("1") || c4Msg.contains("2") || c4Msg.contains("Kabul") || c4Msg.contains("zihin")
            when {
                natashaBond && wandaBond -> "Natasha ve Wanda ile derin bağlar kurdun. Kule'de hem gölgelerin anlayışını hem de duygusal sığınağı hissettin."
                natashaBond -> "Natasha Romanoff ile aranızdaki gizli bağ güçlendi. Gölgelerden gelen iki ajanın kaderi kesişiyor."
                wandaBond -> "Wanda Maximoff ile aranızda empati filizlendi. Geçmişin acılarını taşıyan iki güç sahibi birbirini anlıyor."
                else -> "Mesafeli duruşunu korudun; Avengers ekibi senin duvarlarını aşmak için zaman kolluyor."
            }
        } else {
            val c4Msg = userMsgs.getOrNull(3)?.text ?: ""
            when {
                c4Msg.contains("Natasha") || c4Msg.contains("kızıl") || c4Msg.contains("1") ->
                    "Natasha Romanoff ile aranızda özel bir bağ kuruldu. İlerleyen bölümlerde sana daha fazla güvenebilir."
                c4Msg.contains("Wanda") || c4Msg.contains("pencere") || c4Msg.contains("2") ->
                    "Wanda Maximoff'un ilgisini çektin. Sonraki bölümlerde zihinsel bağınız derinleşebilir."
                else -> null
            }
        }
    }

    // Auto Step Controller for Sequential Reveal (Life is Strange Pacing: Adım A ~1.8s, Adım B ~7.5s - 2.7s extra reading time)
    LaunchedEffect(viewMode, currentIndex) {
        if (viewMode == SummaryViewMode.SEQUENTIAL && choices.isNotEmpty()) {
            stepPhase = StepPhase.CARD_ONLY
            delay(1800) // ADIM A duration: ~1.8s (Breath & Read Action)
            if (viewMode == SummaryViewMode.SEQUENTIAL) {
                stepPhase = StepPhase.RESULT_REVEALED // ADIM B (Thread & Impact Reveal)
                delay(7500) // ADIM B duration: ~7.5s (2.7s extra time for deep impact reading)
                if (viewMode == SummaryViewMode.SEQUENTIAL) {
                    if (currentIndex < choices.lastIndex) {
                        currentIndex++
                    } else {
                        // Transition to Final Memory Thread after last choice
                        viewMode = SummaryViewMode.FINAL_GRID
                    }
                }
            }
        }
    }

    // FULL-SCREEN DIALOG WITH DARK CINEMATIC ATMOSPHERE & FADING AUDIO
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { safeExitWithFade(onDismiss) },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val currentChoice = choices.getOrNull(currentIndex)
        val characterAccentColor = remember(currentChoice) {
            if (currentChoice != null) {
                val text = (currentChoice.selectedOption + " " + currentChoice.impactText + " " + currentChoice.title).lowercase()
                when {
                    text.contains("natasha") || text.contains("romanoff") || text.contains("kızıl") || text.contains("red") -> Color(0xFFEF4444)
                    text.contains("wanda") || text.contains("scarlet") || text.contains("cadı") || text.contains("witch") -> Color(0xFFE11D48)
                    text.contains("tony") || text.contains("stark") || text.contains("zırh") || text.contains("iron") -> Color(0xFFF59E0B)
                    text.contains("steve") || text.contains("rogers") || text.contains("kaptan") || text.contains("captain") -> Color(0xFF3B82F6)
                    else -> Color(0xFF8B5CF6)
                }
            } else {
                Color(0xFF8B5CF6)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070814)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Cosmic Nebula & Character Specific Color Accent & Drifting Star Dust
                CosmicParticleBackground(accentColor = characterAccentColor)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    // TOP BAR: Minimal Dots Indicator & Close Button (Fixed Top Layer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (viewMode == SummaryViewMode.SEQUENTIAL && choices.isNotEmpty()) {
                            // Minimal Dots Progress Indicator with Pulse & Smooth Fill (Rule 7)
                            val infiniteTransition = rememberInfiniteTransition(label = "ActiveDotPulse")
                            val activeDotScale by infiniteTransition.animateFloat(
                                initialValue = 1.0f,
                                targetValue = 1.18f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "DotPulse"
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                choices.forEachIndexed { idx, _ ->
                                    val isActive = idx == currentIndex
                                    val isPast = idx < currentIndex
                                    val targetDotSize = if (isActive) 11.dp else 6.dp
                                    val targetDotColor = when {
                                        isActive -> Color(0xFFD8B4FE)
                                        isPast -> Color(0xFF8B5CF6)
                                        else -> Color(0x3594A3B8)
                                    }
                                    val animSize by androidx.compose.animation.core.animateDpAsState(
                                        targetValue = targetDotSize,
                                        animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                        label = "DotSizeAnim"
                                    )
                                    val animColor by androidx.compose.animation.animateColorAsState(
                                        targetValue = targetDotColor,
                                        animationSpec = tween(300),
                                        label = "DotColorAnim"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(animSize)
                                            .graphicsLayer {
                                                if (isActive) {
                                                    scaleX = activeDotScale
                                                    scaleY = activeDotScale
                                                }
                                            }
                                            .clip(CircleShape)
                                            .background(animColor)
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (viewMode == SummaryViewMode.PROMPT) "BÖLÜM $completedChapterNumber SEÇİM ÖZETİ" else "HAFIZA ŞERİDİ",
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Close (X) Button
                        IconButton(
                            onClick = { safeExitWithFade(onDismiss) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF231B3D))
                                .border(1.dp, Color(0x60A78BFA), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // MAIN CONTENT AREA (PERFECTLY CENTERED VERTICALLY AND HORIZONTALLY)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (viewMode) {
                            // ==================== INITIAL PROMPT SCREEN ====================
                            SummaryViewMode.PROMPT -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Bu bölümdeki seçimlerini görmek ister misin?",
                                        color = Color.White,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 31.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Verdiğin kararlar ve hikayeye olan etkileri Life is Strange tarzı sinematik reveal akışıyla sunulacak.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.5.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(36.dp))

                                    Button(
                                        onClick = {
                                            currentIndex = 0
                                            stepPhase = StepPhase.CARD_ONLY
                                            viewMode = SummaryViewMode.SEQUENTIAL
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("✨ Evet, Sırayla Göster", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedButton(
                                        onClick = { viewMode = SummaryViewMode.FINAL_GRID },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                    ) {
                                        Text("⚡ Atla (Direkt Hafıza Şeridi)", fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                                    }
                                }
                            }

                            // ==================== SEQUENTIAL CINEMATIC REVEAL ====================
                            SummaryViewMode.SEQUENTIAL -> {
                                if (currentChoice != null) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CinematicSequentialReveal(
                                            choice = currentChoice,
                                            phase = stepPhase
                                        )

                                        Spacer(modifier = Modifier.height(32.dp))

                                        TextButton(
                                            onClick = { viewMode = SummaryViewMode.FINAL_GRID }
                                        ) {
                                            Text(
                                                text = "⚡ Tümünü Göster / Atla",
                                                color = Color(0xB3A78BFA),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // ==================== FINAL MEMORY THREAD TIMELINE ====================
                            SummaryViewMode.FINAL_GRID -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Kaderin Halkaları — Seçim Anıları",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "Verdiğin kararların zaman akışında bıraktığı silinmez izler:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.5.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                    )

                                    MemoryThreadTimeline(
                                        choices = choices,
                                        relationshipTease = relationshipTease
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            safeExitWithFade {
                                                if (isChapter3) {
                                                    android.widget.Toast.makeText(context, "Bölüm 4 yapım aşamasındadır. Çok yakında yayınlanacak!", android.widget.Toast.LENGTH_LONG).show()
                                                } else {
                                                    onDismiss()
                                                    onNextChapter?.invoke()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                if (isChapter3) "✨ Bölüm 3 Tamamlandı (Bölüm 4 Yakında)" else if (isChapter2) "🚀 Bölüm 3'e Geç" else "🚀 Bölüm 2'ye Geç",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            safeExitWithFade {
                                                onDismiss()
                                                onRestartChapter()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C1D95)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(
                                            if (isChapter3) "🔄 Bölüm 3'ü Tekrar Oyna" else if (isChapter2) "🔄 Bölüm 2'yi Tekrar Oyna" else "🔄 Bölüm 1'i Tekrar Oyna",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            safeExitWithFade { onDismiss() }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B38)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382F5E)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Özeti Kapat",
                                                color = Color(0xFFCBD5E1),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CosmicParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val speed: Float
)

@Composable
fun CosmicParticleBackground(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CosmicParticleDrift")

    // Rule 2: Soft radial glow breath loop (scale 0.9 -> 1.05 -> 1.0, 4.5s FastOutSlowIn)
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DriftAnim"
    )

    val particles = remember {
        List(35) {
            CosmicParticle(
                xRatio = (0..100).random() / 100f,
                yRatio = (0..100).random() / 100f,
                radius = (15..45).random() / 10f,
                baseAlpha = (12..28).random() / 100f,
                speed = 0.4f + (0..100).random() / 100f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Broad Center Radial Glow with Character Accent Color & Breathing Scale (Rule 2)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            Color(0x208B5CF6),
                            Color(0x080F172A),
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. Drifting Star Dust / Cosmic Energy Particles Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                val currentY = ((p.yRatio - animProgress * p.speed * 0.35f) % 1f + 1f) % 1f
                val currentX = (p.xRatio + kotlin.math.sin((animProgress * 6.28318f + p.yRatio * 12f).toDouble()).toFloat() * 0.04f) % 1f

                drawCircle(
                    color = accentColor.copy(alpha = p.baseAlpha),
                    radius = p.radius.dp.toPx(),
                    center = Offset(currentX * width, currentY * height)
                )
            }
        }

        // 3. Top Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xEE070814), Color.Transparent)
                    )
                )
        )

        // 4. Bottom Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xEE070814))
                    )
                )
        )
    }
}

@Composable
fun PulsingLoadingText(
    modifier: Modifier = Modifier
) {
    // Rule 3: 2px fine progress line filling from left to right over 1.5s + shimmer text
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerLine")
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LineFill"
    )

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8B4FE).copy(alpha = shimmerAlpha))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sonuçlar dokunuyor...",
                color = Color(0xFFE9D5FF).copy(alpha = shimmerAlpha),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                letterSpacing = 0.9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2px sleek progress fill bar (Rule 3)
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0x308B5CF6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(lineProgress)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFFD8B4FE))
                        )
                    )
            )
        }
    }
}

@Composable
fun CinematicSequentialReveal(
    choice: ChapterChoiceItem,
    phase: StepPhase
) {
    // Screen Shake effect for Branching choices
    val shakeAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(phase) {
        if (phase == StepPhase.RESULT_REVEALED && choice.isBranching) {
            repeat(3) {
                shakeAnim.animateTo(4f, androidx.compose.animation.core.tween(30))
                shakeAnim.animateTo(-4f, androidx.compose.animation.core.tween(30))
            }
            shakeAnim.animateTo(0f, androidx.compose.animation.core.tween(30))
        }
    }

    // Rule 4: Overshoot Elastic Icon Reveal (scale 0 -> 1.35 -> 1.0)
    val iconScaleAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(phase) {
        if (phase == StepPhase.RESULT_REVEALED) {
            iconScaleAnim.snapTo(0f)
            iconScaleAnim.animateTo(
                targetValue = 1.35f,
                animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
            iconScaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(160, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
    }

    val impactColor = when {
        choice.isBranching -> Color(0xFFF59E0B) // Gold / Amber
        choice.isRelationship -> Color(0xFFF472B6) // Pink / Magenta
        else -> Color(0xFFCBD5E1) // Slate / Neutral
    }

    // Rule 5: Mood Shift Smooth Color Accent (800ms transition)
    val animatedImpactColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (phase == StepPhase.RESULT_REVEALED) impactColor else Color(0xFF8B5CF6),
        animationSpec = tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "MoodShiftColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(x = shakeAnim.value.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. ADIM A: Small, subtle top title (e.g., "4. BAKIŞ ODAK NOKTASI")
        Text(
            text = choice.title.uppercase(),
            color = Color(0xFFA78BFA),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. ADIM A (Rule 1 & 8): Selected Option (opacity 0->1, y: +25px->0, 600ms ease-out, 150ms delay)
        // Rule 8: Exit with scale 1->0.95 + fade out
        androidx.compose.animation.AnimatedContent(
            targetState = choice.selectedOption,
            transitionSpec = {
                (androidx.compose.animation.fadeIn(animationSpec = tween(600, delayMillis = 150, easing = androidx.compose.animation.core.FastOutSlowInEasing)) +
                        androidx.compose.animation.slideInVertically(
                            initialOffsetY = { 25 },
                            animationSpec = tween(600, delayMillis = 150, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                        ) +
                        androidx.compose.animation.scaleIn(initialScale = 0.96f, animationSpec = tween(600, delayMillis = 150))) togetherWith
                        (androidx.compose.animation.fadeOut(animationSpec = tween(300, easing = androidx.compose.animation.core.FastOutLinearInEasing)) +
                                androidx.compose.animation.scaleOut(targetScale = 0.95f, animationSpec = tween(300)))
            },
            label = "ActionTextAnim"
        ) { text ->
            Text(
                text = text,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (phase == StepPhase.CARD_ONLY) {
            // 3. ADIM A: Vivid Pulsing Loading Indicator Text
            PulsingLoadingText()
        } else {
            // GEÇİŞ & ADIM B: Light Thread + Impact Reveal
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(600)) +
                        androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Dikey Işık Huzmesi (Connecting Light Thread)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(44.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8B5CF6), animatedImpactColor)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pulse Icon & Impact Tag & Consequence Text (Floating freely in space)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = choice.icon,
                                fontSize = 23.sp,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScaleAnim.value
                                    scaleY = iconScaleAnim.value
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    choice.isBranching -> "💥 BÜYÜK DALLANMA ETKİSİ"
                                    choice.isRelationship -> "💕 İLİŞKİ DOKUNUŞU"
                                    else -> "💭 TAVIR ETKİSİ"
                                },
                                color = animatedImpactColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = choice.impactText,
                            color = Color(0xFFF1F5F9),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 23.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryThreadTimeline(
    choices: List<ChapterChoiceItem>,
    relationshipTease: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Continuous Vertical Thread Line
        Box(
            modifier = Modifier
                .padding(start = 11.dp, top = 12.dp, bottom = 12.dp)
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF8B5CF6), Color(0xFFC084FC), Color(0x308B5CF6))
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            choices.forEachIndexed { idx, choice ->
                // Staggered animated appearance
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(idx * 90L)
                    isVisible = true
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(400)) +
                            androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(400)) { 20 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Node Dot on Timeline
                        val nodeColor = when {
                            choice.isBranching -> Color(0xFFF59E0B)
                            choice.isRelationship -> Color(0xFFF472B6)
                            else -> Color(0xFFA78BFA)
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(nodeColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Timeline Item Content (Clean, unboxed composition)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = choice.title,
                                color = Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = choice.selectedOption,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(choice.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = choice.impactText,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            if (relationshipTease != null) {
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(choices.size * 90L + 100L)
                    isVisible = true
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(400)) +
                            androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(400)) { 20 }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEC4899))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔮 İLERLEYEN BÖLÜM ETKİSİ",
                                color = Color(0xFFF472B6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = relationshipTease,
                                color = Color(0xFFFBCFE8),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChoiceCard(
    title: String,
    choiceText: String,
    impact: String,
    icon: String = "💭",
    isBranching: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isBranching) Color(0xFF2C192E) else Color(0xFF181728)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isBranching) Color(0xFFF43F5E) else Color(0xFF382F58)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = if (isBranching) Color(0xFFFB7185) else Color(0xFFA78BFA),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = choiceText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = if (isBranching) Color(0xFF3B1220) else Color(0xFF121220),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text(
                    text = impact,
                    color = if (isBranching) Color(0xFFFECDD3) else Color(0xFFCBD5E1),
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun BookReaderView(
    bot: com.example.data.local.BotEntity,
    messages: List<com.example.data.local.MessageEntity>,
    isSending: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEnsureOpeningMessage: () -> Unit,
    onResetChat: (Boolean) -> Unit,
    onClearError: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var fontSizeSp by remember { mutableStateOf(16.sp) }
    var showSummaryModal by remember { mutableStateOf(false) }
    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }
    var lastProcessedMsgCount by rememberSaveable { mutableIntStateOf(0) }

    var isAmbientMusicPlaying by remember { mutableStateOf(false) }
    var ambientMediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    fun toggleAmbientMusic() {
        if (isAmbientMusicPlaying) {
            try {
                ambientMediaPlayer?.let { p ->
                    if (p.isPlaying) p.stop()
                    p.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Error stopping player on toggle off", e)
            }
            ambientMediaPlayer = null
            isAmbientMusicPlaying = false
        } else {
            try {
                ambientMediaPlayer?.let { p ->
                    if (p.isPlaying) p.stop()
                    p.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Error releasing old player on toggle on", e)
            }
            ambientMediaPlayer = null

            try {
                val p = android.media.MediaPlayer.create(context, com.example.R.raw.bolumsonu)
                if (p != null) {
                    p.setOnErrorListener { _, what, extra ->
                        android.util.Log.e("BookReaderView", "bolumsonu.mp3 toggle error: what=$what, extra=$extra")
                        true
                    }
                    p.isLooping = true
                    p.setVolume(0.85f, 0.85f)
                    p.start()
                    ambientMediaPlayer = p
                    isAmbientMusicPlaying = true
                } else {
                    android.util.Log.e("BookReaderView", "MediaPlayer.create returned null during toggleAmbientMusic")
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Exception in toggleAmbientMusic", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ambientMediaPlayer?.let { p ->
                    if (p.isPlaying) p.stop()
                    p.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Error releasing ambient player on dispose", e)
            }
            ambientMediaPlayer = null
        }
    }

    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            if (messages.size > lastProcessedMsgCount && lastProcessedMsgCount > 0) {
                listState.animateScrollToItem(messages.lastIndex)
            }
            lastProcessedMsgCount = messages.size
        }
    }

    val lastAssistantMsg = remember(messages) {
        messages.lastOrNull { it.role == "assistant" || it.role == "model" }
    }

    val activeChoices = remember(lastAssistantMsg?.text) {
        if (lastAssistantMsg != null) parseBookChoiceOptions(lastAssistantMsg.text) else emptyList()
    }

    val isChapterCompleted = remember(lastAssistantMsg?.text) {
        val txt = lastAssistantMsg?.text ?: ""
        txt.contains("BÖLÜM 1 SONU") || txt.contains("BÖLÜM 2 SONU") || txt.contains("BÖLÜM 3 SONU") ||
        txt.contains("BÖLÜM TAMAMLANDI") || txt.contains("Tebrikler! Bölüm") ||
        (txt.contains("BÖLÜM") && txt.contains("SONU"))
    }

    LaunchedEffect(isChapterCompleted) {
        if (isChapterCompleted) {
            try {
                ambientMediaPlayer?.let { p ->
                    if (p.isPlaying) p.stop()
                    p.release()
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Error releasing previous MediaPlayer on chapter end", e)
            }
            ambientMediaPlayer = null

            try {
                val player = android.media.MediaPlayer.create(context, com.example.R.raw.bolumsonu)
                if (player != null) {
                    player.setOnErrorListener { _, what, extra ->
                        android.util.Log.e("BookReaderView", "bolumsonu.mp3 chapter-end error: what=$what, extra=$extra")
                        true
                    }
                    player.isLooping = true
                    player.setVolume(0.85f, 0.85f)
                    player.start()
                    ambientMediaPlayer = player
                    isAmbientMusicPlaying = true
                    android.util.Log.d("BookReaderView", "bolumsonu.mp3 playing successfully on chapter completed!")
                } else {
                    android.util.Log.e("BookReaderView", "MediaPlayer.create returned null for bolumsonu.mp3 on chapter completed")
                }
            } catch (e: Exception) {
                android.util.Log.e("BookReaderView", "Exception starting chapter end music", e)
            }
        }
    }

    val completedChapterTitle = remember(lastAssistantMsg?.text) {
        val txt = lastAssistantMsg?.text ?: ""
        when {
            txt.contains("BÖLÜM 3 SONU") -> "📖 BÖLÜM 3 TAMAMLANDI"
            txt.contains("BÖLÜM 2 SONU") -> "📖 BÖLÜM 2 TAMAMLANDI"
            else -> "📖 BÖLÜM 1 TAMAMLANDI"
        }
    }

    val completedChapterDesc = remember(lastAssistantMsg?.text) {
        val txt = lastAssistantMsg?.text ?: ""
        when {
            txt.contains("BÖLÜM 3 SONU") -> "Tebrikler! Verdiğin kararlarla Bölüm 3'ün sonuna ulaştın."
            txt.contains("BÖLÜM 2 SONU") -> "Tebrikler! Verdiğin kararlarla Bölüm 2'nin sonuna ulaştın."
            else -> "Tebrikler! Verdiğin kararlarla Bölüm 1'in sonuna ulaştın."
        }
    }

    if (showSummaryModal) {
        ChapterSummaryModal(
            messages = messages,
            onDismiss = { showSummaryModal = false },
            onRestartChapter = { onResetChat(true) },
            onNextChapter = {
                showSummaryModal = false
                val userCount = messages.filter { it.role == "user" }.size
                val lastMsgText = lastAssistantMsg?.text ?: ""
                if (lastMsgText.contains("BÖLÜM 3 SONU") || userCount >= 19) {
                    android.widget.Toast.makeText(context, "Bölüm 4 yapım aşamasındadır. Çok yakında yayınlanacak!", android.widget.Toast.LENGTH_LONG).show()
                } else if (lastMsgText.contains("BÖLÜM 2 SONU") || userCount in 5..10) {
                    onSendMessage("Bölüm 3: Deneme Süresi'ne başla")
                } else {
                    onSendMessage("Bölüm 2: Şartlar'a başla")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D17))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP BOOK NAVIGATION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141527))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF20223D))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kitaplıktan Çık", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = bot.aiName.ifBlank { "Kozmik Sürükleniş" },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "📖 İnteraktif Roman Okuyucu",
                        color = Color(0xFFA78BFA),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF20223D))
                        .clickable {
                            fontSizeSp = when (fontSizeSp) {
                                14.sp -> 16.sp
                                16.sp -> 18.sp
                                18.sp -> 20.sp
                                else -> 14.sp
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "A ${fontSizeSp.value.toInt()}pt",
                        color = Color(0xFFD8B4FE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { toggleAmbientMusic() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAmbientMusicPlaying) Color(0xFF3B1233) else Color(0xFF20223D))
                ) {
                    Text(
                        text = if (isAmbientMusicPlaying) "🎵" else "🔇",
                        fontSize = 16.sp
                    )
                }

                IconButton(
                    onClick = { onResetChat(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2D162C))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Baştan Başla", tint = Color(0xFFFF758F), modifier = Modifier.size(18.dp))
                }
            }
        }

        errorMessage?.let { err ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3A1414))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(err, color = Color(0xFFFF758F), fontSize = 12.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color(0xFFFF758F), modifier = Modifier.size(16.dp))
                }
            }
        }

        // NOVEL CANVAS BODY
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .border(1.dp, Color(0x30A78BFA), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF10111D))
        ) {
            if (messages.isEmpty() && !isSending) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "📖 Kitap Sayfası Hazırlanıyor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bölüm 1 metnini yüklemek için aşağıdaki butona dokunun.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onEnsureOpeningMessage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📖 Bölüm 1'i Başlat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(messages, key = { _, msg -> msg.id }) { _, msg ->
                        if (msg.role == "user") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1731)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80F59E0B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "✦", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "VERİLEN KARAR / SEÇİM:",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = msg.text.replace(Regex("^(1️⃣|2️⃣|3️⃣|4️⃣|\\d+[\\.\\)]|[-•*])\\s*"), ""),
                                                color = Color(0xFFFDE68A),
                                                fontSize = (fontSizeSp.value - 1f).sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val msgChoices = parseBookChoiceOptions(msg.text)
                            val rawChoiceLines = msgChoices.map { it.rawLine.trim() }.toSet()
                            val paragraphs = msg.text.split("\n\n")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                paragraphs.forEach { paragraph ->
                                    val trimmed = paragraph.trim()
                                    val lines = trimmed.lines().map { it.trim() }
                                    // Hide paragraph if it consists purely of choice options or choice prompt header
                                    val isChoiceBlock = lines.all { line ->
                                        line.isBlank() || rawChoiceLines.contains(line) || line.startsWith("🔀") || line.startsWith("---") || line.matches(Regex("^(1️⃣|2️⃣|3️⃣|4️⃣|5️⃣|6️⃣|\\d+[\\.\\)]|[-•*])\\s*.*"))
                                    }
                                    if (isChoiceBlock && msgChoices.isNotEmpty()) {
                                        return@forEach
                                    }

                                    if (trimmed.startsWith("#") || trimmed.startsWith("📖 BÖLÜM")) {
                                        Text(
                                            text = trimmed.replace("#", "").trim(),
                                            color = Color(0xFFE9D5FF),
                                            fontSize = (fontSizeSp.value + 4f).sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = (fontSizeSp.value * 1.6f).sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else if (trimmed.startsWith("---")) {
                                        HorizontalDivider(color = Color(0x40A78BFA), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    } else {
                                        Text(
                                            text = trimmed,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = fontSizeSp,
                                            lineHeight = (fontSizeSp.value * 1.55f).sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isSending) {
                        item(key = "sending_book_progress") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF18152E)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA78BFA)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFA78BFA),
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "📖 Kararın hikayeye işleniyor, sonraki sayfa yazılıyor...",
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (activeChoices.isNotEmpty() && !isSending) {
                        item(key = "interactive_choices_section") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF18162B)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF3B82F6)))),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔀", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SENİN KARARIN (SEÇİM NOKTASI):",
                                            color = Color(0xFFA78BFA),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    activeChoices.forEach { choice ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252140)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onSendMessage(choice.rawLine)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF3B1065)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("✦", color = Color(0xFFD8B4FE), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = choice.text,
                                                    color = Color.White,
                                                    fontSize = (fontSizeSp.value - 0.5f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = Color(0xFFA78BFA),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isChapterCompleted && !isSending) {
                        item(key = "chapter_end_card") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF24162F)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(completedChapterTitle, color = Color(0xFF34D399), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = completedChapterDesc,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 13.5.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showSummaryModal = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text("✨ BÖLÜMÜ BİTİR VE SEÇİM ÖZETİNİ GÖR", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}