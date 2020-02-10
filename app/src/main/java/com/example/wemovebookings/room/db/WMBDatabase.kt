package com.example.wemovebookings.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wemovebookings.api.response.Schedule
import com.example.wemovebookings.room.dao.AirportDao
import com.example.wemovebookings.room.dao.FlightDao
import com.example.wemovebookings.room.entities.AirlineSchedule
import com.example.wemovebookings.room.entities.Airport

/**
 * Created by Babajide Mustapha on 9/18/2017.
 */
const val name = "WMB_DB"

@Database(
    entities = [Airport::class, AirlineSchedule::class],
    version = 4
)
abstract class WMBDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun flightDao(): FlightDao

    companion object {

        @Volatile
        private var INSTANCE: WMBDatabase? = null

        fun getInstance(context: Context): WMBDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WMBDatabase::class.java,
                name
            ).fallbackToDestructiveMigration().build()

    }
}
