package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FetchAppEvaluationUseCaseTest {

    @Test
    fun `returns evaluation when found`() = runTest {
        val evaluation = evaluation()
        val useCase = FetchAppEvaluationUseCase(fakeRepo(Result.success(evaluation)))

        val result = useCase("com.test.app", gmsType = 1, userType = 3)

        assertTrue(result.isSuccess)
        assertEquals(evaluation, result.getOrNull())
    }

    @Test
    fun `returns null when not found`() = runTest {
        val useCase = FetchAppEvaluationUseCase(fakeRepo(Result.success(null)))

        val result = useCase("com.test.app", gmsType = 1, userType = 3)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        val error = RuntimeException("network error")
        val useCase = FetchAppEvaluationUseCase(fakeRepo(Result.failure(error)))

        val result = useCase("com.test.app", gmsType = 1, userType = 3)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `passes correct arguments to repository`() = runTest {
        val repo = RecordingFakeRepo()
        val useCase = FetchAppEvaluationUseCase(repo)

        useCase("com.test.app", gmsType = 2, userType = 4)

        assertEquals("com.test.app", repo.lastPackageName)
        assertEquals(2, repo.lastGmsType)
        assertEquals(4, repo.lastUserType)
    }

    private fun fakeRepo(result: Result<Evaluation?>) = object : EvaluationRepository {
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = result
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private class RecordingFakeRepo : EvaluationRepository {
        var lastPackageName: String = ""
        var lastGmsType: Int = -1
        var lastUserType: Int = -1
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int): Result<Evaluation?> {
            lastPackageName = appPackageName
            lastGmsType = gmsType
            lastUserType = userType
            return Result.success(null)
        }
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private fun evaluation() = Evaluation(
        name = "Test App", packageName = "com.test.app", iconUrl = null,
        rating = 1, microg = 1, secure = 3,
        updatedAt = Date(), createdAt = Date(), publishedAt = null, versionName = null
    )
}
