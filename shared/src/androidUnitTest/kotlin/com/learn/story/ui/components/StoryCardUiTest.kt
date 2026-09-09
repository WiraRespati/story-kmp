package com.learn.story.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.learn.story.data.model.Story
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class StoryCardUiTest {

    private val sampleStory = Story(
        id = "story-101",
        name = "Ahmad Traveller",
        description = "Melihat pemandangan indah di puncak gunung.",
        photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1.jpg",
        createdAt = "2026-09-09T10:00:00.000Z",
        lat = -7.2575,
        lon = 112.7521
    )

    @Test
    fun testStoryCard_rendersContentAndHandlesCardClick() = runComposeUiTest {
        var cardClicked = false

        setContent {
            StoryCard(
                story = sampleStory,
                onClick = { cardClicked = true }
            )
        }

        onNodeWithText("Ahmad Traveller").assertIsDisplayed()
        onNodeWithText("Melihat pemandangan indah di puncak gunung.").assertIsDisplayed()

        // Location badge should be rendered since lat & lon are present
        onNodeWithContentDescription("Memiliki lokasi").assertIsDisplayed()

        // Click on card
        onNodeWithText("Ahmad Traveller").performClick()
        assertTrue(cardClicked)
    }

    @Test
    fun testStoryCard_bookmarkInteraction() = runComposeUiTest {
        var bookmarkClicked = false

        setContent {
            StoryCard(
                story = sampleStory,
                onClick = {},
                isBookmarked = false,
                onBookmarkClick = { bookmarkClicked = true }
            )
        }

        onNodeWithContentDescription("Tambah bookmark")
            .assertIsDisplayed()
            .performClick()

        assertTrue(bookmarkClicked)
    }

    @Test
    fun testStoryCard_alreadyBookmarked_displaysCorrectDescription() = runComposeUiTest {
        setContent {
            StoryCard(
                story = sampleStory,
                onClick = {},
                isBookmarked = true,
                onBookmarkClick = {}
            )
        }

        onNodeWithContentDescription("Hapus bookmark").assertIsDisplayed()
    }
}
