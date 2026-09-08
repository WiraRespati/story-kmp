package com.learn.story.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learn.story.data.local.entity.OfflineDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDraftDao {
    @Query("SELECT * FROM offline_drafts ORDER BY createdAtEpoch DESC")
    fun getDraftsFlow(): Flow<List<OfflineDraftEntity>>

    @Query("SELECT * FROM offline_drafts ORDER BY createdAtEpoch DESC")
    suspend fun getDrafts(): List<OfflineDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: OfflineDraftEntity)

    @Query("DELETE FROM offline_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: String)

    @Query("DELETE FROM offline_drafts")
    suspend fun clearDrafts()
}
