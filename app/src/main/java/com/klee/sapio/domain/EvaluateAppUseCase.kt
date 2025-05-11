package com.klee.sapio.domain

import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.IconAnswer
import com.klee.sapio.data.UploadEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class EvaluateAppUseCase @Inject constructor() {

    @Inject lateinit var mEvaluationRepository: EvaluationRepository
    @Inject lateinit var mDeviceConfiguration: DeviceConfiguration

    suspend operator fun invoke(
        app: InstalledApplication,
        rating: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        evaluateApp(app, rating, onSuccess, onError)
    }

    private suspend fun evaluateApp(
        app: InstalledApplication,
        rating: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val existingIcons = getExistingIcons(app)
        val uploadAnswer = uploadIcon(app)
        val uploadAnswerBody = uploadAnswer?.body()

        if (uploadAnswerBody == null) {
            onError()
            return
        }

        uploadAnswerBody.let {
            evaluateApp(app, uploadAnswerBody[0].id, rating)
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

    private suspend fun evaluateApp(app: InstalledApplication, iconId: Int, rating: Int) {
        val newEvaluation = UploadEvaluation(
            app.name,
            app.packageName,
            iconId,
            rating,
            mDeviceConfiguration.getGmsType(),
            mDeviceConfiguration.isRisky()
        )

        mEvaluationRepository.addEvaluation(newEvaluation)
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
}
