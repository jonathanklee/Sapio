package com.klee.sapio.ui.view

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.R
import com.klee.sapio.databinding.FragmentSearchBinding
import com.klee.sapio.ui.state.SearchUiState
import com.klee.sapio.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        const val KEYBOARD_DELAY_MS = 50L
    }

    private lateinit var mBinding: FragmentSearchBinding
    private lateinit var mSearchAppAdapter: SearchAppAdapter
    private val mViewModel by viewModels<SearchViewModel>()
    private var searchJob: Job? = null

    private lateinit var mHandler: Handler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSearchBinding.inflate(inflater, container, false)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.visibility = View.INVISIBLE

        mHandler = Handler(Looper.getMainLooper())

        mSearchAppAdapter = SearchAppAdapter(requireContext())
        mBinding.recyclerView.adapter = mSearchAppAdapter

        mBinding.editTextSearch.addTextChangedListener { editable ->
            onTextChanged(editable)
        }

        setupClearButton()
        setSearchIconsColor()
        collectSearch()

        return mBinding.root
    }

    private fun onTextChanged(editable: Editable?) {
        val text = editable?.trim().toString()
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.searchApplication(text, this@SearchFragment::onNetworkError)
        }
    }

    private fun collectSearch() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.uiState.collect { state ->
                renderState(state)
            }
        }
    }

    private fun renderState(state: SearchUiState) {
        mSearchAppAdapter.submitList(state.items)
        showResults(state.query.isNotEmpty() && state.items.isNotEmpty())
    }

    private fun setupClearButton() {
        mBinding.editTextSearch.addTextChangedListener { text ->
            mBinding.clearSearch.visibility = if (text?.isNotEmpty() == true) View.VISIBLE else View.GONE
        }

        mBinding.clearSearch.setOnClickListener {
            mBinding.editTextSearch.text?.clear()
        }
    }

    private fun setSearchIconsColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mBinding.searchIcon.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.material_dynamic_primary80
                ),
                PorterDuff.Mode.SRC_IN
            )
            mBinding.searchIconBig.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.material_dynamic_primary80
                ),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    override fun onResume() {
        super.onResume()
        showKeyboard()
    }

    private fun onNetworkError() {
        // Nothing for now
    }

    private fun showResults(visible: Boolean) {
        if (visible) {
            mBinding.recyclerView.visibility = View.VISIBLE
            mBinding.emptyState.visibility = View.GONE
        } else {
            mBinding.recyclerView.visibility = View.INVISIBLE
            mBinding.emptyState.visibility = View.VISIBLE
        }
    }

    private fun showKeyboard() {
        mBinding.editTextSearch.post {
            mBinding.editTextSearch.requestFocus()

            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager

            inputMethodManager.showSoftInput(
                mBinding.editTextSearch,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }
}
