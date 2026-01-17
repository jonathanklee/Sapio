package com.klee.sapio.data

import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Evaluation as DomainEvaluation
import com.klee.sapio.domain.model.EvaluationRecord as DomainEvaluationRecord
import com.klee.sapio.domain.model.Icon as DomainIcon
import com.klee.sapio.domain.model.InstalledApplication as DomainInstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation as DomainUploadEvaluation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor() :
    EvaluationRepository {

    @Inject
    lateinit var retrofitService: EvaluationService

    override suspend fun listLatestEvaluations(pageNumber: Int): List<DomainEvaluation> {
        return retrofitService.listLatestEvaluations(pageNumber).map { it.toDomain() }
    }

    override suspend fun searchEvaluations(pattern: String): List<DomainEvaluation> {
        return retrofitService.searchEvaluation(pattern).map { it.toDomain() }
    }

    override suspend fun addEvaluation(evaluation: DomainUploadEvaluation) {
        val header = UploadEvaluationHeader(evaluation.toData())
        retrofitService.addEvaluation(header)
    }

    override suspend fun updateEvaluation(evaluation: DomainUploadEvaluation, id: Int) {
        val header = UploadEvaluationHeader(evaluation.toData())
        retrofitService.updateEvaluation(header, id)
    }

    override suspend fun fetchMicrogSecureEvaluation(appPackageName: String): DomainEvaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.SECURE
        )?.toDomain()
    }

    override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): DomainEvaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.MICROG,
            Label.RISKY
        )?.toDomain()
    }

    override suspend fun fetchBareAospSecureEvaluation(appPackageName: String): DomainEvaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.SECURE
        )?.toDomain()
    }

    override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): DomainEvaluation? {
        return retrofitService.fetchEvaluation(
            appPackageName,
            Label.BARE_AOSP,
            Label.RISKY
        )?.toDomain()
    }

    override suspend fun existingEvaluations(packageName: String): List<DomainEvaluationRecord> {
        return retrofitService.existingEvaluations(packageName).map { it.toDomain() }
    }

    override suspend fun uploadIcon(app: DomainInstalledApplication): List<DomainIcon>? {
        val response = retrofitService.uploadIcon(app)
        return response?.body()?.map { it.toDomain() }
    }

    override suspend fun existingIcon(iconName: String): List<DomainIcon> {
        val icons = retrofitService.existingIcon(iconName) ?: return emptyList()
        return icons.map { it.toDomain() }
    }

    override suspend fun deleteIcon(id: Int) {
        retrofitService.deleteIcon(id)
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
