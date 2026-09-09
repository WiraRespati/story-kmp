package com.learn.story.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.AuthRepository
import com.learn.story.data.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.learn.story.util.AppConstants

data class HomeUiState(
    val stories: List<Story> = emptyList(),
    val filteredStories: List<Story> = emptyList(),
    val searchQuery: String = "",
    val showOnlyWithLocation: Boolean = false,
    val showOnlyBookmarks: Boolean = false,
    val bookmarkedIds: Set<String> = emptySet(),
    val offlineDrafts: List<com.learn.story.data.model.OfflineStoryDraft> = emptyList(),
    val offlineDraftsCount: Int = 0,
    val isSyncingDrafts: Boolean = false,
    val syncMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String? = null,
    val isLoggedOut: Boolean = false
)

class HomeViewModel(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                userName = authRepository.getUserName()
            )
        }
        // Offline-first: tampilkan cache SQLite lokal secara instan
        viewModelScope.launch {
            storyRepository.cachedStoriesFlow.collect { cached ->
                if (cached.isNotEmpty() && _uiState.value.stories.isEmpty()) {
                    _uiState.update { current ->
                        current.copy(
                            stories = cached,
                            filteredStories = applyFilters(
                                list = cached,
                                query = current.searchQuery,
                                onlyLocation = current.showOnlyWithLocation,
                                onlyBookmarks = current.showOnlyBookmarks,
                                bookmarks = current.bookmarkedIds
                            )
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            storyRepository.bookmarkedIdsFlow.collect { bookmarks ->
                _uiState.update { current ->
                    current.copy(
                        bookmarkedIds = bookmarks,
                        filteredStories = applyFilters(
                            list = current.stories,
                            query = current.searchQuery,
                            onlyLocation = current.showOnlyWithLocation,
                            onlyBookmarks = current.showOnlyBookmarks,
                            bookmarks = bookmarks
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            storyRepository.offlineDraftsFlow.collect { drafts ->
                _uiState.update { current ->
                    current.copy(
                        offlineDrafts = drafts,
                        offlineDraftsCount = drafts.size
                    )
                }
            }
        }
        loadStories()

        // Auto-refresh: begitu add story berhasil, otomatis hit loadStories()
        viewModelScope.launch {
            storyRepository.storyUploadedEvent.collect {
                loadStories()
            }
        }
    }

    fun loadStories() {
        viewModelScope.launch {
            storyRepository.getStories(page = 1, size = AppConstants.DEFAULT_PAGE_SIZE, location = 0).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is ApiResult.Success -> {
                        _uiState.update { current ->
                            val updatedStories = result.data
                            current.copy(
                                isLoading = false,
                                stories = updatedStories,
                                filteredStories = applyFilters(
                                    list = updatedStories,
                                    query = current.searchQuery,
                                    onlyLocation = current.showOnlyWithLocation,
                                    onlyBookmarks = current.showOnlyBookmarks,
                                    bookmarks = current.bookmarkedIds
                                ),
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                errorMessage = if (current.stories.isEmpty()) result.message else null
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredStories = applyFilters(
                    list = current.stories,
                    query = query,
                    onlyLocation = current.showOnlyWithLocation,
                    onlyBookmarks = current.showOnlyBookmarks,
                    bookmarks = current.bookmarkedIds
                )
            )
        }
    }

    fun onToggleLocationFilter() {
        _uiState.update { current ->
            val newValue = !current.showOnlyWithLocation
            current.copy(
                showOnlyWithLocation = newValue,
                filteredStories = applyFilters(
                    list = current.stories,
                    query = current.searchQuery,
                    onlyLocation = newValue,
                    onlyBookmarks = current.showOnlyBookmarks,
                    bookmarks = current.bookmarkedIds
                )
            )
        }
    }

    fun onToggleBookmarksFilter() {
        _uiState.update { current ->
            val newValue = !current.showOnlyBookmarks
            current.copy(
                showOnlyBookmarks = newValue,
                filteredStories = applyFilters(
                    list = current.stories,
                    query = current.searchQuery,
                    onlyLocation = current.showOnlyWithLocation,
                    onlyBookmarks = newValue,
                    bookmarks = current.bookmarkedIds
                )
            )
        }
    }

    fun toggleBookmark(storyId: String) {
        viewModelScope.launch {
            storyRepository.toggleBookmark(storyId)
        }
    }

    fun checkOfflineDrafts() {
        viewModelScope.launch {
            val drafts = storyRepository.getOfflineDrafts()
            _uiState.update { it.copy(offlineDraftsCount = drafts.size, offlineDrafts = drafts) }
        }
    }

    fun deleteOfflineDraft(draftId: String) {
        viewModelScope.launch {
            storyRepository.removeOfflineDraft(draftId)
        }
    }

    fun syncOfflineDrafts() {
        viewModelScope.launch {
            val drafts = storyRepository.getOfflineDrafts()
            if (drafts.isEmpty()) return@launch

            _uiState.update { it.copy(isSyncingDrafts = true) }
            val successCount = storyRepository.syncOfflineDrafts()
            val remainingDrafts = storyRepository.getOfflineDrafts()
            _uiState.update {
                it.copy(
                    isSyncingDrafts = false,
                    offlineDrafts = remainingDrafts,
                    offlineDraftsCount = remainingDrafts.size,
                    syncMessage = if (successCount > 0) "Berhasil mengunggah $successCount story offline!" else null
                )
            }
            if (successCount > 0) {
                loadStories()
            }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    private fun applyFilters(
        list: List<Story>,
        query: String,
        onlyLocation: Boolean,
        onlyBookmarks: Boolean,
        bookmarks: Set<String>
    ): List<Story> {
        return list.filter { story ->
            val matchesQuery = query.isBlank() ||
                    story.name.contains(query, ignoreCase = true) ||
                    story.description.contains(query, ignoreCase = true)

            val matchesLocation = !onlyLocation || (story.lat != null && story.lon != null)
            val matchesBookmark = !onlyBookmarks || bookmarks.contains(story.id)

            matchesQuery && matchesLocation && matchesBookmark
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun resetLoggedOut() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }
}

