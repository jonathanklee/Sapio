package com.klee.sapio.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.klee.sapio.domain.InstalledApplicationsDataSource
import com.klee.sapio.domain.model.InstalledApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class InstalledApplicationsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledApplicationsDataSource {

    override fun listInstalledApplications(): List<InstalledApplication> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = try {
            pm.queryIntentActivities(intent, 0)
        } catch (e: RuntimeException) {
            return emptyList()
        }

        val results: MutableList<InstalledApplication> = arrayListOf()
        for (resolveInfo in resolveInfoList) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            if (isSystemApp(appInfo) || isGmsRelated(appInfo)) continue
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasAdaptiveIcon(context, appInfo)) continue
            val label = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: RuntimeException) {
                continue
            }
            results.add(InstalledApplication(label, appInfo.packageName))
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    override fun getInstalledApplication(packageName: String): InstalledApplication? {
        val appList = listInstalledApplications()
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
        return try {
            val icon = info.loadUnbadgedIcon(context.packageManager) ?: return false
            icon is AdaptiveIconDrawable
        } catch (e: RuntimeException) {
            false
        }
    }
}
