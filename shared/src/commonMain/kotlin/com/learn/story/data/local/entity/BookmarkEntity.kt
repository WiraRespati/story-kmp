package com.learn.story.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val storyId: String,
    val bookmarkedAtEpoch: Long = 0L
)
