package com.klee.sapio.ui.view

import android.content.Context
import android.widget.Toast

object ToastMessage {

    fun showNetworkIssue(context: Context) {
        Toast.makeText(
            context,
            "Sapio's server cannot be reached.",
            Toast.LENGTH_LONG
        ).show()
    }
}
