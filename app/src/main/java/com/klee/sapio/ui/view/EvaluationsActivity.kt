package com.klee.sapio.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.klee.sapio.R
import com.klee.sapio.data.Rating
import com.klee.sapio.databinding.ActivityEvaluationsBinding
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EvaluationsActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityEvaluationsBinding
    private val mViewModel by viewModels<AppEvaluationsViewModel>()

    companion object {
        const val NO_EVALUATION_CHAR = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityEvaluationsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mViewModel.microgUserEvaluation.observe(this) {
            mBinding.microgUser.text = it?.let {
                Rating.create(it.rating).text
            } ?: NO_EVALUATION_CHAR

            mBinding.microgUser.tooltipText = it?.let {
                computeTooltip(it.rating)
            }
        }

        mViewModel.microgRootEvaluation.observe(this) {
            mBinding.microgRoot.text = it?.let {
                Rating.create(it.rating).text
            } ?: NO_EVALUATION_CHAR

            mBinding.microgRoot.tooltipText = it?.let {
                computeTooltip(it.rating)
            }
        }

        mViewModel.bareAospUserEvaluation.observe(this) {
            mBinding.bareAospUser.text = it?.let {
                Rating.create(it.rating).text
            } ?: NO_EVALUATION_CHAR

            mBinding.bareAospUser.tooltipText = it?.let {
                computeTooltip(it.rating)
            }
        }

        mViewModel.bareAsopRootEvaluation.observe(this) {
            mBinding.bareAospRoot.text = it?.let {
                Rating.create(it.rating).text
            } ?: NO_EVALUATION_CHAR

            mBinding.bareAospRoot.tooltipText = it?.let {
                computeTooltip(it.rating)
            }
        }

        val packageName = intent.getStringExtra("packageName").toString()
        mBinding.packageName.text = packageName

        val appName = intent.getStringExtra("appName").toString()
        mBinding.applicationName.text = appName

        val iconUrl = intent.getStringExtra("iconUrl").toString()
        Glide.with(this).load(iconUrl).into(mBinding.image)

        mBinding.infoIcon.setOnClickListener() {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        mViewModel.listEvaluations(packageName)
    }

    private fun computeTooltip(rating: Int): String {
        return when (rating) {
            Rating.GOOD -> getString(R.string.good)
            Rating.AVERAGE -> getString(R.string.average)
            Rating.BAD -> getString(R.string.bad)
            else -> getString(R.string.unknown)
        }
    }
}