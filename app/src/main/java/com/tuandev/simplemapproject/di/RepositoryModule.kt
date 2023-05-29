package com.tuandev.simplemapproject.di

import android.content.Context
import com.tuandev.simplemapproject.data.repositories.local.*
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFSRepository(): FireStoreRepository {
        return FireStoreRepository()
    }

    @Provides
    @Singleton
    fun provideZoneRepository(@ApplicationContext context: Context): ZoneRepository {
        return ZoneRepository(context)
    }

    @Provides
    @Singleton
    fun provideThrillLevelRepository(@ApplicationContext context: Context): ThrillLevelRepository {
        return ThrillLevelRepository(context)
    }

    @Provides
    @Singleton
    fun providePlaceServiceRepository(@ApplicationContext context: Context): PlaceServiceRepository {
        return PlaceServiceRepository(context)
    }

    @Provides
    @Singleton
    fun providePlaceRepository(
        @ApplicationContext context: Context,
        zoneRepository: ZoneRepository,
        placeServiceRepository: PlaceServiceRepository,
        gameRepository: GameRepository
    ): PlaceRepository {
        return PlaceRepository(context, zoneRepository, placeServiceRepository, gameRepository)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        @ApplicationContext context: Context,
        thrillRepository: ThrillLevelRepository
    ): GameRepository {
        return GameRepository(context, thrillRepository)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(
        zoneRepository: ZoneRepository,
        thrillRepository: ThrillLevelRepository,
        placeServiceRepository: PlaceServiceRepository,
        placeRepository: PlaceRepository,
    ): LocalRepository {
        return LocalRepository(zoneRepository, thrillRepository, placeServiceRepository, placeRepository)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RepositoryEntryPoint {
        fun getPlaceServiceRepository(): PlaceServiceRepository
    }

}