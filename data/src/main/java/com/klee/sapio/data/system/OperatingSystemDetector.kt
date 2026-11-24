package com.klee.sapio.data.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.klee.sapio.domain.OperatingSystemInfo
import com.klee.sapio.domain.model.OperatingSystem
import com.klee.sapio.domain.model.OperatingSystemName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatingSystemDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val propertyReader: SystemPropertyReader
) : OperatingSystemInfo {

    override fun getOperatingSystem(): OperatingSystem = cachedOperatingSystem

    private val cachedOperatingSystem: OperatingSystem by lazy { detectOperatingSystem() }

    private fun detectOperatingSystem(): OperatingSystem {
        return OperatingSystem(detectNameOrFallback(), Build.VERSION.SDK_INT)
    }

    private fun detectNameOrFallback(): String {
        return try {
            detectName()
        } catch (ignored: RuntimeException) {
            OperatingSystemName.OTHER
        }
    }

    private fun detectName(): String = when {
        isEOs() -> OperatingSystemName.E_OS
        hasProperty(IODE_VERSION_PROPERTY) -> OperatingSystemName.IODE_OS
        hasProperty(CALYX_VERSION_PROPERTY) -> OperatingSystemName.CALYX_OS
        isGrapheneOs() -> OperatingSystemName.GRAPHENE_OS
        hasProperty(LINEAGE_DEVICE_PROPERTY) -> OperatingSystemName.LINEAGE_OS
        else -> OperatingSystemName.OTHER
    }

    private fun isEOs(): Boolean {
        return hasProperty(E_LEGAL_URL_PROPERTY) || Build.DISPLAY.startsWith(E_BUILD_DISPLAY_PREFIX)
    }

    private fun isGrapheneOs(): Boolean {
        return try {
            val appStore = context.packageManager.getApplicationInfo(GRAPHENE_OS_APP_STORE_PACKAGE, 0)
            appStore.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun hasProperty(propertyName: String): Boolean = propertyReader.read(propertyName).isNotEmpty()

    private companion object {
        const val E_LEGAL_URL_PROPERTY = "ro.elegal.url"
        const val E_BUILD_DISPLAY_PREFIX = "e_"
        const val IODE_VERSION_PROPERTY = "ro.iode.build.version"
        const val CALYX_VERSION_PROPERTY = "ro.calyxos.version"
        const val LINEAGE_DEVICE_PROPERTY = "ro.lineage.device"
        const val GRAPHENE_OS_APP_STORE_PACKAGE = "app.grapheneos.apps"
    }
}
