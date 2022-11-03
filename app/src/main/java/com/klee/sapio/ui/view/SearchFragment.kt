package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.databinding.FragmentSearchBinding
import com.klee.sapio.domain.IsEvaluationsAvailableUseCase
import com.klee.sapio.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var mBinding: FragmentSearchBinding
    private lateinit var mAppAdapter: AppAdapter
    private val mViewModel by viewModels<SearchViewModel>()

    @Inject
    lateinit var mIsEvaluationsAvailableUseCase: IsEvaluationsAvailableUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSearchBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.visibility = View.INVISIBLE

        mViewModel.foundEvaluations.observe(viewLifecycleOwner) { list ->
            mAppAdapter = AppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        mBinding.editTextSearch.addTextChangedListener { editable ->

            val text = editable?.trim().toString()
            if (text.isNotEmpty()) {
                mViewModel.searchApplication(text)
            } else {
                mViewModel.searchApplication("pprrss")
            }

            showResults(text.isNotEmpty())
        }

        return mBinding.root
    }

    private fun showResults(visible: Boolean) {
        if (visible) {
            mBinding.recyclerView.visibility = View.VISIBLE
            mBinding.searchIconBig.visibility = View.INVISIBLE
        } else {
            mViewModel.searchApplication("pprrss")
            mBinding.recyclerView.visibility = View.INVISIBLE
            mBinding.searchIconBig.visibility = View.VISIBLE
        }
    }
}
