package com.klee.sapio.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.BuildConfig
import com.klee.sapio.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding

    companion object {
        const val WEBSITE = "http://www.github.com/jonathanklee/sapio"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAboutBinding.inflate(layoutInflater)
        mBinding.version.text = "v${BuildConfig.VERSION_NAME}"

        mBinding.root.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE)))
        }

        setContentView(mBinding.root)
    }
}
