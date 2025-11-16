package com.checkeat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Класс Application с Hilt Dependency Injection
 */
@HiltAndroidApp
class CheckEatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
