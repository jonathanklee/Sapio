package com.klee.sapio

import android.os.Build
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
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
        val appContext = org.robolectric.RuntimeEnvironment.getApplication()
        
        val mockRepository = object : com.klee.sapio.domain.EvaluationRepository {
            override suspend fun listLatestEvaluations(pageNumber: Int): List<com.klee.sapio.data.Evaluation> = emptyList()
            override suspend fun searchEvaluations(pattern: String): List<com.klee.sapio.data.Evaluation> = emptyList()
            override suspend fun addEvaluation(evaluation: com.klee.sapio.data.UploadEvaluation) {}
            override suspend fun updateEvaluation(evaluation: com.klee.sapio.data.UploadEvaluation, id: Int) {}
            override suspend fun fetchMicrogSecureEvaluation(appPackageName: String) = null
            override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String) = null
            override suspend fun fetchBareAospSecureEvaluation(appPackageName: String) = null
            override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String) = null
            override suspend fun existingEvaluations(packageName: String) = emptyList<com.klee.sapio.data.StrapiElement>()
            override suspend fun uploadIcon(app: com.klee.sapio.data.InstalledApplication) = null
            override suspend fun existingIcon(iconName: String) = emptyList<com.klee.sapio.data.IconAnswer>()
            override suspend fun deleteIcon(id: Int) = null
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
