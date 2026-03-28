package com.klee.sapio.data.system

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

open class Settings @Inject constructor(
    @ApplicationContext private val mContext: Context
) {

    open fun getUnsafeConfigurationLevel(): Int {
        return if (isUnsafeConfigurationEnabled()) {
            UserType.UNSAFE
        } else {
            UserType.SECURE
        }
    }

    open fun isUnsafeConfigurationEnabled(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        return sharedPreferences.getBoolean("show_root", false)
    }
}
