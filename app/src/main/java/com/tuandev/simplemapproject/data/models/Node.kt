package com.tuandev.simplemapproject.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class Node(
    val id: Int,
    val title: String,
    val position: LatLng,
    val marker: Marker?
)