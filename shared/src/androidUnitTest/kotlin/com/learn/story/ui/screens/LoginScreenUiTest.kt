package com.learn.story.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.learn.story.ui.screens.auth.LoginScreenContent
import com.learn.story.ui.screens.auth.LoginUiState
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class LoginScreenUiTest {

    @Test
    fun testLoginScreen_rendersInitialElements() = runComposeUiTest {
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChanged = {},
                onPasswordChanged = {},
                onLogin = {},
                onNavigateToRegister = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Dicoding Story").assertIsDisplayed()
        onNodeWithText("Masuk Akun").assertIsDisplayed()
        onNodeWithText("Email").assertIsDisplayed()
        onNodeWithText("Password").assertIsDisplayed()
        onNodeWithText("Masuk").performScrollTo().assertIsDisplayed()
        onNodeWithText("Daftar sekarang").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_handlesInputAndSubmit() = runComposeUiTest {
        var emailInput = ""
        var passwordInput = ""
        var loginClicked = false

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            LoginScreenContent(
                uiState = LoginUiState(email = emailInput, password = passwordInput),
                onEmailChanged = { emailInput = it },
                onPasswordChanged = { passwordInput = it },
                onLogin = { loginClicked = true },
                onNavigateToRegister = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Email").performTextInput("test@story.com")
        assertEquals("test@story.com", emailInput)

        onNodeWithText("Password").performTextInput("supersecret")
        assertEquals("supersecret", passwordInput)

        onNodeWithText("Masuk").performScrollTo().performClick()
        assertTrue(loginClicked)
    }

    @Test
    fun testLoginScreen_displaysValidationErrors() = runComposeUiTest {
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            LoginScreenContent(
                uiState = LoginUiState(
                    email = "invalid-email",
                    password = "123",
                    emailError = "Format email tidak valid",
                    passwordError = "Password minimal 8 karakter"
                ),
                onEmailChanged = {},
                onPasswordChanged = {},
                onLogin = {},
                onNavigateToRegister = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Format email tidak valid").performScrollTo().assertIsDisplayed()
        onNodeWithText("Password minimal 8 karakter").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_navigateToRegisterClick() = runComposeUiTest {
        var registerClicked = false

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChanged = {},
                onPasswordChanged = {},
                onLogin = {},
                onNavigateToRegister = { registerClicked = true },
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Daftar sekarang").performScrollTo().performClick()
        assertTrue(registerClicked)
    }
}
