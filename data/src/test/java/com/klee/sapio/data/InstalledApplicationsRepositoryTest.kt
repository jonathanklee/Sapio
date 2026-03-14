package com.klee.sapio.data

import android.content.pm.ApplicationInfo
import android.os.Build
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.M])
class InstalledApplicationsRepositoryTest {

    private lateinit var repository: InstalledApplicationsRepository

    @Before
    fun setUp() {
        repository = InstalledApplicationsRepository()
    }

    @Test
    fun `isSystemApp returns true when FLAG_SYSTEM is set`() {
        val info = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_SYSTEM }
        assertTrue(repository.isSystemApp(info))
    }

    @Test
    fun `isSystemApp returns false when FLAG_SYSTEM is not set`() {
        val info = ApplicationInfo().apply { flags = 0 }
        assertFalse(repository.isSystemApp(info))
    }

    @Test
    fun `isSystemApp returns false for user-installed app with other flags`() {
        val info = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_INSTALLED }
        assertFalse(repository.isSystemApp(info))
    }

    @Test
    fun `isGmsRelated returns true for package ending with gms`() {
        val info = ApplicationInfo().apply { packageName = "com.google.android.gms" }
        assertTrue(repository.isGmsRelated(info))
    }

    @Test
    fun `isGmsRelated returns true for com android vending`() {
        val info = ApplicationInfo().apply { packageName = "com.android.vending" }
        assertTrue(repository.isGmsRelated(info))
    }

    @Test
    fun `isGmsRelated returns false for regular package`() {
        val info = ApplicationInfo().apply { packageName = "org.mozilla.firefox" }
        assertFalse(repository.isGmsRelated(info))
    }

    @Test
    fun `isGmsRelated returns false for package that contains gms but does not end with it`() {
        val info = ApplicationInfo().apply { packageName = "com.google.gms.something" }
        assertFalse(repository.isGmsRelated(info))
    }

    @Test
    fun `isGmsRelated returns false for empty package name`() {
        val info = ApplicationInfo().apply { packageName = "" }
        assertFalse(repository.isGmsRelated(info))
    }
}
