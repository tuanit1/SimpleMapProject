package com.tuandev.simplemapproject.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    object AsiaParkMap {
        val swLatLng = LatLng(16.03803, 108.22535)
        val seLatLng = LatLng(16.03855, 108.22977)
        val nwLatLng = LatLng(16.04404, 108.22457)
        private val neLatLng = LatLng(16.04449, 108.22909)
        val bound = LatLngBounds(swLatLng, neLatLng)
        val borderList = listOf(
            LatLng(16.038330857398098,108.22592474520206),
            LatLng(16.042116334853926, 108.22540909051895),
            LatLng(16.042068646276363, 108.22493366897106),
            LatLng(16.044077034394704, 108.22467718273401),
            LatLng(16.044502038044865, 108.22891272604465),
            LatLng(16.038681761421923, 108.22973348200321)
        )
    }

    const val LOG_TAG = "123123"

    object ErrorMessage {
        const val CONFLICT_LINE = "Conflict in draw line"
    }
}