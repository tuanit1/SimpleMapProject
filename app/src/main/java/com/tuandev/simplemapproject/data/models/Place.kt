package com.tuandev.simplemapproject.data.models

data class Place(
    val id: Int,
    val name: String = "",
    val listService: List<PlaceService> = listOf(),
    var listImage: List<String> = mutableListOf(),
    val zone: Zone,
    val game: Game? = null
) {

}