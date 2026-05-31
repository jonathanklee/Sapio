package com.klee.sapio

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.klee.sapio.work.CompatibilityCheckScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SapioApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyTheme()
        DynamicColors.applyToActivitiesIfAvailable(this)
        CompatibilityCheckScheduler.schedule(this)
    }

    private fun applyTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val mode = when (prefs.getString("theme_mode", "system")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
