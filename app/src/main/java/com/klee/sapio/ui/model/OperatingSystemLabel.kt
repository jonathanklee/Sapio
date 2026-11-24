package com.klee.sapio.ui.model

import com.klee.sapio.domain.model.OperatingSystem
import com.klee.sapio.domain.model.OperatingSystemName

object OperatingSystemLabel {

    private const val SEPARATOR = " · "

    private val displayNames = mapOf(
        OperatingSystemName.E_OS to "/e/OS",
        OperatingSystemName.IODE_OS to "iodéOS",
        OperatingSystemName.CALYX_OS to "CalyxOS",
        OperatingSystemName.GRAPHENE_OS to "GrapheneOS",
        OperatingSystemName.LINEAGE_OS to "LineageOS"
    )

    private val androidReleases = mapOf(
        26 to "8.0",
        27 to "8.1",
        28 to "9",
        29 to "10",
        30 to "11",
        31 to "12",
        32 to "12L",
        33 to "13",
        34 to "14",
        35 to "15",
        36 to "16",
        37 to "17"
    )

    fun format(operatingSystem: OperatingSystem): String {
        val android = androidLabel(operatingSystem.androidVersion)
        val displayName = displayNames[operatingSystem.name] ?: return android

        return displayName + SEPARATOR + android
    }

    private fun androidLabel(sdkVersion: Int): String {
        val release = androidReleases[sdkVersion] ?: "API $sdkVersion"

        return "Android $release"
    }
}
