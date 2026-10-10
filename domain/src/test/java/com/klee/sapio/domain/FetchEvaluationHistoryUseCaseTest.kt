package com.klee.sapio.domain

import com.klee.sapio.domain.model.Environment
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.domain.model.UserType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FetchEvaluationHistoryUseCaseTest {

    @Test
    fun `evaluations are grouped by environment`() = runTest {
        val useCase = FetchEvaluationHistoryUseCase(
            repository(
                listOf(
                    evaluation("microg-standard", GmsType.MICROG, UserType.STANDARD, Date(1_000L)),
                    evaluation("bare-standard", GmsType.BARE_AOSP, UserType.STANDARD, Date(1_000L)),
                    evaluation("microg-permissive", GmsType.MICROG, UserType.PERMISSIVE, Date(1_000L))
                )
            )
        )

        val histories = useCase("pkg").getOrThrow()

        assertEquals(3, histories.size)
        assertEquals(
            "microg-standard",
            histories[Environment(GmsType.MICROG, UserType.STANDARD)]?.current?.name
        )
        assertEquals(
            "bare-standard",
            histories[Environment(GmsType.BARE_AOSP, UserType.STANDARD)]?.current?.name
        )
    }

    @Test
    fun `the most recent evaluation of an environment is the current one`() = runTest {
        val useCase = FetchEvaluationHistoryUseCase(
            repository(
                listOf(
                    evaluation("older", GmsType.MICROG, UserType.STANDARD, Date(1_000L)),
                    evaluation("newer", GmsType.MICROG, UserType.STANDARD, Date(2_000L))
                )
            )
        )

        val history = useCase("pkg").getOrThrow()[Environment(GmsType.MICROG, UserType.STANDARD)]

        assertEquals("newer", history?.current?.name)
        assertTrue(history!!.hasTrend)
    }

    @Test
    fun `no evaluation yields no environment`() = runTest {
        val histories = FetchEvaluationHistoryUseCase(repository(emptyList()))("pkg").getOrThrow()

        assertTrue(histories.isEmpty())
    }

    @Test
    fun `a repository failure is propagated`() = runTest {
        val repository = object : NoopRepository() {
            override suspend fun fetchEvaluationHistory(packageName: String) =
                Result.failure<List<Evaluation>>(IllegalStateException("offline"))
        }

        val result = FetchEvaluationHistoryUseCase(repository)("pkg")

        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
    }

    private fun repository(evaluations: List<Evaluation>) = object : NoopRepository() {
        override suspend fun fetchEvaluationHistory(packageName: String) = Result.success(evaluations)
    }

    private fun evaluation(name: String, microg: Int, secure: Int, updatedAt: Date) = Evaluation(
        name = name,
        packageName = "pkg",
        iconUrl = null,
        rating = 1,
        microg = microg,
        secure = secure,
        updatedAt = updatedAt,
        createdAt = null,
        publishedAt = null,
        versionName = null,
        brokenFeatures = null
    )

    private open class NoopRepository : EvaluationRepository {
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) =
            Result.success(null)
        override suspend fun fetchEvaluationHistory(packageName: String) = Result.success(emptyList<Evaluation>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }
}
