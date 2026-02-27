package com.klee.sapio.data.repository

import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.dto.UploadEvaluationHeader
import com.klee.sapio.data.local.EvaluationDao
import com.klee.sapio.data.local.IconDao
import com.klee.sapio.data.mapper.toData
import com.klee.sapio.data.mapper.toDomain
import com.klee.sapio.data.mapper.toEntity
import com.klee.sapio.domain.EvaluationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import com.klee.sapio.domain.model.Evaluation as DomainEvaluation
import com.klee.sapio.domain.model.EvaluationRecord as DomainEvaluationRecord
import com.klee.sapio.domain.model.Icon as DomainIcon
import com.klee.sapio.domain.model.InstalledApplication as DomainInstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation as DomainUploadEvaluation

class EvaluationRepositoryImpl @Inject constructor(
    private val retrofitService: EvaluationService,
    private val evaluationDao: EvaluationDao,
    private val iconDao: IconDao
) :
    EvaluationRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<DomainEvaluation>> {
        val offset = (pageNumber - 1).coerceAtLeast(0) * PAGE_SIZE
        val remote = retrofitService.listLatestEvaluations(pageNumber)
        if (remote.isSuccess) {
            val evaluations = remote.getOrThrow()
            val now = System.currentTimeMillis()
            evaluationDao.upsertAll(evaluations.map { it.toEntity(now) })
            return Result.success(evaluations.map { it.toDomain() })
        }

        val cached = evaluationDao.listLatestEvaluations(PAGE_SIZE, offset)
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            Result.success(cached)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Failed to load evaluations"))
        }
    }

    override suspend fun searchEvaluations(pattern: String): Result<List<DomainEvaluation>> {
        val remote = retrofitService.searchEvaluation(pattern)
        if (remote.isSuccess) {
            val evaluations = remote.getOrThrow()
            val now = System.currentTimeMillis()
            evaluationDao.upsertAll(evaluations.map { it.toEntity(now) })
            return Result.success(evaluations.map { it.toDomain() })
        }

        val cached = evaluationDao.searchEvaluations("%$pattern%")
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            Result.success(cached)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Failed to search evaluations"))
        }
    }

    override suspend fun addEvaluation(evaluation: DomainUploadEvaluation): Result<Unit> {
        val header = UploadEvaluationHeader(evaluation.toData())
        return retrofitService.addEvaluation(header)
    }

    override suspend fun updateEvaluation(evaluation: DomainUploadEvaluation, id: Int): Result<Unit> {
        val header = UploadEvaluationHeader(evaluation.toData())
        return retrofitService.updateEvaluation(header, id)
    }

    override suspend fun fetchEvaluation(
        appPackageName: String,
        gmsType: Int,
        userType: Int
    ): Result<DomainEvaluation?> {
        return fetchEvaluationWithFallback(appPackageName, gmsType, userType)
    }

    override suspend fun existingEvaluations(packageName: String): Result<List<DomainEvaluationRecord>> {
        return retrofitService.existingEvaluations(packageName)
            .map { evaluations -> evaluations.map { it.toDomain() } }
    }

    override suspend fun uploadIcon(app: DomainInstalledApplication): Result<List<DomainIcon>> {
        val remote = retrofitService.uploadIcon(app)
        if (remote.isSuccess) {
            val icons = remote.getOrThrow()
            val now = System.currentTimeMillis()
            iconDao.upsertAll(icons.map { it.toEntity(now) })
            return Result.success(icons.map { it.toDomain() })
        }

        val cached = iconDao.findByName("${app.packageName}.png")
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            Result.success(cached)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Failed to upload icon"))
        }
    }

    override suspend fun existingIcon(iconName: String): Result<List<DomainIcon>> {
        val remote = retrofitService.existingIcon(iconName)
        if (remote.isSuccess) {
            val icons = remote.getOrThrow()
            val now = System.currentTimeMillis()
            iconDao.upsertAll(icons.map { it.toEntity(now) })
            return Result.success(icons.map { it.toDomain() })
        }

        val cached = iconDao.findByName(iconName)
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            Result.success(cached)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Failed to load icon"))
        }
    }

    override suspend fun deleteIcon(id: Int): Result<Unit> {
        val remote = retrofitService.deleteIcon(id)
        if (remote.isSuccess) {
            iconDao.deleteById(id)
        }
        return remote
    }

    private suspend fun fetchEvaluationWithFallback(
        packageName: String,
        microg: Int,
        secure: Int
    ): Result<DomainEvaluation?> {
        val remote = retrofitService.fetchEvaluation(packageName, microg, secure)
        if (remote.isSuccess) {
            val evaluation = remote.getOrNull()
            if (evaluation != null) {
                val now = System.currentTimeMillis()
                evaluationDao.upsertAll(listOf(evaluation.toEntity(now)))
            }
            return Result.success(evaluation?.toDomain())
        }

        val cached = evaluationDao.getEvaluation(packageName, microg, secure)
            ?.toDomain()
        return if (cached != null) {
            Result.success(cached)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Failed to load evaluation"))
        }
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
