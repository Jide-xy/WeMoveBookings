package com.example.wemovebookings.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wemovebookings.room.db.WMBDatabase
import com.example.wemovebookings.room.entities.Airport
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AirportDao() {

    @Query("SELECT * FROM Airport")
    abstract fun getAllAirports(): Flow<List<Airport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveAirports(airports: List<Airport>)
}