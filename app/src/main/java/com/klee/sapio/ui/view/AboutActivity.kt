package com.klee.sapio.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
    }
}
