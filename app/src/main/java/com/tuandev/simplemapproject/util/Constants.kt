package com.tuandev.simplemapproject.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    object AsiaParkMap{
        val swLatLng = LatLng(16.03803, 108.22535)
        val seLatLng = LatLng(16.03855, 108.22977)
        val nwLatLng = LatLng(16.04404,108.22457)
        val neLatLng = LatLng(16.04449,108.22909)
        val bound = LatLngBounds(swLatLng, neLatLng)
    }
}