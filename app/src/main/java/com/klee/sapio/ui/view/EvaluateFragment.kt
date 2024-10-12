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
import com.klee.sapio.data.EvaluationRepositoryImpl
import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.data.Label
import com.klee.sapio.data.Rating
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.domain.EvaluateAppUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val NOT_EXISTING = -1
    }

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository
    @Inject lateinit var mEvaluationRepository: EvaluationRepositoryImpl
    @Inject lateinit var mEvaluateAppUseCase: EvaluateAppUseCase
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration
    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(inflater, container, false)

        val microgLabel = Label.create(requireContext(), mDeviceConfiguration.getGmsType())
        mBinding.microgConfiguration.text = microgLabel.text
        mBinding.microgConfiguration.setBackgroundColor(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), mDeviceConfiguration.isRooted())
        mBinding.rootConfiguration.text = isRootedLabel.text
        mBinding.rootConfiguration.setBackgroundColor(isRootedLabel.color)

        mPackageName = arguments?.getString("package")!!
        mBinding.validateButton.isEnabled = false
        mBinding.note.setOnCheckedChangeListener { _, value ->
            mBinding.validateButton.isEnabled = value != -1
        }

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
            val app = mInstalledApplicationsRepository.getApplicationFromPackageName(
                requireContext(),
                mPackageName
            ) ?: return@runBlocking

            val rate = getRateFromId(mBinding.note.checkedRadioButtonId, requireView())
            mEvaluateAppUseCase.invoke(app, rate,
                { onUploadSuccess() },
                { onUploadError() }
            )
        }
    }

    private fun onUploadSuccess() {
        findNavController().navigate(R.id.action_evaluateFragment_to_successFragment)
    }

    private fun onUploadError() {
       Toast.makeText(context, getString(R.string.upload_error), Toast.LENGTH_LONG).show()
    }

    private fun getRateFromId(id: Int, view: View): Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when (radioButton.text) {
            getString(R.string.works_perfectly) -> Rating.GOOD
            getString(R.string.works_partially) -> Rating.AVERAGE
            getString(R.string.dont_work) -> Rating.BAD
            else -> 0
        }
    }
}
