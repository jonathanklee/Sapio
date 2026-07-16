package com.klee.sapio.ui.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.klee.sapio.R
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType

data class Label(val text: String, val color: Int) {

    companion object {
        const val MICROG = GmsType.MICROG
        const val BARE_AOSP = GmsType.BARE_AOSP
        const val STANDARD = UserType.STANDARD
        const val PERMISSIVE = UserType.PERMISSIVE

        @RequiresApi(Build.VERSION_CODES.M)
        fun create(context: Context, label: Int): Label {
            return when (label) {
                MICROG -> Label(
                    context.getString(R.string.microg_label),
                    context.getColor(R.color.blue_200)
                )
                BARE_AOSP -> Label(
                    context.getString(R.string.bare_aosp_label),
                    context.getColor(R.color.blue_700)
                )
                STANDARD -> Label(
                    context.getString(R.string.standard_label),
                    context.getColor(R.color.purple_200)
                )
                PERMISSIVE -> Label(
                    context.getString(R.string.permissive_label),
                    context.getColor(R.color.purple_700)
                )
                else -> Label(" Empty label ", context.getColor(R.color.black))
            }
        }
    }
}
