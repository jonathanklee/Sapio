package com.klee.sapio

import com.klee.sapio.domain.model.OperatingSystem
import com.klee.sapio.domain.model.OperatingSystemName
import com.klee.sapio.ui.model.OperatingSystemLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class OperatingSystemLabelTest {

    @Test
    fun `formats a known system with its version and the android release`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.E_OS, "4.5.1", 35))

        assertEquals("/e/OS 4.5.1 · Android 15", label)
    }

    @Test
    fun `omits the version when the system has none`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.CALYX_OS, "", 36))

        assertEquals("CalyxOS · Android 16", label)
    }

    @Test
    fun `shows only the android release for an unknown system`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.OTHER, "BP1A.250505.005", 34))

        assertEquals("Android 14", label)
    }

    @Test
    fun `falls back to the api level for an unknown android release`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.LINEAGE_OS, "23.0", 99))

        assertEquals("LineageOS 23.0 · Android API 99", label)
    }
}
