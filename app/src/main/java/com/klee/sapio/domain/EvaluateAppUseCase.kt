package com.klee.sapio.domain

import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EvaluateAppUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val deviceConfiguration: DeviceConfiguration
) {

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
        val uploadedIcons = uploadIcon(app)

        if (uploadedIcons.isNullOrEmpty()) {
            onError()
            return
        }

        uploadedIcons.let {
            evaluateApp(app, uploadedIcons[0].id, rating)
            for (icon in existingIcons) {
                deleteIcon(icon.id)
            }
            onSuccess()
        }
    }

    private suspend fun uploadIcon(
        app: InstalledApplication
    ): List<Icon>? {
        return evaluationRepository.uploadIcon(app)
    }

    private suspend fun deleteIcon(
        id: Int
    ) {
        evaluationRepository.deleteIcon(id)
    }

    private suspend fun evaluateApp(app: InstalledApplication, iconId: Int, rating: Int) {
        val newEvaluation = UploadEvaluation(
            app.name,
            app.packageName,
            iconId,
            rating,
            deviceConfiguration.getGmsType(),
            deviceConfiguration.isRisky()
        )

        evaluationRepository.addEvaluation(newEvaluation)
    }

    private suspend fun getExistingIcons(app: InstalledApplication): List<Icon> {
        return withContext(Dispatchers.IO) {
            val icons = evaluationRepository.existingIcon("${app.packageName}.png")
            if (icons.isEmpty()) {
                return@withContext arrayListOf()
            } else {
                return@withContext icons
            }
        }
    }
}
