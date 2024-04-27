package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.databinding.FragmentMainBinding
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.ui.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mFeedAppAdapter: FeedAppAdapter
    private val mViewModel by viewModels<FeedViewModel>()

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        val coroutineScope = viewLifecycleOwner.lifecycleScope
        fetchFeed(coroutineScope)

        mBinding.refreshView.setOnRefreshListener {
            fetchFeed(coroutineScope)
        }

        return mBinding.root
    }

    private fun fetchFeed(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            collectFeed()
        }
        refreshFeed()
    }

    private suspend fun collectFeed() {
        mViewModel.evaluations.collect { list ->
            mFeedAppAdapter = FeedAppAdapter(
                requireContext(),
                list,
                mEvaluationRepository,
                lifecycleScope
            )
            mBinding.recyclerView.adapter = mFeedAppAdapter
        }
    }

    private fun refreshFeed() {
        mViewModel.listEvaluation(this::onSuccess, this::onNetworkError)
        mBinding.refreshView.isRefreshing = true
    }

    private fun onNetworkError() {
        ToastMessage.showNetworkIssue(requireContext())
        mBinding.refreshView.isRefreshing = false
    }

    private fun onSuccess() {
        mBinding.refreshView.isRefreshing = false
    }
}
