package com.example.wemovebookings.ui.main

import androidx.lifecycle.*
import com.dropbox.android.external.store4.StoreResponse
import com.example.wemovebookings.api.Resource
import com.example.wemovebookings.api.request.FlightRequestBody
import com.example.wemovebookings.repository.Repository
import com.example.wemovebookings.room.entities.AirlineSchedule
import com.example.wemovebookings.room.entities.Airport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _airportLiveData: MutableLiveData<Int> = MutableLiveData()
    val airportLiveData: LiveData<Resource<List<Airport>>> = _airportLiveData.switchMap {
        liveData<Resource<List<Airport>>> {
            emitSource(repository.getAirports(it).asLiveData())
        }
    }

    private val _flightsLiveData: MutableLiveData<FlightRequestBody> = MutableLiveData()
    val flightsLiveData: LiveData<Resource<List<AirlineSchedule>>> = _flightsLiveData.switchMap {
        liveData<Resource<List<AirlineSchedule>>> {
            emitSource(repository.getFlights(it).asLiveData())
        }
    }

    fun getAirports(offset: Int) {
        _airportLiveData.value = offset
    }

    fun getFlights(flights: FlightRequestBody) {
        _flightsLiveData.value = flights
    }
}
