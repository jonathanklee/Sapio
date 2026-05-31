package com.klee.sapio.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.klee.sapio.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            v.updatePadding(
                left = bars.left,
                right = bars.right,
                top = bars.top,
                bottom = bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }

        setupPreferenceClickListeners()
        setupThemePreference()
    }

    private fun setupThemePreference() {
        findPreference<ListPreference>("theme_mode")?.setOnPreferenceChangeListener { _, newValue ->
            val mode = when (newValue as String) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            true
        }
    }

    private fun setupPreferenceClickListeners() {
        findPreference<Preference>("github_star_preference")?.setOnPreferenceClickListener {
            val githubUrl = getString(R.string.github_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
            true
        }

        findPreference<Preference>("about_preference")?.setOnPreferenceClickListener {
            (requireActivity() as MainActivity).navigateToAbout()
            true
        }

        findPreference<Preference>("donate_preference")?.setOnPreferenceClickListener {
            val donateUrl = "https://ko-fi.com/jnthnkl"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
            startActivity(intent)
            true
        }
    }
}
