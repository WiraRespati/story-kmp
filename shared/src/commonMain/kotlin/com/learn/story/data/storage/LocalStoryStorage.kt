package com.learn.story.data.storage

import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStoryStorage(
    private val settings: Settings = Settings()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // --- Story Cache ---
    fun saveStoriesCache(stories: List<Story>) {
        try {
            val jsonString = json.encodeToString(stories)
            settings.putString(KEY_CACHED_STORIES, jsonString)
        } catch (_: Exception) {
        }
    }

    fun getStoriesCache(): List<Story> {
        val jsonString = settings.getStringOrNull(KEY_CACHED_STORIES) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- Bookmarks (Favorite) ---
    fun isStoryBookmarked(storyId: String): Boolean {
        val bookmarks = getBookmarkedIds()
        return bookmarks.contains(storyId)
    }

    fun toggleBookmark(storyId: String): Boolean {
        val bookmarks = getBookmarkedIds().toMutableSet()
        val newState = if (bookmarks.contains(storyId)) {
            bookmarks.remove(storyId)
            false
        } else {
            bookmarks.add(storyId)
            true
        }
        try {
            settings.putString(KEY_BOOKMARKS, json.encodeToString(bookmarks.toList()))
        } catch (_: Exception) {
        }
        return newState
    }

    fun getBookmarkedIds(): Set<String> {
        val raw = settings.getStringOrNull(KEY_BOOKMARKS) ?: return emptySet()
        return try {
            json.decodeFromString<List<String>>(raw).toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    // --- Offline Outbox Drafts ---
    fun getDrafts(): List<OfflineStoryDraft> {
        val raw = settings.getStringOrNull(KEY_OFFLINE_DRAFTS) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveDraft(draft: OfflineStoryDraft) {
        val current = getDrafts().filterNot { it.id == draft.id }.toMutableList()
        current.add(0, draft)
        try {
            settings.putString(KEY_OFFLINE_DRAFTS, json.encodeToString(current))
        } catch (_: Exception) {
        }
    }

    fun removeDraft(draftId: String) {
        val updated = getDrafts().filterNot { it.id == draftId }
        try {
            settings.putString(KEY_OFFLINE_DRAFTS, json.encodeToString(updated))
        } catch (_: Exception) {
        }
    }

    fun clearDrafts() {
        settings.remove(KEY_OFFLINE_DRAFTS)
    }

    companion object {
        private const val KEY_CACHED_STORIES = "cached_stories_feed"
        private const val KEY_BOOKMARKS = "saved_bookmarks"
        private const val KEY_OFFLINE_DRAFTS = "offline_story_drafts"
    }
}
