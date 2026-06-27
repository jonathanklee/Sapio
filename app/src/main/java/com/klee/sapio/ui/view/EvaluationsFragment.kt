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
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
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
        mBinding.shareButton.visibility = View.INVISIBLE
        mBinding.progressBar.visibility = View.VISIBLE
    }

    private fun showCard() {
        mBinding.progressBar.visibility = View.GONE
        mBinding.card.visibility = View.VISIBLE
        mBinding.shareButton.visibility = View.VISIBLE
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
                if (state.isFullyLoaded) {
                    val unsafeEnabled = settings.isUnsafeConfigurationEnabled()

                    mBinding.microgSecureBadge.isVisible = unsafeEnabled
                    mBinding.bareAospSecureBadge.isVisible = unsafeEnabled

                    val showMicrog = state.microgUser != null || (unsafeEnabled && state.microgRoot != null)
                    val showBareAosp = state.bareAospUser != null || (unsafeEnabled && state.bareAospRoot != null)

                    mBinding.microgCard.isVisible = showMicrog
                    mBinding.bareAospCard.isVisible = showBareAosp

                    val showMicrogUser = state.microgUser != null
                    val showMicrogRoot = unsafeEnabled && state.microgRoot != null
                    val showBareAospUser = state.bareAospUser != null
                    val showBareAospRoot = unsafeEnabled && state.bareAospRoot != null

                    mBinding.microgUserCell.isVisible = showMicrogUser
                    mBinding.bareAospUserCell.isVisible = showBareAospUser
                    mBinding.microgRootCell.isVisible = showMicrogRoot
                    mBinding.bareAospRootCell.isVisible = showBareAospRoot

                    adjustChipMargins(mBinding.microgUserCell, mBinding.microgRootCell, showMicrogUser, showMicrogRoot)
                    adjustChipMargins(mBinding.bareAospUserCell, mBinding.bareAospRootCell, showBareAospUser, showBareAospRoot)

                    renderChip(
                        mBinding.microgUserCell,
                        mBinding.microgUser,
                        mBinding.microgUserRating,
                        mBinding.microgUserDate,
                        mBinding.microgUserBrokenFeatures,
                        state.microgUser
                    )
                    renderChip(
                        mBinding.bareAospUserCell,
                        mBinding.bareAospUser,
                        mBinding.bareAospUserRating,
                        mBinding.bareAospUserDate,
                        mBinding.bareAospUserBrokenFeatures,
                        state.bareAospUser
                    )
                    renderChip(
                        mBinding.microgRootCell,
                        mBinding.microgRoot,
                        mBinding.microgRootRating,
                        mBinding.microgRootDate,
                        mBinding.microgRootBrokenFeatures,
                        state.microgRoot
                    )
                    renderChip(
                        mBinding.bareAospRootCell,
                        mBinding.bareAospRoot,
                        mBinding.bareAospRootRating,
                        mBinding.bareAospRootDate,
                        mBinding.bareAospRootBrokenFeatures,
                        state.bareAospRoot
                    )

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

    private fun adjustChipMargins(
        userCell: MaterialCardView,
        rootCell: MaterialCardView,
        showUser: Boolean,
        showRoot: Boolean
    ) {
        val gap = resources.getDimensionPixelSize(R.dimen.chip_gap)
        val bothVisible = showUser && showRoot

        (userCell.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let { params ->
            params.marginEnd = if (bothVisible) gap else 0
            userCell.layoutParams = params
        }
        (rootCell.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let { params ->
            params.marginStart = if (bothVisible) gap else 0
            rootCell.layoutParams = params
        }
    }

    private fun renderChip(
        chipView: MaterialCardView,
        iconView: ImageView,
        ratingTextView: TextView,
        dateTextView: TextView,
        brokenFeaturesChipGroup: ChipGroup,
        evaluation: com.klee.sapio.domain.model.Evaluation?
    ) {
        if (evaluation != null) {
            iconView.setImageResource(Rating.create(evaluation.rating).drawable)
            iconView.isVisible = true
            ratingTextView.text = getRatingShortLabel(evaluation.rating)
            dateTextView.text = formatVersionAndDate(evaluation.versionName, evaluation.updatedAt)
            renderBrokenFeatures(brokenFeaturesChipGroup, evaluation)
        } else {
            iconView.isVisible = false
            ratingTextView.text = "–"
            dateTextView.text = ""
            (brokenFeaturesChipGroup.parent as? ViewGroup)?.isVisible = false
        }
    }

    private fun renderBrokenFeatures(
        chipGroup: ChipGroup,
        evaluation: com.klee.sapio.domain.model.Evaluation
    ) {
        val container = chipGroup.parent as? ViewGroup
        val features = evaluation.brokenFeatures

        if (evaluation.rating != Rating.AVERAGE || features.isNullOrEmpty()) {
            container?.isVisible = false
            return
        }

        chipGroup.removeAllViews()
        features.forEach { key ->
            val label = brokenFeatureLabelForKey(key) ?: return@forEach
            val pill = TextView(requireContext()).apply {
                text = "× $label"
                textSize = 10f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_label_rounded)
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.red_600)
                )
                val h = resources.getDimensionPixelSize(R.dimen.chip_horizontal_padding)
                val v = resources.getDimensionPixelSize(R.dimen.chip_vertical_padding)
                setPadding(h, v, h, v)
            }
            chipGroup.addView(pill)
        }
        container?.isVisible = true
    }

    private fun brokenFeatureLabelForKey(key: String): String? = when (key) {
        "notifications" -> getString(R.string.broken_feature_notifications)
        "in_app_purchase" -> getString(R.string.broken_feature_in_app_purchase)
        "login" -> getString(R.string.broken_feature_login)
        "maps" -> getString(R.string.broken_feature_maps)
        "location" -> getString(R.string.broken_feature_location)
        "payments" -> getString(R.string.broken_feature_payments)
        "cast" -> getString(R.string.broken_feature_cast)
        "augmented_reality" -> getString(R.string.broken_feature_augmented_reality)
        else -> null
    }

    private fun getRatingShortLabel(rating: Int): String {
        return when (rating) {
            Rating.GOOD -> getString(R.string.good_short)
            Rating.AVERAGE -> getString(R.string.average_short)
            Rating.BAD -> getString(R.string.bad_short)
            else -> ""
        }
    }

    private fun formatVersionAndDate(versionName: String?, date: Date?): String {
        val dateStr = relativeDate(date, resources)
        return if (versionName != null) "v$versionName · $dateStr" else dateStr
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
                state.microgUser?.brokenFeatures,
                state.bareAospUser?.brokenFeatures,
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
                add(
                    formatRatingPart(
                        R.string.microg_label,
                        sharedEvaluation.ratingMicrog,
                        sharedEvaluation.brokenFeaturesMicrog
                    )
                )
            }
            if (isKnownRating(sharedEvaluation.ratingBareAOSP)) {
                add(
                    formatRatingPart(
                        R.string.bare_aosp_label,
                        sharedEvaluation.ratingBareAOSP,
                        sharedEvaluation.brokenFeaturesBareAOSP
                    )
                )
            }
        }
        val ratingSuffix = if (ratingParts.isNotEmpty()) ": ${ratingParts.joinToString(", ")}" else ""
        return "$header$ratingSuffix\n\n${getString(R.string.website_url)} #degoogle #privacy #android #sapio"
    }

    private fun formatRatingPart(labelRes: Int, rating: Int, brokenFeatures: List<String>?): String {
        val base = "${getString(labelRes)} ${ratingToSymbol(rating)}"
        val brokenSuffix = brokenFeaturesSuffix(rating, brokenFeatures)
        return if (brokenSuffix != null) "$base $brokenSuffix" else base
    }

    private fun brokenFeaturesSuffix(rating: Int, brokenFeatures: List<String>?): String? {
        if (rating != Rating.AVERAGE || brokenFeatures.isNullOrEmpty()) {
            return null
        }
        val negation = getString(R.string.feature_negation)
        val labels = brokenFeatures.mapNotNull { brokenFeatureLabelForKey(it)?.lowercase() }
        if (labels.isEmpty()) {
            return null
        }
        return labels.joinToString(", ") { "$negation $it" }.let { "($it)" }
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
