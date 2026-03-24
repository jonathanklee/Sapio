package com.klee.sapio.ui.view

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.data.repository.EvaluationRepositoryImpl
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.databinding.FragmentWarningBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WarningFragment : Fragment() {

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepositoryImpl

    @Inject
    lateinit var mDeviceConfiguration: DeviceConfiguration

    private lateinit var mBinding: FragmentWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentWarningBinding.inflate(inflater, container, false)
        mBinding.reportAppDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.warning_desc, AboutFragment.RATING_RULES), Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(getString(R.string.warning_desc, AboutFragment.RATING_RULES))
        }

        mBinding.reportAppDescription.movementMethod = LinkMovementMethod.getInstance()

        mBinding.proceedButton.setOnClickListener {
            findNavController().navigate(R.id.action_warningFragment_to_chooseAppFragment)
        }

        mBinding.checkbox.setOnClickListener {
            updateProceedButton()
        }

        updateProceedButton()
        return mBinding.root
    }

    private fun updateProceedButton() {
        mBinding.proceedButton.isEnabled =
            mDeviceConfiguration.getGmsType() != GmsType.GOOGLE_PLAY_SERVICES && mBinding.checkbox.isChecked
    }
}
