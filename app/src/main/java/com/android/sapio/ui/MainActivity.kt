package com.android.sapio.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.sapio.R
import com.android.sapio.databinding.ActivityMainBinding
import com.parse.Parse

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initParse()
    }

    private fun initParse() {
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )
    }
}
