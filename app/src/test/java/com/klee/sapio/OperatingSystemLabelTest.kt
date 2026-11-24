package com.klee.sapio

import com.klee.sapio.domain.model.OperatingSystem
import com.klee.sapio.domain.model.OperatingSystemName
import com.klee.sapio.ui.model.OperatingSystemLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class OperatingSystemLabelTest {

    @Test
    fun `formats a known system with the android release`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.E_OS, 35))

        assertEquals("/e/OS · Android 15", label)
    }

    @Test
    fun `shows only the android release for an unknown system`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.OTHER, 34))

        assertEquals("Android 14", label)
    }

    @Test
    fun `falls back to the api level for an unknown android release`() {
        val label = OperatingSystemLabel.format(OperatingSystem(OperatingSystemName.LINEAGE_OS, 99))

        assertEquals("LineageOS · Android API 99", label)
    }
}
