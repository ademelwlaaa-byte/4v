package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val EmochiBackground = Color(0xFF070510)
val EmochiSurface = Color(0xFF0D0A1C)
val EmochiCard = Color(0xFF140E2D)
val EmochiCardElevated = Color(0xFF1B143B)
val EmochiBorder = Color(0xFF221A4C)
val EmochiBorderFocused = Color(0xFF7C3AED)

val EmochiPrimary = Color(0xFFA78BFA)
val EmochiPrimaryContainer = Color(0xFF2E1065)
val EmochiOnPrimary = Color(0xFFFFFFFF)

val EmochiCoralStart = Color(0xFFFFB4A2)
val EmochiCoralEnd = Color(0xFFF7967E)
val EmochiUserBubbleText = Color(0xFF21130D)

val EmochiViolet = Color(0xFFC084FC)
val EmochiCyan = Color(0xFF38BDF8)
val EmochiRose = Color(0xFFFB7185)
val EmochiGold = Color(0xFFFBBF24)

val EmochiTextPrimary = Color(0xFFF4F1FA)
val EmochiTextSecondary = Color(0xFF9E92C9)
val EmochiTextMuted = Color(0xFF6B628F)
val EmochiError = Color(0xFFF87171)
val EmochiErrorContainer = Color(0xFF3D1A1A)

object EmochiGradients {
    val primaryButton = Brush.horizontalGradient(
        colors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC))
    )
    val userBubble = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFC0B2), Color(0xFFF7967E))
    )
    val botBubble = Brush.verticalGradient(
        colors = listOf(Color(0xFF191333), Color(0xFF100B24))
    )
    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF140F30), Color(0xFF0F0A24))
    )
    val heroHeader = Brush.linearGradient(
        colors = listOf(Color(0xFF1E1145), Color(0xFF0C091A), Color(0xFF070510))
    )
    val auraGlow = Brush.radialGradient(
        colors = listOf(Color(0x40A78BFA), Color(0x000F1021))
    )
    val glassBorder = Brush.linearGradient(
        colors = listOf(Color(0x60A78BFA), Color(0x2038BDF8), Color(0x102C2F54))
    )
    val goldBadge = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
    )
}

