package com.klee.sapio.data

import com.klee.sapio.domain.EvaluationRepository
import retrofit2.Response
import javax.inject.Inject

class EvaluationRepositoryStrapi @Inject constructor() : EvaluationRepository {

    @Inject
    lateinit var retrofitService: EvaluationService

    override fun isAvailable(): Boolean {
        return retrofitService.hasConnectivity()
    }

    override suspend fun listLatestEvaluations(): List<Evaluation> {
        return retrofitService.listLatestEvaluations()
    }

    override suspend fun searchEvaluations(pattern: String): List<Evaluation> {
        return retrofitService.searchEvaluation(pattern)
    }

    override suspend fun addEvaluation(evaluation: UploadEvaluation) {
        val header = UploadEvaluationHeader(evaluation)
        retrofitService.addEvaluation(header)
    }

    override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) {
        val header = UploadEvaluationHeader(evaluation)
        retrofitService.updateEvaluation(header, id)
    }

    override suspend fun fetchMicrogUserEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.USER
        )
    }

    override suspend fun fetchMicrogRootEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.ROOTED
        )
    }

    override suspend fun fetchBareAospUserEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.USER
        )
    }

    override suspend fun fetchBareAospRootEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.ROOTED
        )
    }

    suspend fun existingEvaluations(packageName: String): List<StrapiElement> {
        return retrofitService.existingEvaluations(packageName)
    }

    suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<UploadIconAnswer>>? {
        return retrofitService.uploadIcon(app)
    }

    suspend fun existingIcon(iconName: String): List<UploadIconAnswer> {
        return retrofitService.existingIcon(iconName)
    }
}
