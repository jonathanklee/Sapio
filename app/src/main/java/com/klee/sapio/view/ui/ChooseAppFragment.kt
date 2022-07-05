package com.klee.sapio.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentChooseAppBinding
import com.klee.sapio.model.InstalledApplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ChooseAppFragment : Fragment() {

    private lateinit var mBinding: FragmentChooseAppBinding
    private var mApp: InstalledApplication? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentChooseAppBinding.inflate(layoutInflater)
        mBinding.chooseAppButton.setOnClickListener {
            val chooseApp = ChooseAppDialog { app ->
                mBinding.appName.text = app.name
                mApp = app
            }
            chooseApp.show(parentFragmentManager, "")
        }

        mBinding.nextButton.setOnClickListener { onNextClicked() }
        mBinding.backButton.setOnClickListener { findNavController().navigate(R.id.action_chooseAppFragment_to_warningFragment) }
        return mBinding.root
    }

    private fun onNextClicked() {
        runBlocking {
            if (mApp == null) {
                Toast.makeText(context, "Please select an app.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }
            val bundle = bundleOf("package" to mApp?.packageName)
            findNavController().navigate(R.id.action_chooseAppFragment_to_evaluateFragment, bundle)
        }
    }
}
