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

        val roboContext = org.robolectric.RuntimeEnvironment.getApplication()
        val deviceConfiguration = com.klee.sapio.data.system.DeviceConfiguration(roboContext)

        fakeRepository = FakeRepository()
        evaluateAppUseCase = EvaluateAppUseCase(fakeRepository, deviceConfiguration)

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
        fakeRepository.uploadIconResult = Result.success(emptyList())
        fakeRepository.existingIconsResult = Result.success(emptyList())

        val result = evaluateAppUseCase(realInstalledApplication, 1)

        Assert.assertTrue("Should return failure", result.isFailure)
    }

    @Test
    fun test_evaluateApp_withSuccessfulIconUpload() = runTest {
        val fakeIcon = Icon(id = 123, name = "test.png", url = "http://example.com/test.png")
        fakeRepository.uploadIconResult = Result.success(listOf(fakeIcon))
        fakeRepository.existingIconsResult = Result.success(emptyList())

        val result = evaluateAppUseCase(realInstalledApplication, 1)

        Assert.assertTrue("Should return success", result.isSuccess)
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
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int): Result<Evaluation?> =
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
