package com.klee.sapio.ui.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.data.repository.EvaluationRepositoryImpl
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.ui.model.Label
import com.klee.sapio.ui.model.Rating
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val NOT_EXISTING = -1
    }

    @Inject
    lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepositoryImpl

    @Inject
    lateinit var mEvaluateAppUseCase: EvaluateAppUseCase

    @Inject
    lateinit var mDeviceConfiguration: DeviceConfiguration

    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String
    private lateinit var mAppName: String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(inflater, container, false)

        val microgLabel = Label.create(requireContext(), mDeviceConfiguration.getGmsType())
        mBinding.microgConfiguration.text = microgLabel.text
        mBinding.microgConfiguration.setBackgroundColor(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), mDeviceConfiguration.isRisky())
        mBinding.secureConfiguration.text = isRootedLabel.text
        mBinding.secureConfiguration.setBackgroundColor(isRootedLabel.color)

        mPackageName = arguments?.getString("package").orEmpty()
        mAppName = arguments?.getString("name").orEmpty()
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
        mBinding.validateButton.isEnabled = false
        mBinding.validateButton.isClickable = false
        viewLifecycleOwner.lifecycleScope.launch {
            val app = mInstalledApplicationsRepository.getApplicationFromPackageName(
                requireContext(),
                mPackageName
            ) ?: run {
                mBinding.validateButton.isEnabled = true
                mBinding.validateButton.isClickable = true
                return@launch
            }

            val rating = getRatingFromRadioId(mBinding.note.checkedRadioButtonId, requireView())
            mEvaluateAppUseCase(
                app,
                rating,
                { onUploadSuccess() },
                { onUploadError() }
            )
        }
    }

    private fun onUploadSuccess() {
        val bundle = bundleOf(
            "package" to mPackageName,
            "name" to mAppName
        )

        findNavController().navigate(R.id.action_evaluateFragment_to_successFragment, bundle)
    }

    private fun onUploadError() {
        Toast.makeText(context, getString(R.string.upload_error), Toast.LENGTH_LONG).show()
        mBinding.validateButton.isEnabled = true
        mBinding.validateButton.isClickable = true
    }

    private fun getRatingFromRadioId(id: Int, view: View): Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when (radioButton.text) {
            getString(R.string.works_perfectly) -> Rating.GOOD
            getString(R.string.works_partially) -> Rating.AVERAGE
            getString(R.string.dont_work) -> Rating.BAD
            else -> 0
        }
    }
}
