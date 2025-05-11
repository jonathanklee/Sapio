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
        const val GOOGLE_PLAY_SERVICES = "Google Play Services"
    }

    private var packageManager: PackageManager = mContext.packageManager
    private var apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    fun getGmsType(): Int {
        var type = GmsType.BARE_AOSP

        for (app in apps) {
            if (app.packageName != GMS_SERVICES_PACKAGE_NAME) {
                continue
            }

            if (packageManager.getApplicationLabel(app).toString().contains("microG")) {
                type = GmsType.MICROG
                break
            }

            if (packageManager.getApplicationLabel(app).toString() == GOOGLE_PLAY_SERVICES) {
                type = GmsType.GOOGLE_PLAY_SERVICES
                break;
            }
        }

        return type
    }

    fun isRisky(): Int {
        return if (RootBeer(mContext).isRooted) {
            Label.RISKY
        } else {
            Label.SECURE
        }
    }
}
