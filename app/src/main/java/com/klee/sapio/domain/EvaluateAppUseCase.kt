package com.klee.sapio.domain

import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.IconAnswer
import com.klee.sapio.data.UploadEvaluation
import com.klee.sapio.data.Evaluation
import com.klee.sapio.ui.view.EvaluateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class EvaluateAppUseCase @Inject constructor() {

    @Inject lateinit var mEvaluationRepository: EvaluationRepository
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
        val uploadAnswer = uploadIcon(app)?.body()

        uploadAnswer?.let {
            evaluateApp(app, uploadAnswer[0].id, rate)
            for (icon in existingIcons) {
                deleteIcon(icon.id)
            }
            onSuccess()
        }
    }

    private suspend fun uploadIcon(
        app: InstalledApplication
    ): Response<ArrayList<IconAnswer>>? {
        return mEvaluationRepository.uploadIcon(app)
    }

    private suspend fun deleteIcon(
        id: Int
    ) {
        mEvaluationRepository.deleteIcon(id)
    }

    private suspend fun evaluateApp(app: InstalledApplication, iconId: Int, rate: Int) {
        val newEvaluation = UploadEvaluation(
            app.name,
            app.packageName,
            iconId,
            rate,
            mDeviceConfiguration.getGmsType(),
            mDeviceConfiguration.isRooted()
        )

        val existingEvaluationId = getExistingEvaluationId(newEvaluation)
        if (existingEvaluationId == EvaluateFragment.NOT_EXISTING) {
            mEvaluationRepository.addEvaluation(newEvaluation)
        } else {
            mEvaluationRepository.updateEvaluation(newEvaluation, existingEvaluationId)
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

    private suspend fun getExistingIcons(app: InstalledApplication): List<IconAnswer> {
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
        return one.packageName == two.packageName &&
            one.microg == two.microg && one.rooted == two.rooted
    }
}
