package com.coffeehub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CoffeeHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
