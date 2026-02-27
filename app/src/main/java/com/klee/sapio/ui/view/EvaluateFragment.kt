package com.klee.sapio.ui.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.ui.model.Label
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.state.EvaluateEvent
import com.klee.sapio.ui.viewmodel.EvaluateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        mBinding.microgConfiguration.setBackgroundColor(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), state.userType)
        mBinding.secureConfiguration.text = isRootedLabel.text
        mBinding.secureConfiguration.setBackgroundColor(isRootedLabel.color)

        mBinding.validateButton.isEnabled = false
        mBinding.note.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        mBinding.validateButton.setOnClickListener {
            val rating = getRatingFromRadioId(mBinding.note.checkedRadioButtonId, requireView())
            mViewModel.submit(packageName, appName, rating)
        }

        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_evaluateFragment_to_chooseAppFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect {
                updateButtonState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.events.collect { event ->
                when (event) {
                    is EvaluateEvent.NavigateToSuccess -> {
                        val bundle = bundleOf("package" to event.packageName, "name" to event.appName)
                        findNavController().navigate(R.id.action_evaluateFragment_to_successFragment, bundle)
                    }
                    is EvaluateEvent.ShowError -> {
                        Toast.makeText(context, getString(R.string.upload_error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        return mBinding.root
    }

    private fun updateButtonState() {
        val radioSelected = mBinding.note.checkedRadioButtonId != -1
        val isSubmitting = mViewModel.uiState.value.isSubmitting
        mBinding.validateButton.isEnabled = !isSubmitting && radioSelected
        mBinding.validateButton.isClickable = !isSubmitting
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
