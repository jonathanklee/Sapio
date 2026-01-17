package com.klee.sapio

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.Settings
import com.klee.sapio.domain.FetchAppBareAospRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospSecureEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogSecureEvaluationUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
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
    fun `listEvaluations posts only user evaluations when root disabled`() = runTest(dispatcher) {
        val vm = buildViewModel(rootEnabled = false)

        vm.listEvaluations("pkg")
        advanceUntilIdle()

        assertEquals("microg-secure", vm.microgUserEvaluation.value?.name)
        assertEquals("bare-secure", vm.bareAospUserEvaluation.value?.name)
        assertNull(vm.microgRootEvaluation.value)
        assertNull(vm.bareAsopRootEvaluation.value)
        assertEquals("https://icon", vm.iconUrl.value)
    }

    @Test
    fun `listEvaluations posts root evaluations when root enabled`() = runTest(dispatcher) {
        val vm = buildViewModel(rootEnabled = true)

        vm.listEvaluations("pkg")
        advanceUntilIdle()

        assertEquals("microg-risky", vm.microgRootEvaluation.value?.name)
        assertEquals("bare-risky", vm.bareAsopRootEvaluation.value?.name)
    }

    @Test
    fun `listEvaluations handles null evaluations`() = runTest(dispatcher) {
        val vm = buildViewModel(rootEnabled = true, returnNullEvals = true, iconUrl = "")

        vm.listEvaluations("pkg")
        advanceUntilIdle()

        assertNull(vm.microgUserEvaluation.value)
        assertNull(vm.bareAospUserEvaluation.value)
        assertNull(vm.microgRootEvaluation.value)
        assertNull(vm.bareAsopRootEvaluation.value)
        assertEquals("", vm.iconUrl.value)
    }

    private fun buildViewModel(
        rootEnabled: Boolean,
        returnNullEvals: Boolean = false,
        iconUrl: String = "https://icon"
    ): AppEvaluationsViewModel {
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
        
        val microgSecureUseCase = object : FetchAppMicrogSecureEvaluationUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): Evaluation? =
                if (returnNullEvals) null else eval("microg-secure", packageName)
        }
        val microgRiskyUseCase = object : FetchAppMicrogRiskyEvaluationUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): Evaluation? =
                if (returnNullEvals) null else eval("microg-risky", packageName)
        }
        val bareSecureUseCase = object : FetchAppBareAospSecureEvaluationUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): Evaluation? =
                if (returnNullEvals) null else eval("bare-secure", packageName)
        }
        val bareRiskyUseCase = object : FetchAppBareAospRiskyEvaluationUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): Evaluation? =
                if (returnNullEvals) null else eval("bare-risky", packageName)
        }
        val iconUrlUseCase = object : FetchIconUrlUseCase(mockRepository) {
            override suspend fun invoke(packageName: String): String = iconUrl
        }
        val settingsObj = object : Settings(appContext) {
            override fun isRootConfigurationEnabled(): Boolean = rootEnabled
        }
        
        return AppEvaluationsViewModel(
            microgSecureUseCase,
            microgRiskyUseCase,
            bareSecureUseCase,
            bareRiskyUseCase,
            iconUrlUseCase,
            settingsObj
        ).apply {
            ioDispatcher = dispatcher
        }
    }

    private fun eval(name: String, packageName: String) = Evaluation(
        name = name,
        packageName = packageName,
        iconUrl = null,
        rating = 1,
        microg = 1,
        secure = 1,
        updatedAt = null,
        createdAt = null,
        publishedAt = null,
        versionName = null
    )
}
