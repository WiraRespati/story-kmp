package com.learn.story.ui

import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeLocationRepository
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.map.StoryMapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StoryMapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var fakeLocationRepository: FakeLocationRepository
    private lateinit var viewModel: StoryMapViewModel

    private val storyWithLocation = Story(
        id = "map-1",
        name = "Jakarta Spot",
        description = "Deskripsi titik Jakarta",
        photoUrl = "https://example.com/jkt.jpg",
        createdAt = "2024-01-01T00:00:00Z",
        lat = -6.2088,
        lon = 106.8456
    )

    private val storyWithoutLocation = Story(
        id = "map-2",
        name = "Tanpa Lokasi",
        description = "Deskripsi tanpa koordinat",
        photoUrl = "https://example.com/noloc.jpg",
        createdAt = "2024-01-02T00:00:00Z",
        lat = null,
        lon = null
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(storyWithLocation, storyWithoutLocation)
        )
        fakeLocationRepository = FakeLocationRepository(geocodeAddress = "Jakarta Pusat, DKI Jakarta")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInit_filtersOnlyStoriesWithCoordinatesAndGeneratesMarkers() = runTest {
        viewModel = StoryMapViewModel(fakeStoryRepository, fakeLocationRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.storiesWithLocation.size)
        assertEquals("map-1", state.storiesWithLocation.first().id)
        assertEquals(1, state.markers.size)
        assertEquals("Jakarta Spot", state.markers.first().title)
        assertEquals(-6.2088, state.markers.first().lat)
        assertEquals(106.8456, state.markers.first().lon)
        assertFalse(state.isEmpty)
    }

    @Test
    fun testInit_emptyLocations_setsEmptyTrue() = runTest {
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(storyWithoutLocation)
        )
        viewModel = StoryMapViewModel(fakeStoryRepository, fakeLocationRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.storiesWithLocation.isEmpty())
        assertTrue(state.markers.isEmpty())
        assertTrue(state.isEmpty)
    }

    @Test
    fun testInit_error_setsErrorMessage() = runTest {
        fakeStoryRepository = FakeStoryRepository(initialStories = emptyList())
        fakeStoryRepository.storiesResult = ApiResult.Error("Gagal mengambil data peta")

        viewModel = StoryMapViewModel(fakeStoryRepository, fakeLocationRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Gagal mengambil data peta", state.errorMessage)
    }

    @Test
    fun testSelectStoryById_andDismiss() = runTest {
        viewModel = StoryMapViewModel(fakeStoryRepository, fakeLocationRepository)
        advanceUntilIdle()

        viewModel.selectStoryById("map-1")
        advanceUntilIdle()

        val selectedState = viewModel.uiState.value
        assertNotNull(selectedState.selectedStory)
        assertEquals("map-1", selectedState.selectedStory.id)
        assertEquals("Jakarta Pusat, DKI Jakarta", selectedState.selectedStoryAddress)

        viewModel.dismissSelectedStory()
        assertNull(viewModel.uiState.value.selectedStory)
        assertNull(viewModel.uiState.value.selectedStoryAddress)
    }
}
