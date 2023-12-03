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

    override suspend fun existingEvaluations(packageName: String): List<StrapiElement> {
        return try {
            retrofitService.existingEvaluations(packageName)
        } catch (exception: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<IconAnswer>>? {
        return try {
            retrofitService.uploadIcon(app)
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun existingIcon(iconName: String): List<IconAnswer> {
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

    override suspend fun deleteIcon(id: Int): Response<IconAnswer>? {
        return try {
            retrofitService.deleteIcon(id)
        } catch (exception: Exception) {
            null
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
