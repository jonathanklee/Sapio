package com.android.sapio

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.android.sapio.model.InstalledApplicationsRepository
import junit.framework.TestCase.assertEquals
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

    private lateinit var fakeRegularApplicationInfo: ApplicationInfo
    private lateinit var fakeSystemApplicationInfo: ApplicationInfo

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


        fakeListApplicationInfo = mutableListOf(fakeRegularApplicationInfo, fakeSystemApplicationInfo)

        Mockito.`when`(mockedContext.packageManager).thenReturn(mockedPackageManager)
        Mockito.`when`(mockedPackageManager.getInstalledApplications(eq(PackageManager.GET_META_DATA))).thenReturn(fakeListApplicationInfo)
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeRegularApplicationInfo))).thenReturn("FakeApplicationOne")
        Mockito.`when`(mockedPackageManager.getApplicationLabel(eq(fakeSystemApplicationInfo))).thenReturn("FakeApplicationTwo")
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
    fun listApplicationCheckListSize() {
        val list = repository.getAppList(mockedContext)
        assertEquals("Wrong list size.", 1, list.size)
    }

    @Test
    fun listApplicationCheckElement() {
        val list = repository.getAppList(mockedContext)
        assertEquals(
            "Package name are not the same.",
            fakeRegularApplicationInfo.packageName,
            list[0].packageName)
    }
}
