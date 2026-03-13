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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.emoji2.widget.EmojiTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.klee.sapio.databinding.FragmentEvaluationsBinding
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.model.SharedEvaluation
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@AndroidEntryPoint
class EvaluationsFragment : Fragment() {

    @Inject
    lateinit var settings: Settings

    private var _binding: FragmentEvaluationsBinding? = null
    private val mBinding get() = _binding!!
    private val mViewModel by viewModels<AppEvaluationsViewModel>()

    private lateinit var shareLauncher: ActivityResultLauncher<Intent>

    private var shareImage: Uri? = null
    private var iconReady = false

    companion object {
        const val TAG = "EvaluationsFragment"
        const val NO_EVALUATION_CHAR = ""
        const val COMPRESSION_QUALITY = 100
        const val SCREENSHOT_WIDTH_DP = 200
        const val SCREENSHOT_HEIGHT_DP = 115
        private const val ARG_PACKAGE_NAME = "packageName"
        private const val ARG_APP_NAME = "appName"
        private const val ARG_SHARE_IMMEDIATELY = "shareImmediately"
        private const val ARG_NOTIFICATION_ID = "notificationId"

        fun newInstance(
            packageName: String,
            appName: String,
            shareImmediately: Boolean = false,
            notificationId: Int = -1
        ) = EvaluationsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PACKAGE_NAME, packageName)
                putString(ARG_APP_NAME, appName)
                putBoolean(ARG_SHARE_IMMEDIATELY, shareImmediately)
                putInt(ARG_NOTIFICATION_ID, notificationId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            shareImage?.let { requireContext().contentResolver.delete(it, null, null) }
            shareImage = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEvaluationsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageName = arguments?.getString(ARG_PACKAGE_NAME).orEmpty()
        val appName = arguments?.getString(ARG_APP_NAME).orEmpty()
        val shareImmediately = arguments?.getBoolean(ARG_SHARE_IMMEDIATELY) ?: false
        val notificationId = arguments?.getInt(ARG_NOTIFICATION_ID) ?: -1

        mBinding.packageName.text = packageName
        mBinding.applicationName.text = appName

        mBinding.shareButton.setOnClickListener {
            startTakingScreenshot(appName, packageName)
        }

        mBinding.infoIcon.setOnClickListener {
            (requireActivity() as MainActivity).navigateToAbout()
        }

        hideCard()
        mViewModel.listEvaluations(packageName)

        if (shareImmediately) {
            if (notificationId != -1) {
                val notificationManager =
                    requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
            onElementsLoaded {
                startTakingScreenshot(appName, packageName)
            }
        }

        onElementsLoaded {
            showCard()
        }

        handleRootConfigurationSetting()
        observeEvaluations()
    }

    private fun hideCard() {
        mBinding.card.visibility = View.INVISIBLE
        mBinding.progressBar.visibility = View.VISIBLE
    }

    private fun showCard() {
        mBinding.progressBar.visibility = View.GONE
        mBinding.card.visibility = View.VISIBLE
    }

    private fun onElementsLoaded(callback: () -> Unit) {
        mViewModel.uiState
            .filter { it.isFullyLoaded }
            .onEach { callback.invoke() }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                renderEvaluation(mBinding.microgUser, state.microgUser)
                renderEvaluation(mBinding.bareAospUser, state.bareAospUser)

                if (settings.isRootConfigurationEnabled()) {
                    renderEvaluation(mBinding.bareAospRoot, state.bareAospRoot)
                    renderEvaluation(mBinding.microgRoot, state.microgRoot)
                }

                if (state.isFullyLoaded) {
                    val rootEnabled = settings.isRootConfigurationEnabled()
                    val microgHasData = state.microgUser != null || (rootEnabled && state.microgRoot != null)
                    val bareAospHasData = state.bareAospUser != null || (rootEnabled && state.bareAospRoot != null)
                    mBinding.microgRow.isVisible = microgHasData
                    mBinding.bareAospRow.isVisible = bareAospHasData
                }

                if (state.iconUrl != null && !iconReady) {
                    iconReady = true
                    if (state.iconUrl.isNotEmpty()) {
                        Glide.with(requireContext().applicationContext)
                            .load(EvaluationService.BASE_URL + state.iconUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mViewModel.onIconDisplayed()
                                    return false
                                }

                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    mViewModel.onIconDisplayed()
                                    return false
                                }
                            })
                            .into(mBinding.image)
                    } else {
                        mViewModel.onIconDisplayed()
                    }
                }
            }
        }
    }

    private fun renderEvaluation(
        textView: EmojiTextView,
        evaluation: com.klee.sapio.domain.model.Evaluation?
    ) {
        textView.text = evaluation?.let {
            Rating.create(it.rating).text
        } ?: NO_EVALUATION_CHAR

        textView.tooltipText = evaluation?.let {
            computeTooltip(it.rating)
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
        viewLifecycleOwner.lifecycleScope.launch {
            val state = mViewModel.uiState.value
            val icon = saveImageToFile(
                requireContext(),
                EvaluationService.BASE_URL + (state.iconUrl ?: "")
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
        return composeToBitmap(requireContext(), SCREENSHOT_WIDTH_DP, SCREENSHOT_HEIGHT_DP) {
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

        Glide.with(requireContext().applicationContext)
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

        mBinding.bitmapContainer.addView(composeView)

        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = createBitmap(
            (composeView.width * scaleFactor).toInt(),
            (composeView.height * scaleFactor).toInt()
        )
        val canvas = Canvas(bitmap)
        canvas.scale(scaleFactor, scaleFactor)
        composeView.draw(canvas)

        mBinding.bitmapContainer.removeView(composeView)

        return bitmap
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
            requireContext().contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return

        try {
            requireContext().contentResolver.openOutputStream(shareImage!!)?.use { outputStream ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        shareImage?.let {
            requireContext().contentResolver.delete(it, null, null)
            shareImage = null
        }
        _binding = null
    }
}
