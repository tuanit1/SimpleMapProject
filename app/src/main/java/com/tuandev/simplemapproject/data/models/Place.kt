package com.tuandev.simplemapproject.data.models

data class Place(
    val id: Int,
    val name: String = "",
    val serviceType: PlaceService? = null,
    var listImage: List<String> = mutableListOf(),
    val zone: Zone,
    val game: Game? = null
)