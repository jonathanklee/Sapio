package com.klee.sapio.ui.view

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.emoji2.widget.EmojiTextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.klee.sapio.R
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.system.Settings
import com.klee.sapio.databinding.ActivityEvaluationsBinding
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.model.SharedEvaluation
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
@AndroidEntryPoint
class EvaluationsActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: Settings

    private lateinit var mBinding: ActivityEvaluationsBinding
    private val mViewModel by viewModels<AppEvaluationsViewModel>()

    private val microgUserReceived = MutableSharedFlow<Boolean>()
    private val microgRootReceived = MutableSharedFlow<Boolean>()
    private val bareAospUserReceived = MutableSharedFlow<Boolean>()
    private val bareAospRootReceived = MutableSharedFlow<Boolean>()
    private val iconReceived = MutableSharedFlow<Boolean>()
    private lateinit var shareLauncher: ActivityResultLauncher<Intent>

    private var shareImage: Uri? = null
    private var microgUserReady = false
    private var microgRootReady = false
    private var bareAospUserReady = false
    private var bareAospRootReady = false
    private var iconReady = false

    companion object {
        const val TAG = "EvaluationsActivity"
        const val NO_EVALUATION_CHAR = ""
        const val COMPRESSION_QUALITY = 100
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_APP_NAME = "appName"
        const val EXTRA_SHARE_IMMEDIATELY = "shareImmediately"
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val IMAGE_LOADING_DELAY_IN_MS = 200L
        const val SCREENSHOT_WIDTH_DP = 200
        const val SCREENSHOT_HEIGHT_DP = 115
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityEvaluationsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        observeEvaluations()
        observeShare()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).toString()
        mBinding.packageName.text = packageName

        val appName = intent.getStringExtra(EXTRA_APP_NAME).toString()
        mBinding.applicationName.text = appName

        mBinding.shareButton.setOnClickListener {
            startTakingScreenshot(appName, packageName)
        }

        mBinding.infoIcon.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        hideCard()
        resetLoadedFlags()
        mViewModel.listEvaluations(packageName)

        val shareImmediately = intent.getBooleanExtra(EXTRA_SHARE_IMMEDIATELY, false)
        if (shareImmediately) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (notificationId != -1) {
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.cancel(notificationId)
            }
            onElementsLoaded(once = true) {
                startTakingScreenshot(appName, packageName)
            }
        }

        onElementsLoaded {
            showCard()
        }

        handleRootConfigurationSetting()
    }

    private fun hideCard() {
        mBinding.card.visibility = View.INVISIBLE
    }

    private fun showCard() {
        mBinding.card.visibility = View.VISIBLE
    }

    private fun onElementsLoaded(once: Boolean = false, callback: () -> Unit) {
        elementsLoadedFlow(
            settings.isRootConfigurationEnabled(),
            ElementsLoadedSignals(
                microgUserReceived = microgUserReceived,
                microgRootReceived = microgRootReceived,
                bareAospUserReceived = bareAospUserReceived,
                bareAospRootReceived = bareAospRootReceived,
                iconReceived = iconReceived
            ),
            once
        ).onEach { callback.invoke() }
            .launchIn(lifecycleScope)
    }

    private fun handleRootConfigurationSetting() {
        val shouldShow = settings.isRootConfigurationEnabled()
        with(mBinding) {
            secure.isVisible = shouldShow
            microgRoot.isVisible = shouldShow
            bareAospRoot.isVisible = shouldShow
            empty.isVisible = shouldShow
            risky.isVisible = shouldShow
        }
    }

    private fun observeEvaluations() {
        lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                if (state.microgUserLoaded && !microgUserReady) {
                    microgUserReady = true
                    updateEvaluation(mBinding.microgUser, state.microgUser, microgUserReceived)
                }

                if (state.bareAospUserLoaded && !bareAospUserReady) {
                    bareAospUserReady = true
                    updateEvaluation(mBinding.bareAospUser, state.bareAospUser, bareAospUserReceived)
                }

                if (settings.isRootConfigurationEnabled()) {
                    if (state.bareAospRootLoaded && !bareAospRootReady) {
                        bareAospRootReady = true
                        updateEvaluation(mBinding.bareAospRoot, state.bareAospRoot, bareAospRootReceived)
                    }

                    if (state.microgRootLoaded && !microgRootReady) {
                        microgRootReady = true
                        updateEvaluation(mBinding.microgRoot, state.microgRoot, microgRootReceived)
                    }
                }

                if (state.iconLoaded && !iconReady) {
                    iconReady = true
                    Glide.with(this@EvaluationsActivity.applicationContext)
                        .load(EvaluationService.BASE_URL + state.iconUrl)
                        .listener(glideListener)
                        .into(mBinding.image)
                    lifecycleScope.launch { iconReceived.emit(true) }
                }
            }
        }
    }

    private fun resetLoadedFlags() {
        microgUserReady = false
        microgRootReady = false
        bareAospUserReady = false
        bareAospRootReady = false
        iconReady = false
    }

    private fun updateEvaluation(
        textView: EmojiTextView,
        evaluation: com.klee.sapio.domain.model.Evaluation?,
        flow: MutableSharedFlow<Boolean>
    ) {
        textView.text = evaluation?.let {
            Rating.create(it.rating).text
        } ?: NO_EVALUATION_CHAR

        textView.tooltipText = evaluation?.let {
            computeTooltip(it.rating)
        }

        lifecycleScope.launch {
            flow.emit(true)
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

    private fun startTakingScreenshot(appName: String, packageName: String) {
        lifecycleScope.launch {
            val state = mViewModel.uiState.value
            val icon = saveImageToFile(
                this@EvaluationsActivity,
                EvaluationService.BASE_URL + state.iconUrl
            )
            val sharedEvaluation = SharedEvaluation(
                appName,
                packageName,
                icon,
                state.microgUser?.rating ?: 0,
                state.bareAospUser?.rating ?: 0,
            )
            share(takeScreenshot(sharedEvaluation), appName)
        }
    }

    private fun takeScreenshot(sharedEvaluation: SharedEvaluation): Bitmap {
        return composeToBitmap(this@EvaluationsActivity, SCREENSHOT_WIDTH_DP, SCREENSHOT_HEIGHT_DP) {
            ShareScreenshot(sharedEvaluation)
        }
    }

    private suspend fun saveImageToFile(
        context: Context,
        url: String
    ): Bitmap = suspendCancellableCoroutine { continuation ->
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                continuation.resume(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                continuation.resumeWithException(Exception("Failed to load image"))
            }
        }

        Glide.with(applicationContext)
            .asBitmap()
            .load(url)
            .into(target)

        continuation.invokeOnCancellation {
            Glide.with(context).clear(target)
        }
    }

    private fun composeToBitmap(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        scaleFactor: Float = 2f,
        composable: @Composable () -> Unit,
    ): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            widthDp.toFloat(),
            displayMetrics
        ).toInt()
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightDp.toFloat(),
            displayMetrics
        ).toInt()

        val composeView = ComposeView(context).apply {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            setContent { composable() }
            layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)
        }

        // Add to container for proper rendering context
        mBinding.bitmapContainer.addView(composeView)

        // Measure and layout
        composeView.measure(
            MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(heightPx, MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        // Create bitmap and draw
        val bitmap = createBitmap(
            (composeView.width * scaleFactor).toInt(),
            (composeView.height * scaleFactor).toInt()
        )
        val canvas = Canvas(bitmap)
        canvas.scale(scaleFactor, scaleFactor)
        composeView.draw(canvas)

        // Clean up
        mBinding.bitmapContainer.removeView(composeView)

        return bitmap
    }

    private fun observeShare() {
        shareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            contentResolver.delete(shareImage!!, null, null)
            shareImage = null
        }
    }

    private fun share(bitmap: Bitmap, appName: String) {
        val contentValues = ContentValues().apply {
            put(Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}")
            put(Media.DESCRIPTION, getString(R.string.share_android_compatibility, appName))
            put(Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/${Environment.DIRECTORY_SCREENSHOTS}"
                )
            }
        }

        shareImage =
            contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return

        try {
            contentResolver.openOutputStream(shareImage!!)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Failed to share matrix", exception)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareImage)
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_android_compatibility_text, appName)
            )
        }

        shareLauncher.launch(Intent.createChooser(shareIntent, "Share"))
    }

    override fun onDestroy() {
        super.onDestroy()
        shareLauncher.unregister()
        if (shareImage != null) {
            contentResolver.delete(shareImage!!, null, null)
        }
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
