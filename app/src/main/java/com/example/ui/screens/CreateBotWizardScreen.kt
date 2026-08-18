package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.local.EmotionState
import com.example.data.local.WorldAtmosphere
import com.example.data.repository.KeyCharacter
import com.example.ui.components.AppBackground
import com.example.ui.components.OrbView
import com.example.ui.theme.EmochiError
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

@Composable
fun CreateBotWizardScreen(
    onBack: () -> Unit,
    onFinish: (BotEntity) -> Unit,
    onGenerateOpening: suspend (BotEntity) -> String
) {
    var stepIdx by remember { mutableIntStateOf(0) }

    var mode by remember { mutableStateOf("personal") }
    var aiName by remember { mutableStateOf("") }
    var aiPersonality by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var pinnedMemory by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("") }
    var universeName by remember { mutableStateOf("") }
    var userCharName by remember { mutableStateOf("") }
    var userCharDesc by remember { mutableStateOf("") }
    var openingMessage by remember { mutableStateOf("") }
    var writingStyle by remember { mutableStateOf("rp") }
    var intensity by remember { mutableStateOf("normal") }
    var isPublic by remember { mutableStateOf(true) }

    var keyCharacters by remember { mutableStateOf<List<KeyCharacter>>(emptyList()) }

    var isGeneratingOpening by remember { mutableStateOf(false) }
    var genError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { inputUri ->
            scope.launch {
                val savedUrl = com.example.util.ImageStorageManager.compressAndSaveImage(context, inputUri)
                avatarUrl = savedUrl
            }
        }
    }

    val totalSteps = 5

    fun buildCurrentDraft(): BotEntity {
        val serializedCast = if (keyCharacters.isEmpty()) "[]" else {
            val array = org.json.JSONArray()
            for (char in keyCharacters) {
                val obj = org.json.JSONObject()
                obj.put("id", char.id)
                obj.put("name", char.name)
                obj.put("desc", char.desc)
                array.put(obj)
            }
            array.toString()
        }
        val baselineEmotion = EmotionState.calculateBaselineEmotionState(
            aiName = if (mode == "personal") aiName.trim() else universeName.trim(),
            personality = aiPersonality.trim(),
            scenario = scenario.trim(),
            userCharName = userCharName.trim(),
            userCharDesc = userCharDesc.trim()
        ).toJson()

        val initialWorldAtmosphere = if (mode == "universe") {
            val intensityInt = when (intensity) {
                "düşük" -> 3
                "yüksek" -> 8
                else -> 5
            }
            WorldAtmosphere(
                mood = "sakin ve beklentili",
                intensity = intensityInt,
                currentEvent = "Açılış sahnesi ve hikayenin başlangıcı",
                macroAtmosphere = "${universeName.trim()} evreninin genel düzeni ve toplumsal kuralları hakim.",
                microAtmosphere = "Hikayenin başladığı fiziki mekan ve başlangıç ortamı."
            ).toJson()
        } else ""

        return BotEntity(
            id = UUID.randomUUID().toString(),
            mode = mode,
            aiName = aiName.trim(),
            aiPersonality = aiPersonality.trim(),
            avatarUrl = avatarUrl.trim(),
            pinnedMemory = pinnedMemory.trim(),
            scenario = scenario.trim(),
            universeName = universeName.trim(),
            keyCharactersJson = serializedCast,
            userCharName = userCharName.trim().ifBlank { "Kullanıcı" },
            userCharDesc = userCharDesc.trim(),
            openingMessage = openingMessage.trim(),
            writingStyle = writingStyle,
            intensity = intensity,
            customLength = "default",
            isNsfw = true,
            isPublic = isPublic,
            emotionState = baselineEmotion,
            previousEmotionState = baselineEmotion,
            worldAtmosphere = initialWorldAtmosphere,
            updatedAt = System.currentTimeMillis()
        )
    }

    val canNext = when (stepIdx) {
        0 -> true
        1 -> if (mode == "personal") aiName.isNotBlank() && aiPersonality.isNotBlank() else universeName.isNotBlank()
        2 -> scenario.isNotBlank() && userCharName.isNotBlank()
        3 -> openingMessage.isNotBlank()
        4 -> true
        else -> true
    }

    // Glow and pulse animations for cosmic effects
    val infiniteTransition = rememberInfiniteTransition(label = "wizardAnimations")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val buttonPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonPulse"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBackground()

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x601A0D38))
                            .border(1.dp, Color(0x40C084FC), CircleShape)
                            .clickable {
                                if (stepIdx > 0) stepIdx-- else onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bot Oluştur ",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${stepIdx + 1}/$totalSteps)",
                            color = Color(0xFFC084FC),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Geri Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                BorderStroke(1.2.dp, Color(0x607C3AED)),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(Color(0x4013082A))
                            .clickable {
                                if (stepIdx > 0) stepIdx-- else onBack()
                            }
                            .padding(horizontal = 22.dp, vertical = 11.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Geri",
                                color = Color(0xFFE9D5FF),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right İleri / Tamamla Pill Button
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = if (canNext) buttonPulseScale else 1f
                                scaleY = if (canNext) buttonPulseScale else 1f
                            }
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                BorderStroke(
                                    width = 1.3.dp,
                                    brush = if (canNext) {
                                        Brush.sweepGradient(
                                            listOf(Color(0xFFFFFFFF), Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFFFFFFFF))
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(Color(0x307C3AED), Color(0x203B0764)))
                                    }
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(
                                if (canNext) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFD8B4FE), Color(0xFF9333EA), Color(0xFF581C87))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0x403B0764), Color(0x301E1035))
                                    )
                                }
                            )
                            .clickable(enabled = canNext) {
                                if (stepIdx < totalSteps - 1) {
                                    stepIdx++
                                } else {
                                    onFinish(buildCurrentDraft())
                                }
                            }
                            .padding(horizontal = 26.dp, vertical = 11.dp)
                            .testTag("wizard_next_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (stepIdx == totalSteps - 1) "Tamamla" else "İleri",
                                color = if (canNext) Color.White else Color(0x60A78BFA),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (stepIdx == totalSteps - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (canNext) Color.White else Color(0x60A78BFA),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 3D Glowing Purple Orb Widget
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFC084FC).copy(alpha = pulseGlowAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    OrbView(hue = 275f, size = 56.dp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (stepIdx) {
                    0 -> {
                        // Step 1: Mode Selection matching screenshot 1/5
                        Text(
                            text = "Nasıl bir etkileşim istersiniz?",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI karakter tipini ve hikaye yapısını seçin.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        WizardModeCard(
                            title = "Kişisel Karakter",
                            subtitle1 = "Sana özel bire bir sohbet.",
                            subtitle2 = "Tek ve sabit bir karakterle gerçek zamanlı bağ.",
                            iconVector = Icons.Default.Person,
                            isSelected = mode == "personal",
                            glowAlpha = pulseGlowAlpha,
                            onClick = { mode = "personal" }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        WizardModeCard(
                            title = "Evren / Hikaye (RP)",
                            subtitle1 = "Kurgusal bir dünyada geçen roleplay.",
                            subtitle2 = "AI evrenin anlatıcısı olur ve kadroyu yönetir.",
                            iconVector = Icons.Default.Book,
                            isSelected = mode == "universe",
                            glowAlpha = pulseGlowAlpha,
                            onClick = { mode = "universe" }
                        )
                    }

                    1 -> {
                        // Step 2: Universe & Characters matching screenshot 2/5
                        if (mode == "personal") {
                            Text(
                                text = "Karakter Detayları",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Karşınızdaki karakterin resmini, adını ve kişiliğini tanımlayın.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Avatar Upload Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0x800E0620))
                                    .border(1.2.dp, Color(0x507C3AED), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF23104D))
                                            .border(1.5.dp, Color(0xFFC084FC), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarUrl.isNotBlank()) {
                                            coil.compose.AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = "Avatar",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Bot Profil Fotoğrafı",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Galeriden kapak resmi yükle",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFF7C3AED), Color(0xFF581C87))
                                                        )
                                                    )
                                                    .clickable { photoPickerLauncher.launch("image/*") }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    "🖼️ Fotoğraf Seç",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (avatarUrl.isNotBlank()) {
                                                TextButton(
                                                    onClick = { avatarUrl = "" },
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Sil", fontSize = 11.sp, color = EmochiError)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Karakter Adı *",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = aiName,
                                onValueChange = { aiName = it },
                                placeholder = { Text("ör. Aiden", color = Color(0x80A78BFA)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_name_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = customWizardTextFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                "Kişilik ve Özellikler *",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = aiPersonality,
                                onValueChange = { aiPersonality = it },
                                placeholder = { Text("Nazik mi, korumacı mı, esprili mi, konuşma tarzı nasıl...", color = Color(0x80A78BFA)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("ai_personality_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = customWizardTextFieldColors()
                            )
                        } else {
                            Text(
                                text = "Evren ve Karakter Kadrosu",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kurgusal dünyanın adını ve ana karakter kadrosunu belirleyin.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "Evren Adı *",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = universeName,
                                onValueChange = { universeName = it },
                                placeholder = { Text("Ör. Karanlık Akademi", color = Color(0x80A78BFA)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = customWizardTextFieldColors(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                "Ana Karakter Kadrosu (Opsiyonel)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            keyCharacters.forEachIndexed { idx, char ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = char.name,
                                        onValueChange = { newName ->
                                            keyCharacters = keyCharacters.toMutableList().apply {
                                                this[idx] = this[idx].copy(name = newName)
                                            }
                                        },
                                        placeholder = { Text("İsim", fontSize = 12.sp, color = Color(0x80A78BFA)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = customWizardTextFieldColors(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = char.desc,
                                        onValueChange = { newDesc ->
                                            keyCharacters = keyCharacters.toMutableList().apply {
                                                this[idx] = this[idx].copy(desc = newDesc)
                                            }
                                        },
                                        placeholder = { Text("Tanım", fontSize = 12.sp, color = Color(0x80A78BFA)) },
                                        modifier = Modifier.weight(1.8f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = customWizardTextFieldColors(),
                                        singleLine = true
                                    )
                                    IconButton(onClick = {
                                        keyCharacters = keyCharacters.toMutableList().apply { removeAt(idx) }
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Sil", tint = EmochiError, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Add character outline button matching screenshot 2/5
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        BorderStroke(1.2.dp, Color(0x607C3AED)),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(Color(0x3013082A))
                                    .clickable {
                                        keyCharacters = keyCharacters + KeyCharacter(name = "", desc = "")
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Karakter Ekle",
                                        color = Color(0xFFC084FC),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 3: Scenario & User Character matching screenshot 3/5
                        Text(
                            text = "Senaryo & Kendi Karakteriniz",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sahneye giriş bağlamını ve kendi karakterinizi tanımlayın.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Senaryo / Bağlam *",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = scenario,
                            onValueChange = { scenario = it },
                            placeholder = {
                                Text(
                                    "Neredesiniz, aranızdaki ilişki ne, gerilim veya durum nasıl başladı...",
                                    color = Color(0x80A78BFA)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("scenario_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = customWizardTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Sizin Canlandırdığınız Karakterin Adı *",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = userCharName,
                            onValueChange = { userCharName = it },
                            placeholder = { Text("ör. Zeynep", color = Color(0x80A78BFA)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_char_name_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = customWizardTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Karakterinizin Kısa Açıklaması (Opsiyonel)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = userCharDesc,
                            onValueChange = { userCharDesc = it },
                            placeholder = { Text("Yaş, görünüş, kişilik özellikleri...", color = Color(0x80A78BFA)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = customWizardTextFieldColors()
                        )
                    }

                    3 -> {
                        // Step 4: Opening Scene matching screenshot 4/5
                        Text(
                            text = "Başlangıç Sahnesi",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sohbet bu açılış mesajı/sahnesi ile başlayacak.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = openingMessage,
                                onValueChange = { if (it.length <= 1000) openingMessage = it },
                                placeholder = {
                                    Text(
                                        "Açılış mesajını kendiniz yazın veya aşağıdaki yapay zeka butonunu kullanın...",
                                        color = Color(0x80A78BFA)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .testTag("opening_message_input"),
                                shape = RoundedCornerShape(18.dp),
                                colors = customWizardTextFieldColors()
                            )

                            // Counter 0/1000 inside bottom right
                            Text(
                                text = "${openingMessage.length}/1000",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AI Auto-write Button matching screenshot 4/5
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(1.3.dp, Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(Color(0x801A0D38))
                                .clickable(enabled = !isGeneratingOpening) {
                                    scope.launch {
                                        isGeneratingOpening = true
                                        genError = null
                                        try {
                                            val generated = onGenerateOpening(buildCurrentDraft())
                                            openingMessage = generated
                                        } catch (e: Exception) {
                                            genError = e.message ?: "Oluşturulamadı."
                                        } finally {
                                            isGeneratingOpening = false
                                        }
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGeneratingOpening) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Yapay Zeka Yazıyor...",
                                        color = Color(0xFFC084FC),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "AI Otomatik Yazsın",
                                        color = Color(0xFFC084FC),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        genError?.let { err ->
                            Text(
                                text = err,
                                color = EmochiError,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    4 -> {
                        // Step 5: Writing Style & Tone matching screenshot 5/5
                        Text(
                            text = "Yazım Tarzı & Ton",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yanıtların biçimini ve tematik yoğunluğunu özelleştirin.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Yazım Formatı",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WizardOptionCard(
                            title = "RP / Anlatı Tarzı",
                            desc = "Üçüncü şahıs roman anlatımı. Ortam ve mimik betimlemeleri + diyaloglar.",
                            iconVector = Icons.Default.Book,
                            isSelected = writingStyle == "rp",
                            onClick = { writingStyle = "rp" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WizardOptionCard(
                            title = "Sade Sohbet",
                            desc = "Doğal mesajlaşma tarzı. Kısa ve samimi yanıtlar.",
                            iconVector = Icons.Default.Person,
                            isSelected = writingStyle == "chat",
                            onClick = { writingStyle = "chat" }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            "Tema Yoğunluğu",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WizardOptionCard(
                            title = "Normal",
                            desc = "Dengeli ve yumuşak ton.",
                            iconVector = Icons.Default.Settings,
                            isSelected = intensity == "normal",
                            onClick = { intensity = "normal" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WizardOptionCard(
                            title = "Yoğun",
                            desc = "Gerilimi ve karanlık temalar yumuşatılmadan aktarılır (Ginsel içerik hariç).",
                            iconVector = Icons.Default.AutoAwesome,
                            isSelected = intensity == "intense",
                            onClick = { intensity = "intense" }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            "Görünürlük & Yayınlama",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        WizardOptionCard(
                            title = "🌐 Herkese Açık (Keşfette Yayınla)",
                            desc = "Botunuz Velora Keşfet sekmesinde tüm toplulukla paylaşılır.",
                            iconVector = Icons.Default.Public,
                            isSelected = isPublic,
                            onClick = { isPublic = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WizardOptionCard(
                            title = "🔒 Sadece Bana Özel",
                            desc = "Bot sadece sizin cihazınızda ve sohbetlerinizde gözükür.",
                            iconVector = Icons.Default.Lock,
                            isSelected = !isPublic,
                            onClick = { isPublic = false }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardModeCard(
    title: String,
    subtitle1: String,
    subtitle2: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFFE9D5FF),
                            Color(0xFFC084FC),
                            Color(0xFF7C3AED),
                            Color(0xFFE9D5FF)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color(0x307C3AED), Color(0x203B0764)))
                },
                shape = cardShape
            )
            .clip(cardShape)
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(Color(0x952E105C), Color(0x95150833))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0x800E0620), Color(0x80080314))
                    )
                }
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon Square
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0x603B1065) else Color(0x3023104D))
                    .border(
                        1.2.dp,
                        if (isSelected) Color(0xFFC084FC) else Color(0x307C3AED),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFC084FC) else Color(0x80A78BFA),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle1,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = subtitle2,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFC084FC) else Color(0x50A78BFA),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WizardOptionCard(
    title: String,
    desc: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFFE9D5FF),
                            Color(0xFFC084FC),
                            Color(0xFF7C3AED),
                            Color(0xFFE9D5FF)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color(0x307C3AED), Color(0x203B0764)))
                },
                shape = cardShape
            )
            .clip(cardShape)
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(Color(0x902E105C), Color(0x90150833))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0x800E0620), Color(0x80080314))
                    )
                }
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0x603B1065) else Color(0x3023104D))
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFFC084FC) else Color(0x307C3AED),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFC084FC) else Color(0x80A78BFA),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = desc,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Checkmark Radio Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF7C3AED) else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) Color(0xFFC084FC) else Color(0x607C3AED),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun customWizardTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0x900E0622),
    unfocusedContainerColor = Color(0x800E0620),
    focusedBorderColor = Color(0xFFC084FC),
    unfocusedBorderColor = Color(0x507C3AED),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFFC084FC)
)
