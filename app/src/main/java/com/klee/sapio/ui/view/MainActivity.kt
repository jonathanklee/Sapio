package com.klee.sapio.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.klee.sapio.R
import com.klee.sapio.databinding.ActivityMainBinding
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import com.klee.sapio.ui.viewmodel.ChooseAppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val mEvaluationsViewModel by viewModels<AppEvaluationsViewModel>()
    private val mChooseAppViewModel by viewModels<ChooseAppViewModel>()
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    companion object {
        const val DONATE_URL = "https://ko-fi.com/jnthnkl"
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_APP_NAME = "appName"
        const val EXTRA_SHARE_IMMEDIATELY = "shareImmediately"
        const val EXTRA_NOTIFICATION_ID = "notificationId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        requestNotificationPermissionIfNeeded()
        mChooseAppViewModel.uiState

        if (savedInstanceState == null) {
            displayFragment(FeedFragment())
            handleDeepLinkIntent(intent)
        }

        handleEdgeToEdgeInsets()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else if (mBinding.bottomNavigation.selectedItemId != R.id.feed) {
                        mBinding.bottomNavigation.selectedItemId = R.id.feed
                    } else {
                        finish()
                    }
                }
            }
        )

        mBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.feed -> displayFragment(FeedFragment())
                R.id.search -> displayFragment(SearchFragment())
                R.id.my_apps -> displayFragment(MyAppsFragment())
                R.id.contribute -> displayFragment(ContributeFragment())
                R.id.options -> displayFragment(PreferencesFragment())
            }
            return@setOnItemSelectedListener true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
        val appName = intent.getStringExtra(EXTRA_APP_NAME).orEmpty()
        val shareImmediately = intent.getBooleanExtra(EXTRA_SHARE_IMMEDIATELY, false)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        navigateToEvaluations(packageName, appName, shareImmediately, notificationId)
    }

    fun navigateToAbout() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AboutFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToContribute() {
        mBinding.bottomNavigation.selectedItemId = R.id.contribute
    }

    fun navigateToEvaluations(
        packageName: String,
        appName: String,
        shareImmediately: Boolean = false,
        notificationId: Int = -1
    ) {
        mEvaluationsViewModel.listEvaluations(packageName)
        val fragment = EvaluationsFragment.newInstance(packageName, appName, shareImmediately, notificationId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun handleEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { v, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = maxOf(bars.bottom, ime.bottom)
                leftMargin = bars.left
                rightMargin = bars.right
                topMargin = bars.top
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            notificationPermissionLauncher.launch(permission)
        }
    }
}
