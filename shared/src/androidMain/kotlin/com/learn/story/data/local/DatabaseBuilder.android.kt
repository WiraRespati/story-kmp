package com.learn.story.data.local

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = StoryContextProvider.appContext
        ?: error("StoryContextProvider has not initialized Application Context yet")
    val dbFile = context.getDatabasePath("story_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
