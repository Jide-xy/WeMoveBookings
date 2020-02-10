package com.example.wemovebookings.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.wemovebookings.api.Resource
import com.example.wemovebookings.api.WMBService
import com.example.wemovebookings.api.response.AirportResponse
import com.example.wemovebookings.room.dao.AirportDao
import com.example.wemovebookings.room.db.WMBDatabase
import com.example.wemovebookings.room.entities.Airport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import retrofit2.Response

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(JUnit4::class)
class RepositoryTest {

    private lateinit var repository: Repository
    private val dao = mock(AirportDao::class.java)
    private val service = mock(WMBService::class.java)
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        val db = mock(WMBDatabase::class.java)
        `when`(db.airportDao()).thenReturn(dao)
        `when`(db.runInTransaction(ArgumentMatchers.any())).thenCallRealMethod()
        repository = Repository(db, service)
    }

    @Test
    fun loadRepoFromNetwork() = runBlocking {
        val airports = listOf(mock(Airport::class.java))
        val mData = flow {
            emit(airports)
        }
        `when`(dao.getAllAirports()).thenReturn(mData)

        val airportResponse = mock(AirportResponse::class.java)
        `when`(airportResponse.AirportResource.Airports.Airport[0].toAirport()).thenReturn(airports[0])
        val call = Response.success(airportResponse)
        `when`(service.getAirports()).thenReturn(call)

        val data = repository.getAirports()
        verify(dao).getAllAirports()
        verifyNoMoreInteractions(service)

        //Assert.assertThat(mData, CoreMatchers.`is`(data))

//        val observer = mock<Observer<Resource<Repo>>>()
//        data.observeForever(observer)
//        verifyNoMoreInteractions(service)
//        verify(observer).onChanged(Resource.loading(null))
//        val updatedDbData = MutableLiveData<Repo>()
//        `when`(dao.load("foo", "bar")).thenReturn(updatedDbData)
//
//        mData.postValue(null)
//        verify(service).getRepo("foo", "bar")
//        verify(dao).insert(repo)
//
//        updatedDbData.postValue(repo)
//        verify(observer).onChanged(Resource.success(repo))
    }
}

