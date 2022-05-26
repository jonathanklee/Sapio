package com.android.sapio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.sapio.R
import com.android.sapio.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {

    private lateinit var mBinding: FragmentSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSuccessBinding.inflate(layoutInflater)
        mBinding.emoji.text = "\uD83C\uDF89 \uD83E\uDD73"
        mBinding.evaludateAgainButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_restart)
        }

        return mBinding.root
    }
}