package com.learn.story.data.repository

import com.learn.story.data.local.dao.BookmarkDao
import com.learn.story.data.local.dao.OfflineDraftDao
import com.learn.story.data.local.dao.StoryDao
import com.learn.story.data.local.entity.BookmarkEntity
import com.learn.story.data.local.entity.toDomain
import com.learn.story.data.local.entity.toEntity
import com.learn.story.data.model.OfflineStoryDraft
import com.learn.story.data.model.Story
import com.learn.story.data.network.ApiResult
import com.learn.story.data.remote.StoryApiService
import com.learn.story.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface StoryRepository {
    val storyUploadedEvent: SharedFlow<Unit>
    val bookmarkedIdsFlow: Flow<Set<String>>
    val bookmarkedStoriesFlow: Flow<List<Story>>
    val offlineDraftsFlow: Flow<List<OfflineStoryDraft>>
    val cachedStoriesFlow: Flow<List<Story>>

    fun getStories(page: Int = 1, size: Int = 20, location: Int = 0): Flow<ApiResult<List<Story>>>
    fun getStoryDetail(id: String): Flow<ApiResult<Story>>
    fun uploadStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double? = null,
        lon: Double? = null
    ): Flow<ApiResult<String>>
    fun uploadGuestStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double? = null,
        lon: Double? = null
    ): Flow<ApiResult<String>>
    suspend fun toggleBookmark(storyId: String): Boolean
    suspend fun isStoryBookmarked(storyId: String): Boolean
    suspend fun getBookmarkedIds(): Set<String>
    suspend fun saveOfflineDraft(
        id: String,
        description: String,
        photoBytes: ByteArray,
        lat: Double? = null,
        lon: Double? = null,
        isGuest: Boolean = false,
        locationName: String? = null
    )
    suspend fun getOfflineDrafts(): List<OfflineStoryDraft>
    suspend fun removeOfflineDraft(draftId: String)
    suspend fun syncOfflineDrafts(): Int
    fun uploadDraft(draft: OfflineStoryDraft): Flow<ApiResult<String>>
}

fun StoryRepository(
    apiService: StoryApiService,
    storyDao: StoryDao,
    bookmarkDao: BookmarkDao,
    offlineDraftDao: OfflineDraftDao
): StoryRepository = StoryRepositoryImpl(apiService, storyDao, bookmarkDao, offlineDraftDao)

class StoryRepositoryImpl(
    private val apiService: StoryApiService,
    private val storyDao: StoryDao,
    private val bookmarkDao: BookmarkDao,
    private val offlineDraftDao: OfflineDraftDao
) : StoryRepository {
    private val _storyUploadedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val storyUploadedEvent: SharedFlow<Unit> = _storyUploadedEvent.asSharedFlow()

    // Reactive streams from Room SQLite Database
    override val bookmarkedIdsFlow: Flow<Set<String>> = bookmarkDao.getBookmarkedStoryIdsFlow().map { it.toSet() }
    override val bookmarkedStoriesFlow: Flow<List<Story>> = storyDao.getBookmarkedStoriesFlow().map { list ->
        list.map { it.toDomain() }
    }
    override val offlineDraftsFlow: Flow<List<OfflineStoryDraft>> = offlineDraftDao.getDraftsFlow().map { list ->
        list.map { it.toDomain() }
    }
    override val cachedStoriesFlow: Flow<List<Story>> = storyDao.getStoriesFlow().map { list ->
        list.map { it.toDomain() }
    }

    override fun getStories(page: Int, size: Int, location: Int): Flow<ApiResult<List<Story>>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getStories(page = page, size = size, location = location)
            if (!response.error) {
                if (page == 1 && location == 0) {
                    // Update Room SQLite Cache
                    storyDao.clearStories()
                    storyDao.insertStories(response.listStory.map { it.toEntity() })
                }
                emit(ApiResult.Success(response.listStory))
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Failed to fetch stories"
                if (page == 1) {
                    val cached = storyDao.getStories().map { it.toDomain() }
                    val filtered = if (location == 1) cached.filter { it.lat != null && it.lon != null } else cached
                    if (filtered.isNotEmpty()) {
                        emit(ApiResult.Success(filtered))
                        return@flow
                    }
                }
                emit(ApiResult.Error(errorMsg))
            }
        } catch (e: Exception) {
            if (page == 1) {
                val cached = storyDao.getStories().map { it.toDomain() }
                val filtered = if (location == 1) cached.filter { it.lat != null && it.lon != null } else cached
                if (filtered.isNotEmpty()) {
                    emit(ApiResult.Success(filtered))
                    return@flow
                }
            }
            emit(ApiResult.Error(e.message ?: "Failed to fetch stories"))
        }
    }

    override fun getStoryDetail(id: String): Flow<ApiResult<Story>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.getStoryDetail(id)
            if (!response.error && response.story != null) {
                emit(ApiResult.Success(response.story))
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Story not found"
                val cachedStory = storyDao.getStoryById(id)?.toDomain()
                if (cachedStory != null) {
                    emit(ApiResult.Success(cachedStory))
                } else {
                    emit(ApiResult.Error(errorMsg))
                }
            }
        } catch (e: Exception) {
            val cachedStory = storyDao.getStoryById(id)?.toDomain()
            if (cachedStory != null) {
                emit(ApiResult.Success(cachedStory))
            } else {
                emit(ApiResult.Error(e.message ?: "Failed to fetch story detail"))
            }
        }
    }

    override fun uploadStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.addStory(
                description = description,
                photoBytes = photoBytes,
                lat = lat,
                lon = lon
            )
            if (!response.error) {
                _storyUploadedEvent.tryEmit(Unit)
                emit(ApiResult.Success(response.message))
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Failed to upload story"
                emit(ApiResult.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to upload story"))
        }
    }

    override fun uploadGuestStory(
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?
    ): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading)
        try {
            val response = apiService.addGuestStory(
                description = description,
                photoBytes = photoBytes,
                lat = lat,
                lon = lon
            )
            if (!response.error) {
                _storyUploadedEvent.tryEmit(Unit)
                emit(ApiResult.Success(response.message))
            } else {
                val errorMsg = if (response.message.trim().isNotEmpty()) response.message else "Failed to upload guest story"
                emit(ApiResult.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to upload guest story"))
        }
    }

    // --- Bookmarks Feature (Room SQLite) ---
    override suspend fun toggleBookmark(storyId: String): Boolean {
        val isBookmarked = bookmarkDao.isBookmarked(storyId)
        return if (isBookmarked) {
            bookmarkDao.removeBookmark(storyId)
            false
        } else {
            bookmarkDao.addBookmark(
                BookmarkEntity(
                    storyId = storyId,
                    bookmarkedAtEpoch = currentTimeMillis()
                )
            )
            true
        }
    }

    override suspend fun isStoryBookmarked(storyId: String): Boolean = bookmarkDao.isBookmarked(storyId)
    override suspend fun getBookmarkedIds(): Set<String> = bookmarkDao.getBookmarkedStoryIds().toSet()

    // --- Offline Mode / Outbox Queue (Room SQLite) ---
    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveOfflineDraft(
        id: String,
        description: String,
        photoBytes: ByteArray,
        lat: Double?,
        lon: Double?,
        isGuest: Boolean,
        locationName: String?
    ) {
        val base64 = Base64.encode(photoBytes)
        val draft = OfflineStoryDraft(
            id = id,
            description = description,
            photoBase64 = base64,
            lat = lat,
            lon = lon,
            isGuest = isGuest,
            createdAtEpoch = currentTimeMillis(),
            locationName = locationName
        )
        offlineDraftDao.insertDraft(draft.toEntity())
    }

    override suspend fun getOfflineDrafts(): List<OfflineStoryDraft> = offlineDraftDao.getDrafts().map { it.toDomain() }

    override suspend fun removeOfflineDraft(draftId: String) = offlineDraftDao.deleteDraftById(draftId)

    override suspend fun syncOfflineDrafts(): Int {
        val drafts = getOfflineDrafts()
        if (drafts.isEmpty()) return 0
        var successCount = 0
        for (draft in drafts) {
            uploadDraft(draft).collect { result ->
                if (result is ApiResult.Success) {
                    removeOfflineDraft(draft.id)
                    successCount++
                }
            }
        }
        return successCount
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun uploadDraft(draft: OfflineStoryDraft): Flow<ApiResult<String>> {
        val bytes = try {
            Base64.decode(draft.photoBase64)
        } catch (_: Exception) {
            return flow { emit(ApiResult.Error("Format gambar draft tidak valid")) }
        }

        return if (draft.isGuest) {
            uploadGuestStory(
                description = draft.description,
                photoBytes = bytes,
                lat = draft.lat,
                lon = draft.lon
            )
        } else {
            uploadStory(
                description = draft.description,
                photoBytes = bytes,
                lat = draft.lat,
                lon = draft.lon
            )
        }
    }
}
