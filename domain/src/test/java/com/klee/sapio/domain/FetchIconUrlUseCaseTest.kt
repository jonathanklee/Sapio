package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.UploadEvaluation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchIconUrlUseCaseTest {

    @Test
    fun `returns url of first icon when found`() = runTest {
        val icon = Icon(id = 1, name = "com.test.app.png", url = "http://icon/test.png")
        val useCase = FetchIconUrlUseCase(fakeRepo(Result.success(listOf(icon))))

        val result = useCase("com.test.app")

        assertTrue(result.isSuccess)
        assertEquals("http://icon/test.png", result.getOrNull())
    }

    @Test
    fun `returns empty string when icon list is empty`() = runTest {
        val useCase = FetchIconUrlUseCase(fakeRepo(Result.success(emptyList())))

        val result = useCase("com.test.app")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun `appends png extension when querying`() = runTest {
        val repo = RecordingFakeRepo()
        val useCase = FetchIconUrlUseCase(repo)

        useCase("com.test.app")

        assertEquals("com.test.app.png", repo.lastIconName)
    }

    @Test
    fun `returns url of first icon when multiple icons exist`() = runTest {
        val icons = listOf(
            Icon(id = 1, name = "com.test.app.png", url = "http://icon/first.png"),
            Icon(id = 2, name = "com.test.app.png", url = "http://icon/second.png")
        )
        val useCase = FetchIconUrlUseCase(fakeRepo(Result.success(icons)))

        val result = useCase("com.test.app")

        assertEquals("http://icon/first.png", result.getOrNull())
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        val error = RuntimeException("network error")
        val useCase = FetchIconUrlUseCase(fakeRepo(Result.failure(error)))

        val result = useCase("com.test.app")

        assertTrue(result.isFailure)
    }

    private fun fakeRepo(result: Result<List<Icon>>) = object : EvaluationRepository {
        override suspend fun existingIcon(iconName: String) = result
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private class RecordingFakeRepo : EvaluationRepository {
        var lastIconName: String = ""
        override suspend fun existingIcon(iconName: String): Result<List<Icon>> {
            lastIconName = iconName
            return Result.success(emptyList())
        }
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) = Result.success(null)
        override suspend fun existingEvaluations(packageName: String) = Result.success(emptyList<EvaluationRecord>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }
}
