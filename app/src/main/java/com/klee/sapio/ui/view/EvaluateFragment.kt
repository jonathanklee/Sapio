package com.klee.sapio.ui.view

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluateBinding
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
        mBinding.note.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        mBinding.validateButton.setOnClickListener {
            val rating = getRatingFromRadioId(mBinding.note.checkedRadioButtonId, requireView())
            val bundle = bundleOf("package" to packageName, "name" to appName, "rating" to rating)
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

    private fun getRatingFromRadioId(id: Int, view: View): Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when (radioButton.text) {
            getString(R.string.works_perfectly) -> Rating.GOOD
            getString(R.string.works_partially) -> Rating.AVERAGE
            getString(R.string.dont_work) -> Rating.BAD
            else -> 0
        }
    }
}
