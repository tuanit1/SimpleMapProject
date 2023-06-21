package com.tuandev.simplemapproject.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    object AsiaParkMap {
        private val swLatLng = LatLng(16.037344198329023, 108.22603572160006)
        private val neLatLng = LatLng(16.044502038044865, 108.22891272604465)
        val bound = LatLngBounds(swLatLng, neLatLng)
        val borderList = listOf(
            swLatLng,
            LatLng(16.042116334853926, 108.22540909051895),
            LatLng(16.042068646276363, 108.22493366897106),
            LatLng(16.044077034394704, 108.22467718273401),
            neLatLng,
            LatLng(16.037764060704916, 108.22976231575012)
        )
    }

    const val LOG_TAG = "123123"

    const val LANGUAGE_EN = "lang_en"
    const val LANGUAGE_VI = "lang_vi"
}