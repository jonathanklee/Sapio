package com.klee.sapio

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, application = SapioApplication::class)
class SapioApplicationTest {

    @Test
    fun onCreate_shouldApplyDynamicColorsWithoutCrashing() {
        val app = ApplicationProvider.getApplicationContext<SapioApplication>()
        app.onCreate()
        assertNotNull(app)
    }
}
