package com.learn.story.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.learn.story.data.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedStoriesUiState(
    val bookmarkedStories: List<Story> = emptyList(),
    val offlineDrafts: List<OfflineStoryDraft> = emptyList(),
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

class SavedStoriesViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedStoriesUiState())
    val uiState: StateFlow<SavedStoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            storyRepository.bookmarkedStoriesFlow.collect { stories ->
                _uiState.update { it.copy(bookmarkedStories = stories) }
            }
        }
        viewModelScope.launch {
            storyRepository.offlineDraftsFlow.collect { drafts ->
                _uiState.update { it.copy(offlineDrafts = drafts) }
            }
        }
    }

    fun toggleBookmark(storyId: String) {
        viewModelScope.launch {
            storyRepository.toggleBookmark(storyId)
        }
    }

    fun deleteOfflineDraft(draftId: String) {
        viewModelScope.launch {
            storyRepository.removeOfflineDraft(draftId)
        }
    }

    fun syncOfflineDrafts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val count = storyRepository.syncOfflineDrafts()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncMessage = if (count > 0) "Berhasil mengunggah $count draft offline!" else "Tidak ada draft baru yang diunggah"
                )
            }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }
}
