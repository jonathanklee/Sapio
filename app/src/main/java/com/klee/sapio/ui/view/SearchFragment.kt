package com.klee.sapio.ui.view

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var mBinding: FragmentSearchBinding
    private lateinit var mSearchAppAdapter: SearchAppAdapter
    private val mViewModel by viewModels<SearchViewModel>()

    private lateinit var mHandler: Handler

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentSearchBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.visibility = View.INVISIBLE

        mHandler = Handler(Looper.getMainLooper())

        mViewModel.foundEvaluations.observe(viewLifecycleOwner) { list ->
            mSearchAppAdapter = SearchAppAdapter(
                requireContext(),
                list,
                mEvaluationRepository,
                lifecycleScope
            )
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

        setSearchIconsColor()

        return mBinding.root
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
            mBinding.searchIconBig.visibility = View.INVISIBLE
            mBinding.searchText.visibility = View.INVISIBLE
        } else {
            mViewModel.searchApplication("pprrss", this::onNetworkError)
            mBinding.recyclerView.visibility = View.INVISIBLE
            mBinding.searchIconBig.visibility = View.VISIBLE
            mBinding.searchText.visibility = View.VISIBLE
        }
    }

    private fun showKeyboard() {
        mHandler.postDelayed({
            mBinding.editTextSearch.requestFocus()

            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager

            inputMethodManager.showSoftInput(
                mBinding.editTextSearch,
                InputMethodManager.SHOW_IMPLICIT
            )
        }, 50)
    }
}
