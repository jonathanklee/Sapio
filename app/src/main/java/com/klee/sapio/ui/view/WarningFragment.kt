package com.klee.sapio.ui.view
import android.content.res.Resources
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.databinding.FragmentWarningBinding
import com.klee.sapio.data.EvaluationRepositoryImpl
import com.klee.sapio.data.GmsType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WarningFragment : Fragment() {

    @Inject lateinit var mEvaluationRepository: EvaluationRepositoryImpl
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration
    private lateinit var mBinding: FragmentWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentWarningBinding.inflate(layoutInflater)
        mBinding.reportAppDescription.text = Html.fromHtml(getString(R.string.warning_desc, AboutActivity.RATING_RULES), Html.FROM_HTML_MODE_LEGACY)
        mBinding.reportAppDescription.movementMethod = LinkMovementMethod.getInstance()

        mBinding.proceedButton.setOnClickListener {
            findNavController().navigate(R.id.action_warningFragment_to_chooseAppFragment)
        }

        mBinding.proceedButton.isEnabled = mDeviceConfiguration.getGmsType() != GmsType.GOOGLE_PLAY_SERVICES

        return mBinding.root
    }
}
