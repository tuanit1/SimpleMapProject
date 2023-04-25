package com.tuandev.simplemapproject.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tuandev.simplemapproject.data.database.daos.LineDao
import com.tuandev.simplemapproject.data.database.daos.NodeDao
import com.tuandev.simplemapproject.data.database.entities.LineEntity
import com.tuandev.simplemapproject.data.database.entities.NodeEntity

@Database(entities = [LineEntity::class, NodeEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lineDao(): LineDao
    abstract fun nodeDao(): NodeDao
}