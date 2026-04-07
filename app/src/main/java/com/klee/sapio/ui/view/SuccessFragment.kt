package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.klee.sapio.databinding.FragmentSuccessBinding
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SuccessFragment : Fragment() {

    private lateinit var mBinding: FragmentSuccessBinding
    private val mViewModel by activityViewModels<AppEvaluationsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val packageName = arguments?.getString("package").orEmpty()
        val appName = arguments?.getString("name").orEmpty()

        mBinding = FragmentSuccessBinding.inflate(inflater, container, false)
        mBinding.emoji.text = "\uD83C\uDF89 \uD83E\uDD73"
        mBinding.shareEvaluation.setOnClickListener {
            (requireActivity() as MainActivity).navigateToEvaluations(
                packageName,
                appName,
                shareImmediately = true
            )
        }

        mViewModel.uiState.onEach { state ->
            mBinding.shareEvaluation.isEnabled = state.microgUser != null || state.bareAospUser != null
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        return mBinding.root
    }
}
