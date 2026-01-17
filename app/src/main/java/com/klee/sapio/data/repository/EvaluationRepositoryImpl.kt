package com.klee.sapio.data.repository

import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.dto.UploadEvaluationHeader
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.UserType
import com.klee.sapio.data.mapper.toData
import com.klee.sapio.data.mapper.toDomain
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

    override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<DomainEvaluation>> {
        return retrofitService.listLatestEvaluations(pageNumber)
            .map { evaluations -> evaluations.map { it.toDomain() } }
    }

    override suspend fun searchEvaluations(pattern: String): Result<List<DomainEvaluation>> {
        return retrofitService.searchEvaluation(pattern)
            .map { evaluations -> evaluations.map { it.toDomain() } }
    }

    override suspend fun addEvaluation(evaluation: DomainUploadEvaluation): Result<Unit> {
        val header = UploadEvaluationHeader(evaluation.toData())
        return retrofitService.addEvaluation(header)
    }

    override suspend fun updateEvaluation(evaluation: DomainUploadEvaluation, id: Int): Result<Unit> {
        val header = UploadEvaluationHeader(evaluation.toData())
        return retrofitService.updateEvaluation(header, id)
    }

    override suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Result<DomainEvaluation?> {
        return retrofitService.fetchEvaluation(
            appPackageName,
            GmsType.MICROG,
            UserType.SECURE
        ).map { it?.toDomain() }
    }

    override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Result<DomainEvaluation?> {
        return retrofitService.fetchEvaluation(
            appPackageName,
            GmsType.MICROG,
            UserType.RISKY
        ).map { it?.toDomain() }
    }

    override suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Result<DomainEvaluation?> {
        return retrofitService.fetchEvaluation(
            appPackageName,
            GmsType.BARE_AOSP,
            UserType.SECURE
        ).map { it?.toDomain() }
    }

    override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Result<DomainEvaluation?> {
        return retrofitService.fetchEvaluation(
            appPackageName,
            GmsType.BARE_AOSP,
            UserType.RISKY
        ).map { it?.toDomain() }
    }

    override suspend fun existingEvaluations(packageName: String): Result<List<DomainEvaluationRecord>> {
        return retrofitService.existingEvaluations(packageName)
            .map { evaluations -> evaluations.map { it.toDomain() } }
    }

    override suspend fun uploadIcon(app: DomainInstalledApplication): Result<List<DomainIcon>> {
        return retrofitService.uploadIcon(app)
            .map { icons -> icons.map { it.toDomain() } }
    }

    override suspend fun existingIcon(iconName: String): Result<List<DomainIcon>> {
        return retrofitService.existingIcon(iconName)
            .map { icons -> icons.map { it.toDomain() } }
    }

    override suspend fun deleteIcon(id: Int): Result<Unit> {
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
