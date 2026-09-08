package com.learn.story.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.data.remote.GeocodingService
import com.learn.story.data.repository.StoryRepository
import com.learn.story.ui.components.map.MapMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoryMapUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val storiesWithLocation: List<Story> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
    val selectedStory: Story? = null,
    val selectedStoryAddress: String? = null
)

class StoryMapViewModel(
    private val storyRepository: StoryRepository,
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryMapUiState())
    val uiState: StateFlow<StoryMapUiState> = _uiState.asStateFlow()

    init {
        loadLocationStories()
    }

    fun loadLocationStories() {
        viewModelScope.launch {
            storyRepository.getStories(page = 1, size = 100, location = 1).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is ApiResult.Success -> {
                        val validStories = result.data.filter { it.lat != null && it.lon != null }
                        val markers = validStories.map { story ->
                            MapMarker(
                                id = story.id,
                                title = story.name,
                                snippet = story.description.take(80) + if (story.description.length > 80) "..." else "",
                                lat = story.lat!!,
                                lon = story.lon!!,
                                photoUrl = story.photoUrl
                            )
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                storiesWithLocation = validStories,
                                markers = markers,
                                errorMessage = null
                            )
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

    fun selectStoryById(storyId: String) {
        val story = _uiState.value.storiesWithLocation.find { it.id == storyId }
        _uiState.update { it.copy(selectedStory = story, selectedStoryAddress = null) }

        if (story?.lat != null && story.lon != null) {
            viewModelScope.launch {
                val address = geocodingService.reverseGeocode(story.lat, story.lon)
                _uiState.update { it.copy(selectedStoryAddress = address) }
            }
        }
    }

    fun dismissSelectedStory() {
        _uiState.update { it.copy(selectedStory = null, selectedStoryAddress = null) }
    }
}
