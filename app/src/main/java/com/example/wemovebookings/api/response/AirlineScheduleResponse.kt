package com.example.wemovebookings.api.response

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class AirlineScheduleResponse(val ScheduleResource: ScheduleResource)

data class ScheduleResource(val Schedule: List<Schedule>, val Meta: Meta)

data class Schedule(
    @Embedded val TotalJourney: TotalJourney,
    @Embedded val Flight: Flight
)

data class TotalJourney(val Duration: String)

data class Flight(
    @Embedded(prefix = "Departure_") val Departure: Departure,
    @Embedded(prefix = "Arrival_") val Arrival: Arrival,
    @Embedded(prefix = "MarketingCarrier_") val MarketingCarrier: MarketingCarrier,
    @Embedded(prefix = "OperatingCarrier_") val OperatingCarrier: OperatingCarrier?,
    @Embedded val Equipment: Equipment,
    @Embedded val Details: Details
)

data class Departure(val AirportCode: String, @Embedded val ScheduledTimeLocal: ScheduledTimeLocal)

data class ScheduledTimeLocal(val DateTime: String)

data class Arrival(val AirportCode: String, @Embedded val ScheduledTimeLocal: ScheduledTimeLocal, @Embedded val Terminal: Terminal?)

data class Terminal(val Name: String?)

data class MarketingCarrier(val AirlineID: String, val FlightNumber: String)

data class OperatingCarrier(val AirlineID: String?)

data class Equipment(val AircraftCode: String)

data class Details(@Embedded val Stops: Stops, val DaysOfOperation: String, @Embedded val DatePeriod: DatePeriod)

data class Stops(val StopQuantity: Int)

data class DatePeriod(val Effective: String, val Expiration: String)

