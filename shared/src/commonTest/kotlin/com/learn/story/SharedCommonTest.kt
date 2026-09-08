package com.learn.story

import com.learn.story.data.model.Story
import com.learn.story.ui.components.formatStoryDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun testStoryModel() {
        val story = Story(
            id = "story-123",
            name = "John Doe",
            description = "A wonderful trip to Bandung",
            photoUrl = "https://story-api.dicoding.dev/images/stories/test.jpg",
            createdAt = "2024-03-01T10:30:00.000Z",
            lat = -6.9175,
            lon = 107.6191
        )

        assertEquals("story-123", story.id)
        assertEquals("John Doe", story.name)
        assertEquals("A wonderful trip to Bandung", story.description)
        assertEquals("https://story-api.dicoding.dev/images/stories/test.jpg", story.photoUrl)
        assertEquals("2024-03-01T10:30:00.000Z", story.createdAt)
        assertEquals(-6.9175, story.lat)
        assertEquals(107.6191, story.lon)
    }

    @Test
    fun testFormatStoryDate() {
        val isoDate = "2022-01-08T06:34:18.598Z"
        val formatted = formatStoryDate(isoDate)
        assertEquals("2022-01-08 • 06:34", formatted)
    }

    @Test
    fun testOfflineStoryDraftModel() {
        val draft = com.learn.story.data.model.OfflineStoryDraft(
            id = "draft-1",
            description = "Cerita offline seru",
            photoBase64 = "dGVzdC1waG90bw==",
            lat = -6.2,
            lon = 106.8,
            isGuest = false,
            createdAtEpoch = 1700000000L,
            locationName = "Jakarta, Indonesia"
        )
        assertEquals("draft-1", draft.id)
        assertEquals("Cerita offline seru", draft.description)
        assertEquals(-6.2, draft.lat)
        assertEquals(106.8, draft.lon)
        assertEquals("Jakarta, Indonesia", draft.locationName)
    }

    @Test
    fun testMapMarkerCreation() {
        val marker = com.learn.story.ui.components.map.MapMarker(
            id = "m1",
            title = "Monas",
            snippet = "Monumen Nasional",
            lat = -6.1754,
            lon = 106.8272
        )
        assertEquals("m1", marker.id)
        assertEquals("Monas", marker.title)
        assertEquals(-6.1754, marker.lat)
        assertEquals(106.8272, marker.lon)
    }

    @Test
    fun testLeafletHtmlBuilder() {
        val markers = listOf(
            com.learn.story.ui.components.map.MapMarker(
                id = "story-1",
                title = "Bandung Point",
                snippet = "Story snippet",
                lat = -6.9175,
                lon = 107.6191
            )
        )

        val markersJson = com.learn.story.ui.components.map.buildMarkersJson(markers)
        kotlin.test.assertTrue(markersJson.contains("story-1"))
        kotlin.test.assertTrue(markersJson.contains("Bandung Point"))

        val html = com.learn.story.ui.components.map.generateLeafletHtml(
            initialLat = -6.9175,
            initialLon = 107.6191,
            zoom = 12,
            mode = com.learn.story.ui.components.map.MapInteractionMode.VIEW_ONLY,
            markers = markers,
            selectedLocation = null
        )

        // Verify OpenStreetMap tile layer is included
        kotlin.test.assertTrue(html.contains("tile.openstreetmap.org"))
        // Verify cross-platform bridges are supported
        kotlin.test.assertTrue(html.contains("window.webkit.messageHandlers.storyBridge"))
        kotlin.test.assertTrue(html.contains("window.AndroidBridge"))
        // Verify Leaflet CDN
        kotlin.test.assertTrue(html.contains("leaflet.js"))
    }

    @Test
    fun testThemeRepositoryAndModes() {
        assertEquals(com.learn.story.ui.theme.AppThemeMode.LIGHT, com.learn.story.ui.theme.AppThemeMode.fromCode("light"))
        assertEquals(com.learn.story.ui.theme.AppThemeMode.DARK, com.learn.story.ui.theme.AppThemeMode.fromCode("dark"))
        assertEquals(com.learn.story.ui.theme.AppThemeMode.SYSTEM, com.learn.story.ui.theme.AppThemeMode.fromCode("system"))
        assertEquals(com.learn.story.ui.theme.AppThemeMode.SYSTEM, com.learn.story.ui.theme.AppThemeMode.fromCode("invalid_code"))

        val settings = com.russhwolf.settings.Settings()
        val themeRepo = com.learn.story.data.repository.ThemeRepository(settings)

        themeRepo.setThemeMode(com.learn.story.ui.theme.AppThemeMode.DARK)
        assertEquals(com.learn.story.ui.theme.AppThemeMode.DARK, themeRepo.themeMode.value)
        assertEquals("dark", settings.getStringOrNull("app_theme_mode"))

        themeRepo.setThemeMode(com.learn.story.ui.theme.AppThemeMode.LIGHT)
        assertEquals(com.learn.story.ui.theme.AppThemeMode.LIGHT, themeRepo.themeMode.value)
        assertEquals("light", settings.getStringOrNull("app_theme_mode"))
    }
}