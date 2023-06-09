package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.Marker

data class Node(
    var id: String? = null,
    var latitude: Double,
    var longitude: Double,
    var placeId: Int? = null,
    var marker: Marker? = null,
    var neighbors: MutableList<NeighborWithDistance> = mutableListOf()
) {
    fun convertToHashMap() = hashMapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "placeId" to placeId
    )

    fun removeMarker() {
        marker?.remove()
        marker = null
    }
}