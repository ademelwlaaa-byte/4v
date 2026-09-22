package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.AffectionEventEntity
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.WorldState
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary

@Composable
fun EmotionStatusSection(
    bot: BotEntity,
    characterEmotions: List<CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<AffectionEventEntity> = emptyList(),
    filterCount: Int = 0,
    onUpdateCharacterEmotion: ((characterName: String, mood: String, affection: Int, trust: Int, tension: Int) -> Unit)? = null,
    onDeleteCharacter: ((characterName: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val emotion = remember(bot.emotionState) { EmotionState.fromJson(bot.emotionState) }
    val isUniverse = bot.mode == "universe"
    val worldState = remember(bot.worldAtmosphere) { WorldState.fromJson(bot.worldAtmosphere) }
    var showTimeline by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = EmochiCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "❤️",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUniverse) "Evren ve Ruh Hali Takibi" else "Duygu ve İlişki Durumu",
                        color = EmochiPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF23253A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${emotion.getMoodEmoji()} ${emotion.mood.replaceFirstChar { it.uppercase() }}",
                        color = EmochiTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // İçerik Filtresi İstatistiği Rozeti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1726))
                    .border(1.dp, Color(0x408B5CF6), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("İçerik Filtresi İstatistiği", color = Color(0xFFD8B4FE), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Son 100 mesajda $filterCount filtre tetiklendi",
                        color = if (filterCount > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Metrics (Affection with Tier, Trust, Tension, Hurt)
            EmotionBarItem(
                label = "Yakınlık Kademesi: ${emotion.getAffectionTierLabel()}",
                value = emotion.affection,
                maxValue = 100,
                color = Color(0xFFFF6B81),
                icon = "❤️"
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmotionBarItem(
                label = "Güven Seviyesi (Trust)",
                value = emotion.trust,
                maxValue = 100,
                color = Color(0xFF4D96FF),
                icon = "🛡️"
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmotionBarItem(
                label = "Fiziksel Yakınlık: ${emotion.getPhysicalComfortTierLabel()}",
                value = emotion.physicalComfortScore,
                maxValue = 100,
                color = Color(0xFFF59E0B),
                icon = "🤝"
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmotionBarItem(
                label = "Kırgınlık / Mesafe (Hurt)",
                value = emotion.hurt,
                maxValue = 100,
                color = Color(0xFF9333EA),
                icon = "💔"
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmotionBarItem(
                label = "Gerginlik / Stres (${emotion.tension})",
                value = when (emotion.tension.lowercase()) { "crisis" -> 100; "conflict" -> 50; else -> 0 },
                maxValue = 100,
                color = Color(0xFFFFB302),
                icon = "⚡"
            )

            // Obsession score - ONLY visible if >= 50
            if (emotion.obsession >= 50) {
                Spacer(modifier = Modifier.height(8.dp))
                EmotionBarItem(
                    label = "Takıntı / Bağımlılık (Obsession)",
                    value = emotion.obsession,
                    maxValue = 100,
                    color = Color(0xFFE11D48),
                    icon = "🖤"
                )
            }

            // User Defined Custom Emotions
            if (emotion.customEmotions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "✨ Özel Tanımlı Duygular",
                    color = Color(0xFFD8B4FE),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                emotion.customEmotions.forEach { ce ->
                    Spacer(modifier = Modifier.height(6.dp))
                    val labelText = if (ce.purpose.isNotBlank()) "${ce.name} (${ce.difficulty}) — ${ce.purpose}" else "${ce.name} (${ce.difficulty})"
                    EmotionBarItem(
                        label = labelText,
                        value = ce.currentValue.coerceIn(ce.minValue, ce.maxValue),
                        maxValue = ce.maxValue.coerceAtLeast(ce.minValue + 1),
                        color = Color(0xFFA855F7),
                        icon = "✨"
                    )
                }
            }

            // Secondary & Suppressed Emotions
            if (emotion.secondaryMood.isNotBlank() || emotion.suppressedEmotion.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1D30))
                        .padding(8.dp)
                ) {
                    if (emotion.secondaryMood.isNotBlank()) {
                        Text(
                            text = "🎭 İkincil Duygu: ${emotion.secondaryMood}",
                            color = EmochiTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (emotion.suppressedEmotion.isNotBlank()) {
                        if (emotion.secondaryMood.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔒 Bastırılmış İç Duygu: ${emotion.suppressedEmotion}",
                            color = Color(0xFFFF8A8A),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Mood Intensity & Resilience & Speech Pattern
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duygu Şiddeti: ${emotion.intensity}/10", color = EmochiTextMuted, fontSize = 11.sp)
                Text("Duygusal Direnç: ${emotion.resilience}/10", color = EmochiTextMuted, fontSize = 11.sp)
            }

            if (emotion.speechPattern.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🗣️ Konuşma Üslubu/Hızı: ${emotion.speechPattern}",
                    color = EmochiTextSecondary,
                    fontSize = 11.sp
                )
            }

            // Universe Mode Specifics
            if (isUniverse) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EmochiBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🌌 Evren & Atmosfer Simülasyonu",
                    color = Color(0xFF8B5CF6),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mekan: ${worldState.meso.currentLocation.ifBlank { "Bilinmiyor" }}", color = EmochiTextPrimary, fontSize = 11.5.sp)
                    Text("Gerilim: ${worldState.micro.sceneTension.uppercase()}", color = EmochiTextMuted, fontSize = 11.5.sp)
                }

                if (worldState.macro.eraRules.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🌐 Makro Kurallar: ${worldState.macro.eraRules}",
                        color = EmochiTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (worldState.meso.locationPersistentNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏢 Mekan Notları: ${worldState.meso.locationPersistentNotes}",
                        color = EmochiTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (worldState.micro.sceneMood.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚡ Sahnede Hava: ${worldState.micro.sceneMood}",
                        color = EmochiPrimary,
                        fontSize = 11.sp
                    )
                }

                // Character Emotions List & Manual Editor
                if (characterEmotions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "👥 Yan Karakterler Duygu Durumu (Düzenlenebilir)",
                        color = EmochiPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    var editingCharacter by remember { mutableStateOf<CharacterEmotionEntity?>(null) }

                    characterEmotions.forEach { charEntity ->
                        val charEmotion = remember(charEntity.emotionState) { EmotionState.fromJson(charEntity.emotionState) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B1D30))
                                .clickable {
                                    if (onUpdateCharacterEmotion != null) {
                                        editingCharacter = charEntity
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = charEntity.characterName + if (onUpdateCharacterEmotion != null) " ✏️" else "",
                                        color = EmochiTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (onDeleteCharacter != null) {
                                        IconButton(
                                            onClick = { onDeleteCharacter(charEntity.characterName) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("🗑️", fontSize = 11.sp)
                                        }
                                    }
                                }
                                Text(
                                    text = "${charEmotion.getMoodEmoji()} ${charEmotion.mood}",
                                    color = EmochiTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("❤️ Sevgi: ${charEmotion.affection}%", color = Color(0xFFFF6B81), fontSize = 10.sp)
                                Text("🛡️ Güven: ${charEmotion.trust}%", color = Color(0xFF4D96FF), fontSize = 10.sp)
                                Text("⚡ Gerginlik: ${charEmotion.tension}%", color = Color(0xFFFFB302), fontSize = 10.sp)
                            }
                            if (charEmotion.customEmotions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✨ Özel: " + charEmotion.customEmotions.joinToString(", ") { ce -> "${ce.name}: ${ce.currentValue}/${ce.maxValue}" },
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    editingCharacter?.let { charEntity ->
                        val currentEmotion = remember(charEntity.emotionState) { EmotionState.fromJson(charEntity.emotionState) }
                        var editMood by remember { mutableStateOf(currentEmotion.mood) }
                        var editAffection by remember { mutableStateOf(currentEmotion.affection.toFloat()) }
                        var editTrust by remember { mutableStateOf(currentEmotion.trust.toFloat()) }
                        var editTension by remember { mutableStateOf(currentEmotion.tension.toIntOrNull() ?: 10) }

                        androidx.compose.ui.window.Dialog(onDismissRequest = { editingCharacter = null }) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "✏️ ${charEntity.characterName} Duygu Düzenleme",
                                        color = EmochiTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Ruh Hali (Mood)", color = EmochiTextSecondary, fontSize = 12.sp)
                                    androidx.compose.material3.OutlinedTextField(
                                        value = editMood,
                                        onValueChange = { editMood = it },
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                        colors = customTextFieldColors(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("❤️ Sevgi: ${editAffection.toInt()}%", color = Color(0xFFFF6B81), fontSize = 12.sp)
                                    androidx.compose.material3.Slider(
                                        value = editAffection,
                                        onValueChange = { editAffection = it },
                                        valueRange = 0f..100f
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("🛡️ Güven: ${editTrust.toInt()}%", color = Color(0xFF4D96FF), fontSize = 12.sp)
                                    androidx.compose.material3.Slider(
                                        value = editTrust,
                                        onValueChange = { editTrust = it },
                                        valueRange = 0f..100f
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("⚡ Gerginlik: $editTension%", color = Color(0xFFFFB302), fontSize = 12.sp)
                                    androidx.compose.material3.Slider(
                                        value = editTension.toFloat(),
                                        onValueChange = { editTension = it.toInt() },
                                        valueRange = 0f..100f
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        androidx.compose.material3.TextButton(onClick = { editingCharacter = null }) {
                                            Text("İptal", color = EmochiTextMuted)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        androidx.compose.material3.Button(
                                            onClick = {
                                                onUpdateCharacterEmotion?.invoke(
                                                    charEntity.characterName,
                                                    editMood,
                                                    editAffection.toInt(),
                                                    editTrust.toInt(),
                                                    editTension
                                                )
                                                editingCharacter = null
                                            },
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                        ) {
                                            Text("Kaydet", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val formattedDate = remember(bot.updatedAt) {
                try {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("tr"))
                    sdf.format(java.util.Date(if (bot.updatedAt > 0) bot.updatedAt else System.currentTimeMillis()))
                } catch (e: Exception) {
                    ""
                }
            }
            if (formattedDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "⏱️ Son Güncelleme: $formattedDate",
                        color = EmochiTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Relationship History Timeline Section
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(EmochiBorder)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B1D30))
                    .clickable { showTimeline = !showTimeline }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📜", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "İlişki Geçmişi / Zaman Çizelgesi (${affectionEvents.size})",
                        color = EmochiTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (showTimeline) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Aç/Kapat",
                    tint = EmochiTextSecondary
                )
            }

            if (showTimeline) {
                Spacer(modifier = Modifier.height(8.dp))
                if (affectionEvents.isEmpty()) {
                    Text(
                        text = "Henüz belirgin bir ilişki kırılması veya büyük yakınlık değişimi kaydedilmedi.",
                        color = EmochiTextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        affectionEvents.forEach { ev ->
                            val isPos = ev.scoreDelta > 0
                            val badgeColor = if (isPos) Color(0xFF10B981) else Color(0xFFEF4444)
                            val badgeText = if (isPos) "+${ev.scoreDelta}" else "${ev.scoreDelta}"
                            val dateStr = try {
                                val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("tr"))
                                sdf.format(java.util.Date(ev.timestamp))
                            } catch (e: Exception) { "" }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF161826))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = badgeColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ev.shortDescription,
                                        color = EmochiTextPrimary,
                                        fontSize = 11.sp
                                    )
                                    if (dateStr.isNotBlank()) {
                                        Text(
                                            text = dateStr,
                                            color = EmochiTextMuted,
                                            fontSize = 9.5.sp
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
private fun EmotionBarItem(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    icon: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, color = EmochiTextSecondary, fontSize = 11.sp)
            }
            Text("$value / $maxValue", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF181A2A)
        )
    }
}
