package com.klee.sapio.data

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Settings @Inject constructor(
    @ApplicationContext private val mContext: Context
) {

    fun getRootConfigurationLevel(): Int {
        return if (isRootConfigurationEnabled()) {
            UserType.RISKY
        } else {
            UserType.SECURE
        }
    }

    fun isRootConfigurationEnabled(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return sharedPreferences.getBoolean("show_root", false)
    }
}
