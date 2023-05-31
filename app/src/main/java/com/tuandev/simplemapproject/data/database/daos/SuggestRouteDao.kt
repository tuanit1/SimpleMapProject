package com.tuandev.simplemapproject.data.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tuandev.simplemapproject.data.database.entities.SaveSuggestRoute

@Dao
interface SuggestRouteDao {
    @Query("SELECT * FROM SaveSuggestRoute")
    fun getAll(): LiveData<List<SaveSuggestRoute>>

    @Query("DELETE FROM SaveSuggestRoute")
    fun deleteAll()

    @Insert
    fun insertAll(suggestList: List<SaveSuggestRoute>)
}