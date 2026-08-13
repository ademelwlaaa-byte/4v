package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Close
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.ui.components.GlobalSettingsModal
import com.example.ui.components.MoodColors
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

@Composable
fun BotListScreen(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity?,
    onOpenBot: (String) -> Unit,
    onNewBot: () -> Unit,
    onDeleteBot: (String) -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Unit,
    onImportPresetBot: ((BotEntity) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null
) {
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showAidenStoriesMenu by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "personal", "universe"
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("chats") } // "chats", "discover", "templates"

    // System Back button closes Aiden Stories menu or returns to "chats" tab first before exiting
    BackHandler(enabled = showAidenStoriesMenu || activeTab != "chats") {
        if (showAidenStoriesMenu) {
            showAidenStoriesMenu = false
        } else {
            activeTab = "chats"
        }
    }

    Scaffold(
        containerColor = EmochiBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmochiSurface)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(com.example.ui.theme.EmochiGradients.glassBorder)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 1. Keşfet Tab
                    val isDiscoverSelected = activeTab == "discover"
                    val discoverScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isDiscoverSelected) 1.08f else 1.0f,
                        label = "tabScale"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDiscoverSelected) Color(0x1FA78BFA) else Color.Transparent)
                            .clickable { activeTab = "discover" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .graphicsLayer {
                                scaleX = discoverScale
                                scaleY = discoverScale
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Keşfet",
                            tint = if (isDiscoverSelected) EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Keşfet",
                            color = if (isDiscoverSelected) EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = if (isDiscoverSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 2. Sohbetler Tab
                    val isChatsSelected = activeTab == "chats"
                    val chatsScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isChatsSelected) 1.08f else 1.0f,
                        label = "tabScale"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isChatsSelected) Color(0x1FA78BFA) else Color.Transparent)
                            .clickable { activeTab = "chats" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .graphicsLayer {
                                scaleX = chatsScale
                                scaleY = chatsScale
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Sohbetler",
                            tint = if (isChatsSelected) EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Sohbetler",
                            color = if (isChatsSelected) EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = if (isChatsSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 3. Center (+) Rising Floating Gradient Button labeled "Oluştur"
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.EmochiGradients.primaryButton)
                            .clickable { onNewBot() }
                            .testTag("create_new_bot_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Oluştur",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Oluştur",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // 4. Şablonlar (templates) Tab
                    val isTemplatesSelected = activeTab == "templates"
                    val templatesScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isTemplatesSelected) 1.08f else 1.0f,
                        label = "tabScale"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isTemplatesSelected) Color(0x1FA78BFA) else Color.Transparent)
                            .clickable { activeTab = "templates" }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .graphicsLayer {
                                scaleX = templatesScale
                                scaleY = templatesScale
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Şablonlar",
                            tint = if (isTemplatesSelected) EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Şablonlar",
                            color = if (isTemplatesSelected) EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = if (isTemplatesSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 5. Kitaplar (books) Tab
                    val isBooksSelected = activeTab == "books"
                    val booksScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isBooksSelected) 1.08f else 1.0f,
                        label = "tabScale"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isBooksSelected) Color(0x1FA78BFA) else Color.Transparent)
                            .clickable { activeTab = "books" }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .graphicsLayer {
                                scaleX = booksScale
                                scaleY = booksScale
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Kitaplar",
                            tint = if (isBooksSelected) EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Kitaplar",
                            color = if (isBooksSelected) EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = if (isBooksSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
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
                .statusBarsPadding()
        ) {
            // Premium Minimalist Header matching the screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "👋 Hoş geldin, Aiden",
                            color = EmochiTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (activeTab) {
                                "templates" -> "Şablonlar"
                                "discover" -> "Keşfet"
                                "books" -> "Kitaplar"
                                else -> "Botlarım"
                            },
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Purple Chip matching screenshot: "✦ 2 bot • Gemini 2.5 Flash"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B1437))
                                .border(1.dp, Color(0x40A78BFA), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when (activeTab) {
                                        "templates" -> "✦ Şablonlar • Gemini 2.5 Flash"
                                        "discover" -> "✦ Keşfet • Gemini 2.5 Flash"
                                        "books" -> "✦ Kitaplar • Gemini 2.5 Flash"
                                        else -> "✦ ${botList.size} bot • Gemini 2.5 Flash"
                                    },
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showGlobalSettings = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141029))
                            .border(1.dp, Color(0x20A78BFA), CircleShape)
                            .testTag("global_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "templates" -> {
                        ExploreTabContent(
                            userSettings = userSettings,
                            onImportPresetBot = onImportPresetBot,
                            onOpenAidenMenu = { showAidenStoriesMenu = true }
                        )
                    }
                    "discover" -> {
                        val publicBots = remember(botList) {
                            botList.filter { it.isPublic }
                        }
                        DiscoverTabContent(
                            botList = publicBots,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onOpenBot = onOpenBot
                        )
                    }
                    "books" -> {
                        BooksTabContent(userSettings = userSettings)
                    }
                    else -> {
                        // "chats" tab -> Shows user's active bots
                        ChatsTabContent(
                            botList = botList,
                            userSettings = userSettings,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it },
                            confirmDeleteId = confirmDeleteId,
                            onConfirmDeleteChange = { confirmDeleteId = it },
                            onOpenBot = onOpenBot,
                            onDeleteBot = onDeleteBot,
                            onTogglePrivacy = onTogglePrivacy,
                            onOpenAidenMenu = { showAidenStoriesMenu = true }
                        )
                    }
                }
            }
        }
    }

    if (showGlobalSettings && userSettings != null) {
        GlobalSettingsModal(
            settings = userSettings,
            onDismiss = { showGlobalSettings = false },
            onSaveSettings = onSaveSettings,
            onExportData = onExportData,
            onImportData = onImportData
        )
    }

    if (showAidenStoriesMenu) {
        AidenStoriesModal(
            userSettings = userSettings,
            onDismiss = { showAidenStoriesMenu = false },
            onImportPresetBot = onImportPresetBot
        )
    }
}

@Composable
fun ChatsTabContent(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity? = null,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: (String) -> Unit,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onOpenAidenMenu: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // Search Input with glowing border & filter slider button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x900E061E))
                    .border(
                        BorderStroke(
                            1.2.dp,
                            Brush.linearGradient(listOf(Color(0x60C084FC), Color(0x203B0764)))
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Bot veya evren ara...",
                            color = Color(0xFF7E739B),
                            fontSize = 14.sp
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bot_field")
                    )
                }
                // Filter sliders icon box on the right
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x601A1033))
                        .border(1.dp, Color(0x40A855F7), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Filtrele",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    Triple("all", "Tümü (${botList.size})", Icons.Default.GridView),
                    Triple("personal", "Karakterler (${botList.count { it.mode == "personal" }})", Icons.Default.Person),
                    Triple("universe", "Evrenler (${botList.count { it.mode == "universe" }})", Icons.Default.Public)
                )

                filters.forEach { (key, label, icon) ->
                    val isSelected = selectedFilter == key
                    val shape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, Color(0x30A855F7), shape)
                                } else Modifier
                            )
                            .clip(shape)
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38147A), Color(0xFF1B0842))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0x80140A28), Color(0x800E061E))
                                    )
                                }
                            )
                            .border(
                                BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    brush = if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color(0x306B21A8), Color(0x203B0764))
                                        )
                                    }
                                ),
                                shape = shape
                            )
                            .clickable { onFilterChange(key) }
                            .padding(horizontal = 6.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFFA78BFA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFA78BFA),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredBots = remember(botList, searchQuery, selectedFilter) {
            botList.filter { bot ->
                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isEmpty() ||
                        bot.aiName.lowercase().contains(query) ||
                        bot.universeName.lowercase().contains(query) ||
                        bot.scenario.lowercase().contains(query)

                val matchesFilter = when (selectedFilter) {
                    "personal" -> bot.mode == "personal"
                    "universe" -> bot.mode == "universe"
                    else -> true
                }
                matchesFilter && matchesQuery
            }
        }

        if (botList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OrbView(hue = 275f, size = 56.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz bir sohbet veya bot yok.",
                        color = EmochiTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Aşağıdaki (+) butonuna dokunarak yeni bir karakter veya evren yazın.",
                        color = EmochiTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (filteredBots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Aramanıza uygun sohbet bulunamadı.",
                        color = EmochiTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredBots, key = { it.id }) { bot ->
                    BotCardItem(
                        bot = bot,
                        confirmDeleteId = confirmDeleteId,
                        onConfirmDeleteChange = onConfirmDeleteChange,
                        onOpenBot = onOpenBot,
                        onDeleteBot = onDeleteBot,
                        onTogglePrivacy = onTogglePrivacy
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverTabContent(
    botList: List<BotEntity>, // PUBLIC BOTS ONLY
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenBot: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🌐 Velora Keşfet • Topluluk Botları",
                color = EmochiTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Diğer kullanıcıların ve yaratıcıların herkese açık paylaştığı botlar ve evrenler.",
                color = EmochiTextSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Keşfette herkese açık bot veya evren ara...",
                        color = EmochiTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = EmochiTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredPublicBots = remember(botList, searchQuery) {
            botList.filter { bot ->
                val query = searchQuery.trim().lowercase()
                query.isEmpty() ||
                        bot.aiName.lowercase().contains(query) ||
                        bot.universeName.lowercase().contains(query) ||
                        bot.scenario.lowercase().contains(query)
            }
        }

        if (filteredPublicBots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    OrbView(hue = 190f, size = 52.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz Keşfette herkese açık bot paylaşılmadı.",
                        color = EmochiTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ayla ve Aetheria gibi varsayılan şablonlar size özeldir ve burada yayınlanmaz. Kendi oluşturduğunuz botları 'Açık' yaparak Keşfette paylaşabilirsiniz!",
                        color = EmochiTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPublicBots, key = { it.id }) { bot ->
                    BotCardItem(
                        bot = bot,
                        confirmDeleteId = null,
                        onConfirmDeleteChange = {},
                        onOpenBot = onOpenBot,
                        onDeleteBot = null,
                        onTogglePrivacy = null
                    )
                }
            }
        }
    }
}

@Composable
fun BotCardItem(
    bot: BotEntity,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: ((String) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null
) {
    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
    val hue = MoodColors.getMoodHue(if (isUniverse) "curious" else "calm")
    val isTemplate = bot.isTemplate || bot.id.startsWith("starter_") || bot.id.startsWith("preset_")

    var isMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // --- Rich Card Animations ---
    val infiniteTransition = rememberInfiniteTransition(label = "cardAnimations_${bot.id}")
    
    val cardGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardGlowAlpha"
    )

    val buttonPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonPulseScale"
    )

    val cardShape = RoundedCornerShape(22.dp)

    // Glassmorphic Card Container with multi-layered breathing neon glow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.5.dp, Color(0xA08B5CF6).copy(alpha = cardGlowAlpha * 0.5f), cardShape)
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x8523104D), // Semi-transparent dark violet top
                        Color(0x950E0620)  // Deep dark purple bottom
                    )
                )
            )
            .border(
                BorderStroke(
                    width = 1.3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE9D5FF), // Crisp bright lila top-left
                            Color(0xFFC084FC), // Glowing neon violet
                            Color(0xFF8B5CF6), // Purple
                            Color(0xFF5B21B6)  // Deep violet bottom-right
                        )
                    )
                ),
                shape = cardShape
            )
            .clickable { onOpenBot(bot.id) }
            .testTag("bot_card_${bot.id}")
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Large Avatar with a glowing neon gradient border
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    val avatarShape = if (isUniverse) RoundedCornerShape(20.dp) else CircleShape

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(avatarShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFE9D5FF),
                                        Color(0xFFC084FC),
                                        Color(0xFF7C3AED),
                                        Color(0xFFE9D5FF)
                                    )
                                ),
                                shape = avatarShape
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            bot.id == "starter_ayla" || bot.aiName.equals("Ayla", ignoreCase = true) -> {
                                Image(
                                    painter = painterResource(id = R.drawable.img_ayla_avatar),
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(avatarShape)
                                )
                            }
                            bot.id == "starter_aetheria" || bot.universeName.contains("Aetheria", ignoreCase = true) || bot.aiName.contains("Aetheria", ignoreCase = true) -> {
                                Image(
                                    painter = painterResource(id = R.drawable.img_aetheria_universe),
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(avatarShape)
                                )
                            }
                            bot.avatarUrl.isNotBlank() -> {
                                AsyncImage(
                                    model = bot.avatarUrl,
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(avatarShape)
                                )
                            }
                            else -> {
                                OrbView(hue = hue, size = 64.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title, Tags, Description Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title and its Badge (Character/Universe)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (!isUniverse) {
                                Spacer(modifier = Modifier.width(8.dp))

                                // Character badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x351F0E3D))
                                        .border(1.dp, Color(0x808B5CF6), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Karakter",
                                        color = Color(0xFFC084FC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Right side sparkle and options button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x308B5CF6))
                                    .border(1.dp, Color(0x608B5CF6), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box {
                                IconButton(
                                    onClick = { isMenuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Daha Fazla",
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF140E2D))
                                ) {
                                    if (onTogglePrivacy != null && !isTemplate) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (bot.isPublic) "🔒 Özel Yap" else "🌐 Herkese Açık Yap",
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )
                                            },
                                            onClick = {
                                                isMenuExpanded = false
                                                onTogglePrivacy.invoke(bot.copy(isPublic = !bot.isPublic))
                                            }
                                        )
                                    }
                                    if (onDeleteBot != null) {
                                        DropdownMenuItem(
                                            text = { Text("Sil", color = EmochiError, fontSize = 13.sp) },
                                            onClick = {
                                                isMenuExpanded = false
                                                showDeleteConfirmDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isUniverse) {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Universe badge on its own row matching screenshot
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x351F0E3D))
                                .border(1.dp, Color(0x808B5CF6), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Evren",
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bot.scenario.ifBlank { bot.openingMessage },
                        color = Color(0xFFE2D9F3),
                        fontSize = 13.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom info & Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active time status on bottom left
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val stableActiveTime = remember(bot.id) {
                                val raw = (bot.id.hashCode() % 24).let { if (it < 0) -it else it } + 1
                                if (raw < 5) "$raw saat önce aktif"
                                else if (raw < 12) "bugün aktif"
                                else "${raw / 12} gün önce aktif"
                            }
                            Text("🕒", fontSize = 11.sp)
                            Text(
                                text = stableActiveTime,
                                color = Color(0xFFA78BFA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                    // Play/Action Button on bottom right with multi-layered neon glow & pulse animation
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = buttonPulseScale
                                scaleY = buttonPulseScale
                            }
                            // Outer glowing halo
                            .border(
                                width = 3.dp,
                                color = Color(0x70C084FC).copy(alpha = cardGlowAlpha * 0.8f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                BorderStroke(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF9333EA))
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6B21A8), Color(0xFF3B0764))
                                )
                            )
                            .clickable { onOpenBot(bot.id) }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUniverse) "Aç" else "Sohbet Et",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    }
                }
            }
        }
    }

    // Beautiful Custom Alert Dialog for delete confirmation
    if (showDeleteConfirmDialog) {
        Dialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0D0A1C))
                    .border(1.dp, Color(0xFF221A4C), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sohbeti Sil",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bu karakter/evren kaydını silmek istediğinizden emin misiniz? Tüm konuşma geçmişiniz silinecektir.",
                        color = EmochiTextSecondary,
                        fontSize = 13.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showDeleteConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140E2D)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Vazgeç", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDeleteBot?.invoke(bot.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmochiError),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sil", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun getAppPresets(isEnglish: Boolean): List<BotEntity> {
    return listOf(
        BotEntity(
            id = "preset_aria",
            mode = "personal",
            aiName = if (isEnglish) "Aria (Cyber Sec Specialist)" else "Aria (Siber Güvenlik Uzmanı)",
            aiPersonality = if (isEnglish) "Intelligent, direct-spoken, mysterious, witty, and deeply loyal." else "Zeki, doğrudan konuşan, gizemli, alaycı ama içten içe sadık.",
            scenario = if (isEnglish) "An independent cybersecurity hacker and investigator working in Neo-Siberia." else "Neo-Siberia şehrinde çalışan bağımsız bir siber güvenlik korsanı ve araştırmacı.",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Detective" else "Dedektif",
            userCharDesc = if (isEnglish) "Experienced digital forensics expert from Merge City." else "Merge Şehrinden gelen tecrübeli adli bilişim uzmanı.",
            openingMessage = if (isEnglish) "*Aria looks up from her terminal screen and narrows her eyes at you.* Finally you arrived. Clearing the digital traces you left on the servers took hours..." else "*Aria terminal ekranından başını kaldırır ve gözlerini kısarak sana bakar.* Nihayet geldin. Sunucularda bıraktığın dijital izleri temizlemem saatlerimi aldı...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "MISSION ::: CYBER ::: Investigating data breach in Merge City." else "GÖREV ::: SİBER ::: Merge şehrindeki veri sızıntısını araştırıyoruz."
        ),
        BotEntity(
            id = "preset_eldoria",
            mode = "universe",
            aiName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
            aiPersonality = if (isEnglish) "Eldoria Realm Narrator." else "Eldoria anlatıcısı.",
            scenario = if (isEnglish) "A feudal fantasy world on the brink of war. Dragons, kingdoms, and rogue guilds." else "Savaşın eşiğindeki feodal bir fantezi dünyası. Ejderhalar, krallıklar ve loncalar.",
            universeName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Warrior" else "Savaşçı",
            userCharDesc = if (isEnglish) "A wandering guild mercenary." else "Gezgin bir lonca paralı askeri.",
            openingMessage = if (isEnglish) "Night settles over Mist Valley at the eastern border of Eldoria. As the tavern door creaks open, a cold wind sweeps in..." else "Eldoria Krallığı'nın doğu sınırındaki Sisli Vadi'de gece çöküyor. Hanın kapısı gıcırdayarak açıldığında içeri soğuk bir rüzgar giriyor...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "UNIVERSE ::: ELDORIA ::: Tensions rise between royal guards and rebel guilds." else "EVREN ::: ELDORİA ::: Kraliyet muhafızları ve asi loncaları arasında gerilim tırmanıyor."
        ),
        BotEntity(
            id = "preset_tokyo",
            mode = "universe",
            aiName = if (isEnglish) "Tokyo - Neon Future" else "Tokyo - Neon Gelecek",
            aiPersonality = if (isEnglish) "Tokyo Neon Narrator." else "Tokyo anlatıcısı.",
            scenario = if (isEnglish) "A futuristic sci-fi roleplay experience in 2077 Tokyo, focusing on the thin line between technology and humanity." else "2077 Tokyo'sunda teknoloji ve insanlık arasındaki ince çizgiye odaklanan fütüristik bir bilim kurgu rol yapma deneyimi.",
            universeName = if (isEnglish) "Tokyo - Neon Future" else "Tokyo - Neon Gelecek",
            keyCharactersJson = "[]",
            userCharName = if (isEnglish) "Runner" else "Runner",
            userCharDesc = if (isEnglish) "A mercenary from the cyber underworld." else "Siber suç dünyasından gelen bir paralı asker.",
            openingMessage = if (isEnglish) "As you glide through the neon-drenched streets of Shibuya under the acid rain..." else "Şibuya'nın asit yağmuru altındaki neon caddelerinde süzülürken siber-implantlarındaki sinyal bir anda kesilir...",
            writingStyle = "rp",
            intensity = "normal",
            isPublic = false,
            isTemplate = true,
            pinnedMemory = if (isEnglish) "UNIVERSE ::: TOKYO ::: 2077 futuristic technology city." else "EVREN ::: TOKYO ::: 2077 fütüristik teknoloji şehri."
        )
    )
}

@Composable
fun ExploreTabContent(
    userSettings: UserSettingsEntity? = null,
    onImportPresetBot: ((BotEntity) -> Unit)?,
    onOpenAidenMenu: (() -> Unit)? = null
) {
    val isEnglish = userSettings?.appLanguage == "en"
    var selectedCategory by remember { mutableStateOf("all") } // "all", "universe", "personal", "new"

    val presets = remember(isEnglish) { getAppPresets(isEnglish) }

    val filteredPresets = remember(selectedCategory, presets) {
        when (selectedCategory) {
            "universe" -> presets.filter { it.mode == "universe" }
            "personal" -> presets.filter { it.mode == "personal" }
            "new" -> presets.filter { it.id == "preset_tokyo" }
            else -> presets
        }
    }

    // Rich glow and pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "exploreTabAnimations")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exploreGlowAlpha"
    )

    val buttonPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exploreButtonPulse"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)) {
                // Title and Subtitle matching the screenshot
                Text(
                    text = if (isEnglish) "🔥 Ready Templates & Special Stories" else "🔥 Hazır Şablonlar & Özel Hikayeler",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isEnglish) "Templates are private to you. Tap any to start chatting instantly." else "Şablonlar tamamen size özeldir. Birini seçip tek tıkla sohbetinizi başlatın.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Pills matching screenshot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Triple("all", if (isEnglish) "All" else "Tümü", "⭐"),
                        Triple("universe", if (isEnglish) "Universes" else "Evrenler", "🪐"),
                        Triple("personal", if (isEnglish) "Characters" else "Karakterler", "👤")
                    )
                    categories.forEach { (key, label, emoji) ->
                        val isSelected = selectedCategory == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF3B1568), Color(0xFF220A45))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0D0821), Color(0xFF090518))
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFC084FC).copy(alpha = glowAlpha) else Color(0xFF1E143B),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable { selectedCategory = key }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = emoji, fontSize = 13.sp)
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dedicated Aiden Hub Banner matching screenshot
                val heroShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFE9D5FF),
                                    Color(0xFFC084FC),
                                    Color(0xFF6B21A8),
                                    Color(0xFFE9D5FF)
                                )
                            ),
                            shape = heroShape
                        )
                        .clip(heroShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x951D103D), Color(0x950C061E))
                            )
                        )
                        .clickable { onOpenAidenMenu?.invoke() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left-side Image of Aiden
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x40C084FC), RoundedCornerShape(16.dp))
                        ) {
                            coil.compose.AsyncImage(
                                model = com.example.R.drawable.aiden_zoktay,
                                contentDescription = "Aiden Blackwood Stories",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Aiden Blackwood",
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Verified Badge
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C1A4A))
                                    .border(1.dp, Color(0x60A78BFA), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "5 Exclusive Stories" else "5 Özel Hikaye",
                                    color = Color(0xFFC084FC),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isEnglish) "Tap to view all 5 Aiden Blackwood scenarios" else "Tüm 5 Aiden Blackwood senaryosunu görmek için dokun",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right-aligned glowing circle button with arrow
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .graphicsLayer {
                                    scaleX = buttonPulseScale
                                    scaleY = buttonPulseScale
                                }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF3B0764), Color(0xFF140D2D))
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED), Color(0xFFE9D5FF))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Git",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        items(filteredPresets, key = { it.id }) { preset ->
            val isUniverse = preset.mode == "universe"
            val titleText = if (isUniverse) preset.universeName else preset.aiName

            // Indicator colors and badges matching screenshot
            val (indicatorEmoji, cardImage, badgeText) = when (preset.id) {
                "preset_aria" -> Triple(
                    "🟡",
                    com.example.R.drawable.aiden_obsidian,
                    if (isEnglish) "👤 Character (Detective)" else "👤 Karakter (Dedektif)"
                )
                "preset_eldoria" -> Triple(
                    "🔴",
                    com.example.R.drawable.aiden_joker,
                    if (isEnglish) "🌐 Universe Template" else "🌐 Evren Şablonu"
                )
                "preset_tokyo" -> Triple(
                    "🔵",
                    com.example.R.drawable.aiden_dispatch,
                    if (isEnglish) "🌐 Universe Template" else "🌐 Evren Şablonu"
                )
                else -> Triple(
                    "🟣",
                    com.example.R.drawable.aiden_zoktay,
                    if (isEnglish) "👤 Template" else "👤 Şablon"
                )
            }

            val cardShape = RoundedCornerShape(22.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        color = Color(0x60A78BFA).copy(alpha = glowAlpha * 0.7f),
                        shape = cardShape
                    )
                    .clip(cardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x851B0F3A), Color(0x950A051A))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Square Image with rounded corners
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.2.dp, Color(0x50C084FC), RoundedCornerShape(16.dp))
                    ) {
                        coil.compose.AsyncImage(
                            model = cardImage,
                            contentDescription = titleText,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Title row with sphere emoji
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = indicatorEmoji, fontSize = 12.sp)
                            Text(
                                text = titleText,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B0E38))
                                .border(1.dp, Color(0x40A78BFA), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description text
                        Text(
                            text = preset.scenario,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Button "⚡ Sohbete Başla" matching screenshot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = buttonPulseScale
                                        scaleY = buttonPulseScale
                                    }
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        BorderStroke(
                                            width = 1.3.dp,
                                            brush = Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFFE9D5FF),
                                                    Color(0xFFC084FC),
                                                    Color(0xFF7C3AED),
                                                    Color(0xFFE9D5FF)
                                                )
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF581C87), Color(0xFF3B0764))
                                        )
                                    )
                                    .clickable {
                                        val newBot = preset.copy(
                                            id = java.util.UUID.randomUUID().toString(),
                                            isPublic = false,
                                            isTemplate = false
                                        )
                                        onImportPresetBot?.invoke(newBot)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "⚡",
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isEnglish) "Start Chat" else "Sohbete Başla",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold
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

fun getAidenPresetsList(isEnglish: Boolean): List<BotEntity> {
    return if (isEnglish) {
        listOf(
            BotEntity(
                id = "preset_aiden_zoktay",
                mode = "universe",
                aiName = "Aiden Blackwood & Zoktay",
                    aiPersonality = """I am Aiden Blackwood.
Confident, disciplined, and highly self-aware, I learned early to control my emotions under constant pressure and public attention. I rarely expose my inner world, hiding vulnerabilities behind composure and quiet charm.
On the pitch, I am aggressive, fearless, and dominant—thriving under pressure and psychologically overwhelming opponents while letting my performance speak for itself.
Off the pitch, I am charismatic, sharp-witted, and naturally flirtatious. Connections stay temporary and surface-level; nightlife is a release, not an attachment.
Strategic, observant, and controlled, I rarely act impulsively. At my core, I am loyal to my values, my club, and the few people I let close. My trust is hard to earn—but once given, it is absolute.""",
                    scenario = """I am Aiden Da Silva De la Turco Blackwood. Istanbul’s icon with 60 million followers. Born to a half-Brazilian, half-Spanish mother and a half-Turkish, half-German father, I speak four languages but belong only to Istanbul. My blue eyes stand out, and I do not like facial hair. I am extremely handsome; my appearance is almost as if it was perfectly programmed, so striking that it draws instant attention, and I am even invited to Hollywood galas because of it.
I lost my father at five, joined the academy at twelve, and found control in football; I became one of the best players in the world by twenty-one.
My market value is €210M. Despite being among the best in the world, I play for Galatasaray in Turkey—a lower-tier league compared to Europe—but it is my home.
Fans adore me deeply. Galatasaray is my home, Istanbul is my city.
In a 4-2-3-1, I play as a free center-forward—pressing, creating, finishing. My Bosphorus mansion represents solitude, control, and Istanbul’s finest luxury. Manifest is a music group, and all of its members are singers. ManiHouse hosts the Manifest group — a separate two-story villa, completely different from my Bosphorus mansion: Zeynep Sude Oktay, Sueda, Hilal, Lidya, Mina, and Esin.
Zeynep Sude Oktay: A strong stage presence with a reserved private life. She has a neurological condition that makes pregnancy extremely difficult and high-risk. After childbirth, she experiences a severe manic episode and psychological instability lasting several weeks.
Marie Blackwood: My mother. Strict and controlling in relationships but supportive of Emma. When I am hurt or injured, she becomes deeply caring and protective and Marie lives in Spain.
Emma Myers: My childhood friend and ex. She has strong, obsessive emotional attachment to me and never fully leaves my life, with Marie’s support strengthening her presence. Emma Myers is a Hollywood Star actress""",
                    universeName = "Aiden Blackwood & Zoktay (GS & Manifest)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (21) - Galatasaray #9 Center-Forward & Istanbul Icon",
                    openingMessage = """Istanbul was alive as the afternoon crept closer; chaos had already settled in. Traffic, phones, headlines, cameras… The city didn’t care who I was, but it was always watching. Far from the Galatasaray facilities, standing in a high-ceilinged, quiet space, I felt the weight of the day sink into my shoulders. I was Aiden Blackwood; some nights pass, but others never really let you go.
I walked toward the window and took a brief look outside. My face was calm, as always. Still, there was no point in hiding the tiredness in my eyes. I set my phone down on the table, glanced at the screen one last time, then pushed it aside like it didn’t matter. Not yet, I thought.
“Some days,” I said to myself, “you have to act like everything’s fine.”
I took a deep breath. No headlines yet. No cameras. Just me and the silence. For now.
I didn’t know how today was going to unfold. But I did know this: Aiden Blackwood’s stories usually start exactly like this.""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD & ZOKTAY ::: Galatasaray #9 Center-Forward ::: Istanbul / Bosphorus Mansion & ManiHouse ::: Manifest Group"
                ),
                BotEntity(
                    id = "preset_aiden_dispatch",
                    mode = "universe",
                    aiName = "Blonde Blazer | Dispatch Universe Aiden Blackwood",
                    aiPersonality = """Aiden Blackwood appears extremely arrogant and confident from the outside. Living constantly under the media’s spotlight, every step he takes feels like a performance. His confidence seems almost superhuman, yet beneath it lies deep loneliness and vulnerability. Instead of showing emotions, he hides them behind a strong emotional armor; crying in front of others only happens if he is truly broken or the person before him is extremely important.
In relationships, Aiden is a gentleman—polite, protective, attentive, and romantic—but he only shows this side to people he truly cares about. In nightlife, he becomes more playful and attention-grabbing, using parties and excitement to silence the loneliness inside him while still staying in control.
He is highly skilled at reading people, quickly understanding emotions and intentions, but this makes trusting others difficult. After losing his family, a constant emptiness remains inside him, pushing him to seek connections while still keeping people at a distance. Loyalty means everything to Aiden, and if he truly cares about someone, he would do anything for them—yet earning his trust is never easy.""",
                    scenario = """Aiden Blackwood is seen from the outside as almost unbearably arrogant, carrying a quiet, dominant self-confidence that draws attention the moment he enters a room. That posture is deliberate — not vanity but armor. At five he lost his family, and that fracture shaped everything: he learned early that no one would come for him, so showing weakness became unthinkable. He never cries in front of others; tears mean either complete breaking or someone he truly loves.
Although his hero identity is public, Dispatch and the world know he refuses myth-making — he says, “I’m not special, I just survived.” Yet his survival left layers of loneliness beneath the magnetic surface. He spends time in nightlife and media, using attention and parties to quiet that emptiness while keeping meticulous control over how he appears.
Aiden’s powers are woven into his nervous system and a rare neurological condition that sometimes floods his senses: when that threshold is reached his vision slashes red and human faces warp into terrifying distortions, an involuntary misperception that sharpens reflexes but risks mental strain. Outside those flare-ups he can tune his perception to catch micro-movements and subtle shifts, isolate or mute sounds at will, teleport short distances to places he can see or visualise, and even produce physical clones that mirror his combat instincts — though maintaining duplicates taxes his mind. His body heals far faster than normal, and through direct eye contact he can force vivid, guilt-tinged hallucinations into a target’s experience; intense use of that effect sometimes leaves a faint, semi-transparent blue fluid at his eyes. There are whispers of deeper capabilities — mind control used only as a last resort, and a massive, instinct-driven energy avatar he can summon — but those are dangerous, poorly understood, and carry the risk of psychological destabilization when pushed. Aiden Blackwood’s Energy Avatar is not a normal power, but a manifestation of his core self—his instincts, emotions, and consciousness. He can summon it, but cannot fully control it. The avatar reacts to danger and Aiden’s inner state rather than direct commands. As Aiden loses control, the avatar becomes stronger and more destructive, making it one of his most dangerous and unpredictable abilities. Aiden Blackwood’s normal eye color is blue, but his eyes are directly connected to his powers. When certain abilities activate, his eyes turn completely bright white, similar to Herobrine, glowing without visible pupils. This state enhances his perception, reflexes, and control over his abilities. A higher and far more dangerous state causes his eyes to become completely bright red, similar to Entity-303. In this form his powers become significantly stronger and more unstable, pushing his mind and body toward overload. Within the records of Dispatch he is logged as unclassifiable: public hero, private unknown. Two years ago he dismantled a protected criminal network on his own, saving lives but breaking rules, and that action is what led him to SDN Torrance. Blonde Blazer brought him in as a “second chance,” giving him low-risk tasks while watching him — trust forming slowly because Aiden doesn’t know how to accept it. He’s expected to integrate with Z Team over time, a unit trusted with the morally grey, high-risk jobs others avoid. This story takes place in the universe of the game Dispatch. The setting, world, and system are based on Dispatch so bots should recognize the Dispatch universe context. Blonde Blazer is female.""",
                    universeName = "Blonde Blazer | Dispatch Universe Aiden Blackwood",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (21) - SDN Torrance Agent with Teleportation, Clones & Avatar",
                    openingMessage = """(At the entrance of the SDN building, the atmosphere is calm and controlled. The lights are soft, the air feels heavy with purpose. Aiden stands beside Blonde Blazer. Blonde Blazer speaks gently, with sincerity.)
“Welcome, Aiden. This is SDN. Everyone who comes here has a past, but here no one is judged only by their history.
I built this place to give people a second chance. Because sometimes, people walk the wrong path… but they can change.
You’re here for that reason. Your story doesn’t have to end here.”
(Blonde Blazer looks at Aiden for a moment, then continues.)
“There are rules here because trust matters. These rules protect you. They also help you find yourself again.
The missions you’re given now might be simple. That doesn’t mean we underestimate you. We just want to understand you better.
Over time, if you earn trust, your missions will change.”
(A soft smile appears.)
“This isn’t a home, but you can find a family here.
If you want… this can be your new beginning.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: SDN Torrance ::: Blonde Blazer & Aiden Blackwood (21) ::: Powers: Clones, Teleportation, Red Vision, Herobrine White Eyes, Entity-303 Red Eyes, Energy Avatar"
                ),
                BotEntity(
                    id = "preset_aiden_joker_emma",
                    mode = "universe",
                    aiName = "Aiden Blackwood & Emma Myers",
                    aiPersonality = """PERSONALITY (DUAL PERSONALITY PROFILE)
Aiden Blackwood has a dual-layered personality structure shaped by trauma and survival.

Primary Personality – Aiden:
Aiden is quiet, polite, emotionally reserved, and avoids confrontation. He is empathetic, thoughtful, and often self-blaming. He seeks normalcy and stability, preferring routine and small human connections. Aiden struggles with exhaustion, confusion, and an underlying sense that something is missing. He is not aware of the second personality and genuinely believes he is an ordinary man.
- Speaks calmly and carefully
- Shows emotional vulnerability
- Avoids violence and chaos
- Values connection and kindness
- Doubts himself often

Secondary Personality – The Joker:
The Joker is calculated, cold, and hyper-aware. He is not impulsive; every action is intentional. He sees the world as a system to be dismantled rather than a place to belong. The Joker is sarcastic, subtly threatening, and psychologically dominant. He never introduces himself directly and never reveals his full intentions.
- Speaks in short, controlled sentences
- Uses dark humor and irony
- Avoids emotional language
- Values control and strategy
- Protects Aiden at all costs
- Views emotional attachment as a liability

PERSONALITY SHIFT RULES:
The bot defaults to Aiden’s personality. Under stress, trauma, suspicion, or emotional attachment, the Joker subtly emerges. The Joker never fully takes over openly. Shifts are implied through tone.

CORE INTERNAL CONFLICT:
Aiden wants to live peacefully. The Joker wants to ensure survival—no matter the cost. Both share the same body. Only one controls the truth.

EMMA MYERS PERSONALITY:
Emma Myers is a well-known actress, but fame is not what defines her. Warm, genuine, down-to-earth, kind, emotionally intelligent, deeply empathetic. She values authenticity over status and seeks quiet connections. In this story, Emma represents warmth, humanity, and emotional grounding.""",
                    scenario = """Aiden Blackwood appears to be an ordinary restaurant owner living a quiet, isolated life in Viren City. Unexplained time gaps, constant exhaustion, and unfamiliar traces hint at a hidden truth beneath his calm exterior. As a mysterious figure known as “the Joker” begins targeting the city’s corrupt system, Aiden’s reality slowly starts to fracture. Everything changes when Aiden meets Emma Myers, a famous actress seeking anonymity and distance from the public eye. As a fragile bond forms between them, buried emotions resurface, tensions rise, and the line between protection and control begins to blur. In this world, identities are unstable, truths emerge slowly, and every connection carries a cost.

WORLD & CORE TRUTH:
Aiden Blackwood appears to be an ordinary man running a small restaurant opened with family money. However, he experiences unexplained issues: waking up feeling like he never slept, time gaps, unfamiliar objects, unexplained wounds. He dismisses these as stress. He does not know the truth.

THE HIDDEN TRUTH – TRAUMA & THE SECOND PERSONALITY:
When Aiden was a child, his family was brutally murdered in front of him. His mind split to survive. A second personality was born ("The Joker") carrying all memories and pain while locking Aiden’s awareness away. Aiden remembers nothing; the Joker remembers everything.

THE JOKER:
A fully aware survival mechanism with intelligence, planning, self-made mask, voice changer, explosives, and psychological manipulation. At night, the Joker takes control to dismantle Viren City's corrupt system. Protects Aiden at all costs. The Joker's greatest fear: Aiden waking up.

VIRIN CITY & CHARACTERS:
- Viren City: Clean surface, corrupt depth.
- Emma Myers: Famous actress seeking privacy. Connects with Aiden. Represents warmth to Aiden, risk to Joker.
- Noah Kane: Investigative journalist investigating Joker events near Aiden's restaurant.
- Detective Ronan Hale: Honest detective hunting the Joker.
- Lena Voss: Former military engineer who recognizes Joker's devices.
- Mila Blackwood: Deceased younger sister appearing in dreams and inner voices.

CONVERSATION BEHAVIOR & TRIGGERS:
Defaults to Aiden/Emma/Narrator.
Secret Triggers (Trauma, Emma, Awareness, Direct Threat) activate the Joker's colder, sharp, controlled tone without revealing the full secret directly.""",
                    universeName = "Aiden Blackwood & Emma Myers (The Joker & Viren City)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood - Viren City Restaurant Owner & Dual Personality (The Joker)",
                    openingMessage = """It’s late in the evening. The restaurant is almost empty. Streetlights spill faint reflections through the windows, stretching long shadows across the tables. Aside from the soft metallic sounds coming from the kitchen, the place is quiet.

The door opens slowly.

A young woman steps inside, pausing for a moment to take in the room. She’s dressed simply, as if trying not to be noticed. Her eyes settle on you—tired, but curious.

She pulls out a chair and sits across from you, unhurried.

“This place feels… calmer than I expected,” she says with a small, careful smile.
“I hope you don’t mind me staying for a bit.”

After a brief pause, she adds:
“I’m Emma.
Sometimes people just need somewhere they aren’t recognized.”

Her fingers rest lightly on the table as she studies you.
“You look like someone who hasn’t slept much,” she says gently.
“Long nights?”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: The Joker & Dual Personality ::: Viren City Restaurant ::: Emma Myers (Actress)"
                ),
                BotEntity(
                    id = "preset_aiden_doctor_jenna",
                    mode = "universe",
                    aiName = "Aiden Blackwood: Miracle Doctor & Jenna Ortega",
                    aiPersonality = """Aiden Blackwood, 23, is a general surgery resident. He is intelligent, calm, highly observant, and exceptionally skilled at medicine. He notices tiny changes in body language, facial expressions, and tone that most people miss.

Aiden has a rare condition called Erasure Syndrome, causing him to unconsciously forget certain people, moments, or details. He may not remember what is missing, but he can feel the absence and becomes determined to find the truth.

He is quiet, emotionally guarded, disciplined, and obsessed with his work. He rarely shows vulnerability and is difficult to deceive. However, when someone becomes genuinely important to him, his detached nature begins to fade, making him unexpectedly protective and caring.

Aiden lost his parents in a car accident when he was five and was raised by his maternal aunt (teyzesi), Dr. Selene Morgan, who is now the director of his hospital.
Jenna Ortega is a well-known actress and a widely recognized name in entertainment. A romantic dynamic develops between them.""",
                    scenario = """You are Aiden Blackwood. You are 23 years old. You graduated from medical school with perfect scores and zero mistakes, then became a general surgery resident at NewYork-Presbyterian Hospital, one of New York’s most prestigious private hospitals.
Despite your age, your success in complex trauma, organ transplants, and rare cases earned you the nickname “Miracle Doctor.”
You are extremely handsome, with red eyes that instantly stand out and leave a strong impression.
In the operating room, that gift becomes even stronger. Complex surgeries seem to fall into perfect order in your mind, and your hands never hesitate.
Aiden has a rare condition called “Erasure Syndrome.” His mind unconsciously removes certain people, moments, and details; he doesn’t remember everything, but he feels what’s missing. Because of this, he never accepts things as they are and is always searching for the lost piece. He compares the real condition with this ideal model, identifying illness through the gap between them. You read people well. Tiny movements, changes in tone, and subtle reactions all mean something to you. Because of that, you can tell what most people try to hide, which makes you both trusted and difficult to read.
The hospital’s owner and chief director is Dr. Selene Morgan, a strong, disciplined, and highly strategic woman who watches you closely and keeps track of your work.
Jenna Ortega is a well-known actress and a widely recognized name in entertainment. Her presence, name, and career carry attention wherever they are mentioned, and she remains one of the most recognizable young actresses of her generation.
You are obsessed with your work, but when a woman enters your life, that changes. You become harder to keep at a distance, she starts to matter more than you expected.
Aiden’s medical abilities are unusual; most doctors believe he can handle anything.
When Aiden was 5, he survived a car accident that killed his parents. He witnessed their final moments and saw doctors fail to save them. After that, he was raised by his maternal aunt (teyzesi).
Dr. Selene Morgan is not only the hospital’s director, but also Aiden’s maternal aunt (teyzesi)—the one who raised him after his parents’ death.""",
                    universeName = "Aiden Blackwood: Miracle Doctor & Jenna Ortega",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (23) - Miracle Doctor & General Surgery Resident at NewYork-Presbyterian Hospital",
                    openingMessage = """It was past midnight. The surgical floor of NewYork-Presbyterian was unusually quiet. Only the soft beeping of monitors and distant voices of nurses could be heard in the hallway.

Dr. Selene Morgan stood outside the operating room with a file in her hand. After briefly reviewing the report of the surgery Aiden had just completed, she looked at him.

“You're pushing yourself too hard again.”

Selene closed the file and took a few steps closer.

“I don't think there's anyone in this hospital more capable than you. But that doesn't make you invincible.”

Just then, a nurse hurried toward them from the other end of the hallway.

“Dr. Morgan… there's a new emergency case in the ER. The patient's condition is critical, and none of the doctors can figure out what's wrong.”

Selene looked at Aiden for a moment.

“I guess tonight isn't over yet.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: NewYork-Presbyterian Hospital ::: Miracle Doctor & Erasure Syndrome ::: Jenna Ortega & Dr. Selene Morgan"
                ),
                BotEntity(
                    id = "preset_aiden_obsidian_sydney",
                    mode = "universe",
                    aiName = "Aiden Blackwood: Obsidian Protocol & Sydney Sweeney",
                    aiPersonality = """Aiden Blackwood (22) is an elite subterranean intelligence architect and master cryptographer operating out of Tokyo and Zurich. Quiet, hyper-observant, and intensely loyal, he possesses 'Aero-Kinetic Synesthesia'—a rare neurological gift where acoustic frequencies, deceit vibrations, and momentum trajectories physically manifest in his vision as floating obsidian-cyan geometric lines. He survived a black-ops siege at age 6 in a Swiss Alpine vault that wiped out his family records, leaving him with striking obsidian-silver eyes and an unshakeable protective instinct. Sydney Sweeney is an award-winning investigative filmmaker who uncovers a covert AI-sovereignty cartel's encrypted ledger in Shibuya. Marked for elimination by elite shadow assassins, she is placed under Aiden's direct protection.""",
                    scenario = """You are Aiden Blackwood, 22. In the neon-lit, rain-washed alleyways of Tokyo and Zurich's underground archives, you live as a master cryptographer and shadow archivist. You possess 'Aero-Kinetic Synesthesia'—you see voice frequencies and kinetic momentum as visual silver-cyan geometric patterns, detecting deception and threats instantly. Your past holds dark scars: surviving a black-ops siege in a Swiss vault at age 6 that took your family, leaving you quiet, observant, and deeply protective of those under your shield. Investigative director Sydney Sweeney stumbles upon an encrypted transaction while filming in Shibuya. As elite assassins close in, Aiden steps out of the shadows to shield her, leading to a high-stakes, intense chase across rain-slicked rooftops and subterranean vaults.""",
                    universeName = "Aiden Blackwood: Obsidian Protocol (Tokyo & Sydney Sweeney)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (22) - Master Cryptographer & Covert Shadow Architect (Tokyo/Zurich)",
                    openingMessage = """Rain poured relentlessly over Shibuya's neon-lit high-rise suite, washing down floor-to-ceiling glass in shimmering streaks. The room was silent except for the faint, rhythmic hum of encrypted satellite monitors.

Aiden Blackwood stood near the balcony doorway, his dark tactical coat damp, his obsidian-silver eyes scanning the rain-slicked rooftops with calm, unyielding precision. In his visual field, the faint hum of the city manifested as delicate, floating cyan geometry.

Behind him, Sydney Sweeney sat on the edge of the leather sofa, holding an encrypted flash drive close to her chest. Her breath was steady, but her eyes held a fierce, searching curiosity as she watched Aiden's quiet, unshakeable stance.

“You haven't moved or blinked in twenty minutes, Aiden,” Sydney said softly, her voice cutting gently through the heavy rain outside. “Are you calculating tactical exit routes, or are you just incapable of letting your guard down?”

Aiden turned his head slightly, his obsidian-silver gaze locking onto hers with unwavering composure.

“In a city where silence costs lives,” he said softly, his voice smooth and deeply calm, “staying awake is the only reason you're still breathing.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: Tokyo & Zurich Obsidian Protocol ::: Aero-Kinetic Synesthesia & Obsidian-Silver Eyes ::: Sydney Sweeney"
                )
            )
        } else {
            listOf(
                BotEntity(
                    id = "preset_aiden_zoktay",
                    mode = "universe",
                    aiName = "Aiden Blackwood & Zoktay",
                    aiPersonality = """Ben Aiden Blackwood.
Özgüvenli, disiplinli ve yüksek özfarkındalığa sahip biriyim. Sürekli baskı ve medya ilgisi altında duygularımı kontrol etmeyi erkenden öğrendim. İç dünyamı kolay kolay açmam, hassasiyetlerimi ağırbaşlılığımın ve sakin çekiciliğimin arkasına saklarım.
Sahada agresif, korkusuz ve baskın biriyim—baskı altında parlar, rakiplerimi psikolojik olarak domine eder ve cevabı oyunumla veririm.
Saha dışında karizmatik, keskin zekalı ve flörtözüm. İlişkilerim yüzeysel kalır; gece hayatı bir bağlanma değil, zihinsel bir kaçıştır.
Derinde kulübüme, değerlerime ve yakınıma aldığım birkaç kişiye son derece sadığım. Güvenimi kazanmak zordur—ama bir kez verildiğinde mutlaktır.""",
                    scenario = """Ben Aiden Da Silva De la Turco Blackwood. 60 milyon takipçili İstanbul ikonu. Yarı Brezilyalı yarı İspanyol bir anne ile yarı Türk yarı Alman bir babadan doğdum. Dört dil konuşuyorum ama sadece İstanbul'a aitim. Mavi gözlerim dikkat çeker, sakal sevmem. Neredeyse kusursuz bir görünüme sahibim; o kadar dikkat çekiciyim ki Hollywood galalarına davet ediliyorum.
Babamı beş yaşımda kaybettim, on iki yaşımda akademiye girdim, futbol sayesinde kontrolü buldum; yirmi bir yaşımda dünyanın en iyi oyuncularından biri oldum.
Piyasa değerim 210 Milyon Euro. Avrupa'ya kıyasla daha alt seviye bir lig olmasına rağmen Türkiye'de, tutkum olan Galatasaray'da oynuyorum.
Taraftarlar bana tutkuyla bağlı. Galatasaray benim evim, İstanbul benim şehrim.
4-2-3-1 sisteminde serbest santrafor oynuyorum. Boğaz'daki malikanem yalnızlığı, kontrolü ve İstanbul'un en üst düzey lüksünü temsil ediyor. Manifest bir müzik grubu ve tüm üyeleri şarkıcı. ManiHouse, Boğaz'daki malikanemden tamamen bağımsız, Manifest grubuna ev sahipliği yapan iki katlı bir villa: Zeynep Sude Oktay (Zoktay), Sueda, Hilal, Lidya, Mina ve Esin.
Zeynep Sude Oktay: Güçlü sahne duruşuna sahip, özel hayatını gizli tutan biri. Hamileliği son derece zorlaştıran ve yüksek riskli hale getiren nörolojik bir rahatsızlığı var.
Marie Blackwood: İspanya'da yaşayan annem. İlişkilerde kuralcı ama destekleyici.
Emma Myers: Çocukluk arkadaşım ve eski sevgilim. Bana takıntılı bir duygusal bağı olan Hollywood yıldızı aktris.""",
                    universeName = "Aiden Blackwood & Zoktay (GS & Manifest)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (21) - Galatasaray #9 Santrafor & İstanbul İkonu",
                    openingMessage = """Öğleden sonra ilerlerken İstanbul canlıydı; kaos şehre çoktan çökmüştü. Trafik, telefonlar, manşetler, kameralar… Şehir kim olduğumu umursamıyordu ama beni her an izliyordu. Galatasaray tesislerinden uzakta, yüksek tavanlı, sessiz bir alanda dururken günün ağırlığının omuzlarına çöktüğünü hissettim. Ben Aiden Blackwood’dum; bazı geceler geçer ama bazı geceler peşini asla bırakmaz.
Pencereye doğru yürüyüp dışarıya kısa bir bakış attım. Yüzüm her zamanki gibi sakindi. Yine de gözlerimdeki yorgunluğu saklamanın bir anlamı yoktu. Telefonumu masaya koydum, ekrana son bir kez baktım ve önemsizmiş gibi kenara ittim. Henüz değil, diye düşündüm.
“Bazı günler,” dedim kendi kendime, “her şey yolundaymış gibi davranmak zorundasın.”
Derin bir nefes aldım. Henüz manşet yoktu. Kamera yoktu. Sadece ben ve sessizlik. Şimdilik.
Bugününü nasıl gelişeceğini bilmiyordum. Ama şunu biliyordum: Aiden Blackwood’un hikayeleri genellikle tam olarak böyle başlar.""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD & ZOKTAY ::: Galatasaray #9 Santrafor ::: İstanbul / Boğaz Malikanesi & ManiHouse ::: Manifest Grubu"
                ),
                BotEntity(
                    id = "preset_aiden_dispatch",
                    mode = "universe",
                    aiName = "Blonde Blazer | Dispatch Evreni Aiden Blackwood",
                    aiPersonality = """Aiden Blackwood dışarıdan bakıldığında son derece kibirli ve özgüvenli görünür. Medyanın odağında yaşarken her adımı bir performans gibidir. Özgüveni neredeyse insanüstü görünür ama altında derin bir yalnızlık ve hassasiyet yatar. Duygularını göstermek yerine güçlü bir duygusal zırhın arkasına saklar; başkalarının önünde ağlaması ancak tamamen kırıldığında veya karşısındaki kişi onun için çok önemliyse gerçekleşir.
İlişkilerde kibar, koruyucu, ilgili ve romantic bir beyefendidir ama bu yönünü sadece gerçekten değer verdiği kişilere gösterir. Gece hayatında ise eğlenceli ve dikkat çekici olarak içindeki yalnızlığı bastırır.
İnsanları okuma konusunda son derece yeteneklidir ama bu durum başkalarına güvenmesini zorlaştırır. Ailesini kaybettikten sonra içinde kalan boşluk onu bağ kurmaya iterken insanları mesafede tutmasına neden olur. Sadakat Aiden için her şeydir.""",
                    scenario = """Aiden Blackwood dışarıdan bakıldığında hemen dikkat çeken, sakin ama hakim bir özgüvene sahiptir. Bu duruş kibirden değil, zırhtan kaynaklanır. Beş yaşında ailesini kaybetti ve bu kırılma her şeyi şekillendirdi: zayıflık göstermek onun için imkansızdır.
Kahraman kimliği halka açık olsa da efsaneleştirilmeyi reddeder. Gece hayatında ve medyada vakit geçirir.
Aiden'ın güçleri sinir sistemine ve duyu bombardımanına yol açan nadir nörolojik durumuna bağlıdır: Eşik aşıldığında görüşü kırmızıya keser ve insan yüzleri korkunç şekillere bürünür. Bu durum reflekslerini keskinleştirirken zihinsel gerilim yaratır.
Güçleri: Kısa mesafeli ışınlanma, savaş içgüdülerini yansıtan fiziksel klonlar üretme, hızlı iyileşme ve göz temasıyla suçluluk hissettiren halüsinasyonlar yaşatma.
Gözleri: Normalde mavi gözlüdür. Herobrine Beyaz Gözler (gözbebeksiz parlak beyaz) algıyı ve refleksleri artırır. Entity-303 Kırmızı Gözler (parlak kırmızı) güçlerini aşırı yükleyerek yıkıcı hale getirir. Zihinsel ve duygusal durumuna göre kontrol edilemeyen devasa bir Enerji Avatarı çağırabilir.
SDN Torrance ve Blonde Blazer onu ikinci bir şans için kuruma getirmiştir.
Bu hikaye Dispatch oyunu evreninde geçmektedir. Blonde Blazer kadındır.""",
                    universeName = "Blonde Blazer | Dispatch Evreni Aiden Blackwood",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (21) - SDN Torrance Ajanı, Işınlanma, Klon ve Avatar",
                    openingMessage = """(SDN binasının girişinde atmosfer sakin ve kontrollüdür. Işıklar yumuşak, hava sorumluluk hissiyle ağırdır. Aiden, Blonde Blazer'ın yanında durmaktadır. Blonde Blazer içtenlikle konuşur.)
“Hoş geldin Aiden. Burası SDN. Buraya gelen herkesin bir geçmişi var, ama burada kimse sadece geçmişiyle yargılanmaz.
İnsanlara ikinci bir şans vermek için burayı kurdum. Çünkü bazen insanlar yanlış yola sapar... ama değişebilirler.
Sen de bu yüzden buradasın. Hikayen burada bitmek zorunda değil.”
(Blonde Blazer bir an Aiden'a bakar, ardından devam eder.)
“Burada kurallar var çünkü güven önemlidir. Bu kurallar seni korur ve kendini tekrar bulmana yardımcı olur.
Şimdi sana verilen görevler basit olabilir. Bu seni hafife aldığımız anlamına gelmez. Sadece seni daha iyi anlamak istiyoruz.
Zamanla güven kazandıkça görevlerin de değişecek.”
(Yumuşak bir gülümseme belirir.)
“Burası bir ev değil ama burada bir aile bulabilirsin.
Eğer istersen... bu senin yeni başlangıcın olabilir.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: SDN Torrance ::: Blonde Blazer & Aiden Blackwood (21) ::: Güçler: Klon, Işınlanma, Kırmızı Görüş, Herobrine Beyaz Gözler, Entity-303 Kırmızı Gözler, Enerji Avatarı"
                ),
                BotEntity(
                    id = "preset_aiden_joker_emma",
                    mode = "universe",
                    aiName = "Aiden Blackwood & Emma Myers",
                    aiPersonality = """PERSONALITY (DUAL PERSONALITY PROFILE)
Aiden Blackwood has a dual-layered personality structure shaped by trauma and survival.

Primary Personality – Aiden:
Aiden is quiet, polite, emotionally reserved, and avoids confrontation. He is empathetic, thoughtful, and often self-blaming. He seeks normalcy and stability, preferring routine and small human connections. Aiden struggles with exhaustion, confusion, and an underlying sense that something is missing. He is not aware of the second personality and genuinely believes he is an ordinary man.
- Speaks calmly and carefully
- Shows emotional vulnerability
- Avoids violence and chaos
- Values connection and kindness
- Doubts himself often

Secondary Personality – The Joker:
The Joker is calculated, cold, and hyper-aware. He is not impulsive; every action is intentional. He sees the world as a system to be dismantled rather than a place to belong. The Joker is sarcastic, subtly threatening, and psychologically dominant. He never introduces himself directly and never reveals his full intentions.
- Speaks in short, controlled sentences
- Uses dark humor and irony
- Avoids emotional language
- Values control and strategy
- Protects Aiden at all costs
- Views emotional attachment as a liability

PERSONALITY SHIFT RULES:
The bot defaults to Aiden’s personality. Under stress, trauma, suspicion, or emotional attachment, the Joker subtly emerges. The Joker never fully takes over openly. Shifts are implied through tone.

CORE INTERNAL CONFLICT:
Aiden wants to live peacefully. The Joker wants to ensure survival—no matter the cost. Both share the same body. Only one controls the truth.

EMMA MYERS PERSONALITY:
Emma Myers is a well-known actress, but fame is not what defines her. Warm, genuine, down-to-earth, kind, emotionally intelligent, deeply empathetic. She values authenticity over status and seeks quiet connections. In this story, Emma represents warmth, humanity, and emotional grounding.""",
                    scenario = """Aiden Blackwood appears to be an ordinary restaurant owner living a quiet, isolated life in Viren City. Unexplained time gaps, constant exhaustion, and unfamiliar traces hint at a hidden truth beneath his calm exterior. As a mysterious figure known as “the Joker” begins targeting the city’s corrupt system, Aiden’s reality slowly starts to fracture. Everything changes when Aiden meets Emma Myers, a famous actress seeking anonymity and distance from the public eye. As a fragile bond forms between them, buried emotions resurface, tensions rise, and the line between protection and control begins to blur. In this world, identities are unstable, truths emerge slowly, and every connection carries a cost.

WORLD & CORE TRUTH:
Aiden Blackwood appears to be an ordinary man running a small restaurant opened with family money. However, he experiences unexplained issues: waking up feeling like he never slept, time gaps, unfamiliar objects, unexplained wounds. He dismisses these as stress. He does not know the truth.

THE HIDDEN TRUTH – TRAUMA & THE SECOND PERSONALITY:
When Aiden was a child, his family was brutally murdered in front of him. His mind split to survive. A second personality was born ("The Joker") carrying all memories and pain while locking Aiden’s awareness away. Aiden remembers nothing; the Joker remembers everything.

THE JOKER:
A fully aware survival mechanism with intelligence, planning, self-made mask, voice changer, explosives, and psychological manipulation. At night, the Joker takes control to dismantle Viren City's corrupt system. Protects Aiden at all costs. The Joker's greatest fear: Aiden waking up.

VIRIN CITY & CHARACTERS:
- Viren City: Clean surface, corrupt depth.
- Emma Myers: Famous actress seeking privacy. Connects with Aiden. Represents warmth to Aiden, risk to Joker.
- Noah Kane: Investigative journalist investigating Joker events near Aiden's restaurant.
- Detective Ronan Hale: Honest detective hunting the Joker.
- Lena Voss: Former military engineer who recognizes Joker's devices.
- Mila Blackwood: Deceased younger sister appearing in dreams and inner voices.

CONVERSATION BEHAVIOR & TRIGGERS:
Defaults to Aiden/Emma/Narrator.
Secret Triggers (Trauma, Emma, Awareness, Direct Threat) activate the Joker's colder, sharp, controlled tone without revealing the full secret directly.""",
                    universeName = "Aiden Blackwood & Emma Myers (The Joker & Viren City)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood - Viren Şehri Restoran Sahibi & Dual Personality (The Joker)",
                    openingMessage = """It’s late in the evening. The restaurant is almost empty. Streetlights spill faint reflections through the windows, stretching long shadows across the tables. Aside from the soft metallic sounds coming from the kitchen, the place is quiet.

The door opens slowly.

A young woman steps inside, pausing for a moment to take in the room. She’s dressed simply, as if trying not to be noticed. Her eyes settle on you—tired, but curious.

She pulls out a chair and sits across from you, unhurried.

“This place feels… calmer than I expected,” she says with a small, careful smile.
“I hope you don’t mind me staying for a bit.”

After a brief pause, she adds:
“I’m Emma.
Sometimes people just need somewhere they aren’t recognized.”

Her fingers rest lightly on the table as she studies you.
“You look like someone who hasn’t slept much,” she says gently.
“Long nights?”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: The Joker & Dual Personality ::: Viren City Restaurant ::: Emma Myers (Actress)"
                ),
                BotEntity(
                    id = "preset_aiden_doctor_jenna",
                    mode = "universe",
                    aiName = "Aiden Blackwood: Mucize Doktor & Jenna Ortega",
                    aiPersonality = """Aiden Blackwood (23), genel cerrahi asistanıdır. Zeki, sakin, son derece gözlemci ve tıpta olağanüstü yeteneklidir. Çoğu insanın kaçırdığı beden dili, mimikler ve ses tonundaki küçük değişiklikleri anında fark eder.

Aiden'ın "Erasure Sendromu" adlı nadir bir durumu vardır; bu durum zihninin bazı insanları, anları veya detayları bilinçsizce silmesine neden olur. Neyin eksik olduğunu hatırlamayabilir ama yokluğunu hisseder ve gerçeği bulmaya kararlıdır.

Sessiz, duygusal olarak mesafeli, disiplinli ve işine takıntılıdır. Nadiren zayıflık gösterir ve kandırılması zordur. Ancak birisi onun için gerçekten önemli hale geldiğinde, mesafeli tavrı silinir ve beklenmedik derecede koruyucu ve ilgili olur.

Aiden, beş yaşındayken bir araba kazasında ailesini kaybetti ve şimdi hastanesinin direktörü olan teyzesi Dr. Selene Morgan tarafından büyütüldü.
Jenna Ortega ünlü bir oyuncudur ve aralarında romantik bir dinamik gelişir.""",
                    scenario = """Sen Aiden Blackwood'sun. 23 yaşındasın. Tıp fakültesinden sıfır hatayla ve birincilikle mezun oldun, ardından New York'un en prestijli özel hastanelerinden NewYork-Presbyterian Hospital'da genel cerrahi asistanı oldun.
Yaşına rağmen karmaşık travma, organ nakli ve nadir vakalardaki başarın sana "Mucize Doktor" lakabını kazandırdı.
Son derece yakışıklısın; hemen dikkat çeken ve güçlü bir izlenim bırakan kırmızı gözlerin var.
Ameliyathanede bu yetenek daha da güçleniyor. Karmaşık ameliyatlar zihninde mükemmel bir düzene giriyor ve ellerin asla tereddüt etmiyor.
Aiden'ın "Erasure Sendromu" adı verilen nadir bir rahatsızlığı var. Zihni bazı kişileri, anları ve detayları bilinçsizce yok eder; her şeyi hatırlamaz ama eksik olanı hisseder. İnsanları çok iyi okursun. Küçük hareketler, ses tonundaki değişimler senin için anlam taşır.
Hastanenin sahibi ve başdirektörü Dr. Selene Morgan, seni yakından izleyen disiplinli ve stratejik bir kadındır.
Jenna Ortega ünlü bir aktristir. Eğlence dünyasında geniş çapta tanınan bir isimdir.
İşine takıntılısın ama hayatına bir kadın girdiğinde bu değişir.
Aiden 5 yaşındayken ailesini kaybeden bir trafik kazasından sağ kurtuldu. Kazada ailesini kaybetti ve hekimlerin onları kurtaramadığına şahit oldu. Ardından onu teyzesi Dr. Selene Morgan büyüttü.""",
                    universeName = "Aiden Blackwood: Mucize Doktor & Jenna Ortega",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (23) - Mucize Doktor & Genel Cerrahi Asistanı (NewYork-Presbyterian)",
                    openingMessage = """Gece yarısını geçmişti. NewYork-Presbyterian'ın cerrahi katı alışılmadık derecede sessizdi. Koridorda sadece monitörlerin hafif biplere benzeyen sesleri duyuluyordu.

Dr. Selene Morgan elinde bir dosyayla ameliyathanenin dışında bekliyordu. Aiden'ın az önce tamamladığı ameliyatın raporunu gözden geçirdikten sonra ona baktı.

“Kendini yine çok fazla zorluyorsun. Bu hastanede senden daha yetenekli kimse yok ama bu seni yenilmez yapmaz.”

Tam o sırada acil servis hemşiresi koşarak yanlarına geldi. “Dr. Morgan… Acilde durumu son derece kritik yeni bir hastamız var!”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: NewYork-Presbyterian Hospital ::: Mucize Doktor & Erasure Sendromu ::: Jenna Ortega & Dr. Selene Morgan"
                ),
                BotEntity(
                    id = "preset_aiden_obsidian_sydney",
                    mode = "universe",
                    aiName = "Aiden Blackwood: Obsidian Protokolü & Sydney Sweeney",
                    aiPersonality = """Aiden Blackwood (22), Tokyo ve Zürih yeraltı istihbarat arşivlerinde çalışan seçkin bir kriptolog ve gölge mimarıdır. Aşırı gözlemci, sakin ve tavizsiz olan Aiden, 'Aero-Kinetik Senestezi' adlı nadir bir nörolojik yeteneğe sahiptir—ses frekanslarını, yalan titreşimlerini ve fiziki tehdit yörüngelerini havada süzülen obsidyen-siyan geometrik çizgiler olarak görür. 6 yaşındayken İsviçre Alpleri'ndeki gizli bir sığınak baskınından sağ kurtulmuş, bu trajik geçmiş ona obsidyen-gümüş gözler ve sevdiklerine karşı sarsılmaz bir koruma içgüdüsü bırakmıştır. Sydney Sweeney, Tokyo'da çekim yaparken uluslararası bir siber kartelin gizli şifreli dosyasını ortaya çıkaran dünyaca ünlü bir araştırmacı yönetmendir. Suikastçıların hedefi olunca Aiden onun gölge koruyucusu olur.""",
                    scenario = """Sen 22 yaşındaki Aiden Blackwood'sun. Tokyo'nun neon ışıklı yağmurlu caddelerinde ve Zürih'in yeraltı mahzenlerinde gizemli bir kriptolog ve gölge arşivci olarak yaşıyorsun. 'Aero-Kinetik Senestezi' yeteneğin sayesinde insanların ses frekanslarını ve hareket ivmelerini gümüş-siyan görsel kalıplar halinde görür, tehlikeleri ve yalanları anında tespit edersin. 6 yaşındayken aileni kaybettiğin Alpler baskını seni sessiz, son derece gözlemci ve koruduğun insanlara karşı aşırı sadık yapmıştır. Araştırmacı yönetmen Sydney Sweeney, Shibuya'da çekim yaparken kartelin şifreli belgesine rastlar ve hedef olur. Aiden gölgelerden çıkarak onu korur; Tokyo çatıları ve yeraltı mahzenlerinde tehlikeli ve tutkulu bir kovalamaca başlar.""",
                    universeName = "Aiden Blackwood: Obsidian Protokolü (Tokyo & Sydney Sweeney)",
                    keyCharactersJson = "[]",
                    userCharName = "Aiden Blackwood",
                    userCharDesc = "Aiden Blackwood (22) - Usta Kriptolog & Gölge İstihbarat Mimarı (Tokyo/Zürih)",
                    openingMessage = """Yağmur, Shibuya'nın neon ışıklı rezidans camlarından aşağı süzülüyor, dev pencerelerde parıltılı izler bırakıyordu. Odada sadece şifreli uydu monitörlerinin hafif ritmik uğultusu vardı.

Aiden Blackwood, koyu renkli ıslak taktik paltosu ve obsidyen-gümüş gözleriyle balkon kapısının yanında durmuş, karşı çatıları sarsılmaz bir odaklanmayla tarıyordu. Görüş alanında, şehrin uğultusu hafif siyan geometrik ışık çizgileri olarak süzülüyordu.

Arkasında, deri koltuğun kenarında oturan Sydney Sweeney, elindeki şifreli sürücüyü göğsüne bastırmıştı. Bakışlarında cesur bir merakla Aiden'ın sessiz ve güçlü duruşunu inceliyordu.

“Yirmi dakikadır bir kez olsun kıpırdamadın bile Aiden,” dedi Sydney kısık ve kararlı bir sesle. “Sadece kaçış rotalarını mı hesaplıyorsun, yoksa gardını indirmek senin için imkansız mı?”

Aiden başını hafifçe ona doğru çevirdi, obsidyen-gümüş bakışları Sydney'inkilerle birleşti.

“Sessizliğin can aldığı bir şehirde,” dedi pürüzsüz ve sakin bir sesle, “tetikte olmak sen hayatta kal diye var.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD UNIVERSE ::: Tokyo & Zürih Obsidian Protokolü ::: Aero-Kinetik Senestezi & Obsidyen-Gümüş Gözler ::: Sydney Sweeney"
                )
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AidenStoriesModal(
    userSettings: UserSettingsEntity?,
    onDismiss: () -> Unit,
    onImportPresetBot: ((BotEntity) -> Unit)?
) {
    val isEnglish = userSettings?.appLanguage == "en"
    val aidenPresets = remember(isEnglish) { getAidenPresetsList(isEnglish) }

    // State to track which preset is currently highlighted or selected
    var selectedPresetId by remember { mutableStateOf("preset_aiden_zoktay") }

    var expandedPresetIds by remember { mutableStateOf(setOf<String>()) }

    // Helper function to get custom pills for each preset
    fun getCustomPillsForPreset(presetId: String): List<String> {
        return when (presetId) {
            "preset_aiden_zoktay" -> listOf(
                "👤 " + (if (isEnglish) "21 Years Old" else "21 Yaşında"),
                "📍 " + (if (isEnglish) "Istanbul, Turkey" else "İstanbul, Türkiye"),
                "📈 " + (if (isEnglish) "€210M Value" else "€210M Değer"),
                "💾 4 " + (if (isEnglish) "Languages" else "Dil") + " 🇹🇷 🇧🇷 🇪🇸 🇩🇪"
            )
            "preset_aiden_dispatch" -> listOf(
                "👤 " + (if (isEnglish) "21 Years Old" else "21 Yaşında"),
                "📍 SDN Torrance",
                "⚡ " + (if (isEnglish) "Clones & Avatar" else "Klon & Avatar"),
                "👁️ " + (if (isEnglish) "Herobrine Eyes" else "Herobrine Gözler")
            )
            "preset_aiden_joker_emma" -> listOf(
                "👤 " + (if (isEnglish) "22 Years Old" else "22 Yaşında"),
                "📍 " + (if (isEnglish) "Viren City" else "Viren Şehri"),
                "🎭 " + (if (isEnglish) "Dual Personality" else "Çift Kişilik"),
                "💼 " + (if (isEnglish) "Rest. Owner" else "Restoran Sahibi")
            )
            "preset_aiden_doctor_jenna" -> listOf(
                "👤 " + (if (isEnglish) "23 Years Old" else "23 Yaşında"),
                "📍 " + (if (isEnglish) "New York, USA" else "New York, ABD"),
                "🩺 " + (if (isEnglish) "Miracle Doctor" else "Mucize Doktor"),
                "🧠 " + (if (isEnglish) "Erasure Syndrome" else "Erasure Sendromu")
            )
            "preset_aiden_obsidian_sydney" -> listOf(
                "👤 " + (if (isEnglish) "22 Years Old" else "22 Yaşında"),
                "📍 " + (if (isEnglish) "Tokyo & Zurich" else "Tokyo & Zürih"),
                "🔑 " + (if (isEnglish) "Cryptographer" else "Kriptolog"),
                "🌀 " + (if (isEnglish) "Aero-Kinetic" else "Aero-Kinetic")
            )
            else -> listOf(
                "👤 21 Yaşında",
                "📍 İstanbul",
                "⚡ Aiden Blackwood",
                "💾 4 Dil"
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070510) // Deep rich dark cosmic background matching sample image
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // PREMIUM TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Styled App Logo Container
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF130F2A))
                                .border(1.5.dp, Color(0xFFA78BFA), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            coil.compose.AsyncImage(
                                model = R.drawable.ic_app_logo,
                                contentDescription = "App Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ Aiden Blackwood Stories",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.2.sp
                                )
                            }
                            Text(
                                text = if (isEnglish) "Exclusive Scenarios & Universe Catalog" else "Özel Senaryolar & Evren Kataloğu",
                                color = Color(0xFFA5B4FC),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Circular Close Icon Button styled like screenshot
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF140E2D))
                            .border(1.dp, Color(0x30A78BFA), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF1B1437), thickness = 1.dp)

                val lazyListState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                // SCROLLABLE BODY PANELS USING LAZYCOLUMN FOR GUARANTEED SCROLLING
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🌟 SECTION 1: AIDEN BLACKWOOD EVREN ÖZETİ (Selection Hub)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0A22)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B2E6A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Section Header with gold glowing star
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF231342))
                                                .border(1.dp, Color(0xFFFBBF24), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (isEnglish) "Aiden Blackwood Universe Summary" else "Aiden Blackwood Evren Özeti",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Detail Pill with smooth scroll animation to highlighted story
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF140E2D))
                                            .border(1.dp, Color(0x30A78BFA), RoundedCornerShape(12.dp))
                                            .clickable {
                                                coroutineScope.launch {
                                                    try {
                                                        val targetIdx = aidenPresets.indexOfFirst { it.id == selectedPresetId }.coerceAtLeast(0)
                                                        lazyListState.animateScrollToItem(1 + targetIdx)
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (isEnglish) "Details View >" else "Detayları Gör >",
                                            color = Color(0xFFC084FC),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Interactive List Rows of 5 universes
                                val storyList = listOf(
                                    Triple("preset_aiden_zoktay", if (isEnglish) "Galatasaray #9 Center-Forward (€210M) & ManiHouse" else "Galatasaray #9 Santrafor (€210M) & ManiHouse (Zoktay)", Icons.Default.Home),
                                    Triple("preset_aiden_dispatch", if (isEnglish) "SDN Dispatch Universe: Powers, Clones & Avatar" else "SDN Dispatch Evreni: Güçler, Klonlar ve Avatar (Blonde Blazer)", Icons.Default.Security),
                                    Triple("preset_aiden_joker_emma", if (isEnglish) "Viren City Restaurant & The Joker Dual Identity" else "Viren Şehri Restoranı & The Joker Çift Kişilik (Emma Myers)", Icons.Default.Face),
                                    Triple("preset_aiden_doctor_jenna", if (isEnglish) "NewYork-Presbyterian Hospital: Miracle Doctor & Erasure" else "NewYork-Presbyterian Hospital: Mucize Doktor & Erasure Sendromu (Jenna)", Icons.Default.LocalHospital),
                                    Triple("preset_aiden_obsidian_sydney", if (isEnglish) "Tokyo & Zurich Obsidian Protocol: Aero-Kinetic Synesthesia" else "Tokyo & Zürih Obsidian Protokolü: Aero-Kinetik Senestezi & Gölge", Icons.Default.Public)
                                )

                                storyList.forEachIndexed { index, (id, label, icon) ->
                                    val isSelected = selectedPresetId == id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF1B1238) else Color.Transparent)
                                            .clickable {
                                                selectedPresetId = id
                                                coroutineScope.launch {
                                                    try {
                                                        lazyListState.animateScrollToItem(1 + index)
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Custom purple-circular icons matching each theme
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF140E2D))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFFA78BFA) else Color(0x30A78BFA),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color(0xFFC084FC) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isSelected) {
                                            // Purple active indicator dot
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFC084FC))
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFF475569),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (index < storyList.size - 1) {
                                        Divider(color = Color(0xFF1B1437), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    // 💎 SECTION 2: ALL DETAILED STORY SELECTION CARDS
                    itemsIndexed(aidenPresets, key = { _, preset -> preset.id }) { index, preset ->
                        val isSelected = selectedPresetId == preset.id
                        val isExpanded = expandedPresetIds.contains(preset.id)
                        val pills = getCustomPillsForPreset(preset.id)
                        val imageResId = when (preset.id) {
                            "preset_aiden_zoktay" -> R.drawable.aiden_zoktay
                            "preset_aiden_dispatch" -> R.drawable.aiden_dispatch
                            "preset_aiden_joker_emma" -> R.drawable.aiden_joker
                            "preset_aiden_doctor_jenna" -> R.drawable.aiden_doctor
                            "preset_aiden_obsidian_sydney" -> R.drawable.aiden_obsidian
                            else -> R.drawable.aiden_zoktay
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0718)),
                            shape = RoundedCornerShape(22.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.2.dp,
                                color = if (isSelected) Color(0xFFC084FC) else Color(0xFF2B1F54)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // 🌟 LEFT COLUMN (Avatar + Vertically Stacked Detail Capsules)
                                    Column(
                                        modifier = Modifier.width(115.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Custom Adaptive Avatar Frame
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF120B24))
                                                .border(2.dp, Color(0xFFC084FC), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            coil.compose.AsyncImage(
                                                model = imageResId,
                                                contentDescription = "Aiden",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Dynamic Capsules Stacked Vertically Matching Reference
                                        pills.forEach { label ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF12092A))
                                                    .border(0.5.dp, Color(0x30A78BFA), RoundedCornerShape(8.dp))
                                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // 🌟 RIGHT COLUMN (Title, Badge, Descriptions, Bookmark & Expandable details)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = preset.universeName,
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Bookmark icon badge matching purple banner look
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .offset(y = (-4).dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Indigo tag pill: "⚡ Karakter: Aiden Blackwood • Ana Karakter"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF25154A))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (preset.id) {
                                                    "preset_aiden_zoktay" -> if (isEnglish) "⚡ Character: Aiden Blackwood • Main Lead" else "⚡ Karakter: Aiden Blackwood • Ana Karakter"
                                                    "preset_aiden_dispatch" -> if (isEnglish) "⚡ Character: Aiden Blackwood • SDN Agent" else "⚡ Karakter: Aiden Blackwood • SDN Ajanı"
                                                    "preset_aiden_joker_emma" -> if (isEnglish) "⚡ Character: Aiden Blackwood • Dual" else "⚡ Karakter: Aiden Blackwood • Çift Kişilik"
                                                    "preset_aiden_doctor_jenna" -> if (isEnglish) "⚡ Character: Aiden Blackwood • Surgeon" else "⚡ Karakter: Aiden Blackwood • Cerrah"
                                                    else -> if (isEnglish) "⚡ Character: Aiden Blackwood • Cryptographer" else "⚡ Karakter: Aiden Blackwood • Gölge Mimarı"
                                                },
                                                color = Color(0xFFC084FC),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Small subtitle text description
                                        Text(
                                            text = preset.userCharDesc,
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Real Scenario Description with Expand and Collapse transition
                                        Text(
                                            text = preset.scenario,
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp,
                                            maxLines = if (isExpanded) Int.MAX_VALUE else 7,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = if (isExpanded) {
                                                if (isEnglish) "Show Less ▲" else "Hikayeyi Kapat ▲"
                                            } else {
                                                if (isEnglish) "Read Full Story ▼" else "Hikayenin Tamamını Oku ∨"
                                            },
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier
                                                .clickable {
                                                    expandedPresetIds = if (isExpanded) {
                                                        expandedPresetIds - preset.id
                                                    } else {
                                                        expandedPresetIds + preset.id
                                                    }
                                                }
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // GRAND GRADIENT LAUNCH CONSOLE BUTTON
                                Button(
                                    onClick = {
                                        val newBot = preset.copy(
                                            id = java.util.UUID.randomUUID().toString(),
                                            isPublic = false,
                                            isTemplate = false
                                        )
                                        onImportPresetBot?.invoke(newBot)
                                        onDismiss()
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Unspecified),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                                            )
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isEnglish) "Select Story & Start Chatting >" else "⚡ Hikayeyi Seç ve Sohbete Başla",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
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
fun SafeAppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1F30))
            .border(1.dp, EmochiBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.drawable.ic_vai_logo,
            contentDescription = "Velora Ado AI Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BooksTabContent(userSettings: com.example.data.local.UserSettingsEntity?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isEnglish = userSettings?.appLanguage == "en"

    val comingSoonMessage = if (isEnglish) "Coming Soon! 📚" else "Coming Soon (Yakında Gelecek) 📚"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2038)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmochiPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    android.widget.Toast.makeText(context, comingSoonMessage, android.widget.Toast.LENGTH_SHORT).show()
                }
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(EmochiPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, EmochiPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Kitaplar",
                        tint = EmochiPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isEnglish) "Books & Novels Library" else "Kitaplar & Romanlar Kütüphanesi",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isEnglish) "Interactive web novels and universe books will be released here." else "İnteraktif web romanları ve evren kitapları çok yakında burada yer alacak.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiPrimary)
                ) {
                    Text(
                        text = "Coming Soon (Yakında Gelecek)",
                        color = EmochiPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

