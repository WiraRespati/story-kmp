package com.learn.story.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.learn.story.data.local.dao.BookmarkDao
import com.learn.story.data.local.dao.OfflineDraftDao
import com.learn.story.data.local.dao.StoryDao
import com.learn.story.data.local.entity.BookmarkEntity
import com.learn.story.data.local.entity.OfflineDraftEntity
import com.learn.story.data.local.entity.StoryEntity

@Database(
    entities = [
        StoryEntity::class,
        BookmarkEntity::class,
        OfflineDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun offlineDraftDao(): OfflineDraftDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
