package com.learn.story.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.learn.story.data.model.Story

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double? = null,
    val lon: Double? = null
)

fun StoryEntity.toDomain(): Story = Story(
    id = id,
    name = name,
    description = description,
    photoUrl = photoUrl,
    createdAt = createdAt,
    lat = lat,
    lon = lon
)

fun Story.toEntity(): StoryEntity = StoryEntity(
    id = id,
    name = name,
    description = description,
    photoUrl = photoUrl,
    createdAt = createdAt,
    lat = lat,
    lon = lon
)
