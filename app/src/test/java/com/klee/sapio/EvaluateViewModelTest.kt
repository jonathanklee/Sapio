package com.klee.sapio

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.UserType
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.state.EvaluateEvent
import com.klee.sapio.ui.viewmodel.EvaluateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class EvaluateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private val appContext = RuntimeEnvironment.getApplication()

    private val fakeApp = InstalledApplication(
        name = "Test App",
        packageName = "com.test.app",
        icon = ColorDrawable(Color.RED)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState initializes with device gmsType and userType`() {
        val vm = buildViewModel(gmsType = GmsType.MICROG, userType = UserType.SECURE)

        assertEquals(GmsType.MICROG, vm.uiState.value.gmsType)
        assertEquals(UserType.SECURE, vm.uiState.value.userType)
    }

    @Test
    fun `submit emits NavigateToSuccess on success`() = runTest(dispatcher) {
        val vm = buildViewModel(app = fakeApp, useCaseResult = Result.success(Unit))

        val events = mutableListOf<EvaluateEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { events.add(it) }
        }

        vm.submit("com.test.app", "Test App", rating = 1)
        advanceUntilIdle()

        val event = events.first()
        assertTrue(event is EvaluateEvent.NavigateToSuccess)
        assertEquals("com.test.app", (event as EvaluateEvent.NavigateToSuccess).packageName)
        assertEquals("Test App", event.appName)
        assertFalse(vm.uiState.value.isSubmitting)
        job.cancel()
    }

    @Test
    fun `submit emits ShowError on failure`() = runTest(dispatcher) {
        val vm = buildViewModel(
            app = fakeApp,
            useCaseResult = Result.failure(IllegalStateException("error"))
        )

        val events = mutableListOf<EvaluateEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.events.collect { events.add(it) }
        }

        vm.submit("com.test.app", "Test App", rating = 1)
        advanceUntilIdle()

        assertTrue(events.first() is EvaluateEvent.ShowError)
        assertFalse(vm.uiState.value.isSubmitting)
        job.cancel()
    }

    @Test
    fun `submit does nothing when app not found`() = runTest(dispatcher) {
        val vm = buildViewModel(app = null, useCaseResult = Result.success(Unit))

        vm.submit("com.unknown.app", "Unknown", rating = 1)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSubmitting)
    }

    private fun buildViewModel(
        gmsType: Int = GmsType.BARE_AOSP,
        userType: Int = UserType.SECURE,
        app: InstalledApplication? = fakeApp,
        useCaseResult: Result<Unit> = Result.success(Unit)
    ): EvaluateViewModel {
        val fakeRepo = object : com.klee.sapio.domain.EvaluationRepository {
            override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<com.klee.sapio.domain.model.Evaluation>())
            override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<com.klee.sapio.domain.model.Evaluation>())
            override suspend fun addEvaluation(evaluation: com.klee.sapio.domain.model.UploadEvaluation) = Result.success(Unit)
            override suspend fun updateEvaluation(evaluation: com.klee.sapio.domain.model.UploadEvaluation, id: Int) = Result.success(Unit)
            override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
            override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<com.klee.sapio.domain.model.EvaluationRecord>())
            override suspend fun uploadIcon(a: InstalledApplication) = Result.success(emptyList<com.klee.sapio.domain.model.Icon>())
            override suspend fun existingIcon(iconName: String) = Result.success(emptyList<com.klee.sapio.domain.model.Icon>())
            override suspend fun deleteIcon(id: Int) = Result.success(Unit)
        }

        val fakeDeviceConfig = object : DeviceConfiguration(appContext) {
            override fun getGmsType() = gmsType
            override fun isRisky() = userType
        }

        val fakeInstalledAppsRepo = object : InstalledApplicationsRepository() {
            override fun getApplicationFromPackageName(
                context: android.content.Context,
                packageName: String
            ) = app
        }

        val fakeUseCase = object : EvaluateAppUseCase(fakeRepo, fakeDeviceConfig) {
            override suspend fun invoke(a: InstalledApplication, rating: Int) = useCaseResult
        }

        return EvaluateViewModel(fakeInstalledAppsRepo, fakeUseCase, fakeDeviceConfig, appContext)
    }
}
