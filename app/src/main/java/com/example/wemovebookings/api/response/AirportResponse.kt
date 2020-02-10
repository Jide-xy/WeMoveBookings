package com.example.wemovebookings.api.response

import com.example.wemovebookings.room.entities.Airport
import com.example.wemovebookings.room.entities.Name
import com.example.wemovebookings.room.entities.Position
import com.google.gson.annotations.SerializedName


data class AirportResponse(val AirportResource: AirportResource)

data class AirportResource(val Airports: Airports, val Meta: Meta)

data class Airports(val Airport: List<AirportBody>)

data class AirportBody(
    val AirportCode: String,
    val Position: Position,
    val CityCode: String,
    val CountryCode: String,
    val LocationType: String,
    val Names: Names,
    val UtcOffset: String,
    val TimeZoneId: String
) {

    fun toAirport() = Airport(
        AirportCode,
        Position, CityCode, CountryCode, LocationType,
        Names.Name, UtcOffset, TimeZoneId
    )
}

data class Names(val Name: Name)

data class Meta(
    @SerializedName("@Version") val Version: String,
    val Link: List<Link>,
    val TotalCount: Int
)

data class Link(
    @SerializedName("@Href") val Href: String,
    @SerializedName("@Rel") val Rel: String
)