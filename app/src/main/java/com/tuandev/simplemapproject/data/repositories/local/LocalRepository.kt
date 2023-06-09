package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.google.gson.Gson
import com.tuandev.simplemapproject.data.models.UserFeature

class LocalRepository(
    context: Context,
    zoneRepository: ZoneRepository,
    thrillRepository: ThrillLevelRepository,
    placeServiceRepository: PlaceServiceRepository,
    placeRepository: PlaceRepository
) {

    companion object {
        private const val KEY_SHARED_PREFERENCE = "shared_preference_key"
        private const val KEY_USER_FEATURE = "shared_user_feature"
    }

    private val sharedPref =
        context.getSharedPreferences(KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE)

    val listThrillLevel = thrillRepository.run {
        listOf(
            thrillLevelFamily,
            thrillLevelMedium,
            thrillLevelHigh,
            thrillLevelExtreme
        )
    }
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
    val listPlaceService = placeServiceRepository.run {
        listOf(
            serviceWC,
            serviceSouvenir,
            serviceGuest,
            serviceFood,
            serviceMedical,
            serviceTicket
        )
    }
    val listPlace = placeRepository.run {
        listOf(
            placeFountain,
            placeEntryGate,
            placeSightSeeingCabin1,
            placeSightSeeingCabin2,
            placeNightMarket,
            placeSunWheelStage,
            placeDragonBoat1,
            placeDragonBoat2,
            placeBuddhaStatue,
            placeDragonStatue,
            placeBambooGarden,
            placePandaRestaurant,
            placeIndiaStage,
            placeTheDark,
            placeWaterPlayground,
            placeNepalGate,
            placeMarinaStage,
            placeMerlionLake,
            placeIndoRestaurant,
            gamePlaceSunWheel,
            gamePlaceNinjaFlyer,
            gamePlaceKabukiTrucks,
            gamePlaceFirefliesForest,
            gamePlaceHappyChooChoo,
            gamePlaceLoveLocks,
            gamePlaceParadiseFall,
            gamePlaceFairyTeaHouse,
            gamePlaceJourneyToTheWest,
            gamePlaceShanghai1920,
            gamePlaceFlyingKirins,
            gamePlaceQueenCobra,
            gamePlaceGoldenSkyTower,
            gamePlaceHighwayBoat,
            gamePlaceSingaporeSling,
            gamePlacePortOfSkyTreasure,
            gamePlaceAngryMotors,
            gamePlaceDinoIsland,
            gamePlaceFestivalCarousel,
            gamePlaceGarudaValley,
            placeWC1,
            placeWC2,
            placeWC3,
            placeWC4,
            placeWC5,
            placeWC6,
            placeWC7,
            placeWC8,
            placeWC9,
            placeExitGate,
            placeSouvenirShop,
            placeGuestService,
            placeTicket1,
            placeTicket2,
            placeTicket3,
            placeTicket4,
            placeTicket5,
            placeMedicalHelp
        )
    }

    fun saveUserFeature(userFeature: UserFeature) {
        with(sharedPref.edit()) {
            putString(KEY_USER_FEATURE, Gson().toJson(userFeature))
            apply()
        }
    }

    fun getUserFeature(): UserFeature? {
        return try {
            sharedPref?.getString(KEY_USER_FEATURE, null)?.let {
                Gson().fromJson(it, UserFeature::class.java)
            }
        } catch (_: Exception) {
            null
        }
    }
}