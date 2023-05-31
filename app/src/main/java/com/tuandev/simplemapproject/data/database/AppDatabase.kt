package com.tuandev.simplemapproject.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tuandev.simplemapproject.data.database.daos.SuggestRouteDao
import com.tuandev.simplemapproject.data.database.entities.SaveSuggestRoute

@Database(entities = [SaveSuggestRoute::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun suggestRouteDao(): SuggestRouteDao
}