package com.klee.sapio.ui.view

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.klee.sapio.R

class PreferencesFragment : PreferenceFragmentCompat()  {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
