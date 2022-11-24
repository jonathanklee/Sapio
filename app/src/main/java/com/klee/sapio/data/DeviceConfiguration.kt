package com.klee.sapio.data

import android.content.Context
import android.content.pm.PackageManager
import com.klee.sapio.ui.view.EvaluateFragment
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceConfiguration @Inject constructor(
    @ApplicationContext private val mContext: Context
) {

    fun isMicroGInstalled(): Int {
        val packageManager = mContext.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            if (app.packageName == "com.google.android.gms" &&
                packageManager.getApplicationLabel(app).toString() == EvaluateFragment.MICRO_G_APP_LABEL
            ) {
                return Label.MICROG
            }
        }

        return Label.BARE_AOSP
    }

    fun isRooted(): Int {
        return if (RootBeer(mContext).isRooted) {
            Label.ROOTED
        } else {
            Label.USER
        }
    }
}