package com.example.wemovebookings.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class WMBServiceTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: WMBService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            //.addCallAdapterFactory(CoroutinesNetworkResponseAdapterFactory)
            .build()
            .create(WMBService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun getAirports() = runBlocking {
        enqueueResponse("airports-response.json")
        val airports = service.getAirports().body()

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/v1/mds-references/airports?limit=100"))

        assertThat(airports, notNullValue())

        assertThat(airports!!.AirportResource.Airports.Airport.size, `is`(20))

        val airport = airports.AirportResource.Airports.Airport[0]
        assertThat(airport.AirportCode, `is`("AAA"))

        val position = airport.Position
        assertThat(position.Coordinate.Latitude, `is`(-17.3525))
        assertThat(position.Coordinate.Longitude, `is`(-145.51))

        val airport2 = airports.AirportResource.Airports.Airport[1]
        assertThat(airport2.AirportCode, `is`("AAB"))
    }

    @Test
    fun getAirlineSchedule() = runBlocking {
        enqueueResponse("airline-schedules-response.json")
        val origin = "AAA"
        val dest = "AAB"
        val date = "20-02-2020"
        val schedules = service.getSchedules(origin, dest, date).body()

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/v1/operations/schedules/${origin}/${dest}/${date}"))

        assertThat(schedules, notNullValue())

        assertThat(schedules!!.ScheduleResource.Schedule.size, `is`(5))

        val schedule = schedules.ScheduleResource.Schedule[0]
        assertThat(schedule.TotalJourney.Duration, `is`("PT1H10M"))

        val flight = schedule.Flight
        assertThat(flight.Departure.AirportCode, `is`("ZRH"))
        assertThat(flight.Arrival.AirportCode, `is`("FRA"))
        assertThat(flight.Arrival.Terminal.Name, `is`("1"))

        val schedule2 = schedules.ScheduleResource.Schedule[1]
        assertThat(schedule2.TotalJourney.Duration, `is`("PT1H5M"))
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!.getResourceAsStream(fileName)

        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }
}