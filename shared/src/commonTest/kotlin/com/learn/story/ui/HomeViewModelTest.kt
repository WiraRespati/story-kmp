package com.learn.story.ui

import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeAuthRepository
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.home.HomeViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: HomeViewModel

    private val story1 = Story(
        id = "s1",
        name = "Alice",
        description = "Liburan seru di Bandung",
        photoUrl = "https://example.com/1.jpg",
        createdAt = "2024-01-01T00:00:00Z",
        lat = -6.9175,
        lon = 107.6191
    )

    private val story2 = Story(
        id = "s2",
        name = "Bob",
        description = "Kopi santai di Jakarta",
        photoUrl = "https://example.com/2.jpg",
        createdAt = "2024-01-02T00:00:00Z",
        lat = null,
        lon = null
    )

    private val story3 = Story(
        id = "s3",
        name = "Charlie",
        description = "Sunset di Pantai Kuta Bali",
        photoUrl = "https://example.com/3.jpg",
        createdAt = "2024-01-03T00:00:00Z",
        lat = -8.7185,
        lon = 115.1686
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository(initialUserName = "Budi Santoso")
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(story1, story2, story3),
            initialBookmarks = setOf("s1")
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_loadsUserNameAndCachedStories() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Budi Santoso", state.userName)
        assertEquals(3, state.stories.size)
        assertEquals(3, state.filteredStories.size)
        assertTrue(state.bookmarkedIds.contains("s1"))
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertFalse(state.isInitialLoading)
    }

    @Test
    fun testLoadStories_successWithData() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        fakeStoryRepository.storiesResult = ApiResult.Success(listOf(story1, story3))
        viewModel.loadStories()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.stories.size)
        assertEquals(2, state.filteredStories.size)
        assertFalse(state.isEmpty)
        assertFalse(state.isLoading)
    }

    @Test
    fun testLoadStories_empty_setsEmptyState() = runTest {
        fakeStoryRepository = FakeStoryRepository(initialStories = emptyList())
        fakeStoryRepository.storiesResult = ApiResult.Success(emptyList())

        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertEquals("Belum ada postingan story saat ini", state.emptyMessage)
    }

    @Test
    fun testSearchFiltering_matchesKeyword() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Bandung")
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredStories.size)
        assertEquals("s1", state.filteredStories.first().id)
        assertFalse(state.isSearchResultEmpty)
    }

    @Test
    fun testSearchFiltering_noMatch_setsSearchResultEmpty() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("nonexistent")
        val state = viewModel.uiState.value
        assertTrue(state.filteredStories.isEmpty())
        assertTrue(state.isSearchResultEmpty)
        assertEquals("Tidak ada story yang cocok dengan pencarian 'nonexistent'", state.emptyMessage)
    }

    @Test
    fun testLocationFilter_onlyShowsStoriesWithCoordinates() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        viewModel.onToggleLocationFilter()
        val state = viewModel.uiState.value
        assertTrue(state.showOnlyWithLocation)
        assertEquals(2, state.filteredStories.size)
        assertTrue(state.filteredStories.all { it.lat != null && it.lon != null })
    }

    @Test
    fun testBookmarksFilter_onlyShowsBookmarkedStories() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        viewModel.onToggleBookmarksFilter()
        val state = viewModel.uiState.value
        assertTrue(state.showOnlyBookmarks)
        assertEquals(1, state.filteredStories.size)
        assertEquals("s1", state.filteredStories.first().id)
    }

    @Test
    fun testLogout_updatesStateAndCallsRepository() = runTest {
        viewModel = HomeViewModel(fakeStoryRepository, fakeAuthRepository)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedOut)
        assertTrue(fakeAuthRepository.logoutCalled)

        viewModel.resetLoggedOut()
        assertFalse(viewModel.uiState.value.isLoggedOut)
    }
}
