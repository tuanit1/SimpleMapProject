package com.tuandev.simplemapproject.data.repositories.local

class LocalRepository(
    zoneRepository: ZoneRepository,
    thrillRepository: ThrillLevelRepository,
    placeServiceRepository: PlaceServiceRepository,
    placeRepository: PlaceRepository
) {
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
            placeSightSeeingCabin,
            placeNightMarket,
            placeSunWheelStage,
            placeDragonBoat,
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
            gamePlaceGarudaValley
        )
    }
}