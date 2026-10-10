package com.klee.sapio

import android.os.Build
import com.klee.sapio.ui.model.absoluteDate
import com.klee.sapio.ui.model.relativeDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M])
class DateFormatterTest {

    private val resources = RuntimeEnvironment.getApplication().resources

    @Test
    fun `absolute date renders the day in UTC`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 9)
        }

        assertEquals("2026-10-09", absoluteDate(calendar.time))
    }

    @Test
    fun `absolute date is empty without a date`() {
        assertEquals("", absoluteDate(null))
    }

    @Test
    fun `relative date is empty without a date`() {
        assertEquals("", relativeDate(null, resources))
    }

    @Test
    fun `relative date counts minutes below one hour`() {
        val expected = resources.getQuantityString(R.plurals.date_minutes_ago, 5, 5)

        assertEquals(expected, relativeDate(minutesAgo(5), resources))
    }

    @Test
    fun `relative date counts hours below one day`() {
        val expected = resources.getQuantityString(R.plurals.date_hours_ago, 3, 3)

        assertEquals(expected, relativeDate(minutesAgo(3 * 60), resources))
    }

    @Test
    fun `relative date counts days below one month`() {
        val expected = resources.getQuantityString(R.plurals.date_days_ago, 4, 4)

        assertEquals(expected, relativeDate(minutesAgo(4 * 24 * 60), resources))
    }

    @Test
    fun `relative date counts months below one year`() {
        val expected = resources.getQuantityString(R.plurals.date_months_ago, 2, 2)

        assertEquals(expected, relativeDate(minutesAgo(60 * 24 * 60), resources))
    }

    @Test
    fun `relative date counts years`() {
        val expected = resources.getQuantityString(R.plurals.date_years_ago, 2, 2)

        assertEquals(expected, relativeDate(minutesAgo(730 * 24 * 60), resources))
    }

    private fun minutesAgo(minutes: Int) = Date(System.currentTimeMillis() - minutes * 60L * 1000L)
}
