package com.klee.sapio.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.R
import com.klee.sapio.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.parse.Parse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initParse()

        val tabLayout = mBinding.tabLayout
        val viewPager = mBinding.viewPager
        viewPager.isUserInputEnabled = false
        viewPager.adapter = FragmentAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, viewPager, true, false) { tab, position ->
            tab.text = when(position) {
                0 -> "Feed"
                1 -> "Search"
                2 -> "Evaluate"
                else -> ""
            }
        }.attach()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
