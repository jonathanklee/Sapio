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
import com.klee.sapio.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var mBinding: FragmentSearchBinding
    private lateinit var mSearchAppAdapter: SearchAppAdapter
    private val mViewModel by viewModels<SearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSearchBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.visibility = View.INVISIBLE

        mViewModel.foundEvaluations.observe(viewLifecycleOwner) { list ->
            mSearchAppAdapter = SearchAppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mSearchAppAdapter
        }

        mBinding.editTextSearch.addTextChangedListener { editable ->

            val text = editable?.trim().toString()
            if (text.isNotEmpty()) {
                mViewModel.searchApplication(text, this::onNetworkError)
            } else {
                mViewModel.searchApplication("", this::onNetworkError)
            }

            showResults(text.isNotEmpty())
        }

        return mBinding.root
    }

    private fun onNetworkError() {
        // Nothing for now
    }

    private fun showResults(visible: Boolean) {
        if (visible) {
            mBinding.recyclerView.visibility = View.VISIBLE
            mBinding.searchIconBig.visibility = View.INVISIBLE
            mBinding.searchText.visibility = View.INVISIBLE
        } else {
            mViewModel.searchApplication("pprrss", this::onNetworkError)
            mBinding.recyclerView.visibility = View.INVISIBLE
            mBinding.searchIconBig.visibility = View.VISIBLE
            mBinding.searchText.visibility = View.VISIBLE
        }
    }
}
