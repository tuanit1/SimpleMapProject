package com.tuandev.simplemapproject.data.database.entities

import androidx.room.Entity

@Entity(primaryKeys = ["startNodeID", "endNodeID"])
data class LineEntity(
    val startNodeID: Int,
    val endNodeID: Int
)