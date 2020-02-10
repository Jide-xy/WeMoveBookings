package com.example.wemovebookings.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wemovebookings.api.response.Schedule

@Entity(primaryKeys = ["Departure_AirportCode", "Arrival_AirportCode", "sourceToDestination"])
data class AirlineSchedule(@Embedded val schedule: Schedule, val sourceToDestination: String)