package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EmochiDarkColorScheme = darkColorScheme(
    primary = EmochiPrimary,
    onPrimary = EmochiOnPrimary,
    primaryContainer = EmochiPrimaryContainer,
    background = EmochiBackground,
    onBackground = EmochiTextPrimary,
    surface = EmochiSurface,
    onSurface = EmochiTextPrimary,
    surfaceVariant = EmochiCard,
    onSurfaceVariant = EmochiTextSecondary,
    outline = EmochiBorder,
    outlineVariant = EmochiBorderFocused,
    error = EmochiError,
    onError = EmochiErrorContainer
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmochiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
