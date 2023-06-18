package com.klee.sapio.data

import com.klee.sapio.domain.EvaluationRepository
import retrofit2.Response
import javax.inject.Inject

class EvaluationRepository @Inject constructor() :
    EvaluationRepository {

    @Inject
    lateinit var retrofitService: EvaluationService

    override fun isAvailable(): Boolean {
        return retrofitService.hasConnectivity()
    }

    override suspend fun listLatestEvaluations(): List<Evaluation> {
        return try {
            retrofitService.listLatestEvaluations()
        } catch (exception: Exception) {
            emptyList()
        }
    }

    override suspend fun searchEvaluations(pattern: String): List<Evaluation> {
        return try {
            retrofitService.searchEvaluation(pattern)
        } catch (exception: Exception) {
            emptyList()
        }
    }

    override suspend fun addEvaluation(evaluation: UploadEvaluation) {
        val header = UploadEvaluationHeader(evaluation)
        try {
            retrofitService.addEvaluation(header)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) {
        val header = UploadEvaluationHeader(evaluation)
        try {
            retrofitService.updateEvaluation(header, id)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun fetchMicrogUserEvaluation(appPackageName: String): Evaluation? {
        return try {
            retrofitService.fetchEvaluation(
                appPackageName,
                Label.MICROG,
                Label.USER
            )
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun fetchMicrogRootEvaluation(appPackageName: String): Evaluation? {
        return try {
            retrofitService.fetchEvaluation(
                appPackageName,
                Label.MICROG,
                Label.ROOTED
            )
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun fetchBareAospUserEvaluation(appPackageName: String): Evaluation? {
        return try {
            retrofitService.fetchEvaluation(
                appPackageName,
                Label.BARE_AOSP,
                Label.USER
            )
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun fetchBareAospRootEvaluation(appPackageName: String): Evaluation? {
        return try {
            retrofitService.fetchEvaluation(
                appPackageName,
                Label.BARE_AOSP,
                Label.ROOTED
            )
        } catch (exception: Exception) {
            null
        }
    }

    suspend fun existingEvaluations(packageName: String): List<StrapiElement> {
        return try {
            retrofitService.existingEvaluations(packageName)
        } catch (exception: Exception) {
            emptyList()
        }
    }

    suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<UploadIconAnswer>>? {
        return try {
            retrofitService.uploadIcon(app)
        } catch (exception: Exception) {
            null
        }
    }

    suspend fun existingIcon(iconName: String): List<UploadIconAnswer> {
        return try {
            val icons = retrofitService.existingIcon(iconName)
            icons?.let {
                return it
            }
            return emptyList()
        } catch (exception: Exception) {
            emptyList()
        }
    }
}
