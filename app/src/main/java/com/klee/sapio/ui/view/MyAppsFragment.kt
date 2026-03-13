package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.databinding.FragmentMyAppsBinding
import com.klee.sapio.ui.viewmodel.MyAppsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyAppsFragment : Fragment() {

    private lateinit var mBinding: FragmentMyAppsBinding
    private lateinit var mAdapter: MyAppsAdapter
    private val mViewModel by activityViewModels<MyAppsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMyAppsBinding.inflate(inflater, container, false)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        mAdapter = MyAppsAdapter(
            requireContext(),
            onAppSelected = { packageName, appName ->
                (requireActivity() as MainActivity).navigateToEvaluations(packageName, appName)
            },
            onContribute = {
                (requireActivity() as MainActivity).navigateToContribute()
            }
        )
        mBinding.recyclerView.adapter = mAdapter

        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.loadApps(forceRefresh = true)
        }

        collectState()
        mViewModel.loadApps()

        return mBinding.root
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                if (state.isLoading) {
                    mAdapter.submitList(emptyList())
                    mBinding.progressBar.visibility = View.VISIBLE
                } else {
                    mAdapter.submitList(state.items)
                    mBinding.progressBar.visibility = View.GONE
                }
                mBinding.recyclerView.visibility =
                    if (state.isLoading) View.GONE else View.VISIBLE
                mBinding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
