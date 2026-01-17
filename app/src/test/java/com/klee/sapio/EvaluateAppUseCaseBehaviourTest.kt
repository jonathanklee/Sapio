package com.klee.sapio

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.klee.sapio.data.IconAnswer
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.data.DeviceConfiguration
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
import java.util.Date

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

    private fun iconAnswer(id: Int, url: String) = IconAnswer(
        id = id,
        name = "icon$id",
        alternativeText = null,
        caption = null,
        width = 10,
        height = 10,
        formats = null,
        hash = "hash$id",
        ext = ".png",
        mime = "image/png",
        size = 12,
        url = url,
        previewUrl = null,
        provider = null,
        provider_metadata = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    private class FakeRepository : com.klee.sapio.domain.EvaluationRepository {
        var existingIcons: List<IconAnswer> = emptyList()
        var uploadResponse: ArrayList<IconAnswer>? = null
        val deletedIds = mutableListOf<Int>()
        val addedEvaluations = mutableListOf<com.klee.sapio.data.UploadEvaluation>()

        override suspend fun listLatestEvaluations(pageNumber: Int): List<com.klee.sapio.data.Evaluation> = emptyList()
        override suspend fun searchEvaluations(pattern: String): List<com.klee.sapio.data.Evaluation> = emptyList()
        override suspend fun addEvaluation(evaluation: com.klee.sapio.data.UploadEvaluation) {
            addedEvaluations.add(evaluation)
        }
        override suspend fun updateEvaluation(evaluation: com.klee.sapio.data.UploadEvaluation, id: Int) = Unit
        override suspend fun fetchMicrogSecureEvaluation(appPackageName: String) = null
        override suspend fun fetchMicrogRiskyEvaluation(appPackageName: String) = null
        override suspend fun fetchBareAospSecureEvaluation(appPackageName: String) = null
        override suspend fun fetchBareAospRiskyEvaluation(appPackageName: String) = null
        override suspend fun existingEvaluations(packageName: String) = emptyList<com.klee.sapio.data.StrapiElement>()
        override suspend fun uploadIcon(app: InstalledApplication) = uploadResponse?.let { retrofit2.Response.success(it) }
        override suspend fun existingIcon(iconName: String) = existingIcons
        override suspend fun deleteIcon(id: Int): retrofit2.Response<IconAnswer>? {
            deletedIds.add(id)
            return retrofit2.Response.success(null)
        }
    }
}
