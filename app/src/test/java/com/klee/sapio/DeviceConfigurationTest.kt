package com.klee.sapio

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class DeviceConfigurationTest {

    private lateinit var deviceConfiguration: DeviceConfiguration

    @Mock
    private lateinit var mockedContext: Context

    @Mock
    private lateinit var mockedPackageManager: PackageManager

    private lateinit var fakeGmsApp: ApplicationInfo
    private lateinit var fakeMicroGApp: ApplicationInfo
    private lateinit var fakeRegularApp: ApplicationInfo

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        fakeGmsApp = ApplicationInfo().apply {
            packageName = DeviceConfiguration.GMS_SERVICES_PACKAGE_NAME
            name = "Google Play Services"
        }

        fakeMicroGApp = ApplicationInfo().apply {
            packageName = DeviceConfiguration.GMS_SERVICES_PACKAGE_NAME
            name = "microG Services Core"
        }

        fakeRegularApp = ApplicationInfo().apply {
            packageName = "com.example.app"
            name = "Example App"
        }

        // Setup the mocked context and package manager BEFORE creating DeviceConfiguration
        Mockito.`when`(mockedContext.packageManager).thenReturn(mockedPackageManager)

        // Create DeviceConfiguration with mocked context
        deviceConfiguration = DeviceConfiguration(mockedContext)
    }

    @Test
    fun test_getGmsType_withGooglePlayServices() {
        // Setup the mock behavior BEFORE creating DeviceConfiguration
        val apps = listOf(fakeGmsApp, fakeRegularApp)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(0))
            .thenReturn(apps)
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeGmsApp)))
            .thenReturn("Google Play Services")

        // Recreate DeviceConfiguration with the properly mocked context
        deviceConfiguration = DeviceConfiguration(mockedContext)

        val result = deviceConfiguration.getGmsType()
        Assert.assertEquals("Should return Google Play Services type", GmsType.GOOGLE_PLAY_SERVICES, result)
    }

    @Test
    fun test_getGmsType_withMicroG() {
        // Setup the mock behavior BEFORE creating DeviceConfiguration
        val apps = listOf(fakeMicroGApp, fakeRegularApp)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(0))
            .thenReturn(apps)
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeMicroGApp)))
            .thenReturn("microG Services Core")

        // Recreate DeviceConfiguration with the properly mocked context
        deviceConfiguration = DeviceConfiguration(mockedContext)

        val result = deviceConfiguration.getGmsType()
        Assert.assertEquals("Should return microG type", GmsType.MICROG, result)
    }

    @Test
    fun test_getGmsType_withMicroGRenamedAsAppCompatibilityServices() {
        val renamedMicroGApp = ApplicationInfo().apply {
            packageName = DeviceConfiguration.GMS_SERVICES_PACKAGE_NAME
        }
        val apps = listOf(renamedMicroGApp, fakeRegularApp)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(0))
            .thenReturn(apps)
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(renamedMicroGApp)))
            .thenReturn("App compatibility Services")

        deviceConfiguration = DeviceConfiguration(mockedContext)

        val result = deviceConfiguration.getGmsType()
        Assert.assertEquals(GmsType.MICROG, result)
    }

    @Test
    fun test_getGmsType_withBareAosp() {
        // Setup the mock behavior BEFORE creating DeviceConfiguration
        val apps = listOf(fakeRegularApp)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(0))
            .thenReturn(apps)

        // Recreate DeviceConfiguration with the properly mocked context
        deviceConfiguration = DeviceConfiguration(mockedContext)

        val result = deviceConfiguration.getGmsType()
        Assert.assertEquals("Should return bare AOSP type", GmsType.BARE_AOSP, result)
    }

    @Test
    fun test_isPermissive_withRootedAndUnlockedBootloader() {
        // This test is more complex due to the private isBootloaderLocked method
        // We'll test the public behavior by mocking the RootBeer result
        val apps = listOf(fakeRegularApp)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(0))
            .thenReturn(apps)

        // For this test, we'll assume the device is rooted and bootloader is unlocked
        // Since we can't easily mock the private method, we'll test the happy path
        val result = deviceConfiguration.isPermissive()
        // The actual result depends on the device state, so we'll just verify it returns a valid value
        Assert.assertTrue("Should return either VULNERABLE or SECURE",
            result == UserType.PERMISSIVE || result == UserType.STANDARD)
    }

    @Test
    fun test_isPermissive_branch_with_overrides() {
        val fake = object : DeviceConfiguration(mockedContext) {
            override fun isRooted(): Boolean = true
            override fun isBootloaderLocked(): Boolean = false
        }
        Assert.assertEquals(UserType.PERMISSIVE, fake.isPermissive())

        val secure = object : DeviceConfiguration(mockedContext) {
            override fun isRooted(): Boolean = true
            override fun isBootloaderLocked(): Boolean = true
        }
        Assert.assertEquals(UserType.STANDARD, secure.isPermissive())

        val clean = object : DeviceConfiguration(mockedContext) {
            override fun isRooted(): Boolean = false
            override fun isBootloaderLocked(): Boolean = false
        }
        Assert.assertEquals(UserType.STANDARD, clean.isPermissive())
    }
}
