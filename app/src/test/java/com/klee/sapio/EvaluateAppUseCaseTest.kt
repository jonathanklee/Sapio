package com.klee.sapio

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class EvaluateAppUseCaseTest {

    private lateinit var evaluateAppUseCase: EvaluateAppUseCase

    private lateinit var fakeRepository: FakeRepository

    private lateinit var realInstalledApplication: InstalledApplication

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Create a real DeviceConfiguration instance since it's a final class
        val roboContext = org.robolectric.RuntimeEnvironment.getApplication()
        val deviceConfiguration = com.klee.sapio.data.system.DeviceConfiguration(roboContext)
        
        fakeRepository = FakeRepository()
        evaluateAppUseCase = EvaluateAppUseCase(fakeRepository, deviceConfiguration)
        
        // Create a real InstalledApplication instance since it's a final class
        val fakeDrawable = ColorDrawable(Color.RED)
        realInstalledApplication = InstalledApplication(
            name = "Test App",
            packageName = "com.test.app",
            icon = fakeDrawable
        )

        fakeRepository.addEvaluationResult = Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_evaluateApp_withFailedIconUpload() = runTest {
        // Setup mock data for failed upload
        fakeRepository.uploadIconResult = Result.success(emptyList())
        
        // Mock the existingIcon call to return empty list to avoid NPE
        fakeRepository.existingIconsResult = Result.success(emptyList())

        var successCalled = false
        var errorCalled = false

        evaluateAppUseCase.invoke(realInstalledApplication, 1,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        Assert.assertFalse("Success callback should not be called", successCalled)
        Assert.assertTrue("Error callback should be called", errorCalled)
    }

    @Test
    fun test_evaluateApp_withSuccessfulIconUpload() = runTest {
        // Setup mock data
        val fakeIcon = Icon(
            id = 123,
            name = "test.png",
            url = "http://example.com/test.png"
        )

        val fakeResponse = listOf(fakeIcon)
        val fakeExistingIcons = emptyList<Icon>()

        fakeRepository.uploadIconResult = Result.success(fakeResponse)
        fakeRepository.existingIconsResult = Result.success(fakeExistingIcons)

        var successCalled = false
        var errorCalled = false

        evaluateAppUseCase.invoke(realInstalledApplication, 1,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        Assert.assertTrue("Success callback should be called", successCalled)
        Assert.assertFalse("Error callback should not be called", errorCalled)
    }

    private class FakeRepository : com.klee.sapio.domain.EvaluationRepository {
        var uploadIconResult: Result<List<Icon>> = Result.success(emptyList())
        var existingIconsResult: Result<List<Icon>> = Result.success(emptyList())
        var addEvaluationResult: Result<Unit> = Result.success(Unit)

        override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> =
            Result.success(emptyList())

        override suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>> =
            Result.success(emptyList())

        override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> =
            addEvaluationResult

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
            uploadIconResult

        override suspend fun existingIcon(iconName: String): Result<List<Icon>> =
            existingIconsResult

        override suspend fun deleteIcon(id: Int): Result<Unit> = Result.success(Unit)
    }
}
