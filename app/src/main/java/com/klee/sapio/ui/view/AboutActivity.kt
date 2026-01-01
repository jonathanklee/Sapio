package com.klee.sapio.ui.view

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.klee.sapio.BuildConfig
import com.klee.sapio.R
import com.klee.sapio.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding

    companion object {
        const val RATING_RULES = "https://github.com/jonathanklee/Sapio?tab=readme-ov-file#rating"
        const val GITHUB_URL = "https://github.com/jonathanklee/Sapio"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAboutBinding.inflate(layoutInflater)
        mBinding.version.text = "v${BuildConfig.VERSION_NAME}"
        mBinding.ratingRules.text = Html.fromHtml(
            getString(R.string.rating_rules, RATING_RULES),
            Html.FROM_HTML_MODE_COMPACT
        )

        mBinding.ratingRules.movementMethod = LinkMovementMethod.getInstance()

        setContentView(mBinding.root)
    }
}
