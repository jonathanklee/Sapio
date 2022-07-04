package com.android.sapio.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sapio.databinding.FragmentMainBinding
import com.android.sapio.view.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mAppAdapter: AppAdapter
    private val mViewModel = FeedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        mViewModel.applications.observe(viewLifecycleOwner) { list ->
            mAppAdapter = AppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        mViewModel.listApplications()

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.listApplications()
    }
}
