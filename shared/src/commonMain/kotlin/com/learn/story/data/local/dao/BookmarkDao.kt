package com.learn.story.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learn.story.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT storyId FROM bookmarks")
    fun getBookmarkedStoryIdsFlow(): Flow<List<String>>

    @Query("SELECT storyId FROM bookmarks")
    suspend fun getBookmarkedStoryIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE storyId = :storyId)")
    suspend fun isBookmarked(storyId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE storyId = :storyId")
    suspend fun removeBookmark(storyId: String)
}
