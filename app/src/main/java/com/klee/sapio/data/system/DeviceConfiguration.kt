package com.klee.sapio.data.system

import android.content.Context
import android.content.pm.PackageManager
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DeviceConfiguration @Inject constructor(
    @ApplicationContext private val mContext: Context
) {
    companion object {
        const val GMS_SERVICES_PACKAGE_NAME = "com.google.android.gms"
    }

    private var packageManager: PackageManager = mContext.packageManager
    private var apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    open fun getGmsType(): Int {
        var type = GmsType.BARE_AOSP

        for (app in apps) {
            if (app.packageName != GMS_SERVICES_PACKAGE_NAME) {
                continue
            }

            if (packageManager.getApplicationLabel(app).toString().contains("Google", true)) {
                type = GmsType.GOOGLE_PLAY_SERVICES
                break
            } else {
                type = GmsType.MICROG
                break
            }
        }

        return type
    }

    open fun isRisky(): Int {
        return if (isRooted() && !isBootloaderLocked()) {
            UserType.RISKY
        } else {
            UserType.SECURE
        }
    }

    protected open fun isRooted(): Boolean = RootBeer(mContext).isRooted

    protected open fun isBootloaderLocked(): Boolean {
        val verifiedBootState = SystemPropertyReader().read("ro.boot.verifiedbootstate")
        return verifiedBootState == "yellow" || verifiedBootState == "green"
    }
}
