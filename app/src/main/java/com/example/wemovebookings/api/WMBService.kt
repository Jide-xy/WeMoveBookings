package com.example.wemovebookings.api

import com.example.wemovebookings.BuildConfig
import com.example.wemovebookings.api.response.AirlineScheduleResponse
import com.example.wemovebookings.api.response.AirportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface WMBService {

    @Headers("Accept: application/json", "Authorization: Bearer ${BuildConfig.TOKEN}")
    @GET("v1/mds-references/airports?limit=100&lang=en")
    suspend fun getAirports(@Query("offset") offset: Int): Response<AirportResponse>

    @Headers("Accept: application/json", "Authorization: Bearer ${BuildConfig.TOKEN}")
    @GET("v1/operations/schedules/{origin}/{destination}/{date}?directFlights=0&limit=100")
    suspend fun getSchedules(
        @Path("origin") origin: String, @Path("destination") destination: String, @Path(
            "date"
        ) date: String
    ): Response<AirlineScheduleResponse>
}