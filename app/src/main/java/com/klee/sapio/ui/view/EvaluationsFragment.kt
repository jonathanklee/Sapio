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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluationsBinding
import com.klee.sapio.domain.AppSettings
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.model.relativeDate
import com.klee.sapio.ui.model.SharedEvaluation
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@AndroidEntryPoint
class EvaluationsFragment : Fragment() {

    @Inject
    lateinit var settings: AppSettings

    private var _binding: FragmentEvaluationsBinding? = null
    private val mBinding get() = _binding!!
    private val mViewModel by activityViewModels<AppEvaluationsViewModel>()

    private lateinit var shareLauncher: ActivityResultLauncher<Intent>

    private var shareImage: Uri? = null
    private var iconReady = false

    companion object {
        const val TAG = "EvaluationsFragment"
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

        mBinding.contributeButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToContribute()
        }

        hideCard()

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

    private fun observeEvaluations() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                renderEvaluation(mBinding.microgUser, mBinding.microgUserRating, state.microgUser)
                renderEvaluation(mBinding.bareAospUser, mBinding.bareAospUserRating, state.bareAospUser)
                mBinding.microgUserDate.text = formatDateAgo(state.microgUser?.updatedAt)
                mBinding.bareAospUserDate.text = formatDateAgo(state.bareAospUser?.updatedAt)

                if (state.isFullyLoaded) {
                    val unsafeEnabled = settings.isUnsafeConfigurationEnabled()
                    val hasSecureData = state.microgUser != null || state.bareAospUser != null
                    val showUnsafeColumn = unsafeEnabled && (state.microgRoot != null || state.bareAospRoot != null)
                    val showSecureHeader = hasSecureData && showUnsafeColumn

                    mBinding.empty.isVisible = showSecureHeader || showUnsafeColumn
                    mBinding.secure.isVisible = showSecureHeader
                    mBinding.microgUserCell.isVisible = hasSecureData
                    mBinding.microgUserDate.isVisible = hasSecureData
                    mBinding.bareAospUserCell.isVisible = hasSecureData
                    mBinding.bareAospUserDate.isVisible = hasSecureData

                    mBinding.unsafe.isVisible = showUnsafeColumn
                    mBinding.microgRootCell.isVisible = showUnsafeColumn
                    mBinding.microgRootDate.isVisible = showUnsafeColumn
                    mBinding.bareAospRootCell.isVisible = showUnsafeColumn
                    mBinding.bareAospRootDate.isVisible = showUnsafeColumn

                    if (showUnsafeColumn) {
                        val extraPadding = resources.getDimensionPixelSize(R.dimen.card_unsafe_extra_padding)
                        mBinding.cardContent.setPadding(extraPadding, 0, extraPadding, 0)
                        renderEvaluation(mBinding.microgRoot, mBinding.microgRootRating, state.microgRoot)
                        renderEvaluation(mBinding.bareAospRoot, mBinding.bareAospRootRating, state.bareAospRoot)
                        mBinding.microgRootDate.text = formatDateAgo(state.microgRoot?.updatedAt)
                        mBinding.bareAospRootDate.text = formatDateAgo(state.bareAospRoot?.updatedAt)
                    }

                    mBinding.microgUserDate.text = formatDateAgo(state.microgUser?.updatedAt)
                    mBinding.bareAospUserDate.text = formatDateAgo(state.bareAospUser?.updatedAt)

                    val showBothColumns = hasSecureData && showUnsafeColumn
                    val nominalWidth = resources.getDimensionPixelSize(R.dimen.card_nominal_min_width)
                    mBinding.cardContent.layoutParams = mBinding.cardContent.layoutParams.also {
                        it.width = if (showBothColumns) ViewGroup.LayoutParams.WRAP_CONTENT else nominalWidth
                    }

                    val microgHasData = state.microgUser != null || (showUnsafeColumn && state.microgRoot != null)
                    val bareAospHasData = state.bareAospUser != null || (showUnsafeColumn && state.bareAospRoot != null)
                    mBinding.microgRow.isVisible = microgHasData
                    mBinding.microgDateRow.isVisible = microgHasData
                    mBinding.bareAospRow.isVisible = bareAospHasData
                    mBinding.bareAospDateRow.isVisible = bareAospHasData
                    mBinding.shareButton.isEnabled = state.microgUser != null || state.bareAospUser != null
                }

                if (state.iconUrl != null && !iconReady) {
                    iconReady = true
                    val needsCount = !state.isFullyLoaded
                    if (state.iconUrl.isNotEmpty()) {
                        Glide.with(requireContext().applicationContext)
                            .load(state.iconUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    if (needsCount) mViewModel.onIconDisplayed()
                                    return false
                                }

                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    if (needsCount) mViewModel.onIconDisplayed()
                                    return false
                                }
                            })
                            .into(mBinding.image)
                    } else {
                        if (needsCount) mViewModel.onIconDisplayed()
                    }
                }
            }
        }
    }

    private fun renderEvaluation(
        imageView: ImageView,
        ratingTextView: TextView,
        evaluation: com.klee.sapio.domain.model.Evaluation?
    ) {
        if (evaluation != null) {
            imageView.setImageResource(Rating.create(evaluation.rating).drawable)
            imageView.visibility = View.VISIBLE
            ratingTextView.text = getRatingShortLabel(evaluation.rating)
        } else {
            imageView.setImageDrawable(null)
            imageView.visibility = View.INVISIBLE
            ratingTextView.text = ""
        }

    }

    private fun getRatingShortLabel(rating: Int): String {
        return when (rating) {
            Rating.GOOD -> getString(R.string.good_short)
            Rating.AVERAGE -> getString(R.string.average_short)
            Rating.BAD -> getString(R.string.bad_short)
            else -> ""
        }
    }

    private fun formatDateAgo(date: Date?): String = relativeDate(date, resources)

    private fun startTakingScreenshot(appName: String, packageName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = mViewModel.uiState.value
            val icon = saveImageToFile(
                requireContext(),
                state.iconUrl.orEmpty()
            )
            val sharedEvaluation = SharedEvaluation(
                appName,
                packageName,
                icon,
                state.microgUser?.rating ?: 0,
                state.bareAospUser?.rating ?: 0,
            )
            share(takeScreenshot(sharedEvaluation), sharedEvaluation)
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
        scaleFactor: Float = 3f,
        composable: @Composable () -> Unit,
    ): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        val scaledDensity = displayMetrics.density * scaleFactor
        val widthPx = (widthDp * scaledDensity).toInt()
        val heightPx = (heightDp * scaledDensity).toInt()

        val composeView = ComposeView(context).apply {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(scaledDensity)) {
                    composable()
                }
            }
            layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)
        }

        mBinding.bitmapContainer.addView(composeView)

        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = createBitmap(widthPx, heightPx)
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        mBinding.bitmapContainer.removeView(composeView)

        return bitmap
    }

    private fun share(bitmap: Bitmap, sharedEvaluation: SharedEvaluation) {
        val contentValues = ContentValues().apply {
            put(Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}")
            put(Media.DESCRIPTION, getString(R.string.share_android_compatibility, sharedEvaluation.name))
            put(Media.MIME_TYPE, "image/png")
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
                bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream)
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Failed to share matrix", exception)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareImage)
            putExtra(Intent.EXTRA_TEXT, buildShareText(sharedEvaluation))
        }

        shareLauncher.launch(Intent.createChooser(shareIntent, "Share"))
    }

    private fun buildShareText(sharedEvaluation: SharedEvaluation): String {
        val header = getString(
            R.string.share_compatibility_report,
            sharedEvaluation.name,
            sharedEvaluation.packageName
        )
        val ratingParts = buildList {
            if (isKnownRating(sharedEvaluation.ratingMicrog)) {
                add("${getString(R.string.microg_label)} ${ratingToSymbol(sharedEvaluation.ratingMicrog)}")
            }
            if (isKnownRating(sharedEvaluation.ratingBareAOSP)) {
                add("${getString(R.string.bare_aosp_label)} ${ratingToSymbol(sharedEvaluation.ratingBareAOSP)}")
            }
        }
        val ratingSuffix = if (ratingParts.isNotEmpty()) ": ${ratingParts.joinToString(", ")}" else ""
        return "$header$ratingSuffix\n\n${getString(R.string.github_url)} #degoogle #sapio"
    }

    private fun ratingToSymbol(rating: Int): String {
        return when (rating) {
            Rating.GOOD -> "✓"
            Rating.AVERAGE -> "~"
            Rating.BAD -> "✗"
            else -> "?"
        }
    }

    private fun isKnownRating(rating: Int): Boolean {
        return rating == Rating.GOOD || rating == Rating.AVERAGE || rating == Rating.BAD
    }

    override fun onDestroyView() {
        super.onDestroyView()
        iconReady = false
        shareImage?.let {
            requireContext().contentResolver.delete(it, null, null)
            shareImage = null
        }
        _binding = null
    }
}
