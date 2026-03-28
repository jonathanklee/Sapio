package com.klee.sapio

import android.os.Build
import androidx.preference.PreferenceManager
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.system.UserType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class SettingsTest {

    private lateinit var settings: Settings

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        // Start with clean preferences for each test
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        settings = Settings(context)
    }

    @Test
    fun `isUnsafeConfigurationEnabled returns false by default`() {
        assertFalse(settings.isUnsafeConfigurationEnabled())
        assertEquals(UserType.SECURE, settings.getUnsafeConfigurationLevel())
    }

    @Test
    fun `isUnsafeConfigurationEnabled reflects stored preference`() {
        val context = RuntimeEnvironment.getApplication()
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("show_root", true)
            .commit()

        assertTrue(settings.isUnsafeConfigurationEnabled())
        assertEquals(UserType.UNSAFE, settings.getUnsafeConfigurationLevel())
    }
}
