package com.klee.sapio.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.klee.sapio.domain.model.InstalledApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class InstalledApplicationsRepository @Inject constructor() {

    fun getAppList(context: Context): List<InstalledApplication> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        val results: MutableList<InstalledApplication> = arrayListOf()
        for (resolveInfo in resolveInfoList) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            if (isSystemApp(appInfo) || isGmsRelated(appInfo)) continue
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasAdaptiveIcon(context, appInfo)) continue
            results.add(
                InstalledApplication(
                    pm.getApplicationLabel(appInfo).toString(),
                    appInfo.packageName
                )
            )
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    open fun getApplicationFromPackageName(
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

    @VisibleForTesting
    fun isSystemApp(info: ApplicationInfo): Boolean {
        return info.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    @VisibleForTesting
    fun isGmsRelated(info: ApplicationInfo): Boolean {
        val pkgName = info.packageName ?: return false
        return pkgName.endsWith(".gms") || pkgName == "com.android.vending"
    }

    @VisibleForTesting
    fun hasAdaptiveIcon(context: Context, info: ApplicationInfo): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val icon = info.loadUnbadgedIcon(context.packageManager) ?: return false
        return icon is AdaptiveIconDrawable
    }
}
