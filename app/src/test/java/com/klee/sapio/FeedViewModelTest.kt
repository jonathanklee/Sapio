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
            override suspend fun listLatestEvaluations(pageNumber: Int): List<Evaluation> = emptyList()
            override suspend fun searchEvaluations(pattern: String): List<Evaluation> = emptyList()
            override suspend fun addEvaluation(evaluation: UploadEvaluation) {}
            override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) {}
            override suspend fun fetchMicrogSecureEvaluation(appPackageName: String) = null
            override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String) = null
            override suspend fun fetchBareAospSecureEvaluation(appPackageName: String) = null
            override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String) = null
            override suspend fun existingEvaluations(packageName: String): List<EvaluationRecord> = emptyList()
            override suspend fun uploadIcon(app: InstalledApplication): List<Icon>? = null
            override suspend fun existingIcon(iconName: String): List<Icon> = emptyList()
            override suspend fun deleteIcon(id: Int) = Unit
        }
        
        val fakeUseCase = object : ListLatestEvaluationsUseCase(mockRepository) {
            override suspend operator fun invoke(pageNumber: Int): List<Evaluation> {
                return listOf(
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
            }
        }

        val vm = FeedViewModel(fakeUseCase)

        val emissions = vm.evaluations.toList()

        assertEquals(10, emissions.size)
        assertEquals("page-1", emissions.first().first().name)
        assertEquals("page-10", emissions.last().first().name)
    }
}
