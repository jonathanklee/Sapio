package com.klee.sapio.ui.view

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.Label
import com.klee.sapio.data.InstalledApplicationsRepository
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryStrapi
import com.klee.sapio.data.UploadIconAnswer
import com.klee.sapio.data.UploadEvaluation
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val NOT_CHECKED = -1
        const val MICRO_G_APP_LABEL = "microG Services Core"
    }

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository
    @Inject lateinit var mEvaluationRepository: EvaluationRepositoryStrapi
    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String
    private var mIsMicroGInstalled by Delegates.notNull<Int>()
    private var mIsRooted by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(layoutInflater)

        mIsMicroGInstalled = isMicroGInstalled()
        mIsRooted = isRooted()

        val microgLabel = Label.create(requireContext(), mIsMicroGInstalled)
        mBinding.microgConfiguration.text = microgLabel.text
        mBinding.microgConfiguration.setBackgroundColor(microgLabel.color)

        val isRootedLabel = Label.create(requireContext(), mIsRooted)
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
                Toast.makeText(context, "Please select an evaluation.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            val app = mInstalledApplicationsRepository.getApplicationFromPackageName(
                requireContext(),
                mPackageName
            ) ?: return@runBlocking

            val uploadAnswer = uploadIcon(app.icon)?.body()
            uploadAnswer?.let {
                evaluateApp(app, uploadAnswer[0], requireView())
                findNavController().navigate(R.id.action_evaluateFragment_to_successFragment)
            } ?: ToastMessage.showNetworkIssue(requireContext())
        }
    }

    private suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>>? {
        return mEvaluationRepository.uploadIcon(icon)
    }

    private suspend fun evaluateApp(app: InstalledApplication, icon: UploadIconAnswer, view: View) {
        val remoteApplication = UploadEvaluation(
            app.name,
            app.packageName,
            icon.id,
            getRateFromId(mBinding.note.checkedRadioButtonId, view),
            isMicroGInstalled(),
            isRooted()
        )

        if (isEvaluationExisting(remoteApplication)) {
            mEvaluationRepository.updateEvaluation(
                remoteApplication,
                getExistingEvaluationId(remoteApplication)
            )
        } else {
            mEvaluationRepository.addEvaluation(remoteApplication)
        }
    }

    private fun isMicroGInstalled(): Int {
        val packageManager = requireContext().packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            if (app.packageName == "com.google.android.gms" &&
                packageManager.getApplicationLabel(app).toString() == MICRO_G_APP_LABEL
            ) {
                return Label.MICROG
            }
        }

        return Label.BARE_AOSP
    }

    private suspend fun isEvaluationExisting(data: UploadEvaluation): Boolean {
        return withContext(Dispatchers.IO) {
            val apps = mEvaluationRepository.getEvaluations()
            for (existingApp in apps) {
                if (hasSameEvaluation(data, existingApp)) {
                    return@withContext true
                }
            }
            return@withContext false
        }
    }

    private suspend fun getExistingEvaluationId(data: UploadEvaluation): Int {
        return withContext(Dispatchers.IO) {
            val apps = mEvaluationRepository.getEvaluationsRawData()
            for (existingApp in apps) {
                if (hasSameEvaluation(data, existingApp.attributes)) {
                    return@withContext existingApp.id
                }
            }
            return@withContext -1
        }
    }

    private fun hasSameEvaluation(one: UploadEvaluation, two: Evaluation): Boolean {
        return one.packageName == two.packageName && one.name == two.name &&
            one.microg == two.microg && one.rooted == two.rooted
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

    private fun isRooted(): Int {
        return if (RootBeer(context).isRooted) {
            Label.ROOTED
        } else {
            Label.USER
        }
    }
}
