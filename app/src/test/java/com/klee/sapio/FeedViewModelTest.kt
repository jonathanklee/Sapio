package com.klee.sapio

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class FeedViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh loads only page 1 on startup`() = runTest(dispatcher) {
        val pagesRequested = mutableListOf<Int>()
        val vm = FeedViewModel(useCaseReturningOnePage(pagesRequested))
        advanceUntilIdle()

        assertEquals(listOf(1), pagesRequested)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("page-1", vm.uiState.value.items.first().name)
    }

    @Test
    fun `loadNextPage appends the next page`() = runTest(dispatcher) {
        val pagesRequested = mutableListOf<Int>()
        val vm = FeedViewModel(useCaseReturningOnePage(pagesRequested))
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), pagesRequested)
        assertEquals(2, vm.uiState.value.items.size)
        assertEquals("page-1", vm.uiState.value.items[0].name)
        assertEquals("page-2", vm.uiState.value.items[1].name)
    }

    @Test
    fun `loadNextPage is ignored while initial load is in progress`() = runTest(dispatcher) {
        val pagesRequested = mutableListOf<Int>()
        val vm = FeedViewModel(useCaseReturningOnePage(pagesRequested))

        // initial load is enqueued but hasn't run yet (isLoading = true from initial state)
        vm.loadNextPage()
        advanceUntilIdle()

        // only page 1 should have been fetched
        assertEquals(listOf(1), pagesRequested)
    }

    @Test
    fun `loadNextPage stops when an empty page is returned`() = runTest(dispatcher) {
        val pagesRequested = mutableListOf<Int>()
        val vm = FeedViewModel(useCaseReturningEmptyAfter(page = 1, pagesRequested))
        advanceUntilIdle()

        vm.loadNextPage() // page 2 returns empty — hasMorePages becomes false
        advanceUntilIdle()

        vm.loadNextPage() // should be no-ops from here
        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), pagesRequested)
    }

    @Test
    fun `refresh resets and reloads from page 1`() = runTest(dispatcher) {
        val pagesRequested = mutableListOf<Int>()
        val vm = FeedViewModel(useCaseReturningOnePage(pagesRequested))
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        vm.refresh()
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 1), pagesRequested)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("page-1", vm.uiState.value.items.first().name)
    }

    @Test
    fun `error during load sets hasError in state`() = runTest(dispatcher) {
        val vm = FeedViewModel(useCaseAlwaysFailing())
        advanceUntilIdle()

        assertTrue(vm.uiState.value.hasError)
        assertFalse(vm.uiState.value.isLoading)
    }

    // --- helpers ---

    private fun useCaseReturningOnePage(tracker: MutableList<Int>): ListLatestEvaluationsUseCase {
        return object : ListLatestEvaluationsUseCase(emptyRepository()) {
            override suspend fun invoke(pageNumber: Int): Result<List<Evaluation>> {
                tracker.add(pageNumber)
                return Result.success(listOf(eval("page-$pageNumber", pageNumber)))
            }
        }
    }

    private fun useCaseReturningEmptyAfter(
        page: Int,
        tracker: MutableList<Int>
    ): ListLatestEvaluationsUseCase {
        return object : ListLatestEvaluationsUseCase(emptyRepository()) {
            override suspend fun invoke(pageNumber: Int): Result<List<Evaluation>> {
                tracker.add(pageNumber)
                return if (pageNumber <= page) {
                    Result.success(listOf(eval("page-$pageNumber", pageNumber)))
                } else {
                    Result.success(emptyList())
                }
            }
        }
    }

    private fun useCaseAlwaysFailing(): ListLatestEvaluationsUseCase {
        return object : ListLatestEvaluationsUseCase(emptyRepository()) {
            override suspend fun invoke(pageNumber: Int): Result<List<Evaluation>> {
                return Result.failure(RuntimeException("network error"))
            }
        }
    }

    private fun emptyRepository(): EvaluationRepository = object : EvaluationRepository {
        override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> = Result.success(emptyList())
        override suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>> = Result.success(emptyList())
        override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int): Result<Unit> = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int): Result<Evaluation?> = Result.success(null)
        override suspend fun existingEvaluations(packageName: String): Result<List<EvaluationRecord>> = Result.success(emptyList())
        override suspend fun uploadIcon(app: InstalledApplication): Result<List<Icon>> = Result.success(emptyList())
        override suspend fun existingIcon(iconName: String): Result<List<Icon>> = Result.success(emptyList())
        override suspend fun deleteIcon(id: Int): Result<Unit> = Result.success(Unit)
    }

    private fun eval(name: String, rating: Int) = Evaluation(
        name = name,
        packageName = "pkg$rating",
        iconUrl = null,
        rating = rating,
        microg = 1,
        secure = 1,
        updatedAt = null,
        createdAt = null,
        publishedAt = null,
        versionName = null
    )
}
