package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.data.InstalledApplicationsRepository
import com.klee.sapio.data.EvaluationRepository
import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.data.Label
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.domain.EvaluateAppUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val NOT_CHECKED = -1
        const val NOT_EXISTING = -1
        const val MICRO_G_APP_LABEL = "microG Services Core"
    }

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository
    @Inject lateinit var mEvaluationRepository: EvaluationRepository
    @Inject lateinit var mEvaluateAppUseCase: EvaluateAppUseCase
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration
    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(layoutInflater)

        val microgLabel = Label.create(requireContext(), mDeviceConfiguration.isMicroGInstalled())
        mBinding.microgConfiguration.text = microgLabel.text
        mBinding.microgConfiguration.setBackgroundColor(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), mDeviceConfiguration.isRooted())
        mBinding.rootConfiguration.text = isRootedLabel.text
        mBinding.rootConfiguration.setBackgroundColor(isRootedLabel.color)

        mPackageName = arguments?.getString("package")!!

        mBinding.validateButton.setOnClickListener {
            onValidateClicked()
        }

        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_evaluateFragment_to_chooseAppFragment)
        }

        return mBinding.root
    }

    private fun onValidateClicked() {
        runBlocking {

            if (mBinding.note.checkedRadioButtonId == NOT_CHECKED) {
                Toast.makeText(context, "Please select an evaluation", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            val app = mInstalledApplicationsRepository.getApplicationFromPackageName(
                requireContext(),
                mPackageName
            ) ?: return@runBlocking

            val rate = getRateFromId(mBinding.note.checkedRadioButtonId, requireView())
            mEvaluateAppUseCase.invoke(app, rate) {
                findNavController().navigate(R.id.action_evaluateFragment_to_successFragment)
            }
        }
    }

    private fun getRateFromId(id: Int, view: View): Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when (radioButton.text) {
            getString(R.string.works_perfectly) -> 1
            getString(R.string.works_partially) -> 2
            getString(R.string.dont_work) -> 3
            else -> 0
        }
    }
}
