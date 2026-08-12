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
import com.example.data.local.BotEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.WorldAtmosphere
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
    modifier: Modifier = Modifier
) {
    val emotion = remember(bot.emotionState) { EmotionState.fromJson(bot.emotionState) }
    val isUniverse = bot.mode == "universe"
    val worldAtmosphere = remember(bot.worldAtmosphere) { WorldAtmosphere.fromJson(bot.worldAtmosphere) }

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

            Spacer(modifier = Modifier.height(10.dp))

            // Main Metrics (Affection, Trust, Tension)
            EmotionBarItem(
                label = "Yakınlık / Sevgi (Affection)",
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
                label = "Gerginlik / Stres (Tension)",
                value = emotion.tension,
                maxValue = 100,
                color = Color(0xFFFFB302),
                icon = "⚡"
            )

            // Mood Intensity
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duygu Şiddeti (Intensity)", color = EmochiTextMuted, fontSize = 11.sp)
                Text("${emotion.intensity} / 10", color = EmochiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    text = "🌌 Dünya Atmosferi",
                    color = Color(0xFF8B5CF6),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Genel Hava: ${worldAtmosphere.getMoodEmoji()} ${worldAtmosphere.mood.replaceFirstChar { it.uppercase() }}", color = EmochiTextPrimary, fontSize = 11.5.sp)
                    Text("Şiddet: ${worldAtmosphere.intensity}/10", color = EmochiTextMuted, fontSize = 11.5.sp)
                }

                if (worldAtmosphere.currentEvent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mevcut Olay: ${worldAtmosphere.currentEvent}",
                        color = EmochiTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Character Emotions List
                if (characterEmotions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "👥 Yan Karakterler Duygu Durumu",
                        color = EmochiPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    characterEmotions.forEach { charEntity ->
                        val charEmotion = remember(charEntity.emotionState) { EmotionState.fromJson(charEntity.emotionState) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B1D30))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = charEntity.characterName,
                                    color = EmochiTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
