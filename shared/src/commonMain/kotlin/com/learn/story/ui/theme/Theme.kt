package com.learn.story.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DicodingPrimary,
    onPrimary = LightSurface,
    primaryContainer = DicodingPrimaryDark,
    onPrimaryContainer = LightSurface,
    secondary = DicodingSecondary,
    onSecondary = LightSurface,
    tertiary = DicodingTertiary,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = DicodingError,
    onError = LightSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DicodingSecondary,
    onPrimary = DarkBackground,
    primaryContainer = DicodingPrimary,
    onPrimaryContainer = DarkOnSurface,
    secondary = DicodingSecondary,
    onSecondary = DarkBackground,
    tertiary = DicodingTertiary,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DicodingError,
    onError = DarkSurface
)

enum class AppThemeMode(
    val code: String,
    val title: String,
    val subtitle: String,
    val icon: String
) {
    SYSTEM("system", "Default Sistem", "Otomatis ikuti tema HP", "⚙️"),
    LIGHT("light", "Mode Terang", "Cerah & nyaman di siang hari", "☀️"),
    DARK("dark", "Mode Gelap", "Kontras & hemat baterai", "🌙");

    companion object {
        fun fromCode(code: String): AppThemeMode =
            entries.find { it.code == code } ?: SYSTEM
    }
}

@Composable
fun StoryTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    fontFamily: AppFontFamily = AppFontFamily.POPPINS,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(family = fontFamily),
        content = content
    )
}

@Composable
fun StoryTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    StoryTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        fontFamily = AppFontFamily.POPPINS,
        content = content
    )
}


