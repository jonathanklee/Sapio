package com.android.libreapps.ui
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.libreapps.R
import com.android.libreapps.databinding.FragmentWarningBinding

class WarningFragment : Fragment() {

    private lateinit var mBinding: FragmentWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentWarningBinding.inflate(layoutInflater)
        mBinding.proceedButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_evaluate)
        }
        return mBinding.root
    }

}