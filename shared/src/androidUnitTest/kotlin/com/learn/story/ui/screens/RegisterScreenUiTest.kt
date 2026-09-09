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
import com.learn.story.ui.screens.auth.RegisterScreenContent
import com.learn.story.ui.screens.auth.RegisterUiState
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class RegisterScreenUiTest {

    @Test
    fun testRegisterScreen_rendersAllFields() = runComposeUiTest {
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            RegisterScreenContent(
                uiState = RegisterUiState(),
                onNameChanged = {},
                onEmailChanged = {},
                onPasswordChanged = {},
                onRegister = {},
                onNavigateToLogin = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Buat Akun Baru").assertIsDisplayed()
        onNodeWithText("Registrasi").assertIsDisplayed()
        onNodeWithText("Nama Lengkap").assertIsDisplayed()
        onNodeWithText("Email").assertIsDisplayed()
        onNodeWithText("Password").assertIsDisplayed()
        onNodeWithText("Daftar").performScrollTo().assertIsDisplayed()
        onNodeWithText("Masuk di sini").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testRegisterScreen_handlesInputsAndSubmit() = runComposeUiTest {
        var nameInput = ""
        var emailInput = ""
        var passwordInput = ""
        var registerClicked = false

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            RegisterScreenContent(
                uiState = RegisterUiState(name = nameInput, email = emailInput, password = passwordInput),
                onNameChanged = { nameInput = it },
                onEmailChanged = { emailInput = it },
                onPasswordChanged = { passwordInput = it },
                onRegister = { registerClicked = true },
                onNavigateToLogin = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Nama Lengkap").performTextInput("Budi Utomo")
        assertEquals("Budi Utomo", nameInput)

        onNodeWithText("Email").performTextInput("budi@story.id")
        assertEquals("budi@story.id", emailInput)

        onNodeWithText("Password").performTextInput("securepassword123")
        assertEquals("securepassword123", passwordInput)

        onNodeWithText("Daftar").performScrollTo().performClick()
        assertTrue(registerClicked)
    }

    @Test
    fun testRegisterScreen_displaysValidationErrors() = runComposeUiTest {
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            RegisterScreenContent(
                uiState = RegisterUiState(
                    nameError = "Nama tidak boleh kosong",
                    emailError = "Format email tidak valid",
                    passwordError = "Password minimal 8 karakter"
                ),
                onNameChanged = {},
                onEmailChanged = {},
                onPasswordChanged = {},
                onRegister = {},
                onNavigateToLogin = {},
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Nama tidak boleh kosong").performScrollTo().assertIsDisplayed()
        onNodeWithText("Format email tidak valid").performScrollTo().assertIsDisplayed()
        onNodeWithText("Password minimal 8 karakter").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testRegisterScreen_navigateToLoginClick() = runComposeUiTest {
        var navigateToLoginClicked = false

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            RegisterScreenContent(
                uiState = RegisterUiState(),
                onNameChanged = {},
                onEmailChanged = {},
                onPasswordChanged = {},
                onRegister = {},
                onNavigateToLogin = { navigateToLoginClicked = true },
                snackbarHostState = snackbarHostState
            )
        }

        onNodeWithText("Masuk di sini").performScrollTo().performClick()
        assertTrue(navigateToLoginClicked)
    }
}
