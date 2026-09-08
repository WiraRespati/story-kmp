package com.learn.story

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val themeMode by themeRepository.themeMode.collectAsState()
    val fontFamily by themeRepository.fontFamily.collectAsState()
    StoryTheme(themeMode = themeMode, fontFamily = fontFamily) {
        AppNavHost()
    }
}