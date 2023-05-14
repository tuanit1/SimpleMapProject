package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.Place
import com.tuandev.simplemapproject.data.models.PlaceService
import com.tuandev.simplemapproject.data.models.ThrillLevel
import com.tuandev.simplemapproject.data.models.Zone
import com.tuandev.simplemapproject.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalRepository @Inject constructor(
    @ApplicationContext context: Context,
    zoneRepository: ZoneRepository,
    thrillRepository: ThrillLevelRepository
) {


    val listThrillLevel = thrillRepository.run {
        listOf(
            thrillLevelFamily,
            thrillLevelModerate,
            thrillLevelHigh,
            thrillLevelExtreme
        )
    }

    private val serviceWC = PlaceService(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_place_service_wc) else context.getString(
            R.string.vi_place_service_wc
        )
    )
    private val serviceSouvenir = PlaceService(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_place_service_souvenir) else context.getString(
            R.string.vi_place_service_souvenir
        )
    )
    private val serviceGuest = PlaceService(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_place_service_guest) else context.getString(
            R.string.vi_place_service_guest
        )
    )
    private val serviceFood = PlaceService(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_place_service_food) else context.getString(
            R.string.vi_place_service_food
        )
    )
    private val serviceMedical = PlaceService(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_place_service_medical) else context.getString(
            R.string.vi_place_service_medical
        )
    )
    private val serviceTicket = PlaceService(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_place_service_ticket) else context.getString(
            R.string.vi_place_service_ticket
        )
    )

    val listService = listOf(
        serviceWC,
        serviceSouvenir,
        serviceGuest,
        serviceFood,
        serviceMedical,
        serviceTicket
    )

    val listZone = zoneRepository.run {
        listOf(
            zoneVietnam,
            zoneJapan,
            zoneKorea,
            zoneChina,
            zoneIndia,
            zoneCambodia,
            zoneThailand,
            zoneNepal,
            zoneSingapore,
            zoneIndonesia
        )
    }

    private val placeFountain = Place(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_place_fountain) else context.getString(
            R.string.vi_place_fountain
        ),
        zone = zoneVietnam
    )
    private val placeEntryGate = Place(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_place_entry_gate) else context.getString(
            R.string.vi_place_entry_gate
        ),
        zone = zoneVietnam,
        listService = listOf(serviceGuest, serviceSouvenir, serviceTicket),
    )
    private val placeSightSeeingCabin = Place(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_place_sightseeing_cabin) else context.getString(
            R.string.vi_place_sightseeing_cabin
        ),
        zone = zoneVietnam,
        listService = listOf(serviceFood),
    )
    private val placeNightMarket = Place(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_place_night_market) else context.getString(
            R.string.vi_place_night_market
        ),
        zone = zoneVietnam,
        listService = listOf(serviceFood),
    )
    private val placeSunWheelStage= Place(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_place_sun_wheel_stage) else context.getString(
            R.string.vi_place_sun_wheel_stage
        ),
        zone = zoneVietnam
    )
    private val placeDragonBoat= Place(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_boat) else context.getString(
            R.string.vi_place_dragon_boat
        ),
        zone = zoneVietnam
    )
    private val placeBuddhaStatue= Place(
        id = 7,
        name = if (isEnglish) context.getString(R.string.en_place_buddha_statue) else context.getString(
            R.string.vi_place_buddha_statue
        ),
        zone = zoneVietnam
    )
    private val placeDragonStatue= Place(
        id = 8,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_statue) else context.getString(
            R.string.vi_place_dragon_statue
        ),
        zone = zoneJapan
    )
    private val placeBambooGarden= Place(
        id = 9,
        name = if (isEnglish) context.getString(R.string.en_place_bamboo_garden) else context.getString(
            R.string.vi_place_bamboo_garden
        ),
        zone = zoneJapan
    )
    private val placePandaRestaurant= Place(
        id = 10,
        name = if (isEnglish) context.getString(R.string.en_place_panda_restaurant) else context.getString(
            R.string.vi_place_panda_restaurant
        ),
        zone = zoneChina,
        listService = listOf(serviceFood)
    )
    private val placeIndiaStage= Place(
        id = 11,
        name = if (isEnglish) context.getString(R.string.en_place_india_stage) else context.getString(
            R.string.vi_place_india_stage
        ),
        zone = zoneIndia
    )
    private val placeTheDark = Place(
        id = 12,
        name = if (isEnglish) context.getString(R.string.en_place_the_dark) else context.getString(
            R.string.vi_place_the_dark
        ),
        zone = zoneCambodia
    )
    private val placeWaterPlayground = Place(
        id = 13,
        name = if (isEnglish) context.getString(R.string.en_place_water_playground) else context.getString(
            R.string.vi_place_water_playground
        ),
        zone = zoneThailand
    )
    private val placeNepalGate = Place(
        id = 14,
        name = if (isEnglish) context.getString(R.string.en_place_nepal_gate) else context.getString(
            R.string.vi_place_nepal_gate
        ),
        zone = zoneNepal
    )
    private val placeMarinaStage = Place(
        id = 15,
        name = if (isEnglish) context.getString(R.string.en_place_marina_stage) else context.getString(
            R.string.vi_place_marina_stage
        ),
        zone = zoneSingapore
    )
    private val placeMerlionLake = Place(
        id = 16,
        name = if (isEnglish) context.getString(R.string.en_place_merlion_lake) else context.getString(
            R.string.vi_place_merlion_lake
        ),
        zone = zoneSingapore
    )
    private val placeIndoRestaurant = Place(
        id = 17,
        name = if (isEnglish) context.getString(R.string.en_place_indo_restaurant) else context.getString(
            R.string.vi_place_indo_restaurant
        ),
        zone = zoneIndonesia
    )

    val listPlace by lazy {
        when (language) {
            Constants.LANGUAGE_VI -> {

            }
            Constants.LANGUAGE_EN -> {

            }
        }
    }
}