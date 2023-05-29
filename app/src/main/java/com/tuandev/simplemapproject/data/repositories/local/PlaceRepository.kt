package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.Place
import com.tuandev.simplemapproject.util.Constants

class PlaceRepository(
    context: Context,
    zoneRepository: ZoneRepository,
    serviceRepository: PlaceServiceRepository,
    gameRepository: GameRepository
) {
    private val language = Constants.LANGUAGE_EN
    private val isEnglish = language == Constants.LANGUAGE_EN

    val placeFountain = Place(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_place_fountain) else context.getString(
            R.string.vi_place_fountain
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeEntryGate = Place(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_place_entry_gate) else context.getString(
            R.string.vi_place_entry_gate
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceGuest, serviceSouvenir, serviceTicket) }
    )
    val placeSightSeeingCabin = Place(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_place_sightseeing_cabin) else context.getString(
            R.string.vi_place_sightseeing_cabin
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceFood) }
    )
    val placeNightMarket = Place(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_place_night_market) else context.getString(
            R.string.vi_place_night_market
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceFood) }
    )
    val placeSunWheelStage = Place(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_place_sun_wheel_stage) else context.getString(
            R.string.vi_place_sun_wheel_stage
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeDragonBoat = Place(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_boat) else context.getString(
            R.string.vi_place_dragon_boat
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeBuddhaStatue = Place(
        id = 7,
        name = if (isEnglish) context.getString(R.string.en_place_buddha_statue) else context.getString(
            R.string.vi_place_buddha_statue
        ),
        zone = zoneRepository.zoneVietnam,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeDragonStatue = Place(
        id = 8,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_statue) else context.getString(
            R.string.vi_place_dragon_statue
        ),
        zone = zoneRepository.zoneJapan,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeBambooGarden = Place(
        id = 9,
        name = if (isEnglish) context.getString(R.string.en_place_bamboo_garden) else context.getString(
            R.string.vi_place_bamboo_garden
        ),
        zone = zoneRepository.zoneJapan,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placePandaRestaurant = Place(
        id = 10,
        name = if (isEnglish) context.getString(R.string.en_place_panda_restaurant) else context.getString(
            R.string.vi_place_panda_restaurant
        ),
        zone = zoneRepository.zoneChina,
        listService = serviceRepository.run { listOf(serviceFood) }
    )
    val placeIndiaStage = Place(
        id = 11,
        name = if (isEnglish) context.getString(R.string.en_place_india_stage) else context.getString(
            R.string.vi_place_india_stage
        ),
        zone = zoneRepository.zoneIndia,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeTheDark = Place(
        id = 12,
        name = if (isEnglish) context.getString(R.string.en_place_the_dark) else context.getString(
            R.string.vi_place_the_dark
        ),
        zone = zoneRepository.zoneCambodia,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeWaterPlayground = Place(
        id = 13,
        name = if (isEnglish) context.getString(R.string.en_place_water_playground) else context.getString(
            R.string.vi_place_water_playground
        ),
        zone = zoneRepository.zoneThailand,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeNepalGate = Place(
        id = 14,
        name = if (isEnglish) context.getString(R.string.en_place_nepal_gate) else context.getString(
            R.string.vi_place_nepal_gate
        ),
        zone = zoneRepository.zoneNepal,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeMarinaStage = Place(
        id = 15,
        name = if (isEnglish) context.getString(R.string.en_place_marina_stage) else context.getString(
            R.string.vi_place_marina_stage
        ),
        zone = zoneRepository.zoneSingapore,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeMerlionLake = Place(
        id = 16,
        name = if (isEnglish) context.getString(R.string.en_place_merlion_lake) else context.getString(
            R.string.vi_place_merlion_lake
        ),
        zone = zoneRepository.zoneSingapore,
        listService = serviceRepository.run { listOf(serviceSightSeeing) }
    )
    val placeIndoRestaurant = Place(
        id = 17,
        name = if (isEnglish) context.getString(R.string.en_place_indo_restaurant) else context.getString(
            R.string.vi_place_indo_restaurant
        ),
        zone = zoneRepository.zoneIndonesia,
        listService = serviceRepository.run { listOf(serviceFood) }
    )
    val gamePlaceSunWheel = Place(
        id = 18,
        zone = zoneRepository.zoneVietnam,
        game = gameRepository.gameSunWheel
    )
    val gamePlaceNinjaFlyer = Place(
        id = 19,
        zone = zoneRepository.zoneJapan,
        game = gameRepository.gameNinjaFlyer
    )
    val gamePlaceKabukiTrucks = Place(
        id = 20,
        zone = zoneRepository.zoneJapan,
        game = gameRepository.gameKabukiTrucks
    )
    val gamePlaceFirefliesForest = Place(
        id = 21,
        zone = zoneRepository.zoneJapan,
        game = gameRepository.gameFirefliesForest
    )
    val gamePlaceHappyChooChoo = Place(
        id = 22,
        zone = zoneRepository.zoneKorea,
        game = gameRepository.gameHappyChooChoo
    )
    val gamePlaceLoveLocks = Place(
        id = 23,
        zone = zoneRepository.zoneKorea,
        game = gameRepository.gameLoveLocks
    )
    val gamePlaceParadiseFall = Place(
        id = 24,
        zone = zoneRepository.zoneJapan,
        game = gameRepository.gameParadiseFall
    )
    val gamePlaceFairyTeaHouse = Place(
        id = 25,
        zone = zoneRepository.zoneChina,
        game = gameRepository.gameFairyTeaHouse
    )
    val gamePlaceJourneyToTheWest = Place(
        id = 26,
        zone = zoneRepository.zoneChina,
        game = gameRepository.gameJourneyToTheWest
    )
    val gamePlaceShanghai1920 = Place(
        id = 27,
        zone = zoneRepository.zoneChina,
        game = gameRepository.gameShanghai1920
    )
    val gamePlaceFlyingKirins = Place(
        id = 28,
        zone = zoneRepository.zoneChina,
        game = gameRepository.gameFlyingKirins
    )
    val gamePlaceQueenCobra = Place(
        id = 29,
        zone = zoneRepository.zoneIndia,
        game = gameRepository.gameQueenCobra
    )
    val gamePlaceGoldenSkyTower = Place(
        id = 30,
        zone = zoneRepository.zoneThailand,
        game = gameRepository.gameGoldenSkyTower
    )
    val gamePlaceHighwayBoat = Place(
        id = 31,
        zone = zoneRepository.zoneThailand,
        game = gameRepository.gameHighwayBoat
    )
    val gamePlaceSingaporeSling = Place(
        id = 32,
        zone = zoneRepository.zoneSingapore,
        game = gameRepository.gameSingaporeSling
    )
    val gamePlacePortOfSkyTreasure = Place(
        id = 33,
        zone = zoneRepository.zoneSingapore,
        game = gameRepository.gamePortOfSkyTreasure
    )
    val gamePlaceAngryMotors = Place(
        id = 34,
        zone = zoneRepository.zoneIndonesia,
        game = gameRepository.gameAngryMotors
    )
    val gamePlaceDinoIsland = Place(
        id = 35,
        zone = zoneRepository.zoneIndonesia,
        game = gameRepository.gameDinoIsland
    )
    val gamePlaceFestivalCarousel = Place(
        id = 36,
        zone = zoneRepository.zoneIndonesia,
        game = gameRepository.gameFestivalCarousel
    )
    val gamePlaceGarudaValley = Place(
        id = 37,
        zone = zoneRepository.zoneIndonesia,
        game = gameRepository.gameGarudaValley
    )
}