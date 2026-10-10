package com.klee.sapio

import android.os.Build
import com.klee.sapio.ui.model.Label
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M])
class LabelTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `microg label carries its own text and colour`() {
        val label = Label.create(context, Label.MICROG)

        assertEquals(context.getString(R.string.microg_label), label.text)
        assertEquals(context.getColor(R.color.blue_200), label.color)
    }

    @Test
    fun `bare aosp label carries its own text and colour`() {
        val label = Label.create(context, Label.BARE_AOSP)

        assertEquals(context.getString(R.string.bare_aosp_label), label.text)
        assertEquals(context.getColor(R.color.blue_700), label.color)
    }

    @Test
    fun `standard label carries its own text and colour`() {
        val label = Label.create(context, Label.STANDARD)

        assertEquals(context.getString(R.string.standard_label), label.text)
        assertEquals(context.getColor(R.color.purple_200), label.color)
    }

    @Test
    fun `permissive label carries its own text and colour`() {
        val label = Label.create(context, Label.PERMISSIVE)

        assertEquals(context.getString(R.string.permissive_label), label.text)
        assertEquals(context.getColor(R.color.purple_700), label.color)
    }

    @Test
    fun `unknown label falls back to an empty black label`() {
        val label = Label.create(context, UNKNOWN_LABEL)

        assertEquals(" Empty label ", label.text)
        assertEquals(context.getColor(R.color.black), label.color)
    }

    private companion object {
        const val UNKNOWN_LABEL = 42
    }
}
