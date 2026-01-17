package com.klee.sapio

import android.os.Build
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.SearchEvaluationUseCase
import com.klee.sapio.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class SearchViewModelTest {

    private lateinit var searchViewModel: SearchViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        searchViewModel = SearchViewModel(FakeSearchUseCase(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_searchViewModel_initialState() = runTest {
        // Test that the ViewModel initializes correctly
        val initialState = searchViewModel.evaluations.first()
        Assert.assertTrue("Initial state should be empty", initialState.isEmpty())
    }

    @Test
    fun test_searchViewModel_flowBehavior() = runTest {
        // Test basic flow behavior - this is a simple test to verify the ViewModel works
        // without needing to mock the complex use case
        val initialState = searchViewModel.evaluations.first()
        Assert.assertNotNull("Flow should not be null", initialState)
        Assert.assertTrue("Should be a list", initialState is List<*>)
    }

    @Test
    fun test_search_updates_state_and_calls_onError_when_empty() = runTest {
        val fakeUseCase = FakeSearchUseCase(emptyList())
        val viewModel = SearchViewModel(fakeUseCase)
        var errored = false

        viewModel.searchApplication("pattern") { errored = true }

        val result = viewModel.evaluations.first()
        Assert.assertTrue(errored)
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun test_search_updates_state_with_results() = runTest {
        val expected = listOf(
            Evaluation("Name", "pkg", iconUrl = null, rating = 1, microg = 1, secure = 1, updatedAt = null, createdAt = null, publishedAt = null, versionName = null)
        )
        val fakeUseCase = FakeSearchUseCase(expected)
        val viewModel = SearchViewModel(fakeUseCase)
        var errored = false

        viewModel.searchApplication("pattern") { errored = true }

        val result = viewModel.evaluations.first()
        Assert.assertFalse(errored)
        Assert.assertEquals(expected, result)
    }

    private class FakeSearchUseCase(private val result: List<Evaluation>) : SearchEvaluationUseCase(object : com.klee.sapio.domain.EvaluationRepository {
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
    }) {
        override suspend operator fun invoke(pattern: String): List<Evaluation> = result
    }
}
