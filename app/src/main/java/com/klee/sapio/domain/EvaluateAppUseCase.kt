package com.klee.sapio.domain

import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class EvaluateAppUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val deviceConfiguration: DeviceConfiguration
) {

    open suspend operator fun invoke(
        app: InstalledApplication,
        rating: Int
    ): Result<Unit> {
        val existingIcons = getExistingIcons(app)
        val uploadedIcons = uploadIcon(app)

        return when {
            uploadedIcons.isEmpty() ->
                Result.failure(IllegalStateException("Failed to upload icon"))
            !submitEvaluation(app, uploadedIcons[0].id, rating) ->
                Result.failure(IllegalStateException("Failed to add evaluation"))
            else -> {
                existingIcons.forEach { deleteIcon(it.id) }
                Result.success(Unit)
            }
        }
    }

    private suspend fun uploadIcon(app: InstalledApplication): List<Icon> {
        return evaluationRepository.uploadIcon(app).getOrDefault(emptyList())
    }

    private suspend fun deleteIcon(id: Int) {
        evaluationRepository.deleteIcon(id)
    }

    private suspend fun submitEvaluation(app: InstalledApplication, iconId: Int, rating: Int): Boolean {
        val newEvaluation = UploadEvaluation(
            app.name,
            app.packageName,
            iconId,
            rating,
            deviceConfiguration.getGmsType(),
            deviceConfiguration.isRisky()
        )

        return evaluationRepository.addEvaluation(newEvaluation).isSuccess
    }

    private suspend fun getExistingIcons(app: InstalledApplication): List<Icon> {
        return withContext(Dispatchers.IO) {
            evaluationRepository.existingIcon("${app.packageName}.png").getOrDefault(emptyList())
        }
    }
}
