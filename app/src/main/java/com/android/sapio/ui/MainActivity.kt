package com.android.sapio.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.sapio.R
import com.android.sapio.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.parse.Parse

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initParse()

        val tabLayout = mBinding.tabLayout
        val viewPager = mBinding.viewPager
        viewPager.adapter = FragmentAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Feed"
                1 -> "Search"
                2 -> "Evaluate"
                else -> ""
            }
        }.attach()

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
