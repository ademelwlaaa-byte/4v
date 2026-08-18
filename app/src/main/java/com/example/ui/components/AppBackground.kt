package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmochiPrimary
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radiusPx: Float,
    val baseAlpha: Float,
    val phaseOffset: Float,
    val color: Color
)

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF0A0A14)
) {
    // Fixed seed random for deterministic star positions across compositions
    val stars = remember {
        val rand = Random(42)
        val list = mutableListOf<StarParticle>()
        val starColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFD8B4FE), // Light lavender
            Color(0xFFBAE6FD), // Light cyan
            Color(0xFFFDE68A)  // Soft warm gold
        )
        for (i in 0 until 75) {
            val x = rand.nextFloat()
            val y = rand.nextFloat()
            val radius = rand.nextFloat() * 3.5f + 2f // 2dp to 5.5dp radius
            val baseAlpha = rand.nextFloat() * 0.30f + 0.10f // 10% to 40% opacity
            val phase = rand.nextFloat() * (2f * PI.toFloat())
            val color = starColors[i % starColors.size]
            list.add(StarParticle(x, y, radius, baseAlpha, phase, color))
        }
        list
    }

    // Gentle infinite twinkle animation
    val infiniteTransition = rememberInfiniteTransition(label = "StarTwinkle")
    val twinkleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TwinklePhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            // 1. Top-center soft radial glow
            val centerGlow = Offset(width * 0.5f, height * 0.22f)
            val glowRadius = width * 0.90f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EmochiPrimary.copy(alpha = 0.12f),
                        Color(0xFF130E2A).copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = centerGlow,
                    radius = glowRadius
                ),
                center = centerGlow,
                radius = glowRadius
            )

            // 2. Deterministic stars with subtle phase-shifted twinkle
            stars.forEach { star ->
                val px = star.xRatio * width
                val py = star.yRatio * height
                val phase = twinkleTime + star.phaseOffset
                val modulation = 0.7f + 0.3f * sin(phase)
                val currentAlpha = (star.baseAlpha * modulation).coerceIn(0.05f, 0.45f)

                drawCircle(
                    color = star.color.copy(alpha = currentAlpha),
                    radius = star.radiusPx,
                    center = Offset(px, py)
                )
            }
        }
    }
}
