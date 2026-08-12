package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.components.OrbView
import com.example.ui.components.customTextFieldColors
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import kotlinx.coroutines.launch
import java.util.UUID

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

    val context = androidx.compose.ui.platform.LocalContext.current

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

    Scaffold(
        containerColor = EmochiBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (stepIdx > 0) stepIdx-- else onBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = EmochiTextPrimary)
                }

                Text(
                    text = "Bot Oluştur (${stepIdx + 1}/$totalSteps)",
                    color = EmochiTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    if (stepIdx > 0) stepIdx-- else onBack()
                }) {
                    Text("Geri", color = EmochiTextSecondary)
                }

                Button(
                    onClick = {
                        if (stepIdx < totalSteps - 1) {
                            stepIdx++
                        } else {
                            onFinish(buildCurrentDraft())
                        }
                    },
                    enabled = canNext,
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("wizard_next_button")
                ) {
                    Text(if (stepIdx == totalSteps - 1) "Tamamla" else "İleri", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (stepIdx == totalSteps - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
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
            OrbView(hue = 275f, size = 48.dp, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            when (stepIdx) {
                0 -> {
                    // Step 1: Mode Selection
                    Text("Nasıl bir etkileşim istersiniz?", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("AI karakter tipini ve hikaye yapısını seçin.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

                    ModeSelectionCard(
                        title = "Kişisel Karakter",
                        desc = "Sana özel bire bir sohbet. Tek ve sabit bir karakterle gerçek zamanlı bağ.",
                        icon = Icons.Default.Person,
                        isSelected = mode == "personal",
                        onClick = { mode = "personal" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ModeSelectionCard(
                        title = "Evren / Hikaye (RP)",
                        desc = "Kurgusal bir dünyada geçen roleplay. AI evrenin anlatıcısı olur ve kadroyu yönetir.",
                        icon = Icons.Default.Book,
                        isSelected = mode == "universe",
                        onClick = { mode = "universe" }
                    )
                }

                1 -> {
                    // Step 2: Personality or Universe details
                    if (mode == "personal") {
                        Text("Karakter Detayları", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Karşınızdaki karakterin resmini, adını ve kişiliğini tanımlayın.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))

                        // Avatar Upload Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmochiCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF252535)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUrl.isNotBlank()) {
                                        coil.compose.AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bot Profil Fotoğrafı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    Text("Galeriden kapak resmi yükle", color = EmochiTextMuted, fontSize = 11.sp)
                                    
                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { photoPickerLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("🖼️ Fotoğraf Seç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                        Text("Karakter Adı *", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = aiName,
                            onValueChange = { aiName = it },
                            placeholder = { Text("ör. Aiden", color = EmochiTextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 14.dp)
                                .testTag("ai_name_input"),
                            colors = customTextFieldColors(),
                            singleLine = true
                        )

                        Text("Kişilik ve Özellikler *", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = aiPersonality,
                            onValueChange = { aiPersonality = it },
                            placeholder = { Text("Nazik mi, korumacı mı, esprili mi, konuşma tarzı nasıl...", color = EmochiTextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(top = 4.dp)
                                .testTag("ai_personality_input"),
                            colors = customTextFieldColors()
                        )
                    } else {
                        Text("Evren ve Karakter Kadrosu", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Kurgusal dünyanın adını ve ana karakter kadrosunu belirleyin.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                        Text("Evren Adı *", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = universeName,
                            onValueChange = { universeName = it },
                            placeholder = { Text("ör. Karanlık Akademi", color = EmochiTextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 14.dp),
                            colors = customTextFieldColors(),
                            singleLine = true
                        )

                        Text("Ana Karakter Kadrosu (Opsiyonel)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        keyCharacters.forEachIndexed { idx, char ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = char.name,
                                    onValueChange = { newName ->
                                        keyCharacters = keyCharacters.toMutableList().apply {
                                            this[idx] = this[idx].copy(name = newName)
                                        }
                                    },
                                    placeholder = { Text("İsim", fontSize = 12.sp, color = EmochiTextMuted) },
                                    modifier = Modifier.weight(1f),
                                    colors = customTextFieldColors(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = char.desc,
                                    onValueChange = { newDesc ->
                                        keyCharacters = keyCharacters.toMutableList().apply {
                                            this[idx] = this[idx].copy(desc = newDesc)
                                        }
                                    },
                                    placeholder = { Text("Tanım", fontSize = 12.sp, color = EmochiTextMuted) },
                                    modifier = Modifier.weight(2f),
                                    colors = customTextFieldColors(),
                                    singleLine = true
                                )
                                IconButton(onClick = {
                                    keyCharacters = keyCharacters.toMutableList().apply { removeAt(idx) }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Sil", tint = EmochiError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        TextButton(onClick = {
                            keyCharacters = keyCharacters + KeyCharacter(name = "", desc = "")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Karakter Ekle", color = EmochiPrimary)
                        }
                    }
                }

                2 -> {
                    // Step 3: Scenario & User character
                    Text("Senaryo & Kendi Karakteriniz", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Sahneye giriş bağlamını ve kendi karakterinizi tanımlayın.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                    Text("Senaryo / Bağlam *", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = scenario,
                        onValueChange = { scenario = it },
                        placeholder = { Text("Neredesiniz, aranızdaki ilişki ne, gerilim veya durum nasıl başladı...", color = EmochiTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(top = 4.dp, bottom = 14.dp)
                            .testTag("scenario_input"),
                        colors = customTextFieldColors()
                    )

                    Text("Sizin Canlandırdığınız Karakterin Adı *", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = userCharName,
                        onValueChange = { userCharName = it },
                        placeholder = { Text("ör. Zeynep", color = EmochiTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 14.dp)
                            .testTag("user_char_name_input"),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )

                    Text("Karakterinizin Kısa Açıklaması (Opsiyonel)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = userCharDesc,
                        onValueChange = { userCharDesc = it },
                        placeholder = { Text("Yaş, görünüş, kişilik özellikleri...", color = EmochiTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(top = 4.dp),
                        colors = customTextFieldColors()
                    )
                }

                3 -> {
                    // Step 4: Opening Message
                    Text("Başlangıç Sahnesi", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Sohbet bu açılış mesajı/sahnesi ile başlayacak.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                    OutlinedTextField(
                        value = openingMessage,
                        onValueChange = { openingMessage = it },
                        placeholder = { Text("Açılış mesajını kendiniz yazın veya aşağıdaki yapay zeka butonunu kullanın...", color = EmochiTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("opening_message_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingOpening,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingOpening) {
                            CircularProgressIndicator(color = EmochiPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Yapay Zeka Yazıyor...", color = EmochiPrimary)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Otomatik Yazsın", color = EmochiPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    genError?.let { err ->
                        Text(text = err, color = EmochiError, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                4 -> {
                    // Step 5: Writing Style & Intensity
                    Text("Yazım Tarzı & Ton", color = EmochiTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Yanıtların biçimini ve tematik yoğunluğunu özelleştirin.", color = EmochiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                    Text("Yazım Formatı", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ModeSelectionCard(
                        title = "RP / Anlatı Tarzı",
                        desc = "Üçüncü şahıs roman anlatımı. Ortam ve mimik betimlemeleri + diyaloglar.",
                        icon = Icons.Default.Book,
                        isSelected = writingStyle == "rp",
                        onClick = { writingStyle = "rp" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ModeSelectionCard(
                        title = "Sade Sohbet",
                        desc = "Doğal mesajlaşma tarzı. Kısa ve samimi yanıtlar.",
                        icon = Icons.Default.Person,
                        isSelected = writingStyle == "chat",
                        onClick = { writingStyle = "chat" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tema Yoğunluğu", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ModeSelectionCard(
                        title = "Normal",
                        desc = "Dengeli ve yumuşak ton.",
                        icon = Icons.Default.Psychology,
                        isSelected = intensity == "normal",
                        onClick = { intensity = "normal" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ModeSelectionCard(
                        title = "Yoğun",
                        desc = "Gerilim ve karanlık temalar yumuşatılmadan aktarılır (Cinsel içerik hariç).",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = intensity == "intense",
                        onClick = { intensity = "intense" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Görünürlük & Yayınlama", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ModeSelectionCard(
                        title = "🌐 Herkese Açık (Keşfette Yayınla)",
                        desc = "Botunuz Velora Keşfet sekmesinde tüm toplulukla paylaşılır.",
                        icon = Icons.Default.Public,
                        isSelected = isPublic,
                        onClick = { isPublic = true }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ModeSelectionCard(
                        title = "🔒 Sadece Bana Özel",
                        desc = "Bot sadece sizin cihazınızda ve sohbetlerinizde gözükür.",
                        icon = Icons.Default.Lock,
                        isSelected = !isPublic,
                        onClick = { isPublic = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ModeSelectionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF242540) else EmochiSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) EmochiPrimary else EmochiBorder
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A2C4A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = EmochiPrimary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = EmochiTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = EmochiTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
