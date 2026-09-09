package com.learn.story.ui

import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.learn.story.data.repository.ThemeRepository
import com.learn.story.fakes.FakeAuthRepository
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.profile.ProfileViewModel
import com.learn.story.ui.theme.AppFontFamily
import com.learn.story.ui.theme.AppThemeMode
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var themeRepository: ThemeRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var viewModel: ProfileViewModel

    private val sampleStory = Story(
        id = "s-profile-1",
        name = "Profile Story",
        description = "Deskripsi",
        photoUrl = "https://example.com/p.jpg",
        createdAt = "2024-01-01T00:00:00Z",
        lat = null,
        lon = null
    )

    private val sampleDraft = OfflineStoryDraft(
        id = "d-profile-1",
        description = "Draft Profile",
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
        themeRepository = ThemeRepository(MapSettings())
        fakeAuthRepository = FakeAuthRepository(initialUserName = "Budi Santoso")
        fakeStoryRepository = FakeStoryRepository(
            initialStories = listOf(sampleStory),
            initialBookmarks = setOf("s-profile-1"),
            initialDrafts = listOf(sampleDraft)
        )
        viewModel = ProfileViewModel(themeRepository, fakeAuthRepository, fakeStoryRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_loadsUserDataAndCounts() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Budi Santoso", state.userName)
        assertEquals(1, state.feedCount)
        assertEquals(1, state.bookmarksCount)
        assertEquals(1, state.draftsCount)
        assertFalse(state.isSyncing)
    }

    @Test
    fun testSetThemeMode_updatesTheme() = runTest {
        viewModel.setThemeMode(AppThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(AppThemeMode.DARK, viewModel.themeMode.value)

        viewModel.setThemeMode(AppThemeMode.LIGHT)
        advanceUntilIdle()
        assertEquals(AppThemeMode.LIGHT, viewModel.themeMode.value)
    }

    @Test
    fun testSetFontFamily_updatesFont() = runTest {
        viewModel.setFontFamily(AppFontFamily.SERIF)
        advanceUntilIdle()
        assertEquals(AppFontFamily.SERIF, viewModel.fontFamily.value)
    }

    @Test
    fun testSyncOfflineDrafts_callsRepositorySync() = runTest {
        viewModel.syncOfflineDrafts()
        advanceUntilIdle()

        assertEquals(1, fakeStoryRepository.syncCount)
        assertFalse(viewModel.uiState.value.isSyncing)
    }

    @Test
    fun testLogout_updatesState() = runTest {
        viewModel.logout()
        advanceUntilIdle()

        assertTrue(viewModel.isLoggedOut.value)
        assertTrue(fakeAuthRepository.logoutCalled)
    }
}
