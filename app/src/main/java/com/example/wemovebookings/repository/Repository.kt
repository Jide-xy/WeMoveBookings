package com.example.wemovebookings.repository

import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.example.wemovebookings.api.CustomNetworkException
import com.example.wemovebookings.api.Resource
import com.example.wemovebookings.api.WMBService
import com.example.wemovebookings.api.request.FlightRequestBody
import com.example.wemovebookings.room.db.WMBDatabase
import com.example.wemovebookings.room.entities.AirlineSchedule
import com.example.wemovebookings.room.entities.Airport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
open class Repository @Inject constructor(
    private val localDb: WMBDatabase,
    private val remoteService: WMBService
) {


    suspend fun getAirports(offset: Int): Flow<Resource<List<Airport>>> {
        return StoreBuilder.from<String, List<Airport>> {
            processResponse {
                remoteService.getAirports(offset)
            }.map {
                it.AirportResource.Airports.Airport.map { airportBody -> airportBody.toAirport() }
            }
        }
            .persister(
                reader = { localDb.airportDao().getAllAirports() },
                writer = { _: String, airport: List<Airport> ->
                    localDb.airportDao().saveAirports(airport)
                }
            )
            .build().stream(StoreRequest.cached("", true)).map {
                when (it) {
                    is StoreResponse.Loading -> Resource.loading(null)
                    is StoreResponse.Data -> Resource.success(it.value)
                    is StoreResponse.Error -> Resource.error(it.error.message.toString())
                }
            }
    }

    suspend fun getFlights(flightRequestBody: FlightRequestBody): Flow<Resource<List<AirlineSchedule>>> {
        return StoreBuilder.from<FlightRequestBody, List<AirlineSchedule>> { body ->
            processResponse {
                remoteService.getSchedules(body.origin, body.destination, body.date)
            }.map {
                it.ScheduleResource.Schedule.map { schedule ->
                    AirlineSchedule(
                        schedule,
                        "${body.origin}-${body.destination}"
                    )
                }
            }
        }
            .persister(
                reader = { localDb.flightDao().getFlights("${it.origin}-${it.destination}") },
                writer = { _: FlightRequestBody, flights: List<AirlineSchedule> ->
                    localDb.flightDao().saveFlights(flights)
                },
                deleteAll = { localDb.flightDao().deleteAllFlights() }
            )
            .build().stream(StoreRequest.cached(flightRequestBody, true)).map {
                when (it) {
                    is StoreResponse.Loading -> Resource.loading(null)
                    is StoreResponse.Data -> Resource.success(it.value)
                    is StoreResponse.Error -> Resource.error(it.error.message.toString())
                }
            }
    }

    private fun <ResponseType> processResponse(
        block: suspend () -> Response<ResponseType>
    ): Flow<ResponseType> {

        return flow {
            try {
                val response = block()
                if (response.isSuccessful) {
                    emit(response.body() ?: throw CustomNetworkException("Null response"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    throw CustomNetworkException(if (errorBody.isNullOrEmpty()) "An error occurred" else errorBody)
//                        emit(
//                            Resource.error(if (response.errorBody()?.string().isNullOrEmpty()) "An error occurred" else response.errorBody()!!.string())
//                        )
                }
            } catch (e: Exception) {
                throw CustomNetworkException(e.message.toString(), e)
                //emit(Resource.error(e.message.toString()))
            }
        }
    }
}