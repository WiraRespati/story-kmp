package com.learn.story.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.learn.story.data.model.OfflineStoryDraft

@Entity(tableName = "offline_drafts")
data class OfflineDraftEntity(
    @PrimaryKey val id: String,
    val description: String,
    val photoBase64: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val isGuest: Boolean = false,
    val createdAtEpoch: Long,
    val locationName: String? = null,
    val lastError: String? = null
)

fun OfflineDraftEntity.toDomain(): OfflineStoryDraft = OfflineStoryDraft(
    id = id,
    description = description,
    photoBase64 = photoBase64,
    lat = lat,
    lon = lon,
    isGuest = isGuest,
    createdAtEpoch = createdAtEpoch,
    locationName = locationName,
    lastError = lastError
)

fun OfflineStoryDraft.toEntity(): OfflineDraftEntity = OfflineDraftEntity(
    id = id,
    description = description,
    photoBase64 = photoBase64,
    lat = lat,
    lon = lon,
    isGuest = isGuest,
    createdAtEpoch = createdAtEpoch,
    locationName = locationName,
    lastError = lastError
)
