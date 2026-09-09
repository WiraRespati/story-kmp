package com.learn.story.ui

import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.saved.SavedStoriesViewModel
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
class SavedStoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var viewModel: SavedStoriesViewModel

    private val sampleStory = Story(
        id = "s-saved-1",
        name = "Saved Story",
        description = "Deskripsi",
        photoUrl = "https://example.com/img.jpg",
        createdAt = "2024-01-01T00:00:00Z",
        lat = null,
        lon = null
    )

    private val sampleDraft = OfflineStoryDraft(
        id = "d-1",
        description = "Draft 1",
        photoBase64 = "base64",
        lat = null,
        lon = null,
        isGuest = false,
        createdAtEpoch = 1000L,
        locationName = null
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(sampleStory),
            initialBookmarks = setOf("s-saved-1"),
            initialDrafts = listOf(sampleDraft)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_loadsBookmarksAndDrafts() = runTest {
        viewModel = SavedStoriesViewModel(fakeStoryRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.bookmarkedStories.size)
        assertEquals("s-saved-1", state.bookmarkedStories.first().id)
        assertEquals(1, state.offlineDrafts.size)
        assertEquals("d-1", state.offlineDrafts.first().id)
        assertFalse(state.isEmpty)
        assertFalse(state.isSyncing)
        assertNull(state.syncMessage)
    }

    @Test
    fun testEmptyState_whenNoBookmarksAndNoDrafts() = runTest {
        fakeStoryRepository = FakeStoryRepository(
            initialStories = emptyList(),
            initialBookmarks = emptySet(),
            initialDrafts = emptyList()
        )
        viewModel = SavedStoriesViewModel(fakeStoryRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.bookmarkedStories.isEmpty())
        assertTrue(state.offlineDrafts.isEmpty())
        assertTrue(state.isEmpty)
    }

    @Test
    fun testDeleteOfflineDraft_removesFromRepository() = runTest {
        viewModel = SavedStoriesViewModel(fakeStoryRepository)
        advanceUntilIdle()

        viewModel.deleteOfflineDraft("d-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.offlineDrafts.isEmpty())
    }

    @Test
    fun testSyncOfflineDrafts_syncsAndSetsMessage() = runTest {
        viewModel = SavedStoriesViewModel(fakeStoryRepository)
        advanceUntilIdle()

        viewModel.syncOfflineDrafts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSyncing)
        assertNotNull(state.syncMessage)
        assertTrue(state.syncMessage.contains("Berhasil mengunggah 1 draft offline!"))

        viewModel.clearSyncMessage()
        assertNull(viewModel.uiState.value.syncMessage)
    }
}
