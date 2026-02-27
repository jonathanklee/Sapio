package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.system.UserType
import com.klee.sapio.databinding.FragmentMainBinding
import com.klee.sapio.ui.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        private const val LOAD_MORE_THRESHOLD = 3
    }

    @Inject
    lateinit var mSettings: Settings

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mFeedAppAdapter: FeedAppAdapter
    private val mViewModel by viewModels<FeedViewModel>()
    private var fetchJob: Job? = null
    private var mPreviousRootVisible: Int = UserType.SECURE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mPreviousRootVisible = mSettings.getRootConfigurationLevel()
        setupAdapter()
        collectFeed()

        mBinding.refreshView.setOnRefreshListener {
            mViewModel.refresh()
        }

        setupScrollListener()

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()

        if (mPreviousRootVisible != mSettings.getRootConfigurationLevel()) {
            mPreviousRootVisible = mSettings.getRootConfigurationLevel()
            mViewModel.refresh()
        }
    }

    private fun setupAdapter() {
        mFeedAppAdapter = FeedAppAdapter(requireContext(), mSettings)
        mBinding.recyclerView.adapter = mFeedAppAdapter
    }

    private fun setupScrollListener() {
        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (total > 0 && lastVisible >= total - LOAD_MORE_THRESHOLD) {
                    mViewModel.loadNextPage()
                }
            }
        })
    }

    private fun collectFeed() {
        fetchJob?.cancel()
        fetchJob = viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                mFeedAppAdapter.submitList(state.items) {
                    if (!state.isLoading && !state.isLoadingMore) {
                        loadMoreIfNeeded()
                    }
                }
                mBinding.refreshView.isRefreshing = state.isLoading
            }
        }
    }

    private fun loadMoreIfNeeded() {
        mBinding.recyclerView.post {
            val layoutManager = mBinding.recyclerView.layoutManager as? LinearLayoutManager ?: return@post
            val lastVisible = layoutManager.findLastVisibleItemPosition()
            val total = layoutManager.itemCount
            if (total > 0 && lastVisible >= total - LOAD_MORE_THRESHOLD) {
                mViewModel.loadNextPage()
            }
        }
    }
}
