package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TypingDots(
    dotSize: Dp = 6.dp,
    dotColor: Color = Color(0xFF8B7FB8),
    modifier: Modifier = Modifier
) {
    val delays = listOf(0, 150, 300)

    Row(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        delays.forEach { delayMs ->
            val offsetY = remember { Animatable(0f) }
            val alpha = remember { Animatable(0.3f) }

            LaunchedEffect(key1 = delayMs) {
                delay(delayMs.toLong())
                offsetY.animateTo(
                    targetValue = -3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }

            LaunchedEffect(key1 = delayMs) {
                delay(delayMs.toLong())
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = offsetY.value.dp)
                    .alpha(alpha.value)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}
