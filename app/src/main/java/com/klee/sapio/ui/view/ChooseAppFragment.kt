package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentChooseAppBinding
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.viewmodel.ChooseAppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseAppFragment : Fragment() {

    private lateinit var mBinding: FragmentChooseAppBinding
    private var mApp: InstalledApplication? = null
    val viewModel: ChooseAppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentChooseAppBinding.inflate(inflater, container, false)
        mBinding.chooseAppButton.setOnClickListener { onChooseButtonClicked() }

        mBinding.nextButton.isEnabled = false
        mBinding.nextButton.setOnClickListener { onNextButtonClicked() }
        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooseAppFragment_to_warningFragment)
        }

        return mBinding.root
    }

    private fun onChooseButtonClicked() {
        mBinding.chooseAppButton.isEnabled = false
        mBinding.nextButton.isEnabled = false
        val dialog = ChooseAppDialog(
            uiState = viewModel.uiState,
            onAppSelected = { chosenApp ->
                mApp = chosenApp
                showSelectedApp(chosenApp)
                mBinding.nextButton.isEnabled = true
                mBinding.chooseAppButton.isEnabled = true
            },
            onDismissed = {
                mBinding.chooseAppButton.isEnabled = true
            }
        )
        dialog.show(parentFragmentManager, "")
    }

    private fun showSelectedApp(app: InstalledApplication) {
        mBinding.defaultContent.isVisible = false
        mBinding.selectedContent.isVisible = true
        mBinding.selectedAppName.text = app.name
        mBinding.appName.text = app.packageName
        try {
            val icon = requireContext().packageManager.getApplicationIcon(app.packageName)
            mBinding.selectedAppIcon.setImageDrawable(icon)
        } catch (e: Exception) {
            mBinding.selectedAppIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    private fun onNextButtonClicked() {
        val bundle = bundleOf(
            "package" to mApp?.packageName,
            "name" to mApp?.name
        )

        findNavController().navigate(R.id.action_chooseAppFragment_to_evaluateFragment, bundle)
    }
}
