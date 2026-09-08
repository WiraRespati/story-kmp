package com.learn.story.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learn.story.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getStoriesFlow(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    suspend fun getStories(): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE id = :id LIMIT 1")
    suspend fun getStoryById(id: String): StoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("DELETE FROM stories")
    suspend fun clearStories()
}
