package com.klee.sapio.view.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentWarningBinding

class WarningFragment : Fragment() {

    private lateinit var mBinding: FragmentWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentWarningBinding.inflate(layoutInflater)

        mBinding.proceedButton.setOnClickListener {
            findNavController().navigate(R.id.action_warningFragment_to_chooseAppFragment)
        }

        return mBinding.root
    }
}
