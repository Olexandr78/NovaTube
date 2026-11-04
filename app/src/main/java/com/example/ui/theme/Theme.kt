package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaTubeColorScheme = darkColorScheme(
    primary = CrimsonPulse,
    onPrimary = Color.White,
    primaryContainer = CrimsonSoft,
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = CyberCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = CyberCyanSoft,
    onSecondaryContainer = Color(0xFFB8F7FF),
    tertiary = AmberVu,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberVuSoft,
    onTertiaryContainer = Color(0xFFFFECB3),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaTubeColorScheme,
        typography = Typography,
        content = content
    )
}
