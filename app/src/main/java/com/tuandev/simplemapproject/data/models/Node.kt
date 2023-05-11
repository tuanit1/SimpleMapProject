package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.Marker

data class Node(
    var id: String? = null,
    val title: String? = null,
    val latitude: Double,
    val longitude: Double,
    var marker: Marker? = null,
    var neighbors: List<String> = listOf()
) {
    fun convertToHashMap() = hashMapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "title" to title,
    )

    fun removeMarker(){
        marker?.remove()
        marker = null
    }
}