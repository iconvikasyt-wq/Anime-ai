package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AnimeColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = AnimeBorderGlow,
    onPrimaryContainer = NeonPurpleGlow,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0E3A4A),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = AnimeDarkBg,
    onBackground = TextPrimary,
    surface = AnimeCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = AnimeCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = AnimeBorderGlow
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AnimeColorScheme,
        typography = Typography,
        content = content
    )
}
