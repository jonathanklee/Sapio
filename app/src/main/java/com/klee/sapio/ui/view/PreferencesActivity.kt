package com.klee.sapio.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.R
import com.klee.sapio.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setContentView(R.layout.activity_preferences)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, PreferencesFragment())
            .commit()
    }
}
