package com.android.sapio.model

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneApplicationRepository @Inject constructor() {

    fun getAppList(context: Context): List<Application> {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apps.removeIf { x -> isSystemApp(x) }
        }

        val results: MutableList<Application> = arrayListOf()
        for (app in apps) {
            results.add(buildApp(context, app))
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    fun getApplicationFromPackageName(context: Context, packageName: String): Application? {
        val appList = getAppList(context)
        for (app in appList) {
            if (app.packageName == packageName) {
                return app
            }
        }

        return null
    }

    private fun buildApp(context: Context, info: ApplicationInfo): Application {
        val packageManager = context.packageManager
        return Application(
            packageManager.getApplicationLabel(info).toString(),
            info.packageName,
            info.loadIcon(packageManager)
        )
    }

    private fun isSystemApp(info: ApplicationInfo): Boolean {
        return info.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

}