package com.learn.story

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learn.story.data.repository.ThemeRepository
import com.learn.story.di.appModule
import com.learn.story.ui.navigation.AppNavHost
import com.learn.story.ui.theme.StoryTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(koinConfiguration {
        modules(appModule)
    }) {
        AppContent()
    }
}

@Composable
private fun AppContent(
    themeRepository: ThemeRepository = koinInject()
) {
    val themeMode by themeRepository.themeMode.collectAsStateWithLifecycle()
    val fontFamily by themeRepository.fontFamily.collectAsStateWithLifecycle()
    StoryTheme(themeMode = themeMode, fontFamily = fontFamily) {
        AppNavHost()
    }
}