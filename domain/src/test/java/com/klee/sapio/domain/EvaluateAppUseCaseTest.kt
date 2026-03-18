package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateAppUseCaseTest {

    private val app = InstalledApplication(name = "Test App", packageName = "com.test.app")
    private val icon = Icon(id = 42, name = "com.test.app.png", url = "http://icon/test.png")
    private val existingIcon = Icon(id = 7, name = "com.test.app.png", url = "http://icon/old.png")

    @Test
    fun `returns failure when icon upload returns empty list`() = runTest {
        val repo = fakeRepo(uploadIconResult = Result.success(emptyList()))
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        val result = useCase(app, rating = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure when icon upload fails`() = runTest {
        val repo = fakeRepo(uploadIconResult = Result.failure(RuntimeException("network error")))
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        val result = useCase(app, rating = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure when add evaluation fails`() = runTest {
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.failure(RuntimeException("server error"))
        )
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        val result = useCase(app, rating = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `returns success when upload and evaluation succeed`() = runTest {
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.success(Unit)
        )
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        val result = useCase(app, rating = 1)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deletes existing icons on success`() = runTest {
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.success(Unit),
            existingIconResult = Result.success(listOf(existingIcon))
        )
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        useCase(app, rating = 1)

        assertTrue(repo.deletedIconIds.contains(existingIcon.id))
    }

    @Test
    fun `does not fail when there are no existing icons to delete`() = runTest {
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.success(Unit),
            existingIconResult = Result.success(emptyList())
        )
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        val result = useCase(app, rating = 1)

        assertTrue(result.isSuccess)
        assertTrue(repo.deletedIconIds.isEmpty())
    }

    @Test
    fun `does not delete icons when evaluation fails`() = runTest {
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.failure(RuntimeException()),
            existingIconResult = Result.success(listOf(existingIcon))
        )
        val useCase = EvaluateAppUseCase(repo, FakeDeviceInfo())

        useCase(app, rating = 1)

        assertFalse(repo.deletedIconIds.contains(existingIcon.id))
    }

    @Test
    fun `uses device gmsType and userType when building evaluation`() = runTest {
        val deviceInfo = FakeDeviceInfo(gmsType = 2, userType = 4)
        val repo = fakeRepo(
            uploadIconResult = Result.success(listOf(icon)),
            addEvaluationResult = Result.success(Unit)
        )
        val useCase = EvaluateAppUseCase(repo, deviceInfo)

        useCase(app, rating = 1)

        val submitted = repo.lastSubmittedEvaluation
        assertTrue(submitted?.microg == 2)
        assertTrue(submitted?.rooted == 4)
    }

    private fun fakeRepo(
        uploadIconResult: Result<List<Icon>> = Result.success(listOf(icon)),
        addEvaluationResult: Result<Unit> = Result.success(Unit),
        existingIconResult: Result<List<Icon>> = Result.success(emptyList())
    ) = FakeEvaluationRepository(
        uploadIconResult = uploadIconResult,
        addEvaluationResult = addEvaluationResult,
        existingIconResult = existingIconResult
    )

    private class FakeEvaluationRepository(
        private val uploadIconResult: Result<List<Icon>>,
        private val addEvaluationResult: Result<Unit>,
        private val existingIconResult: Result<List<Icon>>
    ) : EvaluationRepository {
        val deletedIconIds = mutableListOf<Int>()
        var lastSubmittedEvaluation: UploadEvaluation? = null

        override suspend fun uploadIcon(packageName: String) = uploadIconResult
        override suspend fun existingIcon(iconName: String) = existingIconResult
        override suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit> {
            lastSubmittedEvaluation = evaluation
            return addEvaluationResult
        }
        override suspend fun deleteIcon(id: Int): Result<Unit> {
            deletedIconIds.add(id)
            return Result.success(Unit)
        }
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
    }

    private class FakeDeviceInfo(
        private val gmsType: Int = 1,
        private val userType: Int = 3
    ) : DeviceInfo {
        override fun getGmsType() = gmsType
        override fun isUnsafe() = userType
    }
}
