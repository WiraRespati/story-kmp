package com.learn.story.ui

import com.learn.story.data.model.UserModel
import com.learn.story.data.network.ApiResult
import com.learn.story.fakes.FakeAuthRepository
import com.learn.story.ui.screens.auth.AuthSubmitState
import com.learn.story.ui.screens.auth.LoginViewModel
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = LoginViewModel(fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_isIdle() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertEquals(AuthSubmitState.Idle, state.submitState)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun testOnEmailChanged_updatesStateAndClearsErrors() {
        viewModel.onEmailChanged("tester@example.com")
        assertEquals("tester@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun testOnPasswordChanged_updatesStateAndClearsErrors() {
        viewModel.onPasswordChanged("secret123")
        assertEquals("secret123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun testValidation_emptyFields_setsErrorsAndDoesNotCallApi() = runTest {
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(AuthSubmitState.Idle, state.submitState)
        assertFalse(fakeAuthRepository.loggedIn)
    }

    @Test
    fun testValidation_invalidEmailAndShortPassword_setsSpecificErrors() = runTest {
        viewModel.onEmailChanged("not-an-email")
        viewModel.onPasswordChanged("12345")
        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Format email tidak valid", state.emailError)
        assertEquals("Password minimal 8 karakter", state.passwordError)
        assertEquals(AuthSubmitState.Idle, state.submitState)
    }

    @Test
    fun testLogin_success_transitionsToSuccessState() = runTest {
        fakeAuthRepository.loginResult = ApiResult.Success(
            UserModel("u-123", "Budi", "mock-token-xyz")
        )
        viewModel.onEmailChanged("budi@example.com")
        viewModel.onPasswordChanged("password123")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthSubmitState.Success, state.submitState)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(fakeAuthRepository.loggedIn)
    }

    @Test
    fun testLogin_error_transitionsToErrorState() = runTest {
        fakeAuthRepository.loginResult = ApiResult.Error("Password salah!")
        viewModel.onEmailChanged("budi@example.com")
        viewModel.onPasswordChanged("wrongpassword")

        viewModel.login()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AuthSubmitState.Error("Password salah!"), state.submitState)
        assertEquals("Password salah!", state.errorMessage)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun testResetSuccess_resetsSubmitStateToIdle() = runTest {
        fakeAuthRepository.loginResult = ApiResult.Success(UserModel("1", "User", "token"))
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)

        viewModel.resetSuccess()
        assertEquals(AuthSubmitState.Idle, viewModel.uiState.value.submitState)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun testClearError_resetsSubmitStateToIdle() = runTest {
        fakeAuthRepository.loginResult = ApiResult.Error("Network failure")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.login()
        advanceUntilIdle()

        assertEquals("Network failure", viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertEquals(AuthSubmitState.Idle, viewModel.uiState.value.submitState)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
