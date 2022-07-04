package com.android.sapio.view.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sapio.databinding.FragmentSearchBinding
import com.android.sapio.view.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private lateinit var mBinding: FragmentSearchBinding
    private lateinit var mAppAdapter: AppAdapter
    private val mViewModel = SearchViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSearchBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        mViewModel.foundApplications.observe(viewLifecycleOwner) { list ->
            mAppAdapter = AppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        mBinding.editTextSearch.addTextChangedListener { editable ->
            val text =  editable?.trim().toString()
            if (text.isNotEmpty()) {
                mViewModel.searchApplication(text)
            } else {
                mViewModel.searchApplication("pprrss")
            }
        }

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        mBinding.editTextSearch.requestFocus()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(mBinding.editTextSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onPause() {
        super.onPause()
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mBinding.editTextSearch.windowToken, 0)
    }
}