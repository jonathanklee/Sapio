package com.klee.sapio.ui.view
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.databinding.FragmentWarningBinding
import com.klee.sapio.data.EvaluationRepository
import com.klee.sapio.data.GmsType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WarningFragment : Fragment() {

    @Inject lateinit var mEvaluationRepository: EvaluationRepository
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration
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

        mBinding.proceedButton.isEnabled = hasDeviceLatinLanguage() &&
                mDeviceConfiguration.getGmsType() != GmsType.GOOGLE_PLAY_SERVICES

        return mBinding.root
    }

    private fun hasDeviceLatinLanguage(): Boolean {
        val languageCode = Resources.getSystem().configuration.locales.get(0).language
        if (languageCode.startsWith("en") || languageCode.startsWith("fr") ||
            languageCode.startsWith("es") || languageCode.startsWith("de") ||
            languageCode.startsWith("it")
        ) {
            return true
        }

        return false
    }
}
