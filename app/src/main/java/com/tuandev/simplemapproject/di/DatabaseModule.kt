package com.tuandev.simplemapproject.di

import android.content.Context
import androidx.room.Room
import com.tuandev.simplemapproject.data.database.AppDatabase
import com.tuandev.simplemapproject.data.database.daos.LineDao
import com.tuandev.simplemapproject.data.database.daos.NodeDao
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "db").build()
    }

    @Provides
    @Singleton
    fun provideLineDao(appDatabase: AppDatabase): LineDao {
        return appDatabase.lineDao()
    }

    @Provides
    @Singleton
    fun provideNodeDao(appDatabase: AppDatabase): NodeDao {
        return appDatabase.nodeDao()
    }
}