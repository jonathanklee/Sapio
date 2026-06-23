package com.klee.sapio.ui.view

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.domain.model.BrokenFeature
import com.klee.sapio.ui.model.Label
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.viewmodel.EvaluateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val NOT_EXISTING = -1
    }

    private val mViewModel by viewModels<EvaluateViewModel>()
    private lateinit var mBinding: FragmentEvaluateBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(inflater, container, false)

        val packageName = arguments?.getString("package").orEmpty()
        val appName = arguments?.getString("name").orEmpty()

        val state = mViewModel.uiState.value
        val microgLabel = Label.create(requireContext(), state.gmsType)
        mBinding.microgConfiguration.text = microgLabel.text
        mBinding.microgConfiguration.backgroundTintList = ColorStateList.valueOf(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), state.userType)
        mBinding.secureConfiguration.text = isRootedLabel.text
        mBinding.secureConfiguration.backgroundTintList = ColorStateList.valueOf(isRootedLabel.color)

        mBinding.validateButton.isEnabled = false
        mBinding.note.setOnCheckedChangeListener { _, checkedId ->
            updateButtonState()
            updateBrokenFeaturesVisibility(checkedId)
        }

        setupBrokenFeaturesChips()

        mBinding.validateButton.setOnClickListener {
            val rating = getRatingFromRadioId(mBinding.note.checkedRadioButtonId, requireView())
            val brokenFeatures = if (rating == Rating.AVERAGE) getSelectedBrokenFeatures() else null
            val bundle = bundleOf(
                "package" to packageName,
                "name" to appName,
                "rating" to rating,
                "brokenFeatures" to brokenFeatures?.let { ArrayList(it) }
            )
            findNavController().navigate(R.id.action_evaluateFragment_to_loadingFragment, bundle)
        }

        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_evaluateFragment_to_chooseAppFragment)
        }

        return mBinding.root
    }

    private fun updateButtonState() {
        val radioSelected = mBinding.note.checkedRadioButtonId != -1
        mBinding.validateButton.isEnabled = radioSelected
    }

    private fun updateBrokenFeaturesVisibility(checkedId: Int) {
        val isPartial = checkedId == R.id.orangeRadioButton
        mBinding.brokenFeaturesSection.visibility = if (isPartial) View.VISIBLE else View.GONE
        if (!isPartial) {
            mBinding.brokenFeaturesChips.clearCheck()
        }
    }

    private fun setupBrokenFeaturesChips() {
        BrokenFeature.entries.forEach { feature ->
            val chip = Chip(requireContext()).apply {
                text = getString(feature.labelResId())
                isCheckable = true
                tag = feature.key
            }
            mBinding.brokenFeaturesChips.addView(chip)
        }
    }

    private fun getSelectedBrokenFeatures(): List<String> {
        return (0 until mBinding.brokenFeaturesChips.childCount)
            .map { mBinding.brokenFeaturesChips.getChildAt(it) as Chip }
            .filter { it.isChecked }
            .map { it.tag as String }
    }

    private fun getRatingFromRadioId(id: Int, view: View): Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when (radioButton.text) {
            getString(R.string.works_perfectly) -> Rating.GOOD
            getString(R.string.works_partially) -> Rating.AVERAGE
            getString(R.string.dont_work) -> Rating.BAD
            else -> 0
        }
    }

    @StringRes
    private fun BrokenFeature.labelResId(): Int = when (this) {
        BrokenFeature.NOTIFICATIONS -> R.string.broken_feature_notifications
        BrokenFeature.IN_APP_PURCHASE -> R.string.broken_feature_in_app_purchase
        BrokenFeature.LOGIN -> R.string.broken_feature_login
        BrokenFeature.MAPS -> R.string.broken_feature_maps
        BrokenFeature.LOCATION -> R.string.broken_feature_location
        BrokenFeature.PAYMENTS -> R.string.broken_feature_payments
        BrokenFeature.CAST -> R.string.broken_feature_cast
        BrokenFeature.AUGMENTED_REALITY -> R.string.broken_feature_augmented_reality
    }

}
