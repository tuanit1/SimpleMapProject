package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.Polyline

data class Line(
    var id: String? = null,
    val firstNodeId: String?,
    val secondNodeId: String?,
    var polyline: Polyline? = null,
    var distance: Float? = null,
) {
    fun convertToHashMap() = hashMapOf(
        "firstNodeId" to firstNodeId,
        "secondNodeId" to secondNodeId,
        "distance" to distance
    )

    fun removePolyline(){
        polyline?.remove()
        polyline = null
    }
}