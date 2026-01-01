package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.klee.sapio.R
import com.klee.sapio.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    companion object {
        const val DONATE_URL = "https://ko-fi.com/jnthnkl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        displayFragment(FeedFragment())

        handleEdgeToEdgeInsets()

        mBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.feed -> displayFragment(FeedFragment())
                R.id.search -> displayFragment(SearchFragment())
                R.id.contribute -> displayFragment(ContributeFragment())
                R.id.options -> displayFragment(PreferencesFragment())
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun handleEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { v, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
            )

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = bars.bottom
                leftMargin = bars.left
                rightMargin = bars.right
                topMargin = bars.top
            }

            WindowInsetsCompat.CONSUMED
        }
    }
}
