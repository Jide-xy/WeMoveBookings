package com.example.wemovebookings.di

import com.example.wemovebookings.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [MainNavFragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity

}
