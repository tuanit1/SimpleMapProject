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

    val placeStart = Place(
        id = 0,
        name = "Your start point",
        zone = zoneRepository.zoneVietnam
    )

    val placeFountain = Place(
        id = 1,
        name = if (isEnglish) context.getString(R.string.en_place_fountain) else context.getString(
            R.string.vi_place_fountain
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeEntryGate = Place(
        id = 2,
        name = if (isEnglish) context.getString(R.string.en_place_entry_gate) else context.getString(
            R.string.vi_place_entry_gate
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeSightSeeingCabin1 = Place(
        id = 3,
        name = if (isEnglish) context.getString(R.string.en_place_sightseeing_cabin1) else context.getString(
            R.string.vi_place_sightseeing_cabin
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceFood,
    )
    val placeSightSeeingCabin2 = Place(
        id = 38,
        name = if (isEnglish) context.getString(R.string.en_place_sightseeing_cabin2) else context.getString(
            R.string.vi_place_sightseeing_cabin
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceFood,
    )
    val placeNightMarket = Place(
        id = 4,
        name = if (isEnglish) context.getString(R.string.en_place_night_market) else context.getString(
            R.string.vi_place_night_market
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceFood,
    )
    val placeSunWheelStage = Place(
        id = 5,
        name = if (isEnglish) context.getString(R.string.en_place_sun_wheel_stage) else context.getString(
            R.string.vi_place_sun_wheel_stage
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeDragonBoat1 = Place(
        id = 6,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_boat1) else context.getString(
            R.string.vi_place_dragon_boat
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeDragonBoat2 = Place(
        id = 39,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_boat2) else context.getString(
            R.string.vi_place_dragon_boat
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeBuddhaStatue = Place(
        id = 7,
        name = if (isEnglish) context.getString(R.string.en_place_buddha_statue) else context.getString(
            R.string.vi_place_buddha_statue
        ),
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeDragonStatue = Place(
        id = 8,
        name = if (isEnglish) context.getString(R.string.en_place_dragon_statue) else context.getString(
            R.string.vi_place_dragon_statue
        ),
        zone = zoneRepository.zoneJapan,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeBambooGarden = Place(
        id = 9,
        name = if (isEnglish) context.getString(R.string.en_place_bamboo_garden) else context.getString(
            R.string.vi_place_bamboo_garden
        ),
        zone = zoneRepository.zoneJapan,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placePandaRestaurant = Place(
        id = 10,
        name = if (isEnglish) context.getString(R.string.en_place_panda_restaurant) else context.getString(
            R.string.vi_place_panda_restaurant
        ),
        zone = zoneRepository.zoneChina,
        serviceType = serviceRepository.serviceFood,
    )
    val placeIndiaStage = Place(
        id = 11,
        name = if (isEnglish) context.getString(R.string.en_place_india_stage) else context.getString(
            R.string.vi_place_india_stage
        ),
        zone = zoneRepository.zoneIndia,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeTheDark = Place(
        id = 12,
        name = if (isEnglish) context.getString(R.string.en_place_the_dark) else context.getString(
            R.string.vi_place_the_dark
        ),
        zone = zoneRepository.zoneCambodia,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeWaterPlayground = Place(
        id = 13,
        name = if (isEnglish) context.getString(R.string.en_place_water_playground) else context.getString(
            R.string.vi_place_water_playground
        ),
        zone = zoneRepository.zoneThailand,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeNepalGate = Place(
        id = 14,
        name = if (isEnglish) context.getString(R.string.en_place_nepal_gate) else context.getString(
            R.string.vi_place_nepal_gate
        ),
        zone = zoneRepository.zoneNepal,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeMarinaStage = Place(
        id = 15,
        name = if (isEnglish) context.getString(R.string.en_place_marina_stage) else context.getString(
            R.string.vi_place_marina_stage
        ),
        zone = zoneRepository.zoneSingapore,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeMerlionLake = Place(
        id = 16,
        name = if (isEnglish) context.getString(R.string.en_place_merlion_lake) else context.getString(
            R.string.vi_place_merlion_lake
        ),
        zone = zoneRepository.zoneSingapore,
        serviceType = serviceRepository.serviceSightSeeing,
    )
    val placeIndoRestaurant = Place(
        id = 17,
        name = if (isEnglish) context.getString(R.string.en_place_indo_restaurant) else context.getString(
            R.string.vi_place_indo_restaurant
        ),
        zone = zoneRepository.zoneIndonesia,
        serviceType = serviceRepository.serviceFood,
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
    val placeWC1 = Place(
        id = 40,
        name = "Toilet/WC 1",
        zone = zoneRepository.zoneJapan,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC2 = Place(
        id = 41,
        name = "Toilet/WC 2",
        zone = zoneRepository.zoneIndia,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC3 = Place(
        id = 42,
        name = "Toilet/WC 3",
        zone = zoneRepository.zoneIndia,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC4 = Place(
        id = 43,
        name = "Toilet/WC 4",
        zone = zoneRepository.zoneSingapore,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC5 = Place(
        id = 44,
        name = "Toilet/WC 5",
        zone = zoneRepository.zoneIndonesia,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC6 = Place(
        id = 45,
        name = "Toilet/WC 6",
        zone = zoneRepository.zoneIndonesia,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC7 = Place(
        id = 50,
        name = "Toilet/WC 7",
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceWC,
    )
    val placeWC8 = Place(
        id = 51,
        name = "Toilet/WC 8",
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceWC,
    )
    val placeTicket1 = Place(
        id = 46,
        name = "Ticket counter 1",
        zone = zoneRepository.zoneVietnam,
        serviceType = serviceRepository.serviceTicket,
    )
    val placeTicket2 = Place(
        id = 47,
        name = "Ticket counter 2",
        zone = zoneRepository.zoneChina,
        serviceType = serviceRepository.serviceTicket,
    )
    val placeTicket3 = Place(
        id = 48,
        name = "Ticket counter 3",
        zone = zoneRepository.zoneSingapore,
        serviceType = serviceRepository.serviceTicket,
    )
    val placeTicket4 = Place(
        id = 49,
        name = "Ticket counter 4",
        zone = zoneRepository.zoneIndonesia,
        serviceType = serviceRepository.serviceTicket,
    )
}