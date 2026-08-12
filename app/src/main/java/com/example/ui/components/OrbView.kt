package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object MoodColors {
    fun getMoodHue(mood: String): Float {
        return when (mood.lowercase()) {
            "joy", "neşeli" -> 38f
            "warm", "sıcak" -> 340f
            "sad", "hüzünlü" -> 250f
            "tense", "gergin" -> 8f
            "curious", "meraklı" -> 275f
            else -> 200f // calm / sakin
        }
    }

    fun getMoodLabel(mood: String): String {
        return when (mood.lowercase()) {
            "joy" -> "neşeli"
            "warm" -> "sıcak"
            "sad" -> "hüzünlü"
            "tense" -> "gergin"
            "curious" -> "meraklı"
            else -> "sakin"
        }
    }
}

@Composable
fun OrbView(
    hue: Float,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbAnimations")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val coreColor = Color.hsl(hue = hue, saturation = 0.85f, lightness = 0.75f)
    val outerColor = Color.hsl(hue = hue, saturation = 0.65f, lightness = 0.45f)
    val glowColor = Color.hsl(hue = hue, saturation = 0.80f, lightness = 0.65f, alpha = 0.40f)
    val ringColor = Color.hsl(hue = (hue + 30f) % 360f, saturation = 0.90f, lightness = 0.80f, alpha = 0.60f)
    val specularHighlight = Color.White.copy(alpha = 0.75f)

    Canvas(modifier = modifier.size(size)) {
        val baseRadius = (this.size.minDimension / 2.2f)
        val radius = baseRadius * pulseScale
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // 1. Ambient Outer Radial Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = center,
                radius = radius * 1.7f
            ),
            radius = radius * 1.7f,
            center = center
        )

        // 2. Rotating Orbital Pulse Ring
        val rad = Math.toRadians(rotationAngle.toDouble())
        val ringOffset = Offset(
            x = center.x + (cos(rad) * radius * 0.15f).toFloat(),
            y = center.y + (sin(rad) * radius * 0.15f).toFloat()
        )
        drawCircle(
            color = ringColor,
            radius = radius * 1.15f,
            center = ringOffset,
            style = Stroke(width = 1.8.dp.toPx())
        )

        // 3. Main Core Sphere with Gradient Depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(coreColor, outerColor, Color.hsl(hue = hue, saturation = 0.7f, lightness = 0.25f)),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 1.2f
            ),
            radius = radius,
            center = center
        )

        // 4. Specular Glass Highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(specularHighlight, Color.Transparent),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 0.45f
            ),
            radius = radius * 0.4f,
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
        )
    }
}

