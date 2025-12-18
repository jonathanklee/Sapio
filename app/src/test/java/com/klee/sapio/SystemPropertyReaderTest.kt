package com.klee.sapio

import android.os.Build
import com.klee.sapio.data.SystemPropertyReader
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class SystemPropertyReaderTest {

    private lateinit var systemPropertyReader: SystemPropertyReader

    @Before
    fun setUp() {
        systemPropertyReader = SystemPropertyReader()
    }

    @Test
    fun test_read_withValidProperty() {
        // This test is tricky because SystemPropertyReader uses reflection
        // We'll test that it doesn't throw exceptions and returns a string
        val result = systemPropertyReader.read("ro.product.model")
        Assert.assertNotNull("Result should not be null", result)
    }

    @Test
    fun test_read_withInvalidProperty() {
        // Test with a property that doesn't exist
        val result = systemPropertyReader.read("non.existent.property")
        Assert.assertNotNull("Result should not be null", result)
        // Should return empty string for non-existent properties
        Assert.assertEquals("Should return empty string for non-existent property", "", result)
    }

    @Test
    fun test_read_withEmptyPropertyName() {
        // Test with empty property name
        val result = systemPropertyReader.read("")
        Assert.assertNotNull("Result should not be null", result)
        // Should return empty string for empty property name
        Assert.assertEquals("Should return empty string for empty property name", "", result)
    }
}