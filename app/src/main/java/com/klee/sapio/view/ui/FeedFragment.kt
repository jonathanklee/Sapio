package com.klee.sapio.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.databinding.FragmentMainBinding
import com.klee.sapio.view.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mAppAdapter: AppAdapter
    private val mViewModel by viewModels<FeedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        mViewModel.evaluations.observe(viewLifecycleOwner) { list ->
            mAppAdapter = AppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()

        if (!mViewModel.isEvaluationAvailable()) {
            Toast.makeText(
                requireContext(),
                "You seem to be offline. Connectivity is required.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        mViewModel.listEvaluations()
    }
}
