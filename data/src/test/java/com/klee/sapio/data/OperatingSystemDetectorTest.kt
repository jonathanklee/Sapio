package com.klee.sapio.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.klee.sapio.data.system.OperatingSystemDetector
import com.klee.sapio.data.system.SystemPropertyReader
import com.klee.sapio.domain.model.OperatingSystemName
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class OperatingSystemDetectorTest {

    private val context: Context = Mockito.mock(Context::class.java)
    private val packageManager: PackageManager = Mockito.mock(PackageManager::class.java)
    private val propertyReader: SystemPropertyReader = Mockito.mock(SystemPropertyReader::class.java)

    @Before
    fun setUp() {
        Mockito.`when`(context.packageManager).thenReturn(packageManager)
        Mockito.`when`(propertyReader.read(anyString())).thenReturn("")
        Mockito.`when`(packageManager.getApplicationInfo(GRAPHENE_OS_APP_STORE, 0))
            .thenThrow(PackageManager.NameNotFoundException())
        setBuildDisplay("BP1A.250505.005")
    }

    @Test
    fun `detects eos from its legal url property`() {
        givenProperty("ro.elegal.url", "https://e.foundation/legal")
        givenProperty("ro.lineage.version", "4.5.1-a15-20261118716089-test-kiev")
        givenProperty("ro.lineage.build.version", "4.5.1")

        val operatingSystem = detector().getOperatingSystem()

        assertEquals(OperatingSystemName.E_OS, operatingSystem.name)
        assertEquals("4.5.1", operatingSystem.version)
    }

    @Test
    fun `detects eos from its build display prefix`() {
        setBuildDisplay("e_kiev-user 15 BP1A.250505.005 eng.root test-keys")

        assertEquals(OperatingSystemName.E_OS, detector().getOperatingSystem().name)
    }

    @Test
    fun `detects iodeos before lineageos`() {
        givenProperty("ro.lineage.version", "22.2")
        givenProperty("ro.iode.build.version", "7.1")

        val operatingSystem = detector().getOperatingSystem()

        assertEquals(OperatingSystemName.IODE_OS, operatingSystem.name)
        assertEquals("7.1", operatingSystem.version)
    }

    @Test
    fun `detects calyxos`() {
        givenProperty("ro.calyxos.version", "8.0.4")

        val operatingSystem = detector().getOperatingSystem()

        assertEquals(OperatingSystemName.CALYX_OS, operatingSystem.name)
        assertEquals("8.0.4", operatingSystem.version)
    }

    @Test
    fun `detects grapheneos from its preinstalled app store`() {
        givenGrapheneOsAppStore(ApplicationInfo.FLAG_SYSTEM)

        assertEquals(OperatingSystemName.GRAPHENE_OS, detector().getOperatingSystem().name)
    }

    @Test
    fun `ignores a sideloaded grapheneos app store`() {
        givenGrapheneOsAppStore(0)

        assertEquals(OperatingSystemName.OTHER, detector().getOperatingSystem().name)
    }

    @Test
    fun `detects lineageos`() {
        givenProperty("ro.lineage.version", "22.2-20261120-NIGHTLY-kiev")
        givenProperty("ro.lineage.build.version", "22.2")

        val operatingSystem = detector().getOperatingSystem()

        assertEquals(OperatingSystemName.LINEAGE_OS, operatingSystem.name)
        assertEquals("22.2", operatingSystem.version)
    }

    @Test
    fun `falls back to other with the build display as version`() {
        val operatingSystem = detector().getOperatingSystem()

        assertEquals(OperatingSystemName.OTHER, operatingSystem.name)
        assertEquals("BP1A.250505.005", operatingSystem.version)
    }

    @Test
    fun `reports the android version`() {
        assertEquals(Build.VERSION_CODES.P, detector().getOperatingSystem().androidVersion)
    }

    private fun detector() = OperatingSystemDetector(context, propertyReader)

    private fun givenProperty(name: String, value: String) {
        Mockito.`when`(propertyReader.read(name)).thenReturn(value)
    }

    private fun givenGrapheneOsAppStore(flags: Int) {
        val appStore = ApplicationInfo().apply { this.flags = flags }
        Mockito.doReturn(appStore).`when`(packageManager).getApplicationInfo(GRAPHENE_OS_APP_STORE, 0)
    }

    private fun setBuildDisplay(display: String) {
        ReflectionHelpers.setStaticField(Build::class.java, "DISPLAY", display)
    }

    private companion object {
        const val GRAPHENE_OS_APP_STORE = "app.grapheneos.apps"
    }
}
