package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.Zone
import com.tuandev.simplemapproject.util.Constants

class ZoneRepository(context: Context) {

    private val language = Constants.LANGUAGE_VI
    private val isEnglish = language == Constants.LANGUAGE_EN

    val zoneVietnam = Zone(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_zone_vn) else context.getString(
            R.string.en_zone_vn
        )
    )
    val zoneJapan = Zone(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_zone_jp) else context.getString(
            R.string.en_zone_jp
        )
    )
    val zoneKorea = Zone(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_zone_kr) else context.getString(
            R.string.en_zone_kr
        )
    )
    val zoneChina = Zone(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_zone_cn) else context.getString(
            R.string.en_zone_cn
        )
    )
    val zoneIndia = Zone(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_zone_india) else context.getString(
            R.string.vi_zone_india
        )
    )
    val zoneCambodia = Zone(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_zone_com) else context.getString(
            R.string.vi_zone_com
        )
    )
    val zoneThailand = Zone(
        id = 7,
        name = if (isEnglish) context.getString(R.string.en_zone_thai) else context.getString(
            R.string.vi_zone_thai
        )
    )
    val zoneNepal = Zone(
        id = 8,
        name = if (isEnglish) context.getString(R.string.en_zone_nepal) else context.getString(
            R.string.vi_zone_nepal
        )
    )
    val zoneSingapore = Zone(
        id = 9,
        name = if (isEnglish) context.getString(R.string.en_zone_sin) else context.getString(
            R.string.vi_zone_sin
        )
    )
    val zoneIndonesia = Zone(
        id = 10,
        name = if (isEnglish) context.getString(R.string.en_zone_indo) else context.getString(
            R.string.vi_zone_indo
        )
    )
}