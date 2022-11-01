package com.klee.sapio.view.ui

import android.content.Context
import android.widget.Toast

object ToastMessage {

    fun showConnectivityIssue(context: Context) {
        Toast.makeText(
            context,
            "You seem to be offline. Connectivity is required.",
            Toast.LENGTH_LONG
        ).show()
    }
}
