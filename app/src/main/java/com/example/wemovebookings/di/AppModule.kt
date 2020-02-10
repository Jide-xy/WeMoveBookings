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

package com.example.wemovebookings.di

import com.example.wemovebookings.WMBApplication
import dagger.Module
import dagger.Provides
import com.example.wemovebookings.api.WMBService
import com.example.wemovebookings.api.response.Flight
import com.example.wemovebookings.repository.Repository
import com.example.wemovebookings.room.db.WMBDatabase
import com.example.wemovebookings.util.CustomDeserializer
import com.google.gson.GsonBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@UseExperimental(FlowPreview::class)
@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideService(): WMBService {
        val gson = GsonBuilder()
            .registerTypeAdapter(Flight::class.java, CustomDeserializer(Flight::class.java))
            .create()
        return Retrofit.Builder()
            .baseUrl("https://api.lufthansa.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(WMBService::class.java)
    }

    @Singleton
    @Provides
    fun provideDb(app: WMBApplication): WMBDatabase {
        return WMBDatabase.getInstance(app)
//        val name = "SoccerFlash_DB"
//        return Room.databaseBuilder(
//            app.applicationContext,
//            FMDatabase::class.java,
//            name)
//            .addCallback(object : RoomDatabase.Callback() {
//                override fun onCreate(db: SupportSQLiteDatabase) {
//
//                }
//            })
//            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideRepository(
        localDb: WMBDatabase,
        remoteService: WMBService
    ): Repository {
        return Repository(localDb, remoteService)
    }
}
