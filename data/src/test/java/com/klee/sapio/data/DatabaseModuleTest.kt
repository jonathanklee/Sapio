package com.klee.sapio.data

import android.os.Build
import com.klee.sapio.data.local.DatabaseModule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DatabaseModuleTest {

    private val database = DatabaseModule.provideDatabase(RuntimeEnvironment.getApplication())

    @Test
    fun `the module provides a database`() {
        assertNotNull(database)
    }

    @Test
    fun `the module provides the evaluation dao of the database`() {
        assertSame(database.evaluationDao(), DatabaseModule.provideEvaluationDao(database))
    }

    @Test
    fun `the module provides the icon dao of the database`() {
        assertSame(database.iconDao(), DatabaseModule.provideIconDao(database))
    }

    @Test
    fun `the module provides the device app dao of the database`() {
        assertSame(database.deviceAppDao(), DatabaseModule.provideDeviceAppDao(database))
    }
}
