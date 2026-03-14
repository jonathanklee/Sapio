package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentLoadingBinding
import com.klee.sapio.ui.state.EvaluateEvent
import com.klee.sapio.ui.viewmodel.LoadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoadingFragment : Fragment() {

    private val mViewModel by viewModels<LoadingViewModel>()
    private lateinit var mBinding: FragmentLoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentLoadingBinding.inflate(inflater, container, false)

        val packageName = arguments?.getString("package").orEmpty()
        val appName = arguments?.getString("name").orEmpty()
        val rating = arguments?.getInt("rating") ?: 0

        mViewModel.submit(packageName, appName, rating)

        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.events.collect { event ->
                when (event) {
                    is EvaluateEvent.NavigateToSuccess -> {
                        val bundle = Bundle().apply {
                            putString("package", event.packageName)
                            putString("name", event.appName)
                        }
                        findNavController().navigate(R.id.action_loadingFragment_to_successFragment, bundle)
                    }
                    is EvaluateEvent.ShowError -> {
                        Toast.makeText(context, getString(R.string.upload_error), Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return mBinding.root
    }
}
