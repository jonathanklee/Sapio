package com.klee.sapio.domain

import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.data.EvaluationRepositoryStrapi
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.UploadIconAnswer
import com.klee.sapio.data.UploadEvaluation
import com.klee.sapio.data.Evaluation
import com.klee.sapio.ui.view.EvaluateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class EvaluateAppUseCase @Inject constructor() {

    @Inject lateinit var mEvaluationRepository: EvaluationRepositoryStrapi
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration

    suspend operator fun invoke(
        app: InstalledApplication,
        rate: Int,
        onSuccess: () -> Unit
    ) {
        evaluateApp(app, rate, onSuccess)
    }

    private suspend fun evaluateApp(app: InstalledApplication, rate: Int, onSuccess: () -> Unit) {
        val existingIcons = getExistingIcons(app)
        if (existingIcons.isEmpty()) {
            val uploadAnswer = uploadIcon(app)?.body()
            uploadAnswer?.let {
                evaluateApp(app, uploadAnswer[0].id, rate)
                onSuccess()
            }
        } else {
            evaluateApp(app, existingIcons[0].id, rate)
            onSuccess()
        }
    }

    private suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<UploadIconAnswer>>? {
        return mEvaluationRepository.uploadIcon(app)
    }

    private suspend fun evaluateApp(app: InstalledApplication, iconId: Int , rate: Int) {
        val remoteApplication = UploadEvaluation(
            app.name,
            app.packageName,
            iconId,
            rate,
            mDeviceConfiguration.isMicroGInstalled(),
            mDeviceConfiguration.isRooted()
        )

        val existingEvaluationId = getExistingEvaluationId(remoteApplication)
        if (existingEvaluationId == EvaluateFragment.NOT_EXISTING) {
            mEvaluationRepository.addEvaluation(remoteApplication)
        } else {
            mEvaluationRepository.updateEvaluation(remoteApplication, existingEvaluationId)
        }
    }

    private suspend fun getExistingEvaluationId(data: UploadEvaluation): Int {
        return withContext(Dispatchers.IO) {
            val apps = mEvaluationRepository.existingEvaluations(data.packageName)
            for (existingApp in apps) {
                if (hasSameEvaluation(data, existingApp.attributes)) {
                    return@withContext existingApp.id
                }
            }
            return@withContext -1
        }
    }

    private suspend fun getExistingIcons(app: InstalledApplication): List<UploadIconAnswer> {
        return withContext(Dispatchers.IO) {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
            if (icons.isEmpty()) {
                return@withContext arrayListOf()
            } else {
                return@withContext icons
            }
        }
    }

    private fun hasSameEvaluation(one: UploadEvaluation, two: Evaluation): Boolean {
        return one.packageName == two.packageName && one.name == two.name &&
                one.microg == two.microg && one.rooted == two.rooted
    }
}