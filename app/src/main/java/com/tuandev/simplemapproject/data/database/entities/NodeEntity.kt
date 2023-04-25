package com.tuandev.simplemapproject.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val latitude: Float,
    val longitude: Float
)