package com.klee.sapio.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.BuildConfig
import com.klee.sapio.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAboutBinding.inflate(layoutInflater)
        mBinding.version.text = "v${BuildConfig.VERSION_NAME}"
        setContentView(mBinding.root)
    }
}
