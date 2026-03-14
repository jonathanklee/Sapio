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

class SearchEvaluationUseCaseTest {

    @Test
    fun `returns list from repository on success`() = runTest {
        val evaluations = listOf(evaluation("App A"), evaluation("App B"))
        val useCase = SearchEvaluationUseCase(fakeRepo(Result.success(evaluations)))

        val result = useCase("app")

        assertTrue(result.isSuccess)
        assertEquals(evaluations, result.getOrNull())
    }

    @Test
    fun `returns empty list when no results`() = runTest {
        val useCase = SearchEvaluationUseCase(fakeRepo(Result.success(emptyList())))

        val result = useCase("unknown")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        val error = RuntimeException("network error")
        val useCase = SearchEvaluationUseCase(fakeRepo(Result.failure(error)))

        val result = useCase("app")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `passes pattern to repository`() = runTest {
        val repo = RecordingFakeRepo()
        val useCase = SearchEvaluationUseCase(repo)

        useCase("firefox")

        assertEquals("firefox", repo.lastPattern)
    }

    private fun fakeRepo(result: Result<List<Evaluation>>) = object : EvaluationRepository {
        override suspend fun searchEvaluations(pattern: String) = result
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private class RecordingFakeRepo : EvaluationRepository {
        var lastPattern: String = ""
        override suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>> {
            lastPattern = pattern
            return Result.success(emptyList())
        }
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private fun evaluation(name: String = "App") = Evaluation(
        name = name, packageName = "com.${name.lowercase()}", iconUrl = null,
        rating = 1, microg = 1, secure = 3,
        updatedAt = Date(), createdAt = null, publishedAt = null, versionName = null
    )
}
