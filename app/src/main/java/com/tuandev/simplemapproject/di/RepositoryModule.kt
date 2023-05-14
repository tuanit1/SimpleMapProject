package com.tuandev.simplemapproject.di

import android.content.Context
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.local.ThrillLevelRepository
import com.tuandev.simplemapproject.data.repositories.local.ZoneRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import dagger.Module
import dagger.Provides
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
    fun provideLocalRepository(
        @ApplicationContext context: Context,
        zoneRepository: ZoneRepository,
        thrillRepository: ThrillLevelRepository
    ): LocalRepository {
        return LocalRepository(context, zoneRepository, thrillRepository)
    }

}