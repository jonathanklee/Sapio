package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.klee.sapio.databinding.FragmentEvaluationBinding

class EvaluationFragment : Fragment() {

    private lateinit var mBinding: FragmentEvaluationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluationBinding.inflate(layoutInflater)
        return mBinding.root
    }
}
