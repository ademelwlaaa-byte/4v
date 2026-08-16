package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val coreColor = Color.hsl(hue = hue, saturation = 0.85f, lightness = 0.75f)
    val outerColor = Color.hsl(hue = hue, saturation = 0.65f, lightness = 0.45f)
    val darkEdge = Color.hsl(hue = hue, saturation = 0.70f, lightness = 0.20f)
    val specularHighlight = Color.White.copy(alpha = 0.85f)
    val ringColor = Color.hsl(hue = (hue + 30f) % 360f, saturation = 0.90f, lightness = 0.80f, alpha = 0.50f)

    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // 1. Base Radial Gradient Sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(coreColor, outerColor, darkEdge),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                radius = radius * 1.3f
            ),
            radius = radius,
            center = center
        )

        // 2. Subtle Accent Ring
        drawCircle(
            color = ringColor,
            radius = radius * 0.88f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 3. Specular Glass Highlight
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

