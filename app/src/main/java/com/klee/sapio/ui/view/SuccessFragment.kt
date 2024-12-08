package com.klee.sapio.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.klee.sapio.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {

    private lateinit var mBinding: FragmentSuccessBinding

    companion object {
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_APP_NAME = "appName"
    }

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
            val intent = Intent(requireContext(), EvaluationsActivity::class.java)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(EXTRA_APP_NAME, appName)
            requireContext().startActivity(intent)
        }

        return mBinding.root
    }
}
