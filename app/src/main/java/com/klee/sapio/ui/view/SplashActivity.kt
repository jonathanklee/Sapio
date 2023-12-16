package com.klee.sapio.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.klee.sapio.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    companion object {
        const val SPLASH_DELAY_MS = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val delay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0 else SPLASH_DELAY_MS

        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, delay.toLong())
    }
}
