package com.klee.sapio.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.klee.sapio.R
import com.klee.sapio.data.EvaluationService
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

        mViewModel.iconUrl.observe(this) {
            Glide.with(this).load(EvaluationService.BASE_URL + it).into(mBinding.image)
        }

        val packageName = intent.getStringExtra("packageName").toString()
        mBinding.packageName.text = packageName

        val appName = intent.getStringExtra("appName").toString()
        mBinding.applicationName.text = appName

        mBinding.shareButton.setOnClickListener {
            share(takeScreenshot(mBinding.card), appName)
        }

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

    private fun takeScreenshot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun share(bitmap: Bitmap, appName: String) {
        val bitmapPath = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "screenshot",
            "screenshot"
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_SUBJECT, "Sapio")
            putExtra(Intent.EXTRA_STREAM, Uri.parse(bitmapPath))
            putExtra(
                Intent.EXTRA_TEXT,
                "$appName Android Compatibility Matrix from https://github.com/jonathanklee/Sapio"
            )
        }

        startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}
