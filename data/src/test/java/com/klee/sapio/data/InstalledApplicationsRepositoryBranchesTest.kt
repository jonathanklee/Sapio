package com.klee.sapio.data

import android.content.pm.ApplicationInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class InstalledApplicationsRepositoryBranchesTest {

    private val context = RuntimeEnvironment.getApplication()
    private val repository = InstalledApplicationsRepository(context)

    @Test
    fun `a system application is recognised by its flag`() {
        val info = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_SYSTEM }

        assertTrue(repository.isSystemApp(info))
    }

    @Test
    fun `a regular application carries no system flag`() {
        assertFalse(repository.isSystemApp(ApplicationInfo()))
    }

    @Test
    fun `a gms package is recognised by its suffix`() {
        val info = ApplicationInfo().apply { packageName = "com.google.android.gms" }

        assertTrue(repository.isGmsRelated(info))
    }

    @Test
    fun `the play store package is gms related`() {
        val info = ApplicationInfo().apply { packageName = "com.android.vending" }

        assertTrue(repository.isGmsRelated(info))
    }

    @Test
    fun `an ordinary package is not gms related`() {
        val info = ApplicationInfo().apply { packageName = "com.klee.sapio" }

        assertFalse(repository.isGmsRelated(info))
    }

    @Test
    fun `an application without a package name is not gms related`() {
        assertFalse(repository.isGmsRelated(ApplicationInfo()))
    }

    @Test
    fun `an adaptive icon is detected`() {
        val info = mock(ApplicationInfo::class.java)
        `when`(info.loadUnbadgedIcon(context.packageManager))
            .thenReturn(AdaptiveIconDrawable(ColorDrawable(0), ColorDrawable(0)))

        assertTrue(repository.hasAdaptiveIcon(context, info))
    }

    @Test
    fun `a legacy icon is not adaptive`() {
        val info = mock(ApplicationInfo::class.java)
        `when`(info.loadUnbadgedIcon(context.packageManager)).thenReturn(ColorDrawable(0))

        assertFalse(repository.hasAdaptiveIcon(context, info))
    }

    @Test
    fun `an application without an icon is not adaptive`() {
        val info = mock(ApplicationInfo::class.java)
        `when`(info.loadUnbadgedIcon(context.packageManager)).thenReturn(null)

        assertFalse(repository.hasAdaptiveIcon(context, info))
    }

    @Test
    fun `an icon that cannot be loaded is not adaptive`() {
        val info = mock(ApplicationInfo::class.java)
        `when`(info.loadUnbadgedIcon(context.packageManager))
            .thenThrow(IllegalStateException("broken resources"))

        assertFalse(repository.hasAdaptiveIcon(context, info))
    }

}
