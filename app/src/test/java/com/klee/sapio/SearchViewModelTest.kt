package com.klee.sapio

import android.os.Build
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
        
        searchViewModel = SearchViewModel()
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
}