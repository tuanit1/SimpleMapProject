package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.PlaceService
import com.tuandev.simplemapproject.util.Constants

class PlaceServiceRepository(
    context: Context
) {
    private val language = Constants.LANGUAGE_EN
    private val isEnglish = language == Constants.LANGUAGE_EN

    val serviceWC = PlaceService(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_place_service_wc) else context.getString(
            R.string.vi_place_service_wc
        ),
        imgRes = R.drawable.ic_toilet
    )
    val serviceSouvenir = PlaceService(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_place_service_souvenir) else context.getString(
            R.string.vi_place_service_souvenir
        ),
        imgRes = R.drawable.ic_sourvenir
    )
    val serviceGuest = PlaceService(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_place_service_guest) else context.getString(
            R.string.vi_place_service_guest
        ),
        imgRes = R.drawable.ic_customer
    )
    val serviceFood = PlaceService(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_place_service_food) else context.getString(
            R.string.vi_place_service_food
        ),
        imgRes = R.drawable.ic_restaurant
    )
    val serviceMedical = PlaceService(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_place_service_medical) else context.getString(
            R.string.vi_place_service_medical
        ),
        imgRes = R.drawable.ic_medical
    )
    val serviceTicket = PlaceService(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_place_service_ticket) else context.getString(
            R.string.vi_place_service_ticket
        ),
        imgRes = R.drawable.ic_ticket
    )
    val serviceSightSeeing = PlaceService(
        id = 7,
        name = if (isEnglish) context.getString(R.string.en_place_sight_seeing) else context.getString(
            R.string.vi_place_sight_seeing
        ),
        imgRes = R.drawable.ic_place_node
    )
}