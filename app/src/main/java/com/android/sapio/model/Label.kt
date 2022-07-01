package com.android.sapio.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.sapio.R

data class Label(val text: String, val color: Int) {

    companion object {
        const val MICROG = 1
        const val BARE_AOSP = 2
        const val USER = 3
        const val ROOTED = 4

        @RequiresApi(Build.VERSION_CODES.M)
        fun create(context: Context, label: Int): Label {
            return when (label) {
                MICROG -> Label(
                    context.getString(R.string.microg_label), context.getColor(R.color.teal_200)
                )
                BARE_AOSP -> Label(
                    context.getString(R.string.bare_aosp_label), context.getColor(R.color.teal_700)
                )
                USER -> Label(
                    context.getString(R.string.user_label), context.getColor(R.color.purple_200)
                )
                ROOTED -> Label(
                    context.getString(R.string.rooted_label), context.getColor(R.color.purple_500)
                )
                else -> Label(" Empty label ", context.getColor(R.color.black))
            }
        }
    }
}
