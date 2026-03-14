package com.klee.sapio.data

import com.klee.sapio.data.local.Converters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromTimestamp returns null when value is null`() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `fromTimestamp returns correct Date for given timestamp`() {
        val timestamp = 1700000000000L
        val result = converters.fromTimestamp(timestamp)
        assertEquals(Date(timestamp), result)
    }

    @Test
    fun `fromTimestamp returns epoch date for zero`() {
        val result = converters.fromTimestamp(0L)
        assertEquals(Date(0), result)
    }

    @Test
    fun `dateToTimestamp returns null when date is null`() {
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun `dateToTimestamp returns correct timestamp for given date`() {
        val timestamp = 1700000000000L
        val date = Date(timestamp)
        assertEquals(timestamp, converters.dateToTimestamp(date))
    }

    @Test
    fun `dateToTimestamp returns zero for epoch date`() {
        assertEquals(0L, converters.dateToTimestamp(Date(0)))
    }

    @Test
    fun `fromTimestamp and dateToTimestamp are inverse operations`() {
        val timestamp = 1700000000000L
        val date = converters.fromTimestamp(timestamp)
        assertEquals(timestamp, converters.dateToTimestamp(date))
    }
}
