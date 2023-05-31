package com.tuandev.simplemapproject.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tuandev.simplemapproject.data.models.RouteItem

@Entity
data class SaveSuggestRoute(
    @PrimaryKey
    val placeId: Int,
    var isStart: Boolean = false,
    val itemState: String = RouteItem.NOT_VISITED,
    var itemIndex: Int = 0,
)