package com.example.boardcut.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.boardcut.data.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = WoodPrimaryDark,
    onPrimary = Color(0xFF2C1600),
    primaryContainer = Color(0xFF452206),
    onPrimaryContainer = Color(0xFFFFDCC1),
    secondary = WoodSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3F2000),
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = WoodTertiary,
    background = WorkshopCharcoal,
    onBackground = TextPrimaryDark,
    surface = WorkshopSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = WorkshopCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = WorkshopBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = WoodPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC1),
    onPrimaryContainer = Color(0xFF321200),
    secondary = WoodSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB8),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = WoodTertiary,
    background = WarmWhiteBg,
    onBackground = TextPrimaryLight,
    surface = WarmWhiteSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = WarmWhiteCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = WarmWhiteBorder
)

@Composable
fun BoardCutTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
