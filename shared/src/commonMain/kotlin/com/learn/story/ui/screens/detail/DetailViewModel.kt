package com.learn.story.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.LocationRepository
import com.learn.story.data.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val story: Story? = null,
    val addressName: String? = null,
    val isBookmarked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isInitialLoading: Boolean get() = isLoading && story == null
    val isEmpty: Boolean get() = !isLoading && story == null && errorMessage == null
    val isSuccess: Boolean get() = story != null
}

class DetailViewModel(
    private val storyRepository: StoryRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(id: String) {
        viewModelScope.launch {
            storyRepository.getStoryDetail(id).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is ApiResult.Success -> {
                        val story = result.data
                        val isBookmarked = storyRepository.isStoryBookmarked(story.id)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                story = story,
                                isBookmarked = isBookmarked,
                                errorMessage = null
                            )
                        }

                        if (story.lat != null && story.lon != null) {
                            val addr = locationRepository.reverseGeocode(story.lat, story.lon)
                            _uiState.update { it.copy(addressName = addr) }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleBookmark() {
        val storyId = _uiState.value.story?.id ?: return
        viewModelScope.launch {
            val newState = storyRepository.toggleBookmark(storyId)
            _uiState.update { it.copy(isBookmarked = newState) }
        }
    }
}

