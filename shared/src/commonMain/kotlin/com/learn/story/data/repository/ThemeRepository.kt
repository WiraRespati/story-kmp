package com.learn.story.data.repository

import com.learn.story.ui.theme.AppFontFamily
import com.learn.story.ui.theme.AppThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(
    private val settings: Settings = Settings()
) {
    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _fontFamily = MutableStateFlow(getSavedFontFamily())
    val fontFamily: StateFlow<AppFontFamily> = _fontFamily.asStateFlow()

    private fun getSavedThemeMode(): AppThemeMode {
        val code = settings.getStringOrNull(KEY_THEME_MODE) ?: AppThemeMode.SYSTEM.code
        return AppThemeMode.fromCode(code)
    }

    fun setThemeMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.code)
        _themeMode.value = mode
    }

    private fun getSavedFontFamily(): AppFontFamily {
        val code = settings.getStringOrNull(KEY_FONT_FAMILY) ?: AppFontFamily.POPPINS.code
        return AppFontFamily.fromCode(code)
    }

    fun setFontFamily(family: AppFontFamily) {
        settings.putString(KEY_FONT_FAMILY, family.code)
        _fontFamily.value = family
    }

    companion object {
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_FONT_FAMILY = "app_font_family"
    }
}
