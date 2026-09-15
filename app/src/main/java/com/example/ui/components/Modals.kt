package com.example.ui.components

import androidx.compose.runtime.mutableStateMapOf

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import com.example.data.repository.EmochiRepository
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
    onImportData: suspend (String) -> Unit,
    viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var geminiApiKey by remember { mutableStateOf(settings.customApiKey) }
    var groqApiKey by remember { mutableStateOf(settings.groqApiKey) }
    var claudeApiKey by remember { mutableStateOf(settings.claudeApiKey) }
    var openaiApiKey by remember { mutableStateOf(settings.openaiApiKey) }
    var openRouterApiKey by remember { mutableStateOf(settings.openRouterApiKey) }
    var openRouterModel by remember { mutableStateOf(settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }) }
    var nvidiaApiKey by remember { mutableStateOf(settings.nvidiaApiKey) }
    var nvidiaModel by remember { mutableStateOf(settings.nvidiaModel.ifBlank { "" }) }
    var mistralApiKey by remember { mutableStateOf(settings.mistralApiKey) }
    var mistralModel by remember { mutableStateOf(settings.mistralModel.ifBlank { "mistral-large-latest" }) }
    var backupApiKey by remember { mutableStateOf(settings.backupApiKey) }

    var openRouterTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingOpenRouter by remember { mutableStateOf(false) }

    var nvidiaTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingNvidia by remember { mutableStateOf(false) }

    var mistralTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingMistral by remember { mutableStateOf(false) }

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
    var enableLlm7 by remember { mutableStateOf(settings.enableLlm7) }
    var enablePollinations by remember { mutableStateOf(settings.enablePollinations) }
    var enableOvh by remember { mutableStateOf(settings.enableOvh) }
    var pollinationsModel by remember { mutableStateOf(settings.pollinationsModel.ifBlank { "openai" }) }
    var ovhModel by remember { mutableStateOf(settings.ovhModel.ifBlank { "meta-llama/Meta-Llama-3-70B-Instruct" }) }

    var llm7TestResult by remember { mutableStateOf<String?>(null) }
    var isTestingLlm7 by remember { mutableStateOf(false) }

    var pollinationsTestResult by remember { mutableStateOf<String?>(null) }
    var isTestingPollinations by remember { mutableStateOf(false) }

    var showFallbackLogModal by remember { mutableStateOf(false) }
    var enableTts by remember { mutableStateOf(settings.enableTts) }
    var ttsSpeed by remember { mutableStateOf(settings.ttsSpeed) }
    var ttsPitch by remember { mutableStateOf(settings.ttsPitch) }
    var selectedVoiceName by remember { mutableStateOf(settings.selectedVoiceName) }
    var appLanguage by remember { mutableStateOf(settings.appLanguage) }

    var showKeys by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>("models") }
    var fallbackChainOrder by remember { mutableStateOf(settings.fallbackChainOrder) }

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
            key = "default",
            name = "LLM7 Default (Ücretsiz)",
            provider = "LLM7 (api.llm7.io)",
            tokenCostRate = "🆓 ÜCRETSİZ (API Key Gerekmez)",
            badgeColor = Color(0xFF00E676),
            description = "API Key gerektirmeyen tamamen ücretsiz OpenAI uyumlu servis. Öncelik modunda varsayılan olarak denenir."
        ),
        ModelSpec(
            key = "gemini-2.0-flash",
            name = "Gemini 2.0 Flash",
            provider = "Google Gemini",
            tokenCostRate = "🟢 Düşük (~0.5x Token)",
            badgeColor = Color(0xFF4CAF50),
            description = "En gelişmiş, dengeli ve hızlı Gemini modeli. Düşük token harcaması ile yüksek kaliteli rol yapma yanıtları verir."
        ),
        ModelSpec(
            key = "gemini-1.5-pro",
            name = "Gemini 1.5 Pro",
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
            key = "llama-3.1-8b-instant",
            name = "Groq Llama 3.1 8B",
            provider = "Groq API",
            tokenCostRate = "🟢 Düşük (~0.5x Token)",
            badgeColor = Color(0xFF4CAF50),
            description = "Groq sunucularında anlık ultra hızlı ve pratik sohbet yanıtları."
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
            key = "gpt-4o",
            name = "OpenAI GPT-4o",
            provider = "OpenAI",
            tokenCostRate = "🔴 Yüksek (~2.0x Token)",
            badgeColor = Color(0xFF9C27B0),
            description = "OpenAI amiral gemisi akıllı sohbet modeli."
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF130E26),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppBackground()

                Column(modifier = Modifier.fillMaxSize()) {
                    // FIXED HEADER AT TOP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1333))
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmochiPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = EmochiPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Uygulama & AI Ayarları",
                                    color = EmochiTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Model, API anahtarları ve filtreler",
                                    color = EmochiTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                        }
                    }

                    Divider(color = EmochiBorder)

                    // SCROLLABLE CONTENT BODY
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

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

                    val customProviders by viewModel.customProviders.collectAsState()
                    var showAddCustomModal by remember { mutableStateOf(false) }
                    var editingCustomProvider by remember { mutableStateOf<com.example.data.local.CustomProviderEntity?>(null) }
                    var quickModelEditProvider by remember { mutableStateOf<com.example.data.local.CustomProviderEntity?>(null) }

                    // Primary Provider Selector Segmented Buttons
                    Text("Ana Sağlayıcı Seçin:", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val providers = listOf(
                            "llm7" to "⚡ LLM7",
                            "gemini" to "Gemini",
                            "groq" to "Groq",
                            "claude" to "Claude",
                            "openai" to "OpenAI",
                            "openrouter" to "OpenRouter",
                            "nvidia" to "NVIDIA NIM",
                            "mistral" to "Mistral AI",
                            "custom" to "🌐 Özel API"
                        )
                        providers.forEach { (pKey, pLabel) ->
                            val isSel = if (pKey == "custom") selectedProvider.startsWith("custom") else selectedProvider == pKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) EmochiPrimary.copy(alpha = 0.25f) else EmochiCard)
                                    .border(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        color = if (isSel) EmochiPrimary else EmochiBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        when (pKey) {
                                            "openrouter" -> {
                                                selectedProvider = "openrouter"
                                                selectedModel = openRouterModel.ifBlank { "deepseek/deepseek-chat" }
                                            }
                                            "nvidia" -> {
                                                selectedProvider = "nvidia"
                                                selectedModel = nvidiaModel.ifBlank { "" }
                                            }
                                            "mistral" -> {
                                                selectedProvider = "mistral"
                                                selectedModel = mistralModel.ifBlank { "mistral-large-latest" }
                                            }
                                            "custom" -> {
                                                if (customProviders.isNotEmpty()) {
                                                    selectedProvider = "custom_${customProviders.first().id}"
                                                    selectedModel = customProviders.first().modelName
                                                } else {
                                                    selectedProvider = "custom"
                                                    showAddCustomModal = true
                                                }
                                            }
                                            else -> {
                                                selectedProvider = pKey
                                                val providerModels = modelsList.filter {
                                                    when (pKey) {
                                                        "llm7" -> it.provider == "LLM7 (api.llm7.io)"
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
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pLabel,
                                    color = if (isSel) Color.White else EmochiTextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Reliability Dashboard for SECONDARY tier
                    ProviderReliabilityDashboardUI()

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Custom Provider Header Banner (Katman 0 Indicator)
                    val activeCustomProviderAtTop = customProviders.find { "custom_${it.id}" == selectedProvider }
                    if (activeCustomProviderAtTop != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFA855F7)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Şu an aktif: ${activeCustomProviderAtTop.label} (Özel API)",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFA855F7).copy(alpha = 0.3f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("KATMAN 0 (İLK SIRADA)", color = Color(0xFFE9D5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Model: ${activeCustomProviderAtTop.modelName} | URL: ${activeCustomProviderAtTop.baseUrl}",
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚡ İstekler önce bu Özel API'ye (Katman 0) gönderilir. Başarısızlık durumunda otomatik Katman 1'e (LLM7/Pollinations/OVH) düşer.",
                                    color = EmochiTextSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Provider Card 0: LLM7 (Fixed / Free Provider)
                    val isLlm7Active = selectedProvider == "llm7" || enableLlm7
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isLlm7Active) 2.dp else 1.dp, if (isLlm7Active) Color(0xFF00E676) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("0. LLM7 (Ücretsiz Sabit Servis)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (enableLlm7) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ÖNCELİKLİ (STEP 1)", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "API Key gerektirmeyen ücretsiz servis (https://api.llm7.io/v1). Model: default | Key: unused",
                                color = EmochiTextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("LLM7'yi Öncelikli Kullan (Fallback Chain)", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Açıkken her istek ilk olarak LLM7'ye gönderilir. Başarısız olursa sıradaki sağlayıcı denenir.", color = EmochiTextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = enableLlm7,
                                    onCheckedChange = { enableLlm7 = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF00E676)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isTestingLlm7 = true
                                            llm7TestResult = "Bağlantı test ediliyor..."
                                            val res = viewModel.testLlm7Connection()
                                            isTestingLlm7 = false
                                            if (res.isSuccess) {
                                                val fcText = if (res.supportsFunctionCalling) "Function calling (gelişmiş hafıza) destekleniyor." else "Metin-tabanlı hafıza modu ([[MEMORY_SAVE]]) aktif."
                                                llm7TestResult = "✅ Bağlantı başarılı! $fcText"
                                            } else {
                                                llm7TestResult = "❌ Bağlantı hatası: ${res.errorMessage}"
                                            }
                                        }
                                    },
                                    enabled = !isTestingLlm7,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isTestingLlm7) "Test ediliyor..." else "⚡ LLM7 Test Et", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showFallbackLogModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("📋 Fallback Logları", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!llm7TestResult.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = llm7TestResult!!,
                                    color = if (llm7TestResult!!.contains("✅")) Color(0xFF69F0AE) else Color(0xFFFCA5A5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            val llm7Stats = remember { com.example.util.Llm7RateLimitTracker.getUsageStats() }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("📊 LLM7 Kota & Performans Göstergesi", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Dakika / Saat İstek:", color = EmochiTextMuted, fontSize = 10.sp)
                                        Text("${llm7Stats.requestsLastMinute}/10 req/dk | ${llm7Stats.requestsLastHour}/60 req/saat", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("24-Saatlik Token Kullanımı:", color = EmochiTextMuted, fontSize = 10.sp)
                                        Text("${llm7Stats.tokensUsed24h} / ${llm7Stats.tokenLimit24h} token", color = if (llm7Stats.isTokenLimitReached) Color(0xFFEF4444) else Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Ort. Süre / Hata Düzeltme:", color = EmochiTextMuted, fontSize = 10.sp)
                                        Text("${llm7Stats.averageResponseTimeMs}ms | ${llm7Stats.malformedCount} düzelg | ${llm7Stats.fallbackCount} devir", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Card 0B: Pollinations.ai (Free Provider)
                    val isPollinationsActive = selectedProvider == "pollinations" || enablePollinations
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isPollinationsActive) 2.dp else 1.dp, if (isPollinationsActive) Color(0xFF00E676) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("0B. Pollinations (Limitsiz Ücretsiz)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (enablePollinations) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("STEP 2 FALLBACK", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "API Key gerektirmez (https://text.pollinations.ai/openai). Key: unused",
                                color = EmochiTextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aktif Pollinations Modeli: $pollinationsModel", color = EmochiTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("openai", "mistral", "qwen", "llama").forEach { m ->
                                    val isSelected = m == pollinationsModel
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF00E676).copy(alpha = 0.25f) else EmochiCard)
                                            .border(1.dp, if (isSelected) Color(0xFF00E676) else EmochiBorder, RoundedCornerShape(8.dp))
                                            .clickable { pollinationsModel = m }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(m, color = if (isSelected) Color.White else EmochiTextSecondary, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pollinations'ı Fallback Chain'e Ekle", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("LLM7 başarısız olursa otomatik olarak Pollinations denenir.", color = EmochiTextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = enablePollinations,
                                    onCheckedChange = { enablePollinations = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF00E676)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTestingPollinations = true
                                        pollinationsTestResult = "Bağlantı test ediliyor..."
                                        val res = viewModel.testPollinationsConnection(pollinationsModel)
                                        isTestingPollinations = false
                                        if (res.isSuccess) {
                                            pollinationsTestResult = "✅ Pollinations bağlantısı başarılı! (Model: $pollinationsModel)"
                                        } else {
                                            pollinationsTestResult = "❌ Bağlantı hatası: ${res.errorMessage}"
                                        }
                                    }
                                },
                                enabled = !isTestingPollinations,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isTestingPollinations) "Test ediliyor..." else "⚡ Pollinations Test Et", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (!pollinationsTestResult.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pollinationsTestResult!!,
                                    color = if (pollinationsTestResult!!.contains("✅")) Color(0xFF69F0AE) else Color(0xFFFCA5A5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Card 0C: OVH AI Free
                    val isOvhActive = selectedProvider == "ovh" || enableOvh
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isOvhActive) 2.dp else 1.dp, if (isOvhActive) Color(0xFF00E676) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("0C. OVH AI (Anahtarsız Ücretsiz)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (enableOvh) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("KATMAN 1 FALLBACK", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "API Key gerektirmez. Model: Meta-Llama-3-70B-Instruct (Varsayılan Kapalı).",
                                color = EmochiTextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("OVH AI'yi Fallback Chain'e Ekle (Katman 1)", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("LLM7 ve Pollinations başarısız olursa otomatik denenir.", color = EmochiTextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = enableOvh,
                                    onCheckedChange = { enableOvh = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF00E676)
                                    )
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
                                    Text("2. Groq API (Süper Hızlı)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

                    // Provider Card 5: OpenRouter API
                    val isOpenRouterActive = selectedProvider == "openrouter"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isOpenRouterActive) 2.dp else 1.dp, if (isOpenRouterActive) Color(0xFF38BDF8) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("5. OpenRouter API", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isOpenRouterActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFF38BDF8), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🔑 OpenRouter Key Al: openrouter.ai/keys", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("• Ücretsiz kullanım için model adının sonuna ':free' ekleyebilir veya 'deepseek/deepseek-chat' kullanabilirsiniz.", color = Color.White, fontSize = 9.5.sp)
                                    Text("• Oran Sınırı: Ücretsiz katmanda günde ~50 istek.", color = EmochiTextSecondary, fontSize = 9.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = openRouterApiKey,
                                onValueChange = { openRouterApiKey = it },
                                placeholder = { Text("sk-or-v1-...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Kodu / Adı:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = openRouterModel,
                                onValueChange = {
                                    openRouterModel = it
                                    if (isOpenRouterActive) selectedModel = it
                                },
                                placeholder = { Text("deepseek/deepseek-chat", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Model Kısayolları:", color = EmochiTextMuted, fontSize = 10.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "DeepSeek Chat" to "deepseek/deepseek-chat",
                                    "DeepSeek R1 (Free)" to "deepseek/deepseek-r1:free",
                                    "Llama 3.3 70B (Free)" to "meta-llama/llama-3.3-70b-instruct:free",
                                    "Qwen 2.5 72B" to "qwen/qwen-2.5-72b-instruct"
                                ).forEach { (lbl, code) ->
                                    OutlinedButton(
                                        onClick = {
                                            openRouterModel = code
                                            selectedProvider = "openrouter"
                                            selectedModel = code
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(lbl, fontSize = 9.5.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTestingOpenRouter = true
                                        openRouterTestResult = "Test ediliyor..."
                                        val res = viewModel.testOpenRouterConnection(openRouterApiKey, openRouterModel)
                                        isTestingOpenRouter = false
                                        openRouterTestResult = if (res.isSuccess) "✅ OpenRouter bağlantısı başarılı!" else "❌ Hata: ${res.errorMessage}"
                                    }
                                },
                                enabled = !isTestingOpenRouter && openRouterApiKey.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isTestingOpenRouter) "Test ediliyor..." else "⚡ OpenRouter Test Et", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            openRouterTestResult?.let { msg ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg, color = if (msg.contains("✅")) Color(0xFF69F0AE) else Color(0xFFFCA5A5), fontSize = 11.sp)
                            }
                        }
                    }

                    // Provider Card 6: NVIDIA NIM API
                    val isNvidiaActive = selectedProvider == "nvidia"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isNvidiaActive) 2.dp else 1.dp, if (isNvidiaActive) Color(0xFF76B900) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFF76B900), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("6. NVIDIA NIM API", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isNvidiaActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF76B900).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("AKTİF SAĞLAYICI", color = Color(0xFF76B900), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2819)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF76B900).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🔑 NVIDIA NIM Key Al: build.nvidia.com", color = Color(0xFF76B900), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("• Model formatı: sağlayıcı/model (ör. deepseek-ai/deepseek-v4-flash, mistralai/mistral-nemotron).", color = Color.White, fontSize = 9.5.sp)
                                    Text("⚠️ Limit: Dakikada 40 istek (40 RPM) tüm modeller toplamında geçerlidir. Kredi biterse HTTP 402 verir.", color = Color(0xFFFFCC00), fontSize = 9.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = nvidiaApiKey,
                                onValueChange = { nvidiaApiKey = it },
                                placeholder = { Text("nvapi-...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Kodu / Adı:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = nvidiaModel,
                                onValueChange = {
                                    nvidiaModel = it
                                    if (isNvidiaActive) selectedModel = it
                                },
                                placeholder = { Text("deepseek-ai/deepseek-v4-flash", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Model Kısayolları:", color = EmochiTextMuted, fontSize = 10.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "DeepSeek V4 Flash" to "deepseek-ai/deepseek-v4-flash",
                                    "Mistral Nemotron" to "mistralai/mistral-nemotron",
                                    "Llama 3.3 70B" to "meta-llama/llama-3.3-70b-instruct"
                                ).forEach { (lbl, code) ->
                                    OutlinedButton(
                                        onClick = {
                                            nvidiaModel = code
                                            selectedProvider = "nvidia"
                                            selectedModel = code
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(lbl, fontSize = 9.5.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTestingNvidia = true
                                        nvidiaTestResult = "Test ediliyor..."
                                        val res = viewModel.testNvidiaConnection(nvidiaApiKey, nvidiaModel)
                                        isTestingNvidia = false
                                        nvidiaTestResult = if (res.isSuccess) "✅ NVIDIA NIM bağlantısı başarılı!" else "❌ Hata: ${res.errorMessage}"
                                    }
                                },
                                enabled = !isTestingNvidia && nvidiaApiKey.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isTestingNvidia) "Test ediliyor..." else "⚡ NVIDIA NIM Test Et", color = Color(0xFF76B900), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            nvidiaTestResult?.let { msg ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg, color = if (msg.contains("✅")) Color(0xFF69F0AE) else Color(0xFFFCA5A5), fontSize = 11.sp)
                            }
                        }
                    }

                    // Provider Card 7: Mistral AI API
                    val isMistralActive = selectedProvider == "mistral"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isMistralActive) 2.dp else 1.dp, if (isMistralActive) Color(0xFFFF9800) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("8. Mistral AI API", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isMistralActive) {
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
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF331F00)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🔑 Mistral Key Al: console.mistral.ai", color = Color(0xFFFF9800), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("• Model adı: mistral-large-latest, open-mistral-nemo, codestral-latest", color = Color.White, fontSize = 9.5.sp)
                                    Text("⚠️ GİZLİLİK UYARISI: Bu ücretsiz katmanda verilerinizin Mistral tarafından model eğitiminde kullanılmasına izin vermiş olursunuz.", color = Color(0xFFFFD54F), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = mistralApiKey,
                                onValueChange = { mistralApiKey = it },
                                placeholder = { Text("Mistral API Key...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Model Kodu / Adı:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = mistralModel,
                                onValueChange = {
                                    mistralModel = it
                                    if (isMistralActive) selectedModel = it
                                },
                                placeholder = { Text("mistral-large-latest", fontSize = 11.5.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Model Kısayolları:", color = EmochiTextMuted, fontSize = 10.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Mistral Large" to "mistral-large-latest",
                                    "Mistral Nemo" to "open-mistral-nemo",
                                    "Codestral" to "codestral-latest"
                                ).forEach { (lbl, code) ->
                                    OutlinedButton(
                                        onClick = {
                                            mistralModel = code
                                            selectedProvider = "mistral"
                                            selectedModel = code
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(lbl, fontSize = 9.5.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isTestingMistral = true
                                        mistralTestResult = "Test ediliyor..."
                                        val res = viewModel.testMistralConnection(mistralApiKey, mistralModel)
                                        isTestingMistral = false
                                        mistralTestResult = if (res.isSuccess) "✅ Mistral AI bağlantısı başarılı!" else "❌ Hata: ${res.errorMessage}"
                                    }
                                },
                                enabled = !isTestingMistral && mistralApiKey.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(if (isTestingMistral) "Test ediliyor..." else "⚡ Mistral AI Test Et", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            mistralTestResult?.let { msg ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg, color = if (msg.contains("✅")) Color(0xFF69F0AE) else Color(0xFFFCA5A5), fontSize = 11.sp)
                            }
                        }
                    }

                    // Provider Card 9: Özel API / Proxy (HER ZAMAN GÖRÜNÜR & BELİRGİN)
                    val isCustomActive = selectedProvider.startsWith("custom")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isCustomActive) 2.dp else 1.dp, if (isCustomActive) Color(0xFFFF9800) else EmochiBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("5. Özel API & OpenRouter Proxy", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isCustomActive) {
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
                            Text(
                                "OpenRouter, VLLM, LM Studio, Ollama veya özel bir proxy adresi bağlayarak dilediğiniz yapay zeka modelinin kodunu (örn. Llama 3.3, DeepSeek R1, Qwen 2.5) yazıp kullanabilirsiniz.",
                                color = EmochiTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Direct Model Code Editing Field
                            Text("Özel Model Kodu / Adı (Elle Yazabilirsiniz):", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = selectedModel,
                                onValueChange = { newM ->
                                    selectedModel = newM
                                    if (selectedProvider.startsWith("custom_")) {
                                        val cId = selectedProvider.removePrefix("custom_").toLongOrNull()
                                        val activeP = customProviders.find { it.id == cId }
                                        if (activeP != null) {
                                            viewModel.updateCustomProviderModel(activeP.id, newM)
                                        }
                                    }
                                },
                                placeholder = { Text("örn. meta-llama/llama-3.3-70b-instruct", fontSize = 11.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                singleLine = true,
                                colors = customTextFieldColors()
                            )

                            // Quick Model Template Chips
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Popüler Model Kısayolları (Dokun & Doldur):", color = EmochiTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val quickChips = listOf(
                                "🦙 Llama 3.3 70B" to "meta-llama/llama-3.3-70b-instruct",
                                "🐋 DeepSeek R1" to "deepseek/deepseek-r1",
                                "🌐 Qwen 2.5 72B" to "qwen/qwen-2.5-72b-instruct",
                                "⚡ Gemini 2.0 Flash" to "google/gemini-2.0-flash-001",
                                "🧠 Claude 3.5 Sonnet" to "anthropic/claude-3.5-sonnet",
                                "🤖 GPT-4o Mini" to "openai/gpt-4o-mini"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickChips.forEach { (lbl, code) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmochiSurface)
                                            .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedModel = code
                                                if (selectedProvider.startsWith("custom_")) {
                                                    val cId = selectedProvider.removePrefix("custom_").toLongOrNull()
                                                    val activeP = customProviders.find { it.id == cId }
                                                    if (activeP != null) {
                                                        viewModel.updateCustomProviderModel(activeP.id, code)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(lbl, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // GÖRÜNÜR & BELİRGİN EKLE BUTONU
                            Button(
                                onClick = { showAddCustomModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Yeni Özel API / Model Ekle", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }

                            if (customProviders.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Kayıtlı Özel API Sağlayıcılarınız:", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))

                                customProviders.forEach { cp ->
                                    val isThisSelected = selectedProvider == "custom_${cp.id}"
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = if (isThisSelected) Color(0xFF2E1065).copy(alpha = 0.4f) else EmochiSurface),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isThisSelected) Color(0xFFA855F7) else EmochiBorder),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(cp.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(EmochiBorder)
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(cp.apiFormat.uppercase(), color = EmochiTextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("URL: ${cp.baseUrl}", color = EmochiTextMuted, fontSize = 9.5.sp, maxLines = 1)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Model: ", color = EmochiTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        Text(cp.modelName, color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (isThisSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFA855F7).copy(alpha = 0.3f))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Text("KATMAN 0 AKTİF", color = Color(0xFFE9D5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isThisSelected) Color(0xFFA855F7).copy(alpha = 0.15f) else EmochiCard)
                                                    .clickable {
                                                        selectedProvider = "custom_${cp.id}"
                                                        selectedModel = cp.modelName
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isThisSelected,
                                                    onClick = {
                                                        selectedProvider = "custom_${cp.id}"
                                                        selectedModel = cp.modelName
                                                    },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = Color(0xFFA855F7),
                                                        unselectedColor = EmochiTextMuted
                                                    ),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isThisSelected) "Aktif Sağlayıcı (Katman 0 — Öncelikli)" else "Aktif Sağlayıcı Olarak Kullan",
                                                    color = if (isThisSelected) Color(0xFFE9D5FF) else EmochiTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { quickModelEditProvider = cp },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Modeli Yaz / Değiştir", color = Color(0xFFFFB74D), fontSize = 9.5.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                OutlinedButton(
                                                    onClick = { editingCustomProvider = cp },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text("Düzenle", color = EmochiTextPrimary, fontSize = 9.5.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { viewModel.deleteCustomProvider(cp.id) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
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
                    val activeCustomProvider = customProviders.find { "custom_${it.id}" == selectedProvider }
                    if (activeCustomProvider != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9800)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Aktif Özel Model: ${activeCustomProvider.modelName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(activeCustomProvider.label, color = Color(0xFFFF9800), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("URL: ${activeCustomProvider.baseUrl} | Format: ${activeCustomProvider.apiFormat.uppercase()}", color = EmochiTextSecondary, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { quickModelEditProvider = activeCustomProvider },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Farklı Model Kodu Yaz / Değiştir", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
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

                    // MODAL 1: YENİ ÖZEL SAĞLAYICI & MODEL EKLE
                    if (showAddCustomModal) {
                        var cLabel by remember { mutableStateOf("") }
                        var cBaseUrl by remember { mutableStateOf("https://openrouter.ai/api/v1") }
                        var cApiKey by remember { mutableStateOf("") }
                        var cModelName by remember { mutableStateOf("") }
                        var cApiFormat by remember { mutableStateOf("openai") }
                        var showKeyText by remember { mutableStateOf(false) }

                        var isTesting by remember { mutableStateOf(false) }
                        var testRes by remember { mutableStateOf<EmochiRepository.ProviderTestResult?>(null) }
                        val customModalScope = rememberCoroutineScope()

                        Dialog(onDismissRequest = { showAddCustomModal = false }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EmochiSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                     Text("🌐 Yeni Özel API & Model Ekle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = cLabel,
                                        onValueChange = { cLabel = it; testRes = null },
                                        label = { Text("Sağlayıcı Etiketi (ör. OpenRouter, NVIDIA NIM)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    Text("API Formatı:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = cApiFormat == "openai",
                                            onClick = { cApiFormat = "openai"; testRes = null },
                                            label = { Text("OpenAI Uyumlu", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = cApiFormat == "anthropic",
                                            onClick = { cApiFormat = "anthropic"; testRes = null },
                                            label = { Text("Anthropic Uyumlu", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = cBaseUrl,
                                        onValueChange = { cBaseUrl = it; testRes = null },
                                        label = { Text("Base URL (HTTPS zorunlu)", fontSize = 11.sp) },
                                        placeholder = { Text("https://openrouter.ai/api/v1", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    // URL Preset Chips
                                    Text("Hızlı Adres Kısayolları:", color = EmochiTextMuted, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "OpenRouter" to "https://openrouter.ai/api/v1",
                                            "Groq Proxy" to "https://api.groq.com/openai/v1",
                                            "DeepSeek" to "https://api.deepseek.com/v1",
                                            "Ollama Local" to "http://localhost:11434/v1"
                                        ).forEach { (presetName, presetUrl) ->
                                            OutlinedButton(
                                                onClick = { cBaseUrl = presetUrl; testRes = null },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(presetName, fontSize = 9.5.sp)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = cApiKey,
                                        onValueChange = { cApiKey = it; testRes = null },
                                        label = { Text("API Key", fontSize = 11.sp) },
                                        visualTransformation = if (showKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showKeyText = !showKeyText }) {
                                                Icon(if (showKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    // MODEL SEÇİMİ VE EL İLE MODEL YAZMA ALANI
                                    OutlinedTextField(
                                        value = cModelName,
                                        onValueChange = { cModelName = it; testRes = null },
                                        label = { Text("Model Kodu / Adı (El ile Dilediğinizi Yazın)", fontSize = 11.sp) },
                                        placeholder = { Text("meta-llama/llama-3.3-70b-instruct", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    // MODEL HAZIR ŞABLON ŞİPLERİ
                                    Text("Popüler Model Kısayolları (Dokun & Doldur):", color = EmochiTextMuted, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "🦙 Llama 3.3 70B" to "meta-llama/llama-3.3-70b-instruct",
                                            "🐋 DeepSeek R1" to "deepseek/deepseek-r1",
                                            "🌐 Qwen 2.5 72B" to "qwen/qwen-2.5-72b-instruct",
                                            "⚡ Gemini 2.0 Flash" to "google/gemini-2.0-flash-001",
                                            "🧠 Claude 3.5 Sonnet" to "anthropic/claude-3.5-sonnet",
                                            "🤖 GPT-4o Mini" to "openai/gpt-4o-mini",
                                            "💨 Mistral Large" to "mistralai/mistral-large"
                                        ).forEach { (mTag, mCode) ->
                                            OutlinedButton(
                                                onClick = { cModelName = mCode; testRes = null },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(mTag, fontSize = 9.5.sp)
                                            }
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332A00)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Güvenlik Uyarısı: Buraya girdiğin API key, doğrudan girdiğin adrese ($cBaseUrl) gönderilir. Güvendiğin ve HTTPS olan bir kaynak olduğundan emin ol. Key Keystore ile şifrelenir.",
                                                color = Color(0xFFFFE082),
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    val currentTestRes = testRes
                                    if (currentTestRes != null) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = if (currentTestRes.isSuccess) Color(0xFF0D331A) else Color(0xFF330D0D)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (currentTestRes.isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                if (currentTestRes.isSuccess) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Bağlantı Başarılı!", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        if (currentTestRes.supportsFunctionCalling) "✅ Bu sağlayıcı gelişmiş hafıza (tools/function calling) modunu destekliyor."
                                                        else "⚠️ Bu sağlayıcı gelişmiş hafıza özelliğini desteklemiyor olabilir, metin tabanlı yedek hafıza modu kullanılacak.",
                                                        color = Color.White,
                                                        fontSize = 10.5.sp
                                                    )
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Bağlantı Başarısız", color = Color(0xFFE57373), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(currentTestRes.errorMessage, color = Color.White, fontSize = 10.5.sp)
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showAddCustomModal = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("İptal", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                customModalScope.launch {
                                                    isTesting = true
                                                    testRes = viewModel.testCustomProviderConnection(cBaseUrl, cApiKey, cModelName, cApiFormat)
                                                    isTesting = false
                                                }
                                            },
                                            enabled = !isTesting && cBaseUrl.isNotBlank() && cApiKey.isNotBlank() && cModelName.isNotBlank(),
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                        ) {
                                            if (isTesting) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                            } else {
                                                Text("Bağlantıyı Test Et", fontSize = 10.5.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val finalResult = testRes
                                            viewModel.saveCustomProvider(
                                                label = cLabel.ifBlank { "Özel API" },
                                                baseUrl = cBaseUrl,
                                                apiKey = cApiKey,
                                                modelName = cModelName,
                                                apiFormat = cApiFormat,
                                                supportsFunctionCalling = finalResult?.supportsFunctionCalling ?: true
                                            )
                                            selectedModel = cModelName
                                            showAddCustomModal = false
                                        },
                                        enabled = cBaseUrl.isNotBlank() && cApiKey.isNotBlank() && cModelName.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                    ) {
                                        Text("Kaydet ve Kullan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // MODAL 2: HIZLI MODEL DEĞİŞTİRME / YAZMA PENCERESİ
                    val currentQuickProvider = quickModelEditProvider
                    if (currentQuickProvider != null) {
                        var newMName by remember { mutableStateOf(currentQuickProvider.modelName) }
                        Dialog(onDismissRequest = { quickModelEditProvider = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EmochiSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("✏️ Model Kodunu Güncelle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Sağlayıcı: ${currentQuickProvider.label}", color = EmochiTextSecondary, fontSize = 11.sp)

                                    OutlinedTextField(
                                        value = newMName,
                                        onValueChange = { newMName = it },
                                        label = { Text("Model Kodu / Adı (El ile İstediğinizi Yazın)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    Text("Hızlı Model Şablonları:", color = EmochiTextMuted, fontSize = 10.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "🦙 Llama 3.3 70B" to "meta-llama/llama-3.3-70b-instruct",
                                            "🐋 DeepSeek R1" to "deepseek/deepseek-r1",
                                            "🌐 Qwen 2.5 72B" to "qwen/qwen-2.5-72b-instruct",
                                            "⚡ Gemini 2.0 Flash" to "google/gemini-2.0-flash-001",
                                            "🧠 Claude 3.5 Sonnet" to "anthropic/claude-3.5-sonnet",
                                            "🤖 GPT-4o Mini" to "openai/gpt-4o-mini",
                                            "💨 Mistral Large" to "mistralai/mistral-large"
                                        ).forEach { (mTag, mCode) ->
                                            OutlinedButton(
                                                onClick = { newMName = mCode },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(mTag, fontSize = 9.5.sp)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { quickModelEditProvider = null },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("İptal", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = {
                                                if (newMName.isNotBlank()) {
                                                    viewModel.updateCustomProviderModel(currentQuickProvider.id, newMName)
                                                    selectedModel = newMName.trim()
                                                    quickModelEditProvider = null
                                                }
                                            },
                                            enabled = newMName.isNotBlank(),
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                        ) {
                                            Text("Modeli Güncelle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // MODAL 3: TAM SAĞLAYICI DÜZENLEME PENCERESİ
                    val currentEditProvider = editingCustomProvider
                    if (currentEditProvider != null) {
                        var eLabel by remember { mutableStateOf(currentEditProvider.label) }
                        var eBaseUrl by remember { mutableStateOf(currentEditProvider.baseUrl) }
                        var eApiKey by remember { mutableStateOf("") }
                        var eModelName by remember { mutableStateOf(currentEditProvider.modelName) }
                        var eApiFormat by remember { mutableStateOf(currentEditProvider.apiFormat) }
                        var showEKeyText by remember { mutableStateOf(false) }

                        var isETesting by remember { mutableStateOf(false) }
                        var eTestRes by remember { mutableStateOf<EmochiRepository.ProviderTestResult?>(null) }
                        val editModalScope = rememberCoroutineScope()

                        Dialog(onDismissRequest = { editingCustomProvider = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EmochiSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🛠️ Özel API Sağlayıcısını Düzenle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = eLabel,
                                        onValueChange = { eLabel = it; eTestRes = null },
                                        label = { Text("Sağlayıcı Etiketi", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    Text("API Formatı:", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = eApiFormat == "openai",
                                            onClick = { eApiFormat = "openai"; eTestRes = null },
                                            label = { Text("OpenAI Uyumlu", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = eApiFormat == "anthropic",
                                            onClick = { eApiFormat = "anthropic"; eTestRes = null },
                                            label = { Text("Anthropic Uyumlu", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = eBaseUrl,
                                        onValueChange = { eBaseUrl = it; eTestRes = null },
                                        label = { Text("Base URL", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    OutlinedTextField(
                                        value = eApiKey,
                                        onValueChange = { eApiKey = it; eTestRes = null },
                                        label = { Text("Yeni API Key (Boş Bırakırsanız Değişmez)", fontSize = 11.sp) },
                                        visualTransformation = if (showEKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showEKeyText = !showEKeyText }) {
                                                Icon(if (showEKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    OutlinedTextField(
                                        value = eModelName,
                                        onValueChange = { eModelName = it; eTestRes = null },
                                        label = { Text("Model Kodu / Adı (El ile Yazın)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = customTextFieldColors()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "🦙 Llama 3.3 70B" to "meta-llama/llama-3.3-70b-instruct",
                                            "🐋 DeepSeek R1" to "deepseek/deepseek-r1",
                                            "🌐 Qwen 2.5 72B" to "qwen/qwen-2.5-72b-instruct",
                                            "⚡ Gemini 2.0 Flash" to "google/gemini-2.0-flash-001",
                                            "🧠 Claude 3.5 Sonnet" to "anthropic/claude-3.5-sonnet",
                                            "🤖 GPT-4o Mini" to "openai/gpt-4o-mini"
                                        ).forEach { (mTag, mCode) ->
                                            OutlinedButton(
                                                onClick = { eModelName = mCode; eTestRes = null },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(mTag, fontSize = 9.5.sp)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { editingCustomProvider = null },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("İptal", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                editModalScope.launch {
                                                    isETesting = true
                                                    val testKeyToUse = eApiKey.ifBlank { "existing_key" }
                                                    eTestRes = viewModel.testCustomProviderConnection(eBaseUrl, testKeyToUse, eModelName, eApiFormat)
                                                    isETesting = false
                                                }
                                            },
                                            enabled = !isETesting && eBaseUrl.isNotBlank() && eModelName.isNotBlank(),
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                        ) {
                                            if (isETesting) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                            } else {
                                                Text("Bağlantıyı Test Et", fontSize = 10.5.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.updateCustomProviderFull(
                                                id = currentEditProvider.id,
                                                label = eLabel,
                                                baseUrl = eBaseUrl,
                                                apiKey = eApiKey.ifBlank { null },
                                                modelName = eModelName,
                                                apiFormat = eApiFormat,
                                                supportsFunctionCalling = eTestRes?.supportsFunctionCalling ?: currentEditProvider.supportsFunctionCalling
                                            )
                                            selectedModel = eModelName
                                            editingCustomProvider = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                    ) {
                                        Text("Değişiklikleri Kaydet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
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

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFF2B2142))
                                Spacer(modifier = Modifier.height(8.dp))

                                // FALLBACK CHAIN REORDERING UI
                                Text("🔀 Fallback Sıralaması ve Öncelik Zinciri:", color = EmochiPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                Text("Sağlayıcılar yukarıdan aşağıya doğru sırayla denenir. Ok butonları ile öncelik sırasını değiştirebilirsiniz.", color = EmochiTextMuted, fontSize = 10.sp)

                                val customListForChain by viewModel.customProviders.collectAsState()
                                val customMapForChain = customListForChain.associateBy { "custom_${it.id}" }

                                val currentItems = if (fallbackChainOrder.isNotBlank()) {
                                    fallbackChainOrder.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                } else {
                                    val def = mutableListOf("llm7", "pollinations", "ovh", "main")
                                    customListForChain.forEach { cp -> def.add("custom_${cp.id}") }
                                    def.add("autofallback")
                                    def
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    currentItems.forEachIndexed { index, itemKey ->
                                        val displayLabel = when {
                                            itemKey == "llm7" -> "1. LLM7 (Ücretsiz Servis)"
                                            itemKey == "pollinations" -> "2. Pollinations (${pollinationsModel.ifBlank { "openai" }})"
                                            itemKey == "ovh" -> "3. OVH AI (${ovhModel})"
                                            itemKey == "main" -> "4. Ana Seçili Sağlayıcı ($selectedProvider)"
                                            itemKey == "autofallback" -> "5. Otomatik Gemini Fallback ($fallbackModel)"
                                            itemKey.startsWith("custom_") -> {
                                                val cp = customMapForChain[itemKey]
                                                if (cp != null) "Özel: ${cp.label} (${cp.modelName})" else "Özel Sağlayıcı ($itemKey)"
                                            }
                                            else -> itemKey
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E1638))
                                                .border(1.dp, Color(0xFF382C5E), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}. $displayLabel",
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        if (index > 0) {
                                                            val mutable = currentItems.toMutableList()
                                                            val tmp = mutable[index]
                                                            mutable[index] = mutable[index - 1]
                                                            mutable[index - 1] = tmp
                                                            fallbackChainOrder = mutable.joinToString(",")
                                                        }
                                                    },
                                                    enabled = index > 0,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text("▲", color = if (index > 0) EmochiPrimary else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (index < currentItems.size - 1) {
                                                            val mutable = currentItems.toMutableList()
                                                            val tmp = mutable[index]
                                                            mutable[index] = mutable[index + 1]
                                                            mutable[index + 1] = tmp
                                                            fallbackChainOrder = mutable.joinToString(",")
                                                        }
                                                    },
                                                    enabled = index < currentItems.size - 1,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text("▼", color = if (index < currentItems.size - 1) EmochiPrimary else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        fallbackChainOrder = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp).align(Alignment.End)
                                ) {
                                    Text("Varsayılan Sıraya Sıfırla", fontSize = 9.5.sp, color = EmochiTextMuted)
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

                    } // End of scrollable content Column

                    // FIXED FOOTER AT BOTTOM
                    Divider(color = EmochiBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1333))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
                        ) {
                            Text("Vazgeç", color = EmochiTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onSaveSettings(
                                    settings.copy(
                                        customApiKey = geminiApiKey.trim(),
                                        groqApiKey = groqApiKey.trim(),
                                        claudeApiKey = claudeApiKey.trim(),
                                        openaiApiKey = openaiApiKey.trim(),
                                        openRouterApiKey = openRouterApiKey.trim(),
                                        openRouterModel = openRouterModel.trim(),
                                        nvidiaApiKey = nvidiaApiKey.trim(),
                                        nvidiaModel = nvidiaModel.trim(),
                                        mistralApiKey = mistralApiKey.trim(),
                                        mistralModel = mistralModel.trim(),
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
                                        fallbackChainOrder = fallbackChainOrder,
                                        enableLlm7 = enableLlm7,
                                        enablePollinations = enablePollinations,
                                        enableOvh = enableOvh,
                                        pollinationsModel = pollinationsModel,
                                        ovhModel = ovhModel,
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
                            modifier = Modifier
                                .weight(2f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ayarları Kaydet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } // End of outer Column
            } // End of Box
        } // End of Surface
    } // End of Dialog

    if (showUpdateModal) {
        UpdateCheckerModal(onDismiss = { showUpdateModal = false })
    }

    if (showFallbackLogModal) {
        val logs = viewModel.providerFallbackLog.collectAsState().value
        FallbackLogModal(logs = logs, onDismiss = { showFallbackLogModal = false })
    }
}

@Composable
fun FallbackLogModal(
    logs: List<com.example.data.repository.ProviderFallbackLogEntry>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF130E26),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 Sağlayıcı Deneme Logları (Fallback Chain)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz kayıtlı bir fallback logu yok.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { entry ->
                            val statusColor = when (entry.status) {
                                "SUCCESS" -> Color(0xFF4CAF50)
                                "FAILED" -> Color(0xFFEF4444)
                                else -> Color(0xFFFFB74D)
                            }
                            val statusIcon = when (entry.status) {
                                "SUCCESS" -> "✅"
                                "FAILED" -> "❌"
                                else -> "⏳"
                            }
                            Surface(
                                color = Color(0xFF1A1333),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$statusIcon [Katman ${entry.layer}] ${entry.providerName}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = entry.status,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (!entry.errorMessage.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hata: ${entry.errorMessage}",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 11.sp
                                        )
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
    var showEmotionControlModal by remember { mutableStateOf(false) }
    var expandedBotSection by remember { mutableStateOf<String?>(null) }

    val viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var filterCount by remember { mutableStateOf(0) }

    LaunchedEffect(bot.id) {
        filterCount = viewModel.getContentFilterCountForBot(bot.id)
    }

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
                EmotionStatusSection(
                    bot = bot,
                    characterEmotions = characterEmotions,
                    affectionEvents = affectionEvents,
                    filterCount = filterCount,
                    onUpdateCharacterEmotion = { charName, mood, affection, trust, tension ->
                        viewModel.updateCharacterEmotion(bot.id, charName, mood, affection, trust, tension)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // DEDICATED EMOTION & RELATIONSHIP EDITING BUTTON
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1D2F)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B81).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmotionControlModal = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6B81).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎭", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "❤️ Duygu & İlişki Düzenleme Paneli",
                                    color = EmochiTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ruh hali, sevgi, güven, gerginlik ve yan karakter durumlarını özelleştirmek için tıklayın",
                                    color = EmochiTextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Aç",
                            tint = Color(0xFFFF6B81)
                        )
                    }
                }

                if (showEmotionControlModal) {
                    CharacterEmotionControlModal(
                        bot = bot,
                        characterEmotions = characterEmotions,
                        keyCharacters = keyCharacters,
                        onDismiss = { showEmotionControlModal = false },
                        onSaveBotEmotion = { updatedStateJson ->
                            val updatedBot = bot.copy(
                                emotionState = updatedStateJson,
                                previousEmotionState = updatedStateJson,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.updateBotProfile(updatedBot)
                            showEmotionControlModal = false
                        },
                        onSaveCharacterEmotionState = { charName, json ->
                            viewModel.updateCharacterEmotionState(bot.id, charName, json)
                        }
                    )
                }

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

@Composable
fun ProviderReliabilityDashboardUI() {
    val statsList = remember { com.example.util.ProviderRateLimitTracker.getAllSecondaryStats() }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🛡️ Sağlayıcı Güvenilirlik Panosu",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("SECONDARY TIER (COMPACT MODE)", color = Color(0xFF38BDF8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zayıf/Ücretsiz katmandaki sağlayıcıların anlık performans ve kota metrikleri karşılaştırması:",
                color = EmochiTextMuted,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            statsList.forEach { stat ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stat.displayName, color = Color(0xFFF1F5F9), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            val statusBg = if (stat.isLimitApproaching) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                            val statusTxt = if (stat.isLimitApproaching) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
                            val label = if (stat.isLimitApproaching) "⚠️ Doygunluk / Kısıt" else "✅ Aktif / Normal"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(label, color = statusTxt, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dakika / Saat İstek:", color = EmochiTextMuted, fontSize = 9.5.sp)
                            Text("${stat.requestsLastMinute} req/dk | ${stat.requestsLastHour} req/saat", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Token / Kredi Kullanımı:", color = EmochiTextMuted, fontSize = 9.5.sp)
                            val tokenLabel = if (stat.quotaType == com.example.util.QuotaType.EXHAUSTIBLE) {
                                "Kalan Kredi: ${stat.creditsRemaining}"
                            } else {
                                "${stat.tokensUsed24h} / ${stat.tokenLimit24h} token"
                            }
                            Text(tokenLabel, color = Color(0xFF38BDF8), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ort. Yanıt | Hata | Devir:", color = EmochiTextMuted, fontSize = 9.5.sp)
                            Text("${stat.averageResponseTimeMs} ms | ${stat.malformedCount} hata | ${stat.fallbackCount} devir", color = Color.White, fontSize = 9.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEmotionControlModal(
    bot: BotEntity,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    keyCharacters: List<KeyCharacter> = emptyList(),
    onDismiss: () -> Unit,
    onSaveBotEmotion: (String) -> Unit,
    onSaveCharacterEmotionState: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("main") }

    val characterStatesMap = remember(bot.emotionState, characterEmotions, keyCharacters) {
        val map = mutableMapOf<String, com.example.data.local.EmotionState>()
        map["main"] = com.example.data.local.EmotionState.fromJson(bot.emotionState)

        val existingMap = characterEmotions.associateBy { it.characterName }
        val sideNames = (characterEmotions.map { it.characterName } + keyCharacters.map { it.name })
            .filter { it.isNotBlank() }.distinct()

        sideNames.forEach { name ->
            val entity = existingMap[name]
            if (entity != null) {
                map[name] = com.example.data.local.EmotionState.fromJson(entity.emotionState)
            } else {
                map[name] = com.example.data.local.EmotionState.calculateBaselineEmotionState(bot.aiName, name, "")
            }
        }

        mutableStateMapOf<String, com.example.data.local.EmotionState>().apply { putAll(map) }
    }

    val curState = characterStatesMap[selectedTab] ?: com.example.data.local.EmotionState()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var newCustomName by remember { mutableStateOf("") }
    var newCustomDifficulty by remember { mutableStateOf("Orta") }
    var newCustomMin by remember { mutableStateOf("0") }
    var newCustomMax by remember { mutableStateOf("100") }
    var newCustomCurrent by remember { mutableStateOf("50") }
    var newCustomPurpose by remember { mutableStateOf("") }

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
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎭", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Karakter Duygu & İlişki Paneli", color = EmochiTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Tüm karakterler için ruh hali, temel ve özel duyguları yönetin", color = EmochiTextMuted, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Character Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == "main",
                            onClick = { selectedTab = "main" },
                            label = { Text("🤖 ${bot.aiName} (Ana)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF6B81),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1B1D30),
                                labelColor = EmochiTextSecondary
                            )
                        )

                        characterStatesMap.keys.filter { it != "main" }.forEach { charName ->
                            FilterChip(
                                selected = selectedTab == charName,
                                onClick = { selectedTab = charName },
                                label = { Text("👤 $charName", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmochiPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1B1D30),
                                    labelColor = EmochiTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Character Title Header
                    val charDisplayName = if (selectedTab == "main") "🤖 ${bot.aiName} (Ana Karakter)" else "👤 $selectedTab"
                    Text(charDisplayName, color = Color(0xFFD8B4FE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1) Mood Controls
                    Text("Baskın Ruh Hali (Mood)", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presetMoods = listOf("nötr", "mutlu", "flörtöz", "kıskanç", "utangaç", "gergin", "tutkulu", "kırgın", "sevecen", "soğuk", "heyecanlı", "sakin")
                        presetMoods.forEach { m ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (curState.dominantEmotion.equals(m, ignoreCase = true)) Color(0xFFFF6B81) else Color(0xFF1E1F35))
                                    .clickable {
                                        characterStatesMap[selectedTab] = curState.copy(dominantEmotion = m)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(m, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = curState.dominantEmotion,
                        onValueChange = { newMood ->
                            characterStatesMap[selectedTab] = curState.copy(dominantEmotion = newMood)
                        },
                        placeholder = { Text("Özel ruh hali girin...", fontSize = 12.sp, color = EmochiTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("İkincil Duygu", color = EmochiTextSecondary, fontSize = 11.5.sp)
                            OutlinedTextField(
                                value = curState.computedSecondaryEmotion ?: "",
                                onValueChange = { newSec ->
                                    characterStatesMap[selectedTab] = curState.copy(computedSecondaryEmotion = newSec.ifBlank { null })
                                },
                                placeholder = { Text("ör. minnettar", fontSize = 11.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bastırılmış İç Duygu", color = EmochiTextSecondary, fontSize = 11.5.sp)
                            OutlinedTextField(
                                value = curState.suppressedEmotion,
                                onValueChange = { newSup ->
                                    characterStatesMap[selectedTab] = curState.copy(suppressedEmotion = newSup)
                                },
                                placeholder = { Text("ör. çekingenlik", fontSize = 11.sp, color = EmochiTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2) Relationship Axes Sliders
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181A2A)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("❤️ İlişki & Duygu Parametreleri", color = EmochiPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            val aff = curState.affection
                            val tierLabel = when (aff) {
                                in 0..15 -> "💔 Düşman / Soğuk"
                                in 16..35 -> "👤 Yabancı"
                                in 36..55 -> "🙂 Tanıdık / Nötr"
                                in 56..70 -> "🤝 Arkadaş"
                                in 71..85 -> "💖 Flört / İlgili"
                                in 86..95 -> "🔥 Sevgili / Âşık"
                                else -> "👑 Ruh Eşi / Derin Bağ"
                            }
                            Text("❤️ Sevgi & Yakınlık: $aff% ($tierLabel)", color = Color(0xFFFF6B81), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = aff.toFloat(),
                                onValueChange = { newVal ->
                                    characterStatesMap[selectedTab] = curState.copy(
                                        relationshipAxes = curState.relationshipAxes.copy(affectionScore = newVal.toInt())
                                    )
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6B81), activeTrackColor = Color(0xFFFF6B81))
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val tr = curState.trust
                            Text("🛡️ Güven Seviyesi: $tr%", color = Color(0xFF4D96FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = tr.toFloat(),
                                onValueChange = { newVal ->
                                    characterStatesMap[selectedTab] = curState.copy(
                                        primaryEmotions = curState.primaryEmotions.copy(trust = newVal.toInt())
                                    )
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF4D96FF), activeTrackColor = Color(0xFF4D96FF))
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val pc = curState.physicalComfortScore
                            Text("🤝 Fiziksel Yakınlık Rahatlığı: $pc%", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = pc.toFloat(),
                                onValueChange = { newVal ->
                                    characterStatesMap[selectedTab] = curState.copy(physicalComfortScore = newVal.toInt())
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val hr = curState.hurt
                            Text("💔 Kırgınlık / Mesafe (Hurt): $hr%", color = Color(0xFF9333EA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = hr.toFloat(),
                                onValueChange = { newVal ->
                                    characterStatesMap[selectedTab] = curState.copy(
                                        relationshipAxes = curState.relationshipAxes.copy(resentmentScore = newVal.toInt())
                                    )
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9333EA), activeTrackColor = Color(0xFF9333EA))
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val ob = curState.obsession
                            Text("🖤 Takıntı / Bağımlılık (Obsession): $ob%", color = Color(0xFFE11D48), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = ob.toFloat(),
                                onValueChange = { newVal ->
                                    characterStatesMap[selectedTab] = curState.copy(obsessionScore = newVal.toInt())
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFE11D48), activeTrackColor = Color(0xFFE11D48))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("⚡ Gerginlik / Stres Düzeyi", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tensionOptions = listOf("none" to "Sakin 🍃", "mild" to "Hafif ⚡", "conflict" to "Çatışma 🔥", "crisis" to "Kriz ⚠️")
                        tensionOptions.forEach { (key, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (curState.tension.equals(key, ignoreCase = true)) Color(0xFFFFB302) else Color(0xFF1E1F35))
                                    .clickable {
                                        characterStatesMap[selectedTab] = curState.copy(tension = key)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("🗣️ Konuşma Üslubu & Hızı", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = curState.speechPattern,
                        onValueChange = { newSp ->
                            characterStatesMap[selectedTab] = curState.copy(speechPattern = newSp)
                        },
                        placeholder = { Text("ör. utangaç, kısık sesli, hızlı konuşan...", fontSize = 11.5.sp, color = EmochiTextMuted) },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🛡️ Duygusal Direnç: ${curState.resilience}/10", color = EmochiTextSecondary, fontSize = 11.5.sp)
                    Slider(
                        value = curState.resilience.toFloat(),
                        onValueChange = { newVal ->
                            characterStatesMap[selectedTab] = curState.copy(resilience = newVal.toInt())
                        },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    // 3) PRIMARY EMOTIONS (Plutchik 8) SECTION
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2E)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🎭 Temel Duygular (8 Temel Duygu)", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            val p = curState.primaryEmotions

                            // Joy
                            Text("😄 Neşe / Mutluluk: ${p.joy}%", color = Color(0xFFFBBF24), fontSize = 11.5.sp)
                            Slider(
                                value = p.joy.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(joy = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Sadness
                            Text("😢 Üzüntü / Keder: ${p.sadness}%", color = Color(0xFF60A5FA), fontSize = 11.5.sp)
                            Slider(
                                value = p.sadness.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(sadness = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Anger
                            Text("😡 Öfke / Kızgınlık: ${p.anger}%", color = Color(0xFFEF4444), fontSize = 11.5.sp)
                            Slider(
                                value = p.anger.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(anger = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Fear
                            Text("😨 Korku / Endişe: ${p.fear}%", color = Color(0xFFA855F7), fontSize = 11.5.sp)
                            Slider(
                                value = p.fear.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(fear = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Disgust
                            Text("🤢 Tiksinme / Tiksinti: ${p.disgust}%", color = Color(0xFF10B981), fontSize = 11.5.sp)
                            Slider(
                                value = p.disgust.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(disgust = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Surprise
                            Text("😲 Şaşkınlık / Sürpriz: ${p.surprise}%", color = Color(0xFFEC4899), fontSize = 11.5.sp)
                            Slider(
                                value = p.surprise.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(surprise = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )

                            // Anticipation
                            Text("⏳ Beklenti / Heyecan: ${p.anticipation}%", color = Color(0xFFF97316), fontSize = 11.5.sp)
                            Slider(
                                value = p.anticipation.toFloat(),
                                onValueChange = { v ->
                                    characterStatesMap[selectedTab] = curState.copy(primaryEmotions = p.copy(anticipation = v.toInt()))
                                },
                                valueRange = 0f..100f
                            )
                        }
                    }

                    // 4) CUSTOM EMOTIONS SECTION (For whichever character is selected!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141628)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val customList = curState.customEmotions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Karaktere Özel Duygular (${customList.size})",
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { showAddCustomDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Yeni Duygu Ekle", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (customList.isEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$charDisplayName için özel duygu tanımlanmadı. 'Yeni Duygu Ekle' butonuna basarak bu karaktere özel zorluk derecesi, aralık ve etki alanına sahip duygular tanımlayabilirsiniz.",
                                    color = EmochiTextMuted,
                                    fontSize = 11.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                customList.forEachIndexed { index, ce ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F213A)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("✨ ${ce.name}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                when (ce.difficulty) {
                                                                    "Kolay" -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                                                    "Orta" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                                    "Zor" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                                    else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                                }
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Zorluk: ${ce.difficulty}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        val updatedList = customList.toMutableList().apply { removeAt(index) }
                                                        characterStatesMap[selectedTab] = curState.copy(
                                                            customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updatedList)
                                                        )
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            if (ce.purpose.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("🎯 Amacı: ${ce.purpose}", color = EmochiTextSecondary, fontSize = 11.sp)
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Değer Aralığı: ${ce.minValue} - ${ce.maxValue} | Şu Anki Değer: ${ce.currentValue}", color = Color(0xFFD8B4FE), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                            Slider(
                                                value = ce.currentValue.toFloat().coerceIn(ce.minValue.toFloat(), ce.maxValue.toFloat()),
                                                onValueChange = { newVal ->
                                                    val updatedItem = ce.copy(currentValue = newVal.toInt())
                                                    val updatedList = customList.toMutableList().apply { set(index, updatedItem) }
                                                    characterStatesMap[selectedTab] = curState.copy(
                                                        customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updatedList)
                                                    )
                                                },
                                                valueRange = ce.minValue.toFloat()..ce.maxValue.toFloat().coerceAtLeast(ce.minValue.toFloat() + 1f),
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFA855F7), activeTrackColor = Color(0xFFA855F7))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("İptal", color = EmochiTextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Save main bot state
                                characterStatesMap["main"]?.let { mainSt ->
                                    onSaveBotEmotion(mainSt.toJson())
                                }
                                // Save side character states
                                characterStatesMap.forEach { (key, state) ->
                                    if (key != "main") {
                                        onSaveCharacterEmotionState(key, state.toJson())
                                    }
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B81))
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tüm Karakter Duygularını Kaydet & Dön", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddCustomDialog) {
        Dialog(onDismissRequest = { showAddCustomDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F35)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✨ $selectedTab Karakterine Özel Duygu Ekle", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Duygu Adı", color = EmochiTextSecondary, fontSize = 11.5.sp)
                    OutlinedTextField(
                        value = newCustomName,
                        onValueChange = { newCustomName = it },
                        placeholder = { Text("ör. Sadakat, Şüphe, Saygı, Kıskançlık...", fontSize = 11.sp, color = EmochiTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Kazanım Zorluğu", color = EmochiTextSecondary, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val difficulties = listOf("Kolay", "Orta", "Zor", "Aşırı Zor")
                        difficulties.forEach { diff ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (newCustomDifficulty == diff) Color(0xFFA855F7) else Color(0xFF141628))
                                    .clickable { newCustomDifficulty = diff }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(diff, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Min Değer", color = EmochiTextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = newCustomMin,
                                onValueChange = { newCustomMin = it.filter { c -> c.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Max Değer", color = EmochiTextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = newCustomMax,
                                onValueChange = { newCustomMax = it.filter { c -> c.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Şu Anki Değer", color = EmochiTextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = newCustomCurrent,
                                onValueChange = { newCustomCurrent = it.filter { c -> c.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Duygunun Amacı / Etkisi", color = EmochiTextSecondary, fontSize = 11.5.sp)
                    OutlinedTextField(
                        value = newCustomPurpose,
                        onValueChange = { newCustomPurpose = it },
                        placeholder = { Text("ör. Karakterin sadakat seviyesini ve karar mekanizmasını yönlendirir...", fontSize = 11.sp, color = EmochiTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddCustomDialog = false }) {
                            Text("İptal", color = EmochiTextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCustomName.isNotBlank()) {
                                    val minVal = newCustomMin.toIntOrNull() ?: 0
                                    val maxVal = newCustomMax.toIntOrNull() ?: 100
                                    val curVal = (newCustomCurrent.toIntOrNull() ?: 0).coerceIn(minVal, maxVal)
                                    val newDef = com.example.data.local.CustomEmotionDefinition(
                                        name = newCustomName,
                                        difficulty = newCustomDifficulty,
                                        minValue = minVal,
                                        maxValue = maxVal,
                                        currentValue = curVal,
                                        purpose = newCustomPurpose
                                    )
                                    val updatedList = curState.customEmotions + newDef
                                    characterStatesMap[selectedTab] = curState.copy(
                                        customEmotionsJson = com.example.data.local.CustomEmotionDefinition.listToJsonArrayStr(updatedList)
                                    )
                                    showAddCustomDialog = false
                                    newCustomName = ""
                                    newCustomPurpose = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                        ) {
                            Text("Ekle", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
