package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = CleanMinPrimaryDark,
    onPrimary = CleanMinOnPrimaryDark,
    primaryContainer = CleanMinPrimaryContainerDark,
    onPrimaryContainer = CleanMinOnPrimaryContainerDark,
    background = CleanMinBackgroundDark,
    surface = CleanMinSurfaceDark,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E)
)

private val LightColorScheme = lightColorScheme(
    primary = CleanMinPrimary,
    onPrimary = CleanMinOnPrimary,
    primaryContainer = CleanMinPrimaryContainer,
    onPrimaryContainer = CleanMinOnPrimaryContainer,
    background = CleanMinBackground,
    surface = CleanMinSurface,
    onBackground = CleanMinOnBackground,
    onSurface = CleanMinOnSurface,
    onSurfaceVariant = CleanMinOnSurfaceVariant,
    outline = CleanMinOutline,
    outlineVariant = CleanMinOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}

