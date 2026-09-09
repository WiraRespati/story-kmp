package com.learn.story.fakes

import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.data.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class FakeStoryRepository(
    initialStories: List<Story> = emptyList(),
    initialBookmarks: Set<String> = emptySet(),
    initialDrafts: List<OfflineStoryDraft> = emptyList()
) : StoryRepository {

    private val _storyUploadedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val storyUploadedEvent: SharedFlow<Unit> = _storyUploadedEvent.asSharedFlow()

    private val _cachedStories = MutableStateFlow(initialStories)
    override val cachedStoriesFlow: Flow<List<Story>> = _cachedStories.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow(initialBookmarks)
    override val bookmarkedIdsFlow: Flow<Set<String>> = _bookmarkedIds.asStateFlow()

    private val _bookmarkedStories = MutableStateFlow(initialStories.filter { initialBookmarks.contains(it.id) })
    override val bookmarkedStoriesFlow: Flow<List<Story>> = _bookmarkedStories.asStateFlow()

    private val _offlineDrafts = MutableStateFlow(initialDrafts)
    override val offlineDraftsFlow: Flow<List<OfflineStoryDraft>> = _offlineDrafts.asStateFlow()

    var storiesResult: ApiResult<List<Story>> = ApiResult.Success(initialStories)
    var storyDetailResult: ApiResult<Story>? = null
    var uploadResult: ApiResult<String> = ApiResult.Success("Story created successfully")

    val savedDrafts = mutableListOf<OfflineStoryDraft>()
    var syncCount: Int = 0

    fun emitUploadedEvent() {
        _storyUploadedEvent.tryEmit(Unit)
    }

    fun setCachedStories(stories: List<Story>) {
        _cachedStories.value = stories
    }

    override fun getStories(page: Int, size: Int, location: Int): Flow<ApiResult<List<Story>>> = flow {
        emit(ApiResult.Loading)
        emit(storiesResult)
    }

    override fun getStoryDetail(id: String): Flow<ApiResult<Story>> = flow {
        emit(ApiResult.Loading)
        val result = storyDetailResult ?: storiesResult.let { res ->
            if (res is ApiResult.Success) {
                val found = res.data.find { it.id == id }
                if (found != null) ApiResult.Success(found) else ApiResult.Error("Story not found")
            } else {
                ApiResult.Error("Failed to fetch story")
            }
        }
        emit(result)
    }

    override fun uploadStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading)
        if (uploadResult is ApiResult.Success) {
            _storyUploadedEvent.tryEmit(Unit)
        }
        emit(uploadResult)
    }

    override fun uploadGuestStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading)
        if (uploadResult is ApiResult.Success) {
            _storyUploadedEvent.tryEmit(Unit)
        }
        emit(uploadResult)
    }

    override suspend fun toggleBookmark(storyId: String): Boolean {
        val current = _bookmarkedIds.value.toMutableSet()
        val newState = if (current.contains(storyId)) {
            current.remove(storyId)
            false
        } else {
            current.add(storyId)
            true
        }
        _bookmarkedIds.value = current
        return newState
    }

    override suspend fun isStoryBookmarked(storyId: String): Boolean = _bookmarkedIds.value.contains(storyId)

    override suspend fun getBookmarkedIds(): Set<String> = _bookmarkedIds.value

    override suspend fun saveOfflineDraft(
        id: String,
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?,
        isGuest: Boolean,
        locationName: String?
    ) {
        val draft = OfflineStoryDraft(
            id = id,
            description = description,
            photoBase64 = "dummy-base64",
            lat = lat,
            lon = lon,
            isGuest = isGuest,
            createdAtEpoch = 1000L,
            locationName = locationName
        )
        savedDrafts.add(draft)
        _offlineDrafts.value += draft
    }

    override suspend fun getOfflineDrafts(): List<OfflineStoryDraft> = _offlineDrafts.value

    override suspend fun removeOfflineDraft(draftId: String) {
        savedDrafts.removeAll { it.id == draftId }
        _offlineDrafts.value = _offlineDrafts.value.filter { it.id != draftId }
    }

    override suspend fun syncOfflineDrafts(): Int {
        val count = _offlineDrafts.value.size
        _offlineDrafts.value = emptyList()
        savedDrafts.clear()
        syncCount = count
        return count
    }

    override fun uploadDraft(draft: OfflineStoryDraft): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Success("Draft uploaded"))
    }
}
