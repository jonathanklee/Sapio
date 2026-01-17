package com.klee.sapio

import android.os.Build
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class FeedViewModelTest {

    @Test
    fun `evaluations flow emits 10 pages from use case`() = runTest {
        val mockRepository = object : com.klee.sapio.domain.EvaluationRepository {
            override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> =
                Result.success(emptyList())
            override suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>> =
                Result.success(emptyList())
            override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> = Result.success(Unit)
            override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int): Result<Unit> =
                Result.success(Unit)
            override suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Result<Evaluation?> =
                Result.success(null)
            override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Result<Evaluation?> =
                Result.success(null)
            override suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Result<Evaluation?> =
                Result.success(null)
            override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Result<Evaluation?> =
                Result.success(null)
            override suspend fun existingEvaluations(packageName: String): Result<List<EvaluationRecord>> =
                Result.success(emptyList())
            override suspend fun uploadIcon(app: InstalledApplication): Result<List<Icon>> =
                Result.success(emptyList())
            override suspend fun existingIcon(iconName: String): Result<List<Icon>> =
                Result.success(emptyList())
            override suspend fun deleteIcon(id: Int): Result<Unit> = Result.success(Unit)
        }
        
        val fakeUseCase = object : ListLatestEvaluationsUseCase(mockRepository) {
            override suspend operator fun invoke(pageNumber: Int): Result<List<Evaluation>> {
                return Result.success(
                    listOf(
                    Evaluation(
                        name = "page-$pageNumber",
                        packageName = "pkg$pageNumber",
                        iconUrl = null,
                        rating = pageNumber,
                        microg = 1,
                        secure = 1,
                        updatedAt = null,
                        createdAt = null,
                        publishedAt = null,
                        versionName = null
                    )
                    )
                )
            }
        }

        val vm = FeedViewModel(fakeUseCase)

        val emissions = vm.evaluations.toList()

        assertEquals(10, emissions.size)
        assertEquals("page-1", emissions.first().first().name)
        assertEquals("page-10", emissions.last().first().name)
    }
}
