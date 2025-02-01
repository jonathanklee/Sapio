package com.klee.sapio.ui.view

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.klee.sapio.R
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationService
import com.klee.sapio.data.Rating
import com.klee.sapio.databinding.ActivityEvaluationsBinding
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class EvaluationsActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityEvaluationsBinding
    private val mViewModel by viewModels<AppEvaluationsViewModel>()

    private val microgUserReceived = MutableSharedFlow<Boolean>()
    private val microgRootReceived = MutableSharedFlow<Boolean>()
    private val bareAospUserReceived = MutableSharedFlow<Boolean>()
    private val bareAospRootReceived = MutableSharedFlow<Boolean>()
    private val iconReceived = MutableSharedFlow<Boolean>()

    companion object {
        const val TAG = "EvaluationsActivity"
        const val NO_EVALUATION_CHAR = ""
        const val COMPRESSION_QUALITY = 100
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_APP_NAME = "appName"
        const val EXTRA_SHARE_IMMEDIATELY = "shareImmediately"
        const val IMAGE_LOADING_DELAY_IN_MS = 200L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityEvaluationsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        observeEvaluation(mViewModel.microgUserEvaluation, mBinding.microgUser, microgUserReceived)
        observeEvaluation(mViewModel.microgRootEvaluation, mBinding.microgRoot, microgRootReceived)
        observeEvaluation(mViewModel.bareAospUserEvaluation, mBinding.bareAospUser, bareAospUserReceived)
        observeEvaluation(mViewModel.bareAsopRootEvaluation, mBinding.bareAospRoot, bareAospRootReceived)

        mViewModel.iconUrl.observe(this) {
            Glide.with(this.applicationContext)
                .load(EvaluationService.BASE_URL + it)
                .listener(glideListener)
                .into(mBinding.image)
        }

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).toString()
        mBinding.packageName.text = packageName

        val appName = intent.getStringExtra(EXTRA_APP_NAME).toString()
        mBinding.applicationName.text = appName

        mBinding.shareButton.setOnClickListener {
            share(takeScreenshot(), appName)
        }

        mBinding.infoIcon.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        mViewModel.listEvaluations(packageName)

        val shareImmediately = intent.getBooleanExtra(EXTRA_SHARE_IMMEDIATELY, false)
        if (shareImmediately) {
            combine(microgUserReceived, microgRootReceived, bareAospUserReceived,
                bareAospRootReceived, iconReceived) { _, _, _, _, _ ->
                share(takeScreenshot(), appName)
            }.launchIn(lifecycleScope)
        }
    }

    private fun observeEvaluation(
        liveData: MutableLiveData<Evaluation>,
        textView: EmojiTextView,
        flow: MutableSharedFlow<Boolean>
    ) {
        liveData.observe(this) {
            textView.text = it?.let {
                Rating.create(it.rating).text
            } ?: NO_EVALUATION_CHAR

            textView.tooltipText = it?.let {
                computeTooltip(it.rating)
            }

            lifecycleScope.launch {
                flow.emit(true)
            }
        }
    }

    private fun computeTooltip(rating: Int): String {
        return when (rating) {
            Rating.GOOD -> getString(R.string.good)
            Rating.AVERAGE -> getString(R.string.average)
            Rating.BAD -> getString(R.string.bad)
            else -> getString(R.string.unknown)
        }
    }

    private fun takeScreenshot(): Bitmap {
        val view = mBinding.root
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun share(bitmap: Bitmap, appName: String) {

        val contentValues = ContentValues().apply {
            put(Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}")
            put(Media.DESCRIPTION, "$appName Android Compatibility Matrix")
            put(Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/${Environment.DIRECTORY_SCREENSHOTS}"
                )
            }
        }

        val imageUri =
            contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return

        try {
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Failed to share matrix", exception)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(
                Intent.EXTRA_TEXT,
                "$appName Android Compatibility Matrix https://github.com/jonathanklee/Sapio #sapio"
            )
        }

        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    private val glideListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            lifecycleScope.launch {
                delay(IMAGE_LOADING_DELAY_IN_MS)
                iconReceived.emit(true)
            }
            return false
        }
    }
}
