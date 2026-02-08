package com.klee.sapio

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.klee.sapio.work.CompatibilityCheckScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SapioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        CompatibilityCheckScheduler.schedule(this)
    }
}
