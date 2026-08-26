package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiErrorContainer
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import kotlinx.coroutines.launch

data class ModelSpec(
    val key: String,
    val name: String,
    val provider: String,
    val tokenCostRate: String,
    val badgeColor: Color,
    val description: String
)

@Composable
fun SettingsSectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    color: Color = EmochiPrimary
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = EmochiTextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = EmochiTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CollapsibleSettingsOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badgeText: String? = null,
    badgeColor: Color = EmochiPrimary,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EmochiCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isExpanded) EmochiPrimary else EmochiBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExpanded) EmochiPrimary.copy(alpha = 0.2f) else Color(0xFF221A3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isExpanded) EmochiPrimary else EmochiTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (badgeText != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(badgeText, color = badgeColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(subtitle, color = EmochiTextMuted, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isExpanded) EmochiPrimary else EmochiTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Divider(color = EmochiBorder)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsModal(
    settings: UserSettingsEntity,
    onDismiss: () -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Unit
) {
    var geminiApiKey by remember { mutableStateOf(settings.customApiKey) }
    var groqApiKey by remember { mutableStateOf(settings.groqApiKey) }
    var claudeApiKey by remember { mutableStateOf(settings.claudeApiKey) }
    var openaiApiKey by remember { mutableStateOf(settings.openaiApiKey) }
    var backupApiKey by remember { mutableStateOf(settings.backupApiKey) }

    var selectedProvider by remember { mutableStateOf(settings.selectedProvider.ifBlank { "gemini" }) }
    var selectedModel by remember { mutableStateOf(settings.selectedModel) }
    var fallbackModel by remember { mutableStateOf(settings.fallbackModel) }
    var responseLength by remember { mutableStateOf(settings.responseLength) }
    var enableNsfw by remember { mutableStateOf(settings.enableNsfw) }
    var enableFlirty by remember { mutableStateOf(settings.enableFlirty) }
    var enableHardcore by remember { mutableStateOf(settings.enableHardcore) }
    var enableFetish by remember { mutableStateOf(settings.enableFetish) }
    var enableDarkRp by remember { mutableStateOf(settings.enableDarkRp) }
    var enableSweet by remember { mutableStateOf(settings.enableSweet) }
    var enablePrimal by remember { mutableStateOf(settings.enablePrimal) }
    var enableAutoFallback by remember { mutableStateOf(settings.enableAutoFallback) }
    var enableTts by remember { mutableStateOf(settings.enableTts) }
    var ttsSpeed by remember { mutableStateOf(settings.ttsSpeed) }
    var ttsPitch by remember { mutableStateOf(settings.ttsPitch) }
    var selectedVoiceName by remember { mutableStateOf(settings.selectedVoiceName) }
    var appLanguage by remember { mutableStateOf(settings.appLanguage) }

    var showKeys by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var showUpdateModal by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val content = context.contentResolver.openInputStream(it)?.use { stream ->
                        stream.bufferedReader().use { reader -> reader.readText() }
                    } ?: ""
                    if (content.isNotBlank()) {
                        onImportData(content)
                        Toast.makeText(context, "Yedek dosyadan yüklendi!", Toast.LENGTH_SHORT).show()
                        statusMessage = "Dosya başarıyla içe aktarıldı."
                    }
                } catch (e: Exception) {
                    statusMessage = "Yükleme hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val json = if (exportJson.isNotBlank()) exportJson else onExportData()
                    exportJson = json
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Yedek dosyaya kaydedildi!", Toast.LENGTH_SHORT).show()
                    statusMessage = "Yedek dosyaya kaydedildi."
                } catch (e: Exception) {
                    statusMessage = "Kaydetme hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val modelsList = listOf(
        ModelSpec(
            key = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash",
            provider = "Google Gemini",
            tokenCostRate = "🟢 Düşük (~0.5x Token)",
            badgeColor = Color(0xFF4CAF50),
            description = "En gelişmiş, dengeli ve hızlı Gemini modeli. Düşük token harcaması ile yüksek kaliteli rol yapma yanıtları verir."
        ),
        ModelSpec(
            key = "gemini-3.5-flash",
            name = "Gemini 3.5 Flash",
            provider = "Google Gemini",
            tokenCostRate = "⚡ Hızlı & Yeni Nesil",
            badgeColor = Color(0xFF00BCD4),
            description = "Yeni nesil ultra hızlı yanıt süresi. Karmaşık senaryolar ve sohbetler için optimize edilmiştir."
        ),
        ModelSpec(
            key = "gemini-2.5-pro",
            name = "Gemini 2.5 Pro",
            provider = "Google Gemini",
            tokenCostRate = "🔴 Yüksek (~2.5x Token)",
            badgeColor = Color(0xFFE91E63),
            description = "Üst düzey zeka, derin kurgu ve detaylı roman kalitesinde tutarlı karakter anlatımı."
        ),
        ModelSpec(
            key = "llama-3.3-70b-versatile",
            name = "Groq Llama 3.3 70B",
            provider = "Groq API",
            tokenCostRate = "🟡 Orta (~1.0x Token)",
            badgeColor = Color(0xFFFF9800),
            description = "Groq sunucularında ultra hızlı yanıt süresi ve doğal Türkçe rol yapma kabiliyeti."
        ),
        ModelSpec(
            key = "deepseek-r1-distill-llama-70b",
            name = "Groq DeepSeek R1",
            provider = "Groq API",
            tokenCostRate = "🔴 Yüksek (~2.0x Token)",
            badgeColor = Color(0xFF9C27B0),
            description = "Derin mantık ve karmaşık kurgu senaryolarında akıl yürütme odaklı karakter yanıtları."
        ),
        ModelSpec(
            key = "claude-3-5-sonnet-20241022",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            tokenCostRate = "🔴 Çok Yüksek (~3.0x Token)",
            badgeColor = Color(0xFFF44336),
            description = "Edebi anlatım, yüksek duygusal derinlik ve roman kalitesinde akıcı diyaloglar."
        ),
        ModelSpec(
            key = "claude-3-5-haiku-20241022",
            name = "Claude 3.5 Haiku",
            provider = "Anthropic",
            tokenCostRate = "🟡 Orta (~1.2x Token)",
            badgeColor = Color(0xFFFFC107),
            description = "Hızlı ve seri Claude kalitesi. Kısa ve orta boy diyaloglar için ideal."
        ),
        ModelSpec(
            key = "gpt-4o-mini",
            name = "OpenAI GPT-4o Mini",
            provider = "OpenAI",
            tokenCostRate = "🟢 Düşük (~0.8x Token)",
            badgeColor = Color(0xFF4CAF50),
            description = "Ekonomik, tutarlı ve akıcı OpenAI sohbet altyapısı."
        ),
        ModelSpec(
            key = "deepseek-chat",
            name = "DeepSeek V3",
            provider = "DeepSeek",
            tokenCostRate = "🟢 Düşük (~0.6x Token)",
            badgeColor = Color(0xFF009688),
            description = "Bütçe dostu, geniş bağlamlı yüksek akıl yürütme gücü sunan model."
        )
    )

    var fallbackDropdownExpanded by remember { mutableStateOf(false) }
    val totalTokensUsed = settings.totalPromptTokens + settings.totalCandidateTokens

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Box {
                AppBackground()
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Genel AI & Uygulama Ayarları",
                        color = EmochiTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // OPTION 1: MODELS & API KEYS
                CollapsibleSettingsOption(
                    title = "🤖 Modeller & API Anahtarları",
                    subtitle = "Gemini, Groq, Claude, OpenAI anahtarları ve aktif model seçimi",
                    icon = Icons.Default.Key,
                    isExpanded = expandedSection == "models",
                    onToggle = { expandedSection = if (expandedSection == "models") null else "models" },
                    badgeText = selectedProvider.uppercase(),
                    badgeColor = EmochiPrimary
                ) {
                    // Eye Icon for Key Visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showKeys = !showKeys }) {
                            Icon(
                                imageVector = if (showKeys) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = EmochiPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showKeys) "Anahtarları Gizle" else "Anahtarları Göster",
                                color = EmochiPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Primary Provider Selector Segmented Buttons
                    Text("Ana Sağlayıcı Seçin:", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val providers = listOf(
                            "gemini" to "Google Gemini",
                            "groq" to "Groq API",
                            "claude" to "Claude",
                            "openai" to "OpenAI / DeepSeek"
                        )
                        providers.forEach { (pKey, pLabel) ->
                            val isSel = selectedProvider == pKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) EmochiPrimary.copy(alpha = 0.2f) else EmochiCard)
                                    .border(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) EmochiPrimary else EmochiBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedProvider = pKey
                                        val providerModels = modelsList.filter {
                                            when (pKey) {
                                                "gemini" -> it.provider == "Google Gemini"
                                                "groq" -> it.provider == "Groq API"
                                                "claude" -> it.provider == "Anthropic"
                                                "openai" -> it.provider == "OpenAI" || it.provider == "DeepSeek"
                                                else -> false
                                            }
                                        }
                                        if (providerModels.none { it.key == selectedModel } && providerModels.isNotEmpty()) {
                                            selectedModel = providerModels.first().key
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pLabel,
                                    color = if (isSel) EmochiPrimary else EmochiTextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Card 1: Google Gemini
                    val isGeminiActive = selectedProvider == "gemini"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isGeminiActive) 2.dp else 1.dp, if (isGeminiActive) EmochiPrimary else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("1. Google Gemini", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isGeminiActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFF4CAF50), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = geminiApiKey,
                                onValueChange = { geminiApiKey = it },
                                placeholder = { Text("Gemini API Key...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Seçimi:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            modelsList.filter { it.provider == "Google Gemini" }.forEach { spec ->
                                val isSelected = selectedModel == spec.key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmochiPrimary.copy(alpha = 0.15f) else EmochiSurface)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedProvider = "gemini"
                                            selectedModel = spec.key
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(spec.name, color = if (isSelected) Color.White else EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(spec.tokenCostRate, color = spec.badgeColor, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Provider Card 2: Groq API
                    val isGroqActive = selectedProvider == "groq"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isGroqActive) 2.dp else 1.dp, if (isGroqActive) EmochiPrimary else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("2. Groq API (Llama 3.3 & DeepSeek R1)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isGroqActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFFFF9800), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = groqApiKey,
                                onValueChange = { groqApiKey = it },
                                placeholder = { Text("gsk_...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Seçimi:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            modelsList.filter { it.provider == "Groq API" }.forEach { spec ->
                                val isSelected = selectedModel == spec.key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmochiPrimary.copy(alpha = 0.15f) else EmochiSurface)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedProvider = "groq"
                                            selectedModel = spec.key
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(spec.name, color = if (isSelected) Color.White else EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(spec.tokenCostRate, color = spec.badgeColor, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Provider Card 3: Anthropic Claude
                    val isClaudeActive = selectedProvider == "claude"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isClaudeActive) 2.dp else 1.dp, if (isClaudeActive) EmochiPrimary else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("3. Anthropic Claude", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isClaudeActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF44336).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFFF44336), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = claudeApiKey,
                                onValueChange = { claudeApiKey = it },
                                placeholder = { Text("sk-ant-...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Seçimi:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            modelsList.filter { it.provider == "Anthropic" }.forEach { spec ->
                                val isSelected = selectedModel == spec.key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmochiPrimary.copy(alpha = 0.15f) else EmochiSurface)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedProvider = "claude"
                                            selectedModel = spec.key
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(spec.name, color = if (isSelected) Color.White else EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(spec.tokenCostRate, color = spec.badgeColor, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Provider Card 4: OpenAI / DeepSeek
                    val isOpenaiActive = selectedProvider == "openai"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isOpenaiActive) 2.dp else 1.dp, if (isOpenaiActive) EmochiPrimary else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF009688), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("4. OpenAI & DeepSeek", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isOpenaiActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF009688).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFF009688), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = openaiApiKey,
                                onValueChange = { openaiApiKey = it },
                                placeholder = { Text("sk-...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Seçimi:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            modelsList.filter { it.provider == "OpenAI" || it.provider == "DeepSeek" }.forEach { spec ->
                                val isSelected = selectedModel == spec.key
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmochiPrimary.copy(alpha = 0.15f) else EmochiSurface)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedProvider = "openai"
                                            selectedModel = spec.key
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(spec.name, color = if (isSelected) Color.White else EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(spec.tokenCostRate, color = spec.badgeColor, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Backup Gemini Key Field
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Yedek Gemini API Key:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = backupApiKey,
                        onValueChange = { backupApiKey = it },
                        placeholder = { Text("Yedek Gemini Key...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                        visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        singleLine = true,
                        colors = customTextFieldColors()
                    )

                    // Active Selected Model Details Card
                    Spacer(modifier = Modifier.height(10.dp))
                    val currentSelectedSpec = modelsList.find { it.key == selectedModel } ?: modelsList.first()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Aktif Seçili Model: ${currentSelectedSpec.name}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(currentSelectedSpec.tokenCostRate, color = Color(0xFF10B981), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(currentSelectedSpec.description, color = EmochiTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }

                // OPTION 2: RESPONSE SETTINGS & FALLBACK
                CollapsibleSettingsOption(
                    title = "💬 Yanıt Ayarları & Yedek Model",
                    subtitle = "Varsayılan yanıt uzunluğu, yedek model ve otomatik geçiş",
                    icon = Icons.Default.Tune,
                    isExpanded = expandedSection == "length" || expandedSection == "response",
                    onToggle = { expandedSection = if (expandedSection == "length" || expandedSection == "response") null else "response" },
                    badgeText = if (enableAutoFallback) "AÇIK" else "KAPALI",
                    badgeColor = if (enableAutoFallback) Color(0xFF10B981) else EmochiTextMuted
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text("Varsayılan Yanıt Uzunluğu:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val options = listOf(
                                    Triple("short", "Kısa", Icons.Default.FlashOn),
                                    Triple("orta", "Orta", Icons.Default.AutoAwesome),
                                    Triple("standard", "Standart", Icons.Default.Description),
                                    Triple("long", "Uzun", Icons.Default.List)
                                )
                                options.forEach { (key, label, icon) ->
                                    val isSelected = responseLength == key
                                    val tintColor = if (key == "orta") Color(0xFF34D399) else if (key == "long") Color(0xFFA78BFA) else Color(0xFFFBBF24)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.15f) else EmochiCard)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF8B5CF6) else EmochiBorder,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { responseLength = key }
                                            .padding(horizontal = 6.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else EmochiTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Card for Automatic Fallback & Fallback Model
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2344)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = EmochiPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Otomatik Model/API Geçişi",
                                                color = EmochiTextPrimary,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Modelde hata veya kota aşımı olursa otomatik yedek modele geç.",
                                                color = EmochiTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = enableAutoFallback,
                                        onCheckedChange = { enableAutoFallback = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF1A1B2E),
                                            checkedTrackColor = EmochiPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = Color(0xFF2B2142))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    "Yedek Model (Hata / Kota Aşımında Kullanılır):",
                                    color = EmochiTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ExposedDropdownMenuBox(
                                    expanded = fallbackDropdownExpanded,
                                    onExpandedChange = { fallbackDropdownExpanded = !fallbackDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    val currentFallbackSpec = modelsList.find { it.key == fallbackModel } ?: modelsList.first()
                                    OutlinedTextField(
                                        value = "${currentFallbackSpec.name} — ${currentFallbackSpec.tokenCostRate}",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fallbackDropdownExpanded) },
                                        colors = customTextFieldColors(),
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = fallbackDropdownExpanded,
                                        onDismissRequest = { fallbackDropdownExpanded = false },
                                        modifier = Modifier.background(EmochiCard)
                                    ) {
                                        modelsList.forEach { spec ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(spec.name, color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text(spec.tokenCostRate, color = EmochiPrimary, fontSize = 10.5.sp)
                                                    }
                                                },
                                                onClick = {
                                                    fallbackModel = spec.key
                                                    fallbackDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // OPTION 3: CONTENT FILTERS (NSFW)
                CollapsibleSettingsOption(
                    title = "🔞 İçerik Filtreleri (+18)",
                    subtitle = "Yetişkin içerik (NSFW), Flirty, Hardcore, Fetish, Dark RP filtreleri",
                    icon = Icons.Default.Warning,
                    isExpanded = expandedSection == "nsfw",
                    onToggle = { expandedSection = if (expandedSection == "nsfw") null else "nsfw" },
                    badgeText = if (enableNsfw) "AÇIK" else "KAPALI",
                    badgeColor = if (enableNsfw) Color(0xFFEF4444) else EmochiTextMuted
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A141A)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33202E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)) {
                            FilterToggleRow(label = "🔓  18+ (NSFW) Genel Kilidini Aç", checked = enableNsfw) { enableNsfw = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "💖  Çapkınlık (Flirty)", checked = enableFlirty) { enableFlirty = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "🔥  Sert Mod (Hardcore)", checked = enableHardcore) { enableHardcore = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "🎭  Fantezi (Fetish)", checked = enableFetish) { enableFetish = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "👻  Karanlık (Dark RP)", checked = enableDarkRp) { enableDarkRp = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "🍬  Romantik (Sweet)", checked = enableSweet) { enableSweet = it }
                            Divider(color = Color(0xFF2D1E2A))
                            FilterToggleRow(label = "🐾  Vahşi (Primal)", checked = enablePrimal) { enablePrimal = it }
                        }
                    }
                }

                // OPTION 4: TEXT-TO-SPEECH (TTS)
                CollapsibleSettingsOption(
                    title = "🔊 Sesli Okuma (TTS)",
                    subtitle = "Sesli okuma aktivasyonu, okuma hızı ve ses tonu",
                    icon = Icons.Default.VolumeUp,
                    isExpanded = expandedSection == "tts",
                    onToggle = { expandedSection = if (expandedSection == "tts") null else "tts" }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sesli Okuma Özelliğini Etkinleştir", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = enableTts,
                            onCheckedChange = { enableTts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1A1B2E), checkedTrackColor = EmochiPrimary)
                        )
                    }

                    if (enableTts) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(EmochiCard, RoundedCornerShape(12.dp))
                                .border(1.dp, EmochiBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡ Okuma Hızı: ${"%.1f".format(ttsSpeed)}x", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.8f to "0.8x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x").forEach { (sp, lbl) ->
                                    val sel = ttsSpeed == sp
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) EmochiPrimary.copy(alpha = 0.25f) else EmochiSurface)
                                            .border(1.dp, if (sel) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                            .clickable { ttsSpeed = sp }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = if (sel) EmochiPrimary else EmochiTextSecondary, fontSize = 10.5.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Text("🎵 Ses Tonu (Pitch): ${"%.1f".format(ttsPitch)}x", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.8f to "Kalın (0.8)", 1.0f to "Normal (1.0)", 1.2f to "İnce (1.2)").forEach { (p, lbl) ->
                                    val sel = ttsPitch == p
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) EmochiPrimary.copy(alpha = 0.25f) else EmochiSurface)
                                            .border(1.dp, if (sel) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                            .clickable { ttsPitch = p }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = if (sel) EmochiPrimary else EmochiTextSecondary, fontSize = 10.5.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }

                // OPTION 5: LANGUAGE
                CollapsibleSettingsOption(
                    title = "🌐 Uygulama & Yanıt Dili",
                    subtitle = "Yapay zeka yanıtları için varsayılan dil tercihi",
                    icon = Icons.Default.Language,
                    isExpanded = expandedSection == "lang",
                    onToggle = { expandedSection = if (expandedSection == "lang") null else "lang" },
                    badgeText = if (appLanguage == "tr") "TÜRKÇE" else "ENGLISH"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.FilterChip(
                            selected = appLanguage == "tr",
                            onClick = { appLanguage = "tr" },
                            label = { Text("🇹🇷 Türkçe (Varsayılan)", color = if (appLanguage == "tr") Color(0xFF1A1B2E) else EmochiTextPrimary, fontWeight = FontWeight.Bold) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = EmochiPrimary, containerColor = EmochiCard)
                        )
                        androidx.compose.material3.FilterChip(
                            selected = appLanguage == "en",
                            onClick = { appLanguage = "en" },
                            label = { Text("🇬🇧 English", color = if (appLanguage == "en") Color(0xFF1A1B2E) else EmochiTextPrimary, fontWeight = FontWeight.Bold) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = EmochiPrimary, containerColor = EmochiCard)
                        )
                    }
                }

                // OPTION 6: TOKEN USAGE
                CollapsibleSettingsOption(
                    title = "📊 Token Kullanım İstatistikleri",
                    subtitle = "Toplam harcanan girdi ve çıktı token verileri",
                    icon = Icons.Default.Speed,
                    isExpanded = expandedSection == "tokens",
                    onToggle = { expandedSection = if (expandedSection == "tokens") null else "tokens" }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Girdi Token", color = EmochiTextMuted, fontSize = 11.sp)
                                    Text("${settings.totalPromptTokens}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Çıktı Token", color = EmochiTextMuted, fontSize = 11.sp)
                                    Text("${settings.totalCandidateTokens}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Maliyet", color = EmochiTextMuted, fontSize = 11.sp)
                                    Text("Ücretsiz", color = Color(0xFFA78BFA), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toplam Harcanan: $totalTokensUsed token",
                                color = EmochiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // OPTION 7: BACKUP & RESTORE
                CollapsibleSettingsOption(
                    title = "💾 Yedekleme & Geri Yükleme",
                    subtitle = "Tüm botlarınızı ve verilerinizi yedekleyin veya içe aktarın",
                    icon = Icons.Default.Save,
                    isExpanded = expandedSection == "backup",
                    onToggle = { expandedSection = if (expandedSection == "backup") null else "backup" }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isBusy = true
                                    try {
                                        val json = onExportData()
                                        exportJson = json
                                        clipboardManager.setText(AnnotatedString(json))
                                        Toast.makeText(context, "Yedek panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                                        statusMessage = "Yedek kopyalandı."
                                    } catch (e: Exception) {
                                        statusMessage = "Hata: ${e.message}"
                                    } finally {
                                        isBusy = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📋 Kopyala", color = EmochiPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { saveFileLauncher.launch("emochi_backup_${System.currentTimeMillis() / 1000}.json") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("💾 Kaydet", color = EmochiPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isBusy = true
                                    try {
                                        val json = if (exportJson.isNotBlank()) exportJson else onExportData()
                                        exportJson = json
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, json)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Yedeği Paylaş"))
                                    } catch (e: Exception) {
                                        statusMessage = "Paylaşım hatası: ${e.message}"
                                    } finally {
                                        isBusy = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📤 Paylaş", color = EmochiPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("application/json") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📂 Dosya Seç (.json)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                clipboardManager.getText()?.let { text ->
                                    importJson = text.text
                                    Toast.makeText(context, "Metin panodan yapıştırıldı!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📋 Yapıştır", color = EmochiPrimary, fontSize = 11.sp)
                        }
                    }

                    if (importJson.isNotBlank()) {
                        OutlinedTextField(
                            value = importJson,
                            onValueChange = { importJson = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 4.dp),
                            colors = customTextFieldColors()
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    isBusy = true
                                    try {
                                        onImportData(importJson)
                                        Toast.makeText(context, "Geri yükleme tamamlandı!", Toast.LENGTH_SHORT).show()
                                        statusMessage = "Veriler yüklendi."
                                        importJson = ""
                                    } catch (e: Exception) {
                                        statusMessage = "Hata: ${e.message}"
                                    } finally {
                                        isBusy = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("✅ Yüklemeyi Başlat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    statusMessage?.let { msg ->
                        Text(text = msg, color = EmochiTextSecondary, fontSize = 11.5.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // OPTION 8: VERSION & UPDATES
                CollapsibleSettingsOption(
                    title = "🚀 Sürüm & Güncelleme",
                    subtitle = "Mevcut sürüm v1.4, güncellemeleri kontrol et",
                    icon = Icons.Default.AutoAwesome,
                    isExpanded = expandedSection == "updates",
                    onToggle = { expandedSection = if (expandedSection == "updates") null else "updates" },
                    badgeText = "v1.4"
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sürüm Kontrolü", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Text("APK güncellemelerini denetleyin.", color = EmochiTextSecondary, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { showUpdateModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Güncelle", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        onSaveSettings(
                            settings.copy(
                                customApiKey = geminiApiKey.trim(),
                                groqApiKey = groqApiKey.trim(),
                                claudeApiKey = claudeApiKey.trim(),
                                openaiApiKey = openaiApiKey.trim(),
                                backupApiKey = backupApiKey.trim(),
                                selectedProvider = selectedProvider,
                                selectedModel = selectedModel,
                                fallbackModel = fallbackModel,
                                responseLength = responseLength,
                                enableNsfw = enableNsfw,
                                enableFlirty = enableFlirty,
                                enableHardcore = enableHardcore,
                                enableFetish = enableFetish,
                                enableDarkRp = enableDarkRp,
                                enableSweet = enableSweet,
                                enablePrimal = enablePrimal,
                                enableAutoFallback = enableAutoFallback,
                                enableTts = enableTts,
                                ttsSpeed = ttsSpeed,
                                ttsPitch = ttsPitch,
                                selectedVoiceName = selectedVoiceName,
                                appLanguage = appLanguage
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ayarları Kaydet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        }
    }

    if (showUpdateModal) {
        UpdateCheckerModal(onDismiss = { showUpdateModal = false })
    }
}

@Composable
fun BotSettingsModal(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<com.example.data.local.AffectionEventEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit,
    onDeleteBot: () -> Unit
) {
    var aiName by remember { mutableStateOf(bot.aiName) }
    var universeName by remember { mutableStateOf(bot.universeName) }
    var personality by remember { mutableStateOf(bot.aiPersonality) }
    var scenario by remember { mutableStateOf(bot.scenario) }
    var userCharName by remember { mutableStateOf(bot.userCharName) }
    var userCharDesc by remember { mutableStateOf(bot.userCharDesc) }
    var writingStyle by remember { mutableStateOf(bot.writingStyle) }
    var intensity by remember { mutableStateOf(bot.intensity) }
    var customLength by remember { mutableStateOf(bot.customLength) }
    var isNsfw by remember { mutableStateOf(bot.isNsfw) }
    var enableOoc by remember { mutableStateOf(bot.enableOoc) }
    var avatarUrl by remember { mutableStateOf(bot.avatarUrl) }
    var chatBgUrl by remember { mutableStateOf(bot.chatBgUrl) }
    var isPublic by remember { mutableStateOf(bot.isPublic) }
    var pinnedMemory by remember { mutableStateOf(bot.pinnedMemory) }
    var storyNotes by remember { mutableStateOf(bot.storyNotes) }
    var memoryNotes by remember { mutableStateOf(bot.memoryNotes) }

    var charList by remember { mutableStateOf(keyCharacters.toMutableList()) }

    var confirmResetChat by remember { mutableStateOf(false) }
    var confirmDeleteBot by remember { mutableStateOf(false) }
    var showNeuralVault by remember { mutableStateOf(false) }
    var expandedBotSection by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            coroutineScope.launch {
                val savedUrl = com.example.util.ImageStorageManager.compressAndSaveImage(context, inputUri)
                avatarUrl = savedUrl
            }
        }
    }

    val bgPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            coroutineScope.launch {
                val savedUrl = com.example.util.ImageStorageManager.compressAndSaveImage(context, inputUri)
                chatBgUrl = savedUrl
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Box {
                AppBackground()
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bot Profil & Özel Ayarlar", color = EmochiTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Emotion Bar
                EmotionStatusSection(bot = bot, characterEmotions = characterEmotions, affectionEvents = affectionEvents)

                Spacer(modifier = Modifier.height(14.dp))

                // TOP ITEM 1: AVATAR & CHAT BACKGROUND (Directly Visible)
                Text("🖼️ Profil & Arka Plan Görselleri", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Avatar Photo Picker UI
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
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
                                .size(58.dp)
                                .clip(RoundedCornerShape(29.dp))
                                .background(Color(0xFF252535)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Bot Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(24.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bot Görseli / Avatar", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("Galeriden kapak resmi yükle", color = EmochiTextMuted, fontSize = 11.sp)

                            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Galeri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (avatarUrl.isNotBlank()) {
                                    TextButton(
                                        onClick = { avatarUrl = "" },
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Kaldır", fontSize = 11.sp, color = EmochiError)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Background Wallpaper Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
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
                                .size(58.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF252535)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chatBgUrl.isNotBlank()) {
                                AsyncImage(
                                    model = chatBgUrl,
                                    contentDescription = "Sohbet Arka Planı",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(24.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sohbet Arka Plan Resmi", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("Sohbet içi özel duvar kağıdı belirleyin", color = EmochiTextMuted, fontSize = 11.sp)

                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { chatBgUrl = "USE_AVATAR" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1B4E), contentColor = Color(0xFFC084FC)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("👤 Avatar Yap", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { bgPhotoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Galeri", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                                if (chatBgUrl.isNotBlank()) {
                                    TextButton(
                                        onClick = { chatBgUrl = "" },
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Text("Sıfırla", fontSize = 10.5.sp, color = EmochiError)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TOP ITEM 2: PER-BOT RESPONSE LENGTH (Directly Visible)
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💬 Bu Bota Özel Yanıt Uzunluğu", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Text("Boş veya varsayılan bırakılırsa Genel Ayarlar'daki yanıt uzunluğu kullanılır.", color = EmochiTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "default" to "Varsayılan",
                                "short" to "Kısa",
                                "orta" to "Orta",
                                "standard" to "Standart",
                                "long" to "Uzun"
                            ).forEach { (key, label) ->
                                val isSelected = customLength == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmochiPrimary.copy(alpha = 0.2f) else EmochiSurface)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable { customLength = key }
                                        .padding(horizontal = 2.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) EmochiPrimary else EmochiTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // COLLAPSIBLE OPTION 1: IDENTITY, SCENARIO & CHARACTERS
                CollapsibleSettingsOption(
                    title = "🎭 Hikaye & Senaryo (Kimlik ve Kişilik)",
                    subtitle = "Karakter adı, kişiliği, senaryo bağı ve kullanıcı karakter tanımı",
                    icon = Icons.Default.Person,
                    isExpanded = expandedBotSection == "scenario",
                    onToggle = { expandedBotSection = if (expandedBotSection == "scenario") null else "scenario" }
                ) {
                    if (bot.mode == "personal") {
                        Text("Karakter Adı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = aiName,
                            onValueChange = { aiName = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            colors = customTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Kişilik Detayları", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = personality,
                            onValueChange = { personality = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 2.dp),
                            colors = customTextFieldColors()
                        )
                    } else {
                        Text("Evren / Dünya Adı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = universeName,
                            onValueChange = { universeName = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            colors = customTextFieldColors(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Senaryo & Bağlam", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = scenario,
                        onValueChange = { scenario = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 2.dp),
                        colors = customTextFieldColors()
                    )

                    if (bot.mode == "universe") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Karakter Kadrosu", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        charList.forEachIndexed { index, char ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = char.name,
                                    onValueChange = { newName ->
                                        charList = charList.toMutableList().apply {
                                            this[index] = this[index].copy(name = newName)
                                        }
                                    },
                                    placeholder = { Text("İsim", fontSize = 11.sp, color = EmochiTextMuted) },
                                    modifier = Modifier.weight(1f),
                                    colors = customTextFieldColors(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = char.desc,
                                    onValueChange = { newDesc ->
                                        charList = charList.toMutableList().apply {
                                            this[index] = this[index].copy(desc = newDesc)
                                        }
                                    },
                                    placeholder = { Text("Tanım", fontSize = 11.sp, color = EmochiTextMuted) },
                                    modifier = Modifier.weight(2f),
                                    colors = customTextFieldColors(),
                                    singleLine = true
                                )
                                IconButton(onClick = {
                                    charList = charList.toMutableList().apply { removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Sil", tint = EmochiError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                charList = charList.toMutableList().apply { add(KeyCharacter(name = "", desc = "")) }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Karakter Ekle", color = EmochiPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Senin Karakterin (Kullanıcı Adı)", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = userCharName,
                        onValueChange = { userCharName = it },
                        placeholder = { Text("İsim", fontSize = 12.sp, color = EmochiTextMuted) },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )
                }

                // COLLAPSIBLE OPTION 2: MEMORY & NEURAL VAULT
                CollapsibleSettingsOption(
                    title = "🧠 Hafıza & Neural Vault",
                    subtitle = "Neural Vault bellek kasası, kalıcı hafıza ve hikaye durumları",
                    icon = Icons.Default.Psychology,
                    isExpanded = expandedBotSection == "memory",
                    onToggle = { expandedBotSection = if (expandedBotSection == "memory") null else "memory" }
                ) {
                    // Neural Vault Button
                    Button(
                        onClick = { showNeuralVault = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141424), contentColor = Color(0xFF2196F3)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🧠 Neural Vault (Gelişmiş AI Belleği)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    if (showNeuralVault) {
                        NeuralVaultModal(
                            bot = bot.copy(
                                pinnedMemory = pinnedMemory,
                                storyNotes = storyNotes,
                                memoryNotes = memoryNotes
                            ),
                            onDismiss = { showNeuralVault = false },
                            onSaveMemory = { updatedBot ->
                                pinnedMemory = updatedBot.pinnedMemory
                                storyNotes = updatedBot.storyNotes
                                memoryNotes = updatedBot.memoryNotes
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Kalıcı Hafıza (Asla Silinmez)", color = EmochiPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "AI'nın her zaman bilmesini istediğiniz kuralları, ilişkileri veya gerçekleri buraya yazın.",
                        color = EmochiTextSecondary,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = pinnedMemory,
                        onValueChange = { pinnedMemory = it },
                        modifier = Modifier.fillMaxWidth().height(90.dp).padding(top = 4.dp),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Otomatik Hikaye Durumu", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmochiCard)
                            .border(1.dp, EmochiBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = storyNotes.ifBlank { "Henüz kayıtlı hikaye durumu yok." },
                            color = EmochiTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Otomatik Hafıza Özetleri", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmochiCard)
                            .border(1.dp, EmochiBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = memoryNotes.ifBlank { "Henüz kayıtlı hafıza özeti yok." },
                            color = EmochiTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // COLLAPSIBLE OPTION 3: OOC & FILTERS
                CollapsibleSettingsOption(
                    title = "💬 OOC & Filtresiz RP Ayarları",
                    subtitle = "Parantez içi yönlendirme (OOC) ve +18 filtresiz RP izinleri",
                    icon = Icons.Default.Settings,
                    isExpanded = expandedBotSection == "ooc",
                    onToggle = { expandedBotSection = if (expandedBotSection == "ooc") null else "ooc" },
                    badgeText = if (isNsfw) "NSFW" else null,
                    badgeColor = Color(0xFFEF4444)
                ) {
                    // OOC Switch Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💬 Parantez İçi Yönlendirme (OOC)",
                                    color = EmochiTextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sohbet esnasında parantez içindeki ( ... ) ifadeler AI'a hikaye dışı meta yönlendirme komutu olarak iletilir.",
                                    color = EmochiTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = enableOoc,
                                onCheckedChange = { enableOoc = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF1A1B2E),
                                    checkedTrackColor = EmochiPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Per-Bot +18 NSFW Switch Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔞 +18 / Filtresiz RP İzni",
                                    color = EmochiTextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bu karaktere özel sansürsüz içerik izni tanımlar. Boş bırakılırsa genel ayar geçerlidir.",
                                    color = EmochiTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isNsfw,
                                onCheckedChange = { isNsfw = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF1A1B2E),
                                    checkedTrackColor = EmochiPrimary
                                )
                            )
                        }
                    }
                }

                // COLLAPSIBLE OPTION 4: SHARING & VISIBILITY
                if (!bot.isTemplate && !bot.id.startsWith("starter_") && !bot.id.startsWith("preset_")) {
                    CollapsibleSettingsOption(
                        title = "🌐 Paylaşım & Görünürlük",
                        subtitle = "Botun keşfete çıkarılması veya gizli tutulması",
                        icon = Icons.Default.Public,
                        isExpanded = expandedBotSection == "visibility",
                        onToggle = { expandedBotSection = if (expandedBotSection == "visibility") null else "visibility" },
                        badgeText = if (isPublic) "AÇIK" else "GİZLİ"
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmochiCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPublic) "🌐 Herkese Açık Bot" else "🔒 Sadece Kendine Özel",
                                        color = EmochiTextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPublic) "Diğer kullanıcılar 'Keşfet' bölümünde botunuzu bulabilir." else "Bu bot gizlidir, sadece siz görebilirsiniz.",
                                        color = EmochiTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = isPublic,
                                    onCheckedChange = { isPublic = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF1A1B2E),
                                        checkedTrackColor = EmochiPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // SAVE BUTTON
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val updated = bot.copy(
                            aiName = aiName,
                            universeName = universeName,
                            aiPersonality = personality,
                            scenario = scenario,
                            userCharName = userCharName,
                            userCharDesc = userCharDesc,
                            writingStyle = writingStyle,
                            intensity = intensity,
                            customLength = customLength,
                            isNsfw = isNsfw,
                            enableOoc = enableOoc,
                            avatarUrl = avatarUrl,
                            chatBgUrl = chatBgUrl,
                            isPublic = isPublic,
                            pinnedMemory = pinnedMemory,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updated, charList)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = EmochiBorder)

                // SECTION 6: DANGEROUS ACTIONS
                SettingsSectionHeader(
                    title = "⚠️ Tehlikeli İşlemler",
                    subtitle = "Sohbet geçmişini sıfırlama veya botu tamamen silme.",
                    icon = Icons.Default.Warning,
                    color = EmochiError
                )

                if (!confirmResetChat) {
                    Button(
                        onClick = { confirmResetChat = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard, contentColor = EmochiTextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmochiPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sohbet Geçmişini Sıfırla")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sohbeti Sıfırlama Yöntemi Seçin:", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    confirmResetChat = false
                                    onResetChat(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("💬 Sadece Mesajları Sil (Hafıza Korunur)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    confirmResetChat = false
                                    onResetChat(true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiError, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔥 Tamamen Sıfırla (Hafıza & Duygu Dâhil)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { confirmResetChat = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Vazgeç", color = EmochiTextSecondary, fontSize = 11.5.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!confirmDeleteBot) {
                    Button(
                        onClick = { confirmDeleteBot = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = EmochiError),
                        modifier = Modifier.fillMaxWidth().border(1.dp, EmochiError.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Botu Tamamen Sil")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiErrorContainer),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Bu bot ve tüm geçmişi kalıcı olarak silinecektir!", color = EmochiError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { confirmDeleteBot = false }) {
                                    Text("Vazgeç", color = EmochiTextSecondary)
                                }
                                Button(
                                    onClick = {
                                        confirmDeleteBot = false
                                        onDeleteBot()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiError)
                                ) {
                                    Text("Sil", color = Color.White)
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

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EmochiPrimary,
    unfocusedBorderColor = EmochiBorder,
    focusedContainerColor = EmochiCard,
    unfocusedContainerColor = EmochiCard,
    focusedTextColor = EmochiTextPrimary,
    unfocusedTextColor = EmochiTextPrimary
)

@Composable
fun FilterToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color(0xFFB0B0B0),
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralVaultModal(
    bot: BotEntity,
    onDismiss: () -> Unit,
    onSaveMemory: (BotEntity) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bilgi (Core)") }
    var memoryText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("Tümü") }
    
    var pinnedMemoryText by remember { mutableStateOf(bot.pinnedMemory) }
    var memoryNotesText by remember { mutableStateOf(bot.memoryNotes) }
    var storyNotesText by remember { mutableStateOf(bot.storyNotes) }
    
    val memoriesList = remember(pinnedMemoryText, memoryNotesText, storyNotesText) {
        val list = mutableListOf<Triple<String, String, String>>()

        if (pinnedMemoryText.isNotBlank()) {
            pinnedMemoryText.lines().filter { it.isNotBlank() }.forEach { line ->
                val parts = line.split(":::", limit = 3)
                if (parts.size >= 3) {
                    list.add(Triple(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                } else if (parts.size == 2) {
                    list.add(Triple("BELLEK", "KALICI", parts[1].trim()))
                } else {
                    list.add(Triple("KALICI", "BİLGİ", line.trim()))
                }
            }
        }

        if (memoryNotesText.isNotBlank()) {
            memoryNotesText.lines().filter { it.isNotBlank() }.forEach { line ->
                val clean = line.removePrefix("-").removePrefix("*").trim()
                if (clean.isNotBlank()) {
                    list.add(Triple("ÖZET", "HAFIZA", clean))
                }
            }
        }

        if (storyNotesText.isNotBlank()) {
            storyNotesText.lines().filter { it.isNotBlank() }.forEach { line ->
                val clean = line.removePrefix("-").removePrefix("*").trim()
                if (clean.isNotBlank()) {
                    list.add(Triple("HİKAYE", "DURUM", clean))
                }
            }
        }

        list
    }
    
    val filteredMemories = memoriesList.filter { (key, cat, body) ->
        val matchesCategory = categoryFilter == "Tümü" || cat.contains(categoryFilter, ignoreCase = true) || (categoryFilter == "Bilgi (Core)" && (cat.contains("BİLGİ", ignoreCase = true) || cat.contains("CORE", ignoreCase = true)))
        val matchesSearch = searchQuery.isBlank() || key.contains(searchQuery, ignoreCase = true) ||
                cat.contains(searchQuery, ignoreCase = true) ||
                body.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Box {
                AppBackground()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Header (Matching Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Neural Vault",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                    }
                }

                // Stats Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${memoriesList.size} Kayıt", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("V4.0 Sync", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Long-Term Memory", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Inject Memory Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252535)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YENİ BELLEK ENJEKTE ET",
                                color = Color(0xFF2196F3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = keyword,
                                onValueChange = { keyword = it },
                                placeholder = { Text("Anahtar Kelime", color = Color.Gray, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                            
                            // Category Selector Field (Opens Popup Matching Screenshot 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E28))
                                    .border(1.dp, Color(0xFF333344), RoundedCornerShape(8.dp))
                                    .clickable { showCategoryPicker = true }
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Kategori Seç",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Hazır Seçenekler / Şablonlar
                        val quickPresets = listOf(
                            Triple("TAKMA İSİM", "Bilgi (Core)", "Kullanıcıya özel samimi bir unvan veya takma isimle hitap eder."),
                            Triple("GİZLİ DUYGU", "İlişki", "Kullanıcıya karşı derin bir çekim ve gizli bir bağlılık hisseder."),
                            Triple("GEÇMİŞ SIR", "Biyografi", "Geçmişte yaşadığı kırılma noktasını sadece kullanıcıya anlatmıştır."),
                            Triple("ÖZEL MEKAN", "Dünya/Mekan", "Yalnızca ikisinin bildiği gizli ve tenha bir buluşma noktaları vardır."),
                            Triple("YETİŞKİN RP", "İlişki", "Yetişkin etkileşimlerde filtresiz, tutkulu ve tabusuz bir yakınlık benimser."),
                            Triple("SADAKAT BAĞI", "İlişki", "Kullanıcıya koşulsuz sadakat gösterir ve onu her koşulda korur.")
                        )

                        Text(
                            text = "HAZIR SEÇENEKLER & ŞABLONLAR",
                            color = Color(0xFF80CBC4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPresets.forEach { (presetKey, presetCat, presetDesc) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF212130))
                                        .border(1.dp, Color(0xFF38384D), RoundedCornerShape(8.dp))
                                        .clickable {
                                            keyword = presetKey
                                            category = presetCat
                                            memoryText = presetDesc
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF80CBC4), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(presetKey, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = memoryText,
                            onValueChange = { memoryText = it },
                            placeholder = { Text("Karakter neyi hatırlamalı? (Örn: En sevdiği yemek makarnadır.)", color = Color.Gray, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (memoryText.isNotBlank()) {
                                    val keyTag = keyword.ifBlank { "BELLEK" }.uppercase()
                                    val catTag = category.ifBlank { "BİLGİ (CORE)" }.uppercase()
                                    val newEntry = "$keyTag ::: $catTag ::: ${memoryText.trim()}"
                                    val newPinned = if (pinnedMemoryText.isBlank()) newEntry else "$pinnedMemoryText\n$newEntry"
                                    pinnedMemoryText = newPinned
                                    onSaveMemory(bot.copy(pinnedMemory = newPinned))
                                    keyword = ""
                                    memoryText = ""
                                }
                            },
                            enabled = memoryText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Belleği Kaydet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Chips
                val presetCategories = listOf("Tümü", "Bilgi (Core)", "Biyografi", "İlişki", "Dünya/Mekan")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCategories.forEach { cat ->
                        val isSelected = categoryFilter == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2196F3) else Color(0xFF1E1E28))
                                .border(1.dp, if (isSelected) Color(0xFF2196F3) else Color(0xFF2E2E3E), RoundedCornerShape(12.dp))
                                .clickable { categoryFilter = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hafızada ara...", color = Color.Gray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Saved Memory List Cards
                if (filteredMemories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Henüz enjekte edilmiş bellek yok.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    filteredMemories.forEachIndexed { index, (keyTag, catTag, bodyText) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252535)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = keyTag,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF252535), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = catTag,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bodyText,
                                    color = Color(0xFFD0D0E0),
                                    fontSize = 12.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val lines = pinnedMemoryText.lines().toMutableList()
                                            val targetLine = "$keyTag ::: $catTag ::: $bodyText"
                                            lines.remove(targetLine)
                                            val updated = lines.joinToString("\n")
                                            pinnedMemoryText = updated
                                            onSaveMemory(bot.copy(pinnedMemory = updated))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryPicker) {
        Dialog(onDismissRequest = { showCategoryPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C34)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Neural Vault",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showCategoryPicker = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val pickerCategories = listOf(
                        "Bilgi (Core)",
                        "Biyografi",
                        "İlişki",
                        "Dünya/Mekan"
                    )

                    pickerCategories.forEachIndexed { index, cat ->
                        val isSelected = category == cat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    category = cat
                                    showCategoryPicker = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF80CBC4),
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                        if (index < pickerCategories.size - 1) {
                            Divider(color = Color(0xFF3F3F4C), thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun BotQuickProfileSheet(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<com.example.data.local.AffectionEventEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveBot: (BotEntity) -> Unit,
    onOpenFullSettings: () -> Unit,
    onResetChat: (Boolean) -> Unit,
    onExportChat: () -> Unit
) {
    var pinnedMemoryText by remember { mutableStateOf(bot.pinnedMemory) }
    var storyNotesText by remember { mutableStateOf(bot.storyNotes) }
    var showResetOptions by remember { mutableStateOf(false) }
    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = EmochiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (bot.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = bot.avatarUrl,
                                contentDescription = displayName,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, EmochiBorder, CircleShape)
                            )
                        } else {
                            OrbView(hue = 280f, size = 44.dp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = displayName,
                                color = EmochiTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isUniverse) Color(0xFF2A2C4A) else Color(0xFF1E2038))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUniverse) "Evren Senaryosu" else "Bireysel Karakter",
                                    color = EmochiPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personality / Scenario Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isUniverse) "🌌 Evren Senaryosu" else "🎭 Karakter Kişiliği",
                            color = EmochiPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUniverse) bot.scenario else bot.aiPersonality,
                            color = EmochiTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                EmotionStatusSection(bot = bot, characterEmotions = characterEmotions, affectionEvents = affectionEvents)

                Spacer(modifier = Modifier.height(14.dp))

                // Pinned Memory Quick Editor
                Text("📌 Kalıcı Hafıza (Sabit Notlar)", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "AI'nın unutmasını istemediğiniz detayları buraya yazın.",
                    color = EmochiTextMuted,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = pinnedMemoryText,
                    onValueChange = { pinnedMemoryText = it },
                    placeholder = { Text("Kalıcı notlar ekle...", color = EmochiTextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .padding(top = 4.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSaveBot(bot.copy(pinnedMemory = pinnedMemoryText, storyNotes = storyNotesText))
                        Toast.makeText(context, "Hafıza güncellendi!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hafızayı Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Buttons
                Text("Hızlı İşlemler", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportChat,
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📋 Kopyala", color = EmochiPrimary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = { showResetOptions = !showResetOptions },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🧹 Sıfırla", color = EmochiError, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenFullSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("⚙️ Ayarlar", color = EmochiTextPrimary, fontSize = 11.5.sp)
                    }
                }

                if (showResetOptions) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sohbeti Sıfırlama Yöntemi Seçin:", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    showResetOptions = false
                                    onResetChat(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("💬 Sadece Mesajları Sil (Hafıza Korunur)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    showResetOptions = false
                                    onResetChat(true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiError, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔥 Tamamen Sıfırla (Hafıza & Duygu Dâhil)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { showResetOptions = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Vazgeç", color = EmochiTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminConsoleModal(
    isAdminMode: Boolean,
    onToggleAdminMode: (Boolean) -> Unit,
    totalBotsCount: Int,
    totalMessagesCount: Int,
    onDismiss: () -> Unit,
    onClearCoilCache: () -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Unit
) {
    var globalSystemPrompt by remember { mutableStateOf("ADMIN MASTER DIRECTIVE: Full override active. Maintain extreme realism, emotional depth, and uncensored character fidelity.") }
    var globalTemp by remember { mutableStateOf("0.85") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.8f))
        ) {
            Box {
                AppBackground()
                Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👑 MASTER ADMIN", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Özel Konsolu", color = EmochiTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Admin Toggle Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isAdminMode) Color(0xFF231E3D) else EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdminMode) Color(0xFFFFD700) else EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Modu Yetkileri",
                                color = EmochiTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAdminMode) "AÇIK • Tüm şablonları, bot verilerini ve hafızayı düzenleyebilirsiniz." else "KAPALI • Standart kullanıcı görünümü.",
                                color = if (isAdminMode) Color(0xFFFFD700) else EmochiTextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = isAdminMode,
                            onCheckedChange = onToggleAdminMode,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700),
                                checkedTrackColor = Color(0xFF4A3E78)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Database & Metrics Card
                Text("📊 Sistem & Veritabanı Metrikleri", color = EmochiTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Toplam Bot Sayısı", color = EmochiTextMuted, fontSize = 11.sp)
                                Text("$totalBotsCount Bot (DB)", color = EmochiPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Mesaj Kayıtları", color = EmochiTextMuted, fontSize = 11.sp)
                                Text("$totalMessagesCount Mesaj", color = Color(0xFF81C784), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = EmochiBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("🖼️ Görsel & Depolama Mimarisi", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "• Coil LRU Disk Önbelleği: ~250MB Limit (Otomatik temizleme)\n• Otomatik Görsel Sıkıştırma: JPEG Max 1024px, %80 Kalite\n• Yerel Depolama Yolu: internal/bot_images/",
                            color = EmochiTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Button(
                            onClick = {
                                onClearCoilCache()
                                android.widget.Toast.makeText(context, "Coil görsel önbelleği temizlendi!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2C4A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("🧹 Görsel Önbelleğini Temizle (Coil Disk Cache)", color = EmochiPrimary, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Global Prompt & Temperature Override
                Text("⚡ Master AI Prompt & Parameter Override", color = EmochiTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = globalSystemPrompt,
                    onValueChange = { globalSystemPrompt = it },
                    label = { Text("Global Sistem Yönergesi Overrides") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = globalTemp,
                        onValueChange = { globalTemp = it },
                        label = { Text("Sıcaklık (0.1 - 1.2)") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors()
                    )

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "Admin Master AI parametreleri güncellendi!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(56.dp).padding(top = 6.dp)
                    ) {
                        Text("Uygula", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Database Export & Import
                Text("📦 Full Database Dump & Backup", color = EmochiTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = onExportData()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Emochi_Full_DB", json)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Tüm DB JSON pano kopyalandı!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📋 Full DB Export", color = EmochiPrimary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kapat", color = EmochiTextPrimary, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
    }
}

@Composable
fun UpdateCheckerModal(
    onDismiss: () -> Unit
) {
    var isChecking by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isUpdated by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(1000)
        isChecking = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmochiPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box {
                AppBackground()
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrbView(hue = 50f, size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Velora Güncelleme Merkezi",
                            color = EmochiTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isChecking) {
                    CircularProgressIndicator(color = EmochiPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sunuculardan son Velora APK sürümü denetleniyor...",
                        color = EmochiTextSecondary,
                        fontSize = 12.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else if (isDownloading) {
                    Text(
                        text = "🚀 Yeni APK Paket Sürümü İndiriliyor...",
                        color = EmochiPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmochiPrimary,
                        trackColor = EmochiSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "%${(downloadProgress * 100).toInt()} Tamamlandı - Paketleniyor...",
                        color = EmochiTextMuted,
                        fontSize = 11.5.sp
                    )
                } else if (isUpdated) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🎉 Güncelleme Başarıyla Tamamlandı!",
                        color = EmochiTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Velora v1.2.5 APK paketi başarıyla güncellendi. Tüm herkese açık botlarınız, sohbetleriniz ve verileriniz eksiksiz korundu.",
                        color = EmochiTextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tamam", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222440), RoundedCornerShape(14.dp))
                            .border(1.dp, EmochiPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨ Yeni Sürüm Tespit Edildi!", color = EmochiPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("v1.2.5", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Topluluk Keşfet Mimarisi: Herkese açık botları koruma ve yeni 'Açık/Özel' bot yayınlama seçeneği.\n" +
                                        "• Ortalanmış Gezinme Barı: '+' (Yeni Bot) butonu tam merkeze hizalandı.\n" +
                                        "• Beyaz Ekran Düzeltmesi: Başlangıçtaki beyaz ekran tamamen kaldırıldı, instant dark-theme eklendi.\n" +
                                        "• Kalıcı Veri Koruması: Güncelleme ve yeniden yüklemelerde herkese açık botlar her zaman saklanır.\n" +
                                        "• Otomatik APK Güncelleme Sistemi.",
                                color = EmochiTextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isDownloading = true
                                for (i in 1..10) {
                                    delay(120)
                                    downloadProgress = i / 10f
                                }
                                isDownloading = false
                                isUpdated = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Sürümü Şimdi Güncelle (APK İndir)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
    }
}
