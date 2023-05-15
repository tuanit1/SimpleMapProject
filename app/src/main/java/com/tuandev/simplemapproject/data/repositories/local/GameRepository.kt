package com.tuandev.simplemapproject.data.repositories.local

import android.content.Context
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.Game

class GameRepository(
    context: Context,
    thrillLevelRepository: ThrillLevelRepository
) {
    val gameGoldenSkyTower = Game(
        id = 1,
        name = context.getString(R.string.game_golden_sky_tower),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameFairyTeaHouse = Game(
        id = 2,
        name = context.getString(R.string.game_fairy_tea_house),
        thrillLevel = thrillLevelRepository.thrillLevelMedium
    )
    val gameSingaporeSling = Game(
        id = 3,
        name = context.getString(R.string.game_singapore_sling),
        thrillLevel = thrillLevelRepository.thrillLevelHigh,
        isAvailable = false
    )
    val gameHighwayBoat = Game(
        id = 4,
        name = context.getString(R.string.game_highway_boat),
        thrillLevel = thrillLevelRepository.thrillLevelHigh,
        isAvailable = false
    )
    val gameQueenCobra = Game(
        id = 5,
        name = context.getString(R.string.game_queen_cobra),
        thrillLevel = thrillLevelRepository.thrillLevelExtreme,
        duration = 96
    )
    val gamePortOfSkyTreasure = Game(
        id = 6,
        name = context.getString(R.string.game_port_of_sky_treasure),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameParadiseFall = Game(
        id = 7,
        name = context.getString(R.string.game_paradise_fall),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameLoveLocks = Game(
        id = 8,
        name = context.getString(R.string.game_love_locks),
        thrillLevel = thrillLevelRepository.thrillLevelMedium
    )
    val gameFlyingKirins = Game(
        id = 9,
        name = context.getString(R.string.game_flying_kirins),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameJourneyToTheWest = Game(
        id = 10,
        name = context.getString(R.string.game_journey_to_the_west),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameKabukiTrucks = Game(
        id = 11,
        name = context.getString(R.string.game_kabuki_trucks),
        thrillLevel = thrillLevelRepository.thrillLevelMedium
    )
    val gameNinjaFlyer = Game(
        id = 12,
        name = context.getString(R.string.game_ninja_flyer),
        thrillLevel = thrillLevelRepository.thrillLevelHigh
    )
    val gameFirefliesForest = Game(
        id = 13,
        name = context.getString(R.string.game_fireflies_forest),
        thrillLevel = thrillLevelRepository.thrillLevelMedium,
        duration = 90
    )
    val gameShanghai1920 = Game(
        id = 14,
        name = context.getString(R.string.game_shanghai_1920),
        thrillLevel = thrillLevelRepository.thrillLevelFamily
    )
    val gameGarudaValley = Game(
        id = 15,
        name = context.getString(R.string.game_garuda_valley),
        thrillLevel = thrillLevelRepository.thrillLevelMedium,
        duration = 90
    )
    val gameAngryMotors = Game(
        id = 16,
        name = context.getString(R.string.game_angry_motors),
        thrillLevel = thrillLevelRepository.thrillLevelFamily
    )
    val gameFestivalCarousel = Game(
        id = 17,
        name = context.getString(R.string.game_festival_carousel),
        thrillLevel = thrillLevelRepository.thrillLevelFamily
    )
    val gameHappyChooChoo = Game(
        id = 18,
        name = context.getString(R.string.game_happy_choo_choo),
        thrillLevel = thrillLevelRepository.thrillLevelFamily
    )
    val gameDinoIsland = Game(
        id = 19,
        name = context.getString(R.string.game_dino_island),
        thrillLevel = thrillLevelRepository.thrillLevelFamily,
        duration = 90
    )
    val gameSunWheel = Game(
        id = 20,
        name = context.getString(R.string.game_sun_wheel),
        thrillLevel = thrillLevelRepository.thrillLevelMedium,
        duration = 900
    )
}