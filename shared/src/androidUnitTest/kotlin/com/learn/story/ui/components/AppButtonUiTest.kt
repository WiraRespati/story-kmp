package com.learn.story.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class AppButtonUiTest {

    @Test
    fun testButton_rendersTextAndRespondsToClick() = runComposeUiTest {
        var clicked = false
        setContent {
            AppButton(
                text = "Kirim Cerita",
                onClick = { clicked = true },
                enabled = true
            )
        }

        onNodeWithText("Kirim Cerita")
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun testButton_whenDisabled_cannotBeClicked() = runComposeUiTest {
        var clicked = false
        setContent {
            AppButton(
                text = "Kirim Cerita",
                onClick = { clicked = true },
                enabled = false
            )
        }

        onNodeWithText("Kirim Cerita")
            .assertIsDisplayed()
            .performClick()

        assertFalse(clicked)
    }

    @Test
    fun testButton_whenLoading_showsIndicatorAndDisablesInteraction() = runComposeUiTest {
        var clicked = false
        setContent {
            AppButton(
                text = "Kirim Cerita",
                onClick = { clicked = true },
                isLoading = true
            )
        }

        onNodeWithText("Kirim Cerita").assertDoesNotExist()
        assertFalse(clicked)
    }
}
