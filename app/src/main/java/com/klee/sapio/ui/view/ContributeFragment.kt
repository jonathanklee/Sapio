package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.klee.sapio.databinding.FragmentContributeBinding

class ContributeFragment : Fragment() {

    private lateinit var mBinding: FragmentContributeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentContributeBinding.inflate(layoutInflater)
        return mBinding.root
    }
}
