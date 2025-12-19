package com.klee.sapio

import android.os.Build
import androidx.preference.PreferenceManager
import com.klee.sapio.data.Settings
import com.klee.sapio.data.UserType
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
    fun `isRootConfigurationEnabled returns false by default`() {
        assertFalse(settings.isRootConfigurationEnabled())
        assertEquals(UserType.SECURE, settings.getRootConfigurationLevel())
    }

    @Test
    fun `isRootConfigurationEnabled reflects stored preference`() {
        val context = RuntimeEnvironment.getApplication()
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("show_root", true)
            .commit()

        assertTrue(settings.isRootConfigurationEnabled())
        assertEquals(UserType.RISKY, settings.getRootConfigurationLevel())
    }
}
