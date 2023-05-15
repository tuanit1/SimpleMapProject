package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.ThrillLevel
import com.tuandev.simplemapproject.util.Constants

class ThrillLevelRepository (
   context: Context
) {
    private val language = Constants.LANGUAGE_EN
    private val isEnglish = language == Constants.LANGUAGE_EN

    val thrillLevelFamily = ThrillLevel(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_thrill_level_0) else context.getString(
            R.string.vi_thrill_level_0
        ),
        score = 0
    )
    val thrillLevelMedium = ThrillLevel(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_thrill_level_1) else context.getString(
            R.string.vi_thrill_level_1
        ),
        score = 1
    )
    val thrillLevelHigh = ThrillLevel(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_thrill_level_2) else context.getString(
            R.string.vi_thrill_level_2
        ),
        score = 2
    )
    val thrillLevelExtreme = ThrillLevel(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_thrill_level_3) else context.getString(
            R.string.vi_thrill_level_3
        ),
        score = 3
    )
}