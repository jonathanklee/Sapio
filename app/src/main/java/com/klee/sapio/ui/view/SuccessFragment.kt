package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.klee.sapio.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {

    private lateinit var mBinding: FragmentSuccessBinding

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

        return mBinding.root
    }
}
