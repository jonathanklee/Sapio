package com.klee.sapio

import android.os.Build
import com.klee.sapio.data.SystemPropertyReader
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.P])
class SystemPropertyReaderTest {

    private lateinit var reader: SystemPropertyReader

    @org.junit.Before
    fun setUp() {
        reader = SystemPropertyReader()
    }

    @Test
    fun read_returnsValueWhenPropertyIsSet() {
        ReflectionHelpers.callStaticMethod<Void>(
            Class.forName("android.os.SystemProperties"),
            "set",
            ReflectionHelpers.ClassParameter.from(String::class.java, "test.prop"),
            ReflectionHelpers.ClassParameter.from(String::class.java, "value123")
        )

        assertEquals("value123", reader.read("test.prop"))
    }

    @Test
    fun read_returnsEmptyStringWhenPropertyIsMissing() {
        assertEquals("", reader.read("missing.prop"))
    }

    @Test
    fun read_returnsEmptyStringOnIllegalArgument() {
        // Swap out the cached getMethod to one that always throws IllegalArgumentException
        val failingMethod = Class.forName("com.klee.sapio.SystemPropertyReaderTestKt")
            .getDeclaredMethod("throwIllegal", String::class.java, String::class.java)
        org.robolectric.util.ReflectionHelpers.setField(reader, "getMethod\$delegate", lazy { failingMethod })

        assertEquals("", reader.read("any.prop"))
    }
}

fun throwIllegal(name: String, def: String): String {
    throw IllegalArgumentException("boom")
}
