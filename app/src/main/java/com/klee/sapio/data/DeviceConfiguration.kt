package com.klee.sapio.data

import android.content.Context
import android.content.pm.PackageManager
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceConfiguration @Inject constructor(
    @ApplicationContext private val mContext: Context
) {
    companion object {
        const val GMS_SERVICES_PACKAGE_NAME = "com.google.android.gms"
        const val MICRO_G_APP_LABEL = "microG Services Core"
        const val GOOGLE_PLAY_SERVICES = "Google Play Services"
    }

    private var packageManager: PackageManager = mContext.packageManager
    private var apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    fun getGmsType(): Int {
        for (app in apps) {
            if (app.packageName == GMS_SERVICES_PACKAGE_NAME &&
                packageManager.getApplicationLabel(app).toString() == MICRO_G_APP_LABEL
            ) {
                return GmsType.MICROG
            }

            if (app.packageName == GMS_SERVICES_PACKAGE_NAME &&
                packageManager.getApplicationLabel(app).toString() == GOOGLE_PLAY_SERVICES
            ) {
                return GmsType.GOOGLE_PLAY_SERVICES
            }
        }

        return GmsType.BARE_AOSP
    }

    fun isRooted(): Int {
        return if (RootBeer(mContext).isRooted) {
            Label.ROOTED
        } else {
            Label.USER
        }
    }
}