package com.klee.sapio

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.data.DeviceConfiguration
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE
import android.os.Build

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class EvaluateAppUseCaseBehaviourTest {

    private lateinit var useCase: EvaluateAppUseCase
    private lateinit var deviceConfiguration: DeviceConfiguration
    private lateinit var repository: FakeRepository

    private val installedApp = InstalledApplication(
        name = "Demo",
        packageName = "com.demo.app",
        icon = ColorDrawable(Color.BLUE)
    )

    @Before
    fun setup() {
        repository = FakeRepository()
        val roboContext = org.robolectric.RuntimeEnvironment.getApplication()
        deviceConfiguration = DeviceConfiguration(roboContext)
        useCase = EvaluateAppUseCase(repository, deviceConfiguration)
    }

    @Test
    fun `invoke uploads, deletes old icons and adds evaluation`() = runTest {
        repository.existingIcons = listOf(
            iconAnswer(id = 10, url = "old1"),
            iconAnswer(id = 11, url = "old2")
        )
        repository.uploadResponse = arrayListOf(iconAnswer(id = 99, url = "new"))

        var success = false
        var error = false

        useCase.invoke(installedApp, rating = 5, onSuccess = { success = true }, onError = { error = true })

        assertTrue(repository.deletedIds.containsAll(listOf(10, 11)))
        assertTrue(repository.addedEvaluations.isNotEmpty())
        assertEquals(99, repository.addedEvaluations.first().icon)
        assertEquals(5, repository.addedEvaluations.first().rating)
        assertTrue(success)
        assertFalse(error)
    }

    @Test
    fun `invoke calls onError when upload fails`() = runTest {
        repository.existingIcons = emptyList()
        repository.uploadResponse = null // simulate upload failure

        var success = false
        var error = false

        useCase.invoke(installedApp, rating = 2, onSuccess = { success = true }, onError = { error = true })

        assertTrue(error)
        assertFalse(success)
        assertTrue(repository.addedEvaluations.isEmpty())
        assertTrue(repository.deletedIds.isEmpty())
    }

    private fun iconAnswer(id: Int, url: String) = Icon(
        id = id,
        name = "icon$id",
        url = url
    )

    private class FakeRepository : com.klee.sapio.domain.EvaluationRepository {
        var existingIcons: List<Icon> = emptyList()
        var uploadResponse: List<Icon>? = null
        val deletedIds = mutableListOf<Int>()
        val addedEvaluations = mutableListOf<UploadEvaluation>()

        override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<com.klee.sapio.domain.model.Evaluation>> =
            Result.success(emptyList())
        override suspend fun searchEvaluations(pattern: String): Result<List<com.klee.sapio.domain.model.Evaluation>> =
            Result.success(emptyList())
        override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> {
            addedEvaluations.add(evaluation)
            return Result.success(Unit)
        }
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int): Result<Unit> = Result.success(Unit)
        override suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Result<com.klee.sapio.domain.model.Evaluation?> =
            Result.success(null)
        override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Result<com.klee.sapio.domain.model.Evaluation?> =
            Result.success(null)
        override suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Result<com.klee.sapio.domain.model.Evaluation?> =
            Result.success(null)
        override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Result<com.klee.sapio.domain.model.Evaluation?> =
            Result.success(null)
        override suspend fun existingEvaluations(packageName: String): Result<List<com.klee.sapio.domain.model.EvaluationRecord>> =
            Result.success(emptyList())
        override suspend fun uploadIcon(app: InstalledApplication): Result<List<Icon>> =
            Result.success(uploadResponse ?: emptyList())
        override suspend fun existingIcon(iconName: String): Result<List<Icon>> =
            Result.success(existingIcons)
        override suspend fun deleteIcon(id: Int): Result<Unit> {
            deletedIds.add(id)
            return Result.success(Unit)
        }
    }
}
