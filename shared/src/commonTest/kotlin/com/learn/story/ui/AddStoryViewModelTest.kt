package com.learn.story.ui

import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeLocationRepository
import com.learn.story.fakes.FakeStoryRepository
import com.learn.story.ui.screens.add.AddStorySubmitState
import com.learn.story.ui.screens.add.AddStoryViewModel
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
class AddStoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStoryRepository: FakeStoryRepository
    private lateinit var fakeLocationRepository: FakeLocationRepository
    private lateinit var viewModel: AddStoryViewModel

    private val samplePhoto = byteArrayOf(1, 2, 3, 4, 5)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeStoryRepository = FakeStoryRepository()
        fakeLocationRepository = FakeLocationRepository()
        viewModel = AddStoryViewModel(fakeStoryRepository, fakeLocationRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_isIdle() {
        val state = viewModel.uiState.value
        assertNull(state.photoBytes)
        assertEquals("", state.description)
        assertFalse(state.isGuest)
        assertNull(state.lat)
        assertNull(state.lon)
        assertEquals(AddStorySubmitState.Idle, state.submitState)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertFalse(state.savedOffline)
        assertNull(state.errorMessage)
    }

    @Test
    fun testValidation_missingPhoto_setsPhotoError() = runTest {
        viewModel.onDescriptionChanged("Deskripsi tanpa foto")
        viewModel.uploadStory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Pilih foto terlebih dahulu", state.photoError)
        assertEquals(AddStorySubmitState.Idle, state.submitState)
    }

    @Test
    fun testValidation_missingDescription_setsDescriptionError() = runTest {
        viewModel.onPhotoSelected(samplePhoto)
        viewModel.uploadStory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Deskripsi tidak boleh kosong", state.descriptionError)
        assertEquals(AddStorySubmitState.Idle, state.submitState)
    }

    @Test
    fun testUploadStory_success_transitionsToSuccessState() = runTest {
        fakeStoryRepository.uploadResult = ApiResult.Success("Story created successfully")
        viewModel.onPhotoSelected(samplePhoto)
        viewModel.onDescriptionChanged("Pemandangan indah di gunung")

        viewModel.uploadStory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AddStorySubmitState.Success, state.submitState)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun testUploadStory_networkFailure_savesToOfflineDrafts() = runTest {
        fakeStoryRepository.uploadResult = ApiResult.Error("No internet connection")
        viewModel.onPhotoSelected(samplePhoto)
        viewModel.onDescriptionChanged("Cerita offline")

        viewModel.uploadStory()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.submitState is AddStorySubmitState.OfflineSaved)
        assertTrue(state.savedOffline)
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertEquals(1, fakeStoryRepository.savedDrafts.size)
        assertEquals("Cerita offline", fakeStoryRepository.savedDrafts.first().description)
    }

    @Test
    fun testSaveDraftManually_savesDraftAndSetsSavedOffline() = runTest {
        viewModel.onPhotoSelected(samplePhoto)
        viewModel.onDescriptionChanged("Draft manual disimpan")

        viewModel.saveDraftManually()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.savedOffline)
        assertEquals(1, fakeStoryRepository.savedDrafts.size)
    }

    @Test
    fun testOnLocationSelected_updatesCoordsAndAddress() = runTest {
        fakeLocationRepository.geocodeAddress = "Monas, Jakarta"
        viewModel.onLocationSelected(-6.1754, 106.8272)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(-6.1754, state.lat)
        assertEquals(106.8272, state.lon)
        assertEquals("Monas, Jakarta", state.locationName)
        assertFalse(state.isGeocoding)
    }

    @Test
    fun testResetSuccess_resetsToIdle() = runTest {
        viewModel.onPhotoSelected(samplePhoto)
        viewModel.onDescriptionChanged("Test story")
        viewModel.uploadStory()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)

        viewModel.resetSuccess()
        assertEquals(AddStorySubmitState.Idle, viewModel.uiState.value.submitState)
        assertFalse(viewModel.uiState.value.isSuccess)
    }
}
