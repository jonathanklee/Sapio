package com.klee.sapio.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.klee.sapio.domain.model.InstalledApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledApplicationsRepository @Inject constructor() {

    fun getAppList(context: Context): List<InstalledApplication> {
        val apps = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apps.removeIf { x -> isSystemApp(x) || isGmsRelated(x) }
        }

        val results: MutableList<InstalledApplication> = arrayListOf()
        for (app in apps) {
            results.add(createInstalledApplication(context, app))
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    fun getApplicationFromPackageName(
        context: Context,
        packageName: String
    ): InstalledApplication? {
        val appList = getAppList(context)
        for (app in appList) {
            if (app.packageName == packageName) {
                return app
            }
        }

        return null
    }

    private fun createInstalledApplication(
        context: Context,
        info: ApplicationInfo
    ): InstalledApplication {
        val packageManager = context.packageManager
        return InstalledApplication(
            packageManager.getApplicationLabel(info).toString(),
            info.packageName,
            fetchIcon(packageManager, info)
        )
    }

    private fun fetchIcon(packageManager: PackageManager, info: ApplicationInfo): Drawable {
        return packageManager.getDrawable(info.packageName, info.icon, info)!!
    }

    @VisibleForTesting
    fun isSystemApp(info: ApplicationInfo): Boolean {
        return info.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    @VisibleForTesting
    fun isGmsRelated(info: ApplicationInfo): Boolean {
        val pkgName = info.packageName ?: return false
        return pkgName.endsWith(".gms") || pkgName == "com.android.vending"
    }
}
