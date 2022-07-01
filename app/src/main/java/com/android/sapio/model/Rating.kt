package com.android.sapio.model

data class Rating(val value: Int, val text: String) {

    companion object {

        const val GOOD = 1
        const val AVERAGE = 2
        const val BAD = 3

        fun create(rating: Int): Rating {
            return when (rating) {
                GOOD -> Rating(GOOD, "\uD83D\uDFE2 \uD83E\uDD47")
                AVERAGE -> Rating(AVERAGE, "\uD83D\uDFE0 \uD83D\uDE10")
                BAD -> Rating(BAD, "\uD83D\uDD34 \uD83D\uDC4E")
                else -> Rating(BAD, "\uD83D\uDD34 \uD83D\uDC4E")
            }
        }
    }
}
