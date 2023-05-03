package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.Polyline

data class Line(
    val firstNodeId: String,
    val secondNodeId: String,
    var distance: Float,
    var polyline: Polyline
)