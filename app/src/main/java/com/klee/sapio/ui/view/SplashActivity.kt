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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { Build.VERSION.SDK_INT >= 31 }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val delay = if (Build.VERSION.SDK_INT >= 31) 0 else 2000

        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, delay.toLong())
    }
}
