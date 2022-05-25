package com.android.sapio.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sapio.R
import com.android.sapio.databinding.FragmentMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet

class MainFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mAppAdapter: AppAdapter
    private val mViewModel = AppViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        initFabButton()

        mViewModel.data.observe(viewLifecycleOwner) { list ->
            mAppAdapter = AppAdapter(requireContext(), list)
            mBinding.recyclerView.adapter = mAppAdapter
        }

        mViewModel.loadApps()

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        mViewModel.loadApps()
    }

    private fun initFabButton() {
        val fab = mBinding.floatingActionButton
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_to_warning)
        }
    }
}
