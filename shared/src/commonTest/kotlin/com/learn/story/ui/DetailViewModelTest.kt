package com.learn.story.ui

import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeLocationRepository
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.detail.DetailViewModel
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
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var fakeLocationRepository: FakeLocationRepository
    private lateinit var viewModel: DetailViewModel

    private val sampleStory = Story(
        id = "story-456",
        name = "Dewi",
        description = "Liburan di Danau Toba",
        photoUrl = "https://example.com/toba.jpg",
        createdAt = "2024-02-01T08:00:00Z",
        lat = 2.6845,
        lon = 98.8756
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(sampleStory),
            initialBookmarks = setOf("story-456")
        )
        fakeLocationRepository = FakeLocationRepository(geocodeAddress = "Danau Toba, Sumatera Utara")
        viewModel = DetailViewModel(fakeStoryRepository, fakeLocationRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertNull(state.story)
        assertNull(state.addressName)
        assertFalse(state.isBookmarked)
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertFalse(state.isSuccess)
    }

    @Test
    fun testLoadDetail_success_populatesStoryAndAddress() = runTest {
        viewModel.loadDetail("story-456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.story)
        assertEquals("story-456", state.story.id)
        assertEquals("Dewi", state.story.name)
        assertTrue(state.isBookmarked)
        assertEquals("Danau Toba, Sumatera Utara", state.addressName)
        assertTrue(state.isSuccess)
        assertFalse(state.isEmpty)
        assertFalse(state.isLoading)
    }

    @Test
    fun testLoadDetail_error_setsErrorMessage() = runTest {
        fakeStoryRepository.storyDetailResult = ApiResult.Error("Story tidak ditemukan")

        viewModel.loadDetail("nonexistent")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.story)
        assertEquals("Story tidak ditemukan", state.errorMessage)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun testToggleBookmark_updatesState() = runTest {
        viewModel.loadDetail("story-456")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBookmarked)

        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isBookmarked)

        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isBookmarked)
    }
}
