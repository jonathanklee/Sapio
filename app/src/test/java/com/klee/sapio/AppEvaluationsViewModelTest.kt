package com.klee.sapio

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klee.sapio.data.system.Settings
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.FetchEvaluationHistoryUseCase
import com.klee.sapio.domain.model.Environment
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationHistory
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.ui.viewmodel.AppEvaluationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class AppEvaluationsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private val appContext = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `listEvaluations posts only user evaluations when unsafe disabled`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = false)

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        val state = vm.uiState.value
        assertEquals("microg-secure", state.microgUser?.current?.name)
        assertEquals("bare-secure", state.bareAospUser?.current?.name)
        assertNull(state.microgRoot)
        assertNull(state.bareAospRoot)
        assertEquals("https://icon", state.iconUrl)
        assertTrue(state.isFullyLoaded)
        assertEquals(0, state.pendingCount)
    }

    @Test
    fun `listEvaluations posts unsafe evaluations when unsafe enabled`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = true)

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        val state = vm.uiState.value
        assertEquals("microg-unsafe", state.microgRoot?.current?.name)
        assertEquals("bare-unsafe", state.bareAospRoot?.current?.name)
        assertTrue(state.isFullyLoaded)
        assertEquals(0, state.pendingCount)
    }

    @Test
    fun `listEvaluations handles null evaluations`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = true, returnNullEvals = true, iconUrl = "")

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        val state = vm.uiState.value
        assertNull(state.microgUser)
        assertNull(state.bareAospUser)
        assertNull(state.microgRoot)
        assertNull(state.bareAospRoot)
        assertEquals("", state.iconUrl)
        assertTrue(state.isFullyLoaded)
        assertEquals(0, state.pendingCount)
    }

    @Test
    fun `listEvaluations keeps the icon of the most recent evaluation`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = false, history = agedHistory())

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        assertEquals("https://new-icon", vm.uiState.value.iconUrl)
    }

    @Test
    fun `listEvaluations keeps the most recent icon across environments`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = false, history = historyPerEnvironment())

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        assertEquals("https://bare-icon", vm.uiState.value.iconUrl)
    }

    @Test
    fun `listEvaluations falls back to an older icon when the latest has none`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = false, history = historyWithIconlessLatest())

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        assertEquals("https://old-icon", vm.uiState.value.iconUrl)
    }

    @Test
    fun `listEvaluations posts an empty icon when no evaluation carries one`() = runTest(dispatcher) {
        val vm = buildViewModel(unsafeEnabled = false, iconUrl = null)

        vm.listEvaluations("pkg")
        advanceUntilIdle()
        vm.onIconDisplayed()

        val state = vm.uiState.value
        assertEquals("", state.iconUrl)
        assertTrue(state.isFullyLoaded)
    }

    private fun agedHistory(): Map<Environment, EvaluationHistory> {
        val outdated = eval("outdated", "pkg", "https://old-icon", Date(1_000L))
        val latest = eval("latest", "pkg", "https://new-icon", Date(2_000L))

        return mapOf(
            Environment(GmsType.MICROG, UserType.STANDARD) to EvaluationHistory(listOf(outdated, latest))
        )
    }

    private fun historyPerEnvironment(): Map<Environment, EvaluationHistory> {
        val microg = eval("microg", "pkg", "https://microg-icon", Date(1_000L))
        val bareAosp = eval("bare-aosp", "pkg", "https://bare-icon", Date(2_000L))

        return mapOf(
            Environment(GmsType.MICROG, UserType.STANDARD) to EvaluationHistory(listOf(microg)),
            Environment(GmsType.BARE_AOSP, UserType.STANDARD) to EvaluationHistory(listOf(bareAosp))
        )
    }

    private fun historyWithIconlessLatest(): Map<Environment, EvaluationHistory> {
        val outdated = eval("outdated", "pkg", "https://old-icon", Date(1_000L))
        val latest = eval("latest", "pkg", null, Date(2_000L))

        return mapOf(
            Environment(GmsType.MICROG, UserType.STANDARD) to EvaluationHistory(listOf(outdated, latest))
        )
    }

    private fun buildViewModel(
        unsafeEnabled: Boolean,
        returnNullEvals: Boolean = false,
        iconUrl: String? = "https://icon",
        history: Map<Environment, EvaluationHistory>? = null
    ): AppEvaluationsViewModel {
        val mockRepository = object : EvaluationRepository {
            override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> =
                Result.success(emptyList())
            override suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>> =
                Result.success(emptyList())
            override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> = Result.success(Unit)
            override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int): Result<Evaluation?> =
                Result.success(null)
            override suspend fun fetchEvaluationHistory(packageName: String) = Result.success(emptyList<com.klee.sapio.domain.model.Evaluation>())
            override suspend fun uploadIcon(packageName: String): Result<List<Icon>> =
                Result.success(emptyList())
            override suspend fun existingIcon(iconName: String): Result<List<Icon>> =
                Result.success(emptyList())
            override suspend fun deleteIcon(id: Int): Result<Unit> = Result.success(Unit)
        }

        val fetchHistoryUseCase = object : FetchEvaluationHistoryUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): Result<Map<Environment, EvaluationHistory>> {
                if (returnNullEvals) return Result.success(emptyMap())
                if (history != null) return Result.success(history)

                val named = mapOf(
                    Environment(GmsType.MICROG, UserType.STANDARD) to "microg-secure",
                    Environment(GmsType.MICROG, UserType.PERMISSIVE) to "microg-unsafe",
                    Environment(GmsType.BARE_AOSP, UserType.STANDARD) to "bare-secure",
                    Environment(GmsType.BARE_AOSP, UserType.PERMISSIVE) to "bare-unsafe"
                )

                return Result.success(
                    named.mapValues { (_, name) ->
                        EvaluationHistory(listOf(eval(name, packageName, iconUrl, Date(1_000L))))
                    }
                )
            }
        }
        val settingsObj = object : Settings(appContext) {
            override fun isUnsafeConfigurationEnabled(): Boolean = unsafeEnabled
        }

        return AppEvaluationsViewModel(
            fetchHistoryUseCase,
            settingsObj
        ).apply {
            ioDispatcher = dispatcher
        }
    }

    private fun eval(
        name: String,
        packageName: String,
        iconUrl: String?,
        updatedAt: Date?
    ) = Evaluation(
        name = name,
        packageName = packageName,
        iconUrl = iconUrl,
        rating = 1,
        microg = 1,
        secure = 1,
        updatedAt = updatedAt,
        createdAt = null,
        publishedAt = null,
        versionName = null,
        brokenFeatures = null
    )
}
