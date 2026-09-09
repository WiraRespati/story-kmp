package com.learn.story.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class AppTextFieldUiTest {

    @Test
    fun testAppTextField_rendersLabelAndReceivesInput() = runComposeUiTest {
        var textValue by mutableStateOf("")

        setContent {
            AppTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = "Alamat Email"
            )
        }

        onNodeWithText("Alamat Email").assertIsDisplayed()

        onNodeWithText("Alamat Email").performTextInput("user@story.id")
        assertEquals("user@story.id", textValue)
    }

    @Test
    fun testAppTextField_displaysErrorMessage_whenProvided() = runComposeUiTest {
        setContent {
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Password",
                errorMessage = "Password minimal 8 karakter"
            )
        }

        onNodeWithText("Password minimal 8 karakter").assertIsDisplayed()
    }

    @Test
    fun testAppTextField_passwordToggleVisibility() = runComposeUiTest {
        setContent {
            AppTextField(
                value = "secret123",
                onValueChange = {},
                label = "Password",
                isPassword = true
            )
        }

        // Initially shows "Show" button for password
        onNodeWithText("Show").assertIsDisplayed().performClick()

        // After click, toggle changes to "Hide"
        onNodeWithText("Hide").assertIsDisplayed()
    }
}
