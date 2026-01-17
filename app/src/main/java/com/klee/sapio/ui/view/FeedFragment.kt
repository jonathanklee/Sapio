package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.system.UserType
import com.klee.sapio.databinding.FragmentMainBinding
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.ui.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepository

    @Inject
    lateinit var mSettings: Settings

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mFeedAppAdapter: FeedAppAdapter
    private lateinit var mEvaluations: MutableList<Evaluation>
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
        mEvaluations = emptyList<Evaluation>().toMutableList()
        mFeedAppAdapter = FeedAppAdapter(
            requireContext(),
            mEvaluations,
            mEvaluationRepository,
            mSettings
        )
        mBinding.recyclerView.adapter = mFeedAppAdapter
    }

    private fun collectFeed() {
        fetchJob?.cancel()
        fetchJob = viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                mFeedAppAdapter.replaceEvaluations(state.items)
                mBinding.refreshView.isRefreshing = state.isLoading
            }
        }
    }
}
