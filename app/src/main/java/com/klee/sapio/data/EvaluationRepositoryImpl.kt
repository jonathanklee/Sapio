package com.klee.sapio.data

import com.klee.sapio.domain.EvaluationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor() :
    EvaluationRepository {

    @Inject
    lateinit var retrofitService: EvaluationService

    override suspend fun listLatestEvaluations(pageNumber: Int): List<Evaluation> {
        return retrofitService.listLatestEvaluations(pageNumber)
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

    override suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.SECURE
        )
    }

    override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.RISKY
        )
    }

    override suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.SECURE
        )
    }

    override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Evaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.RISKY
        )
    }

    override suspend fun existingEvaluations(packageName: String): List<StrapiElement> {
        return retrofitService.existingEvaluations(packageName)
    }

    override suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<IconAnswer>>? {
        return retrofitService.uploadIcon(app)
    }

    override suspend fun existingIcon(iconName: String): List<IconAnswer> {
        val icons = retrofitService.existingIcon(iconName)
        icons?.let {
            return it
        }

        return emptyList()
    }

    override suspend fun deleteIcon(id: Int): Response<IconAnswer>? {
        return retrofitService.deleteIcon(id)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class EvaluationRepositoryModule {

    @Binds
    abstract fun bindEvaluationRepository(
        evaluationRepositoryImpl: EvaluationRepositoryImpl
    ): EvaluationRepository
}
