package com.example.wemovebookings.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Airport(
    @PrimaryKey val AirportCode: String,
    @Embedded val Position: Position,
    val CityCode: String,
    val CountryCode: String,
    val LocationType: String,
    @Embedded val Name: Name,
    val UtcOffset: String,
    val TimeZoneId: String
)

data class Position(@Embedded val Coordinate: Coordinate)

data class Coordinate(val Latitude: Double, val Longitude: Double)

data class Name(
    @SerializedName("@LanguageCode") val LanguageCode: String,
    @SerializedName("$") val Name: String
)
