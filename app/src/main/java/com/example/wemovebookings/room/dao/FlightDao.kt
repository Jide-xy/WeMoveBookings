package com.example.wemovebookings.room.dao

import androidx.room.*
import com.example.wemovebookings.api.response.Schedule
import com.example.wemovebookings.room.entities.AirlineSchedule
import com.example.wemovebookings.room.entities.Airport
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FlightDao {

//    @Query("SELECT * FROM AirlineSchedule WHERE Departure_AirportCode = :origin AND Arrival_AirportCode = :destination")
//    abstract fun getFlights(origin: String, destination: String): Flow<List<AirlineSchedule>>

    @Query("SELECT * FROM AirlineSchedule WHERE sourceToDestination = :sourceToDestination")
    abstract fun getFlights(sourceToDestination: String): Flow<List<AirlineSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveFlights(flights: List<AirlineSchedule>)

    @Query("DELETE FROM AirlineSchedule")
    abstract suspend fun deleteAllFlights()

}