package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.Polyline

data class Line(
    val id: Int,
    val firstNodeId: Int,
    val secondNodeId: Int,
    var polyline: Polyline? = null,
    var distance: Float? = null,
)