package com.learn.story.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.story.data.network.ApiResult
import com.learn.story.data.remote.GeocodingPlace
import com.learn.story.data.remote.GeocodingService
import com.learn.story.data.repository.StoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddStoryUiState(
    val photoBytes: ByteArray? = null,
    val description: String = "",
    val isGuest: Boolean = false,
    val lat: Double? = null,
    val lon: Double? = null,
    val locationName: String? = null,
    val isGeocoding: Boolean = false,
    val isSearchingLocation: Boolean = false,
    val locationSearchResults: List<GeocodingPlace> = emptyList(),
    val showLocationPicker: Boolean = false,
    val isLoading: Boolean = false,
    val descriptionError: String? = null,
    val photoError: String? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val savedOffline: Boolean = false
)

class AddStoryViewModel(
    private val storyRepository: StoryRepository,
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStoryUiState())
    val uiState: StateFlow<AddStoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onPhotoSelected(bytes: ByteArray?) {
        _uiState.update {
            it.copy(
                photoBytes = bytes,
                photoError = null,
                errorMessage = null
            )
        }
    }

    fun onDescriptionChanged(text: String) {
        _uiState.update {
            it.copy(
                description = text,
                descriptionError = null,
                errorMessage = null
            )
        }
    }

    fun onGuestToggled(isGuest: Boolean) {
        _uiState.update { it.copy(isGuest = isGuest) }
    }

    fun onLocationSelected(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                lat = lat,
                lon = lon,
                isGeocoding = true,
                showLocationPicker = false,
                locationSearchResults = emptyList()
            )
        }

        viewModelScope.launch {
            val address = geocodingService.reverseGeocode(lat, lon)
            _uiState.update {
                it.copy(
                    locationName = address,
                    isGeocoding = false
                )
            }
        }
    }

    fun onSearchLocationQuery(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 3) {
            _uiState.update { it.copy(locationSearchResults = emptyList(), isSearchingLocation = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce for OSM policy
            _uiState.update { it.copy(isSearchingLocation = true) }
            val results = geocodingService.searchPlaces(query)
            _uiState.update {
                it.copy(
                    locationSearchResults = results,
                    isSearchingLocation = false
                )
            }
        }
    }

    fun clearLocation() {
        _uiState.update {
            it.copy(
                lat = null,
                lon = null,
                locationName = null,
                locationSearchResults = emptyList()
            )
        }
    }

    fun setShowLocationPicker(show: Boolean) {
        _uiState.update { it.copy(showLocationPicker = show) }
    }

    private fun validate(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.photoBytes == null || state.photoBytes.isEmpty()) {
            _uiState.update { it.copy(photoError = "Pilih foto terlebih dahulu") }
            isValid = false
        } else if (state.photoBytes.size > 1_000_000) {
            _uiState.update { it.copy(photoError = "Ukuran foto melebihi batas 1MB") }
            isValid = false
        }

        if (state.description.trim().isEmpty()) {
            _uiState.update { it.copy(descriptionError = "Deskripsi tidak boleh kosong") }
            isValid = false
        }

        return isValid
    }

    fun uploadStory(onSuccess: () -> Unit) {
        if (!validate()) return

        val state = _uiState.value
        val bytes = state.photoBytes ?: return
        val desc = state.description.trim()

        viewModelScope.launch {
            val flow = if (state.isGuest) {
                storyRepository.uploadGuestStory(
                    description = desc,
                    photoBytes = bytes,
                    lat = state.lat,
                    lon = state.lon
                )
            } else {
                storyRepository.uploadStory(
                    description = desc,
                    photoBytes = bytes,
                    lat = state.lat,
                    lon = state.lon
                )
            }

            flow.collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        onSuccess()
                    }
                    is ApiResult.Error -> {
                        // Simpan otomatis ke antrean draft offline jika koneksi gagal
                        storyRepository.saveOfflineDraft(
                            id = "draft_${kotlin.time.TimeSource.Monotonic.markNow().hashCode()}",
                            description = desc,
                            photoBytes = bytes,
                            lat = state.lat,
                            lon = state.lon,
                            isGuest = state.isGuest,
                            locationName = state.locationName
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                savedOffline = true,
                                errorMessage = "Gagal terhubung (${result.message}). Story berhasil disimpan ke antrean offline!"
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveDraftManually(onSuccess: () -> Unit) {
        if (!validate()) return
        val state = _uiState.value
        val bytes = state.photoBytes ?: return
        val desc = state.description.trim()

        viewModelScope.launch {
            storyRepository.saveOfflineDraft(
                id = "draft_${kotlin.time.TimeSource.Monotonic.markNow().hashCode()}",
                description = desc,
                photoBytes = bytes,
                lat = state.lat,
                lon = state.lon,
                isGuest = state.isGuest,
                locationName = state.locationName
            )
            _uiState.update { it.copy(savedOffline = true) }
            onSuccess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

