package com.android.libreapps.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.libreapps.R
import com.android.libreapps.databinding.ActivityMainBinding
import com.parse.Parse

class MainActivity : AppCompatActivity() {

    private lateinit var mAppAdapter: AppAdapter
    private lateinit var mBinding: ActivityMainBinding
    private val mViewModel = AppViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        initParse()

        mViewModel.data.observe(this) { list ->
            mAppAdapter = AppAdapter(list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        mViewModel.loadApps()
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
