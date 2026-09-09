package com.learn.story.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class ErrorAndEmptyStateUiTest {

    @Test
    fun testErrorStateView_displaysMessageAndCallsRetry() = runComposeUiTest {
        var retried = false
        setContent {
            ErrorStateView(
                message = "Gagal terhubung ke server",
                onRetry = { retried = true }
            )
        }

        onNodeWithText("Terjadi Kesalahan").assertIsDisplayed()
        onNodeWithText("Gagal terhubung ke server").assertIsDisplayed()

        onNodeWithText("Coba Lagi").performClick()
        assertTrue(retried)
    }

    @Test
    fun testEmptyStateView_displaysMessageAndCallsRefresh() = runComposeUiTest {
        var refreshed = false
        setContent {
            EmptyStateView(
                message = "Belum ada story yang diunggah",
                onRefresh = { refreshed = true }
            )
        }

        onNodeWithText("Tidak Ada Story").assertIsDisplayed()
        onNodeWithText("Belum ada story yang diunggah").assertIsDisplayed()

        onNodeWithText("Muat Ulang").performClick()
        assertTrue(refreshed)
    }
}
