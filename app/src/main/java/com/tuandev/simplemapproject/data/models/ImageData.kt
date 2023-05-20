package com.tuandev.simplemapproject.data.models

class ImageData(
    var name: String,
    var url: String,
    var placeId: Int? = null
) {
    fun getId() = name.hashCode().toLong()
}