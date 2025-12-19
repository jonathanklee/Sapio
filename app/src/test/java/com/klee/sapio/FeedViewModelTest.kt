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
        val fakeUseCase = object : ListLatestEvaluationsUseCase() {
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

        val vm = FeedViewModel().apply { listLatestEvaluationsUseCase = fakeUseCase }

        val emissions = vm.evaluations.toList()

        assertEquals(10, emissions.size)
        assertEquals("page-1", emissions.first().first().name)
        assertEquals("page-10", emissions.last().first().name)
    }
}
