package com.klee.sapio

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.N])
class InstalledApplicationsRepositoryTest {

    private lateinit var repository: InstalledApplicationsRepository

    @Mock
    private lateinit var mockedPackageManager: PackageManager

    @Mock
    private lateinit var mockedContext: Context

    @Mock
    private lateinit var mockedDrawable: Drawable

    private lateinit var fakeRegularApplicationInfo: ApplicationInfo
    private lateinit var fakeSystemApplicationInfo: ApplicationInfo
    private lateinit var fakeGmsApp: ApplicationInfo

    private var fakeListApplicationInfo: List<ApplicationInfo>? = null

    @Before
    fun setUp() {

        MockitoAnnotations.openMocks(this)

        repository = InstalledApplicationsRepository()

        fakeRegularApplicationInfo = ApplicationInfo().apply {
            packageName = "fake.package.name.one"
            name = "FakeApplicationOne"
        }

        fakeSystemApplicationInfo = ApplicationInfo().apply {
            packageName = "fake.package.name.one"
            name = "FakeApplicationTwo"
            flags = ApplicationInfo.FLAG_SYSTEM
        }

        fakeGmsApp = ApplicationInfo().apply {
            packageName = "fake.package.gms"
            name = "FakeApplicationThree"
        }

        fakeListApplicationInfo = mutableListOf(
            fakeRegularApplicationInfo,
            fakeSystemApplicationInfo,
            fakeGmsApp
        )

        Mockito.`when`(mockedContext.packageManager).thenReturn(mockedPackageManager)
        Mockito.`when`(mockedPackageManager.queryIntentActivities(any(Intent::class.java), eq(0)))
            .thenReturn(fakeListApplicationInfo!!.map { makeResolveInfo(it) })

        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeRegularApplicationInfo)))
            .thenReturn("FakeApplicationOne")

        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeSystemApplicationInfo)))
            .thenReturn("FakeApplicationTwo")

        Mockito.`when`(mockedPackageManager.getDrawable(anyString(), anyInt(), any()))
            .thenReturn(mockedDrawable)
    }

    private fun makeResolveInfo(appInfo: ApplicationInfo): ResolveInfo {
        val activityInfo = ActivityInfo().apply { applicationInfo = appInfo }
        return ResolveInfo().apply { this.activityInfo = activityInfo }
    }

    @Test
    fun test_isSystemAppWithRegularApp() {
        Assert.assertEquals(
            "App status error",
            false,
            repository.isSystemApp(fakeRegularApplicationInfo)
        )
    }

    @Test
    fun test_isSystemAppWithSystemApp() {
        Assert.assertEquals(
            "App status error",
            true,
            repository.isSystemApp(fakeSystemApplicationInfo)
        )
    }

    @Test
    fun test_isGmsWithRegularApp() {
        Assert.assertEquals(
            "App status error",
            false,
            repository.isGmsRelated(fakeRegularApplicationInfo)
        )
    }

    @Test
    fun test_isGmsWithGmsApp() {
        Assert.assertEquals(
            "App status error",
            true,
            repository.isGmsRelated(fakeGmsApp)
        )
    }

    @Test
    fun test_isGmsWithPlayStoreApp() {
        val playStoreApp = ApplicationInfo().apply {
            packageName = "com.android.vending"
            name = "PlayStore"
        }

        Assert.assertTrue("Play Store should be treated as GMS", repository.isGmsRelated(playStoreApp))
    }

    @Test
    fun test_listApplicationCheckListSize() {
        val list = repository.getAppList(mockedContext)
        assertEquals("Wrong list size.", 1, list.size)
    }

    @Test
    fun test_listApplicationCheckElement() {
        val list = repository.getAppList(mockedContext)
        assertEquals(
            "Package name are not the same.",
            fakeRegularApplicationInfo.packageName,
            list[0].packageName)
    }

    @Test
    fun test_getApplicationFromPackageName_found() {
        val result = repository.getApplicationFromPackageName(mockedContext, fakeRegularApplicationInfo.packageName)
        Assert.assertNotNull("Should find the application", result)
        Assert.assertEquals("Package names should match", fakeRegularApplicationInfo.packageName, result?.packageName)
    }

    @Test
    fun test_getApplicationFromPackageName_notFound() {
        val result = repository.getApplicationFromPackageName(mockedContext, "non.existent.package")
        Assert.assertNull("Should return null for non-existent package", result)
    }

    @Test
    fun test_getAppList_sorting() {
        val fakeAppZ = ApplicationInfo().apply {
            packageName = "fake.package.name.z"
            name = "ZebraApp"
        }

        val appsWithZ = mutableListOf(
            fakeRegularApplicationInfo,
            fakeSystemApplicationInfo,
            fakeGmsApp,
            fakeAppZ
        )

        Mockito.`when`(mockedPackageManager.queryIntentActivities(any(Intent::class.java), eq(0)))
            .thenReturn(appsWithZ.map { makeResolveInfo(it) })

        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeRegularApplicationInfo)))
            .thenReturn("FakeApplicationOne")

        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeAppZ)))
            .thenReturn("ZebraApp")

        val list = repository.getAppList(mockedContext)

        Assert.assertEquals("Should have 2 apps", 2, list.size)
        Assert.assertEquals("First app should be FakeApplicationOne", "fakeapplicationone", list[0].name.lowercase())
        Assert.assertEquals("Second app should be ZebraApp", "zebraapp", list[1].name.lowercase())
    }

    @Test
    fun test_getAppList_emptyListWhenNoApps() {
        Mockito.`when`(mockedPackageManager.queryIntentActivities(any(Intent::class.java), eq(0)))
            .thenReturn(emptyList())

        val list = repository.getAppList(mockedContext)

        Assert.assertTrue("List should be empty when no apps are installed", list.isEmpty())
    }

    @Test
    fun test_getAppList_filtersSystemAndGmsApps() {
        fakeSystemApplicationInfo.flags = ApplicationInfo.FLAG_SYSTEM
        fakeGmsApp.packageName = "com.google.gms"

        Mockito.`when`(mockedPackageManager.queryIntentActivities(any(Intent::class.java), eq(0)))
            .thenReturn(listOf(fakeRegularApplicationInfo, fakeSystemApplicationInfo, fakeGmsApp).map { makeResolveInfo(it) })

        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeRegularApplicationInfo)))
            .thenReturn("FakeApplicationOne")
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeSystemApplicationInfo)))
            .thenReturn("SystemApp")
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeGmsApp)))
            .thenReturn("GmsApp")

        val list = repository.getAppList(mockedContext)

        Assert.assertEquals(1, list.size)
        Assert.assertEquals(fakeRegularApplicationInfo.packageName, list.first().packageName)
    }
}
