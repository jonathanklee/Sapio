package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {

    private lateinit var mBinding: FragmentSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSuccessBinding.inflate(inflater, container, false)
        mBinding.emoji.text = "\uD83C\uDF89 \uD83E\uDD73"
        mBinding.evaludateAnotherAppButton.setOnClickListener {
            findNavController().navigate(R.id.action_successFragment_to_warningFragment)
        }

        return mBinding.root
    }
}
