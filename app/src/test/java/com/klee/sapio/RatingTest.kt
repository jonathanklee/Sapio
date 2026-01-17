package com.klee.sapio

import com.klee.sapio.ui.model.Rating
import org.junit.Assert.assertEquals
import org.junit.Test

class RatingTest {

    @Test
    fun `create returns green circle for GOOD`() {
        val rating = Rating.create(Rating.GOOD)

        assertEquals(Rating.GOOD, rating.value)
        assertEquals(String(Character.toChars(Rating.GREEN_CIRCLE_EMOJI)), rating.text)
    }

    @Test
    fun `create returns yellow circle for AVERAGE`() {
        val rating = Rating.create(Rating.AVERAGE)

        assertEquals(Rating.AVERAGE, rating.value)
        assertEquals(String(Character.toChars(Rating.YELLOW_CIRCLE_EMOJI)), rating.text)
    }

    @Test
    fun `create returns red circle for BAD`() {
        val rating = Rating.create(Rating.BAD)

        assertEquals(Rating.BAD, rating.value)
        assertEquals(String(Character.toChars(Rating.RED_CIRCLE_EMOJI)), rating.text)
    }

    @Test
    fun `create defaults to BAD when rating is unknown`() {
        val rating = Rating.create(999)

        assertEquals(Rating.BAD, rating.value)
        assertEquals(String(Character.toChars(Rating.RED_CIRCLE_EMOJI)), rating.text)
    }
}
