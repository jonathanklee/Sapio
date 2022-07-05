package com.klee.sapio.model

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledApplicationsRepository @Inject constructor() {

    fun getAppList(context: Context): List<InstalledApplication> {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apps.removeIf { x -> isSystemApp(x) }
        }

        val results: MutableList<InstalledApplication> = arrayListOf()
        for (app in apps) {
            results.add(buildApp(context, app))
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    fun getApplicationFromPackageName(context: Context, packageName: String): InstalledApplication? {
        val appList = getAppList(context)
        for (app in appList) {
            if (app.packageName == packageName) {
                return app
            }
        }

        return null
    }

    private fun buildApp(context: Context, info: ApplicationInfo): InstalledApplication {
        val packageManager = context.packageManager
        return InstalledApplication(
            packageManager.getApplicationLabel(info).toString(),
            info.packageName,
            info.loadIcon(packageManager)
        )
    }

    @VisibleForTesting
    fun isSystemApp(info: ApplicationInfo): Boolean {
        return info.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}