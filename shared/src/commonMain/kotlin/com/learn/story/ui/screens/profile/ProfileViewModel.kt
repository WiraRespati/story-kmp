package com.learn.story.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.repository.AuthRepository
import com.learn.story.data.repository.StoryRepository
import com.learn.story.data.repository.ThemeRepository
import com.learn.story.ui.theme.AppFontFamily
import com.learn.story.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName: String = "Pengguna Story",
    val feedCount: Int = 0,
    val bookmarksCount: Int = 0,
    val draftsCount: Int = 0,
    val isSyncing: Boolean = false
)

class ProfileViewModel(
    private val themeRepository: ThemeRepository,
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = themeRepository.themeMode
    val fontFamily: StateFlow<AppFontFamily> = themeRepository.fontFamily

    private val _isSyncing = MutableStateFlow(false)
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        storyRepository.cachedStoriesFlow,
        storyRepository.bookmarkedStoriesFlow,
        storyRepository.offlineDraftsFlow,
        _isSyncing
    ) { cached, bookmarked, drafts, syncing ->
        ProfileUiState(
            userName = authRepository.getUserName() ?: "Pengguna Story",
            feedCount = cached.size,
            bookmarksCount = bookmarked.size,
            draftsCount = drafts.size,
            isSyncing = syncing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(userName = authRepository.getUserName() ?: "Pengguna Story")
    )

    fun setThemeMode(mode: AppThemeMode) {
        themeRepository.setThemeMode(mode)
    }

    fun setFontFamily(font: AppFontFamily) {
        themeRepository.setFontFamily(font)
    }

    fun syncOfflineDrafts() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                storyRepository.syncOfflineDrafts()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            storyRepository.getStories(page = 1, size = 20, location = 0).collect { }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedOut.value = true
        }
    }
}
