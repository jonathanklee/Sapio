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
        return queryLauncherApplications()
            .filter { appInfo -> isListable(appInfo) }
            .mapNotNull { appInfo -> toInstalledApplication(appInfo) }
            .sortedBy { app -> app.name.lowercase() }
    }

    private fun queryLauncherApplications(): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = try {
            context.packageManager.queryIntentActivities(intent, 0)
        } catch (ignored: RuntimeException) {
            emptyList()
        }

        return resolveInfoList.map { resolveInfo -> resolveInfo.activityInfo.applicationInfo }
    }

    private fun isListable(appInfo: ApplicationInfo): Boolean {
        val hasExpectedIcon = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || hasAdaptiveIcon(context, appInfo)

        return !isSystemApp(appInfo) && !isGmsRelated(appInfo) && hasExpectedIcon
    }

    private fun toInstalledApplication(appInfo: ApplicationInfo): InstalledApplication? {
        val label = try {
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (ignored: RuntimeException) {
            return null
        }

        return InstalledApplication(label, appInfo.packageName, readVersionName(appInfo))
    }

    private fun readVersionName(appInfo: ApplicationInfo): String? {
        return try {
            context.packageManager.getPackageInfo(appInfo.packageName, 0).versionName
        } catch (ignored: Exception) {
            null
        }
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
        } catch (ignored: RuntimeException) {
            false
        }
    }
}
