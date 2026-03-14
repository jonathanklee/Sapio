package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ListLatestEvaluationsUseCaseTest {

    @Test
    fun `returns list from repository on success`() = runTest {
        val evaluations = listOf(
            evaluation(name = "App A", packageName = "com.a"),
            evaluation(name = "App B", packageName = "com.b")
        )
        val useCase = ListLatestEvaluationsUseCase(fakeRepo(Result.success(evaluations)))

        val result = useCase(pageNumber = 1)

        assertTrue(result.isSuccess)
        assertEquals(evaluations, result.getOrNull())
    }

    @Test
    fun `returns empty list when repository returns empty`() = runTest {
        val useCase = ListLatestEvaluationsUseCase(fakeRepo(Result.success(emptyList())))

        val result = useCase(pageNumber = 1)

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Evaluation>(), result.getOrNull())
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        val error = RuntimeException("network error")
        val useCase = ListLatestEvaluationsUseCase(fakeRepo(Result.failure(error)))

        val result = useCase(pageNumber = 1)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `passes page number to repository`() = runTest {
        val repo = RecordingFakeRepo()
        val useCase = ListLatestEvaluationsUseCase(repo)

        useCase(pageNumber = 3)

        assertEquals(3, repo.lastPageNumber)
    }

    private fun fakeRepo(result: Result<List<Evaluation>>) = object : EvaluationRepository {
        override suspend fun listLatestEvaluations(pageNumber: Int) = result
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private class RecordingFakeRepo : EvaluationRepository {
        var lastPageNumber: Int = -1
        override suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> {
            lastPageNumber = pageNumber
            return Result.success(emptyList())
        }
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private fun evaluation(name: String = "App", packageName: String = "com.app") = Evaluation(
        name = name, packageName = packageName, iconUrl = null,
        rating = 1, microg = 1, secure = 3,
        updatedAt = Date(), createdAt = Date(), publishedAt = null, versionName = null
    )
}
