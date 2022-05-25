package com.android.sapio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.sapio.databinding.FragmentEvaluateBinding

class EvaluateFragment : Fragment() {

    private lateinit var mBinding: FragmentEvaluateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(layoutInflater)
        return mBinding.root
    }
}