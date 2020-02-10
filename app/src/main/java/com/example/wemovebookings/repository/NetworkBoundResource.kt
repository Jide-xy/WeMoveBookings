/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wemovebookings.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import com.example.wemovebookings.api.Resource
import retrofit2.Response

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * @param <ResultType>
 * @param <RequestType>
</RequestType></ResultType> */
abstract class NetworkBoundResource<ResultType, RequestType>
@MainThread constructor() {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        @Suppress("LeakingThis")
        val job = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + job)
        uiScope.launch {
            val dbSource = withContext(Dispatchers.IO) { loadFromDb() }
            result.addSource(dbSource) { data ->
                result.removeSource(dbSource)
                if (shouldFetch(data)) {
                    uiScope.launch {
                        fetchFromNetwork(dbSource)
                    }
                } else {
                    result.addSource(dbSource) { newData ->
                        setValue(Resource.success(newData))
                    }
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
//        if (result.value != newValue) {
//            result.value = newValue
//        }
        result.value = newValue
    }

    private suspend fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        try {
            coroutineScope {
                // we re-attach dbSource as a new source, it will dispatch its latest value quickly
                result.addSource(dbSource) { newData ->
                    setValue(Resource.loading(newData))
                }
                val apiResponse = liveData(Dispatchers.IO) {
                    try {
                        emit(createCall())
                    } catch (e: Throwable) {
                        withContext(Dispatchers.Main) {
                            onFetchFailed()
                            result.removeSource(dbSource)
                            result.addSource(dbSource) { newData ->
                                setValue(Resource.error(e.message.toString(), newData))
                            }
                        }
                    }
                }
                result.addSource(apiResponse) { response ->
                    result.removeSource(apiResponse)
                    result.removeSource(dbSource)
                    if (response.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            saveCallResult(processResponse(response)!!)
                            withContext(Dispatchers.Main) {
                                result.addSource(withContext(Dispatchers.IO) { loadFromDb() }) { newData ->
                                    setValue(Resource.success(newData))
                                }
                            }
                        }
                    } else {
                        onFetchFailed()
                        result.addSource(dbSource) { newData ->
                            setValue(Resource.error(processErrorResponse(response), newData))
                        }
                    }

                }
            }
        } catch (e: Throwable) {
            onFetchFailed()
            result.addSource(dbSource) { newData ->
                setValue(Resource.error(e.cause?.message.toString(), newData))
            }
        }
    }

    private fun processErrorResponse(response: Response<RequestType>): String {
        return try {
            Gson().fromJson(response.errorBody()?.string(), ApiErrorResponse::class.java)
                .message.toString()
        } catch (e: JsonSyntaxException) {
            "An error occurred"
        }
    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: Response<RequestType>) = response.body()

    @WorkerThread
    protected abstract suspend fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract suspend fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract suspend fun createCall(): Response<RequestType>
}

data class ApiErrorResponse(val message: String?, val errorCode: Int?)
