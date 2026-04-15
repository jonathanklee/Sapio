package com.klee.sapio.ui.model

import com.klee.sapio.R

data class Rating(val value: Int, val drawable: Int, val text: String) {

    companion object {

        const val GOOD = 1
        const val AVERAGE = 2
        const val BAD = 3

        const val GREEN_CIRCLE_EMOJI = 0x1F7E2
        const val YELLOW_CIRCLE_EMOJI = 0x1F7E1
        const val RED_CIRCLE_EMOJI = 0x1F534

        fun create(rating: Int): Rating {
            return when (rating) {
                GOOD -> Rating(GOOD, R.drawable.ic_status_green, String(Character.toChars(GREEN_CIRCLE_EMOJI)))
                AVERAGE -> Rating(AVERAGE, R.drawable.ic_status_yellow, String(Character.toChars(YELLOW_CIRCLE_EMOJI)))
                BAD -> Rating(BAD, R.drawable.ic_status_red, String(Character.toChars(RED_CIRCLE_EMOJI)))
                else -> Rating(BAD, R.drawable.ic_status_red, String(Character.toChars(RED_CIRCLE_EMOJI)))
            }
        }
    }
}
