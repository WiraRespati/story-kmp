package com.learn.story.ui

import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeAuthRepository
import com.learn.story.ui.screens.auth.RegisterSubmitState
import com.learn.story.ui.screens.auth.RegisterViewModel
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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = RegisterViewModel(fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_isIdle() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertEquals(RegisterSubmitState.Idle, state.submitState)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun testOnFieldChanged_updatesState() {
        viewModel.onNameChanged("Alice")
        viewModel.onEmailChanged("alice@example.com")
        viewModel.onPasswordChanged("securepassword")

        val state = viewModel.uiState.value
        assertEquals("Alice", state.name)
        assertEquals("alice@example.com", state.email)
        assertEquals("securepassword", state.password)
    }

    @Test
    fun testValidation_emptyFields_setsErrorsAndDoesNotRegister() = runTest {
        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.nameError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(RegisterSubmitState.Idle, state.submitState)
    }

    @Test
    fun testRegister_success_transitionsToSuccessState() = runTest {
        fakeAuthRepository.registerResult = ApiResult.Success("Pendaftaran berhasil!")
        viewModel.onNameChanged("Budi Santoso")
        viewModel.onEmailChanged("budi@example.com")
        viewModel.onPasswordChanged("password123")

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RegisterSubmitState.Success("Pendaftaran berhasil!"), state.submitState)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertEquals("Pendaftaran berhasil!", state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun testRegister_error_transitionsToErrorState() = runTest {
        fakeAuthRepository.registerResult = ApiResult.Error("Email sudah terdaftar")
        viewModel.onNameChanged("Budi Santoso")
        viewModel.onEmailChanged("budi@example.com")
        viewModel.onPasswordChanged("password123")

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RegisterSubmitState.Error("Email sudah terdaftar"), state.submitState)
        assertFalse(state.isSuccess)
        assertEquals("Email sudah terdaftar", state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun testClearMessages_resetsStateToIdle() = runTest {
        fakeAuthRepository.registerResult = ApiResult.Error("Ada kesalahan")
        viewModel.onNameChanged("User")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.register()
        advanceUntilIdle()

        assertEquals("Ada kesalahan", viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertEquals(RegisterSubmitState.Idle, viewModel.uiState.value.submitState)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
