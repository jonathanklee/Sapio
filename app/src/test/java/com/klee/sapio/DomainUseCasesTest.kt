package com.klee.sapio

import com.klee.sapio.domain.FetchAppEvaluationUseCase
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class DomainUseCasesTest {

    @Mock
    lateinit var evaluationRepository: EvaluationRepository

    private lateinit var fetchEvaluationUseCase: FetchAppEvaluationUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fetchEvaluationUseCase = FetchAppEvaluationUseCase(evaluationRepository)
    }

    @Test
    fun `fetch microg secure delegates to repository`() = runTest {
        val expected = dummyEvaluation("microg.secure")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.MICROG, UserType.STANDARD))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.MICROG, UserType.STANDARD)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.MICROG, UserType.STANDARD)
    }

    @Test
    fun `fetch microg unsafe delegates to repository`() = runTest {
        val expected = dummyEvaluation("microg.unsafe")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.MICROG, UserType.PERMISSIVE))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.MICROG, UserType.PERMISSIVE)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.MICROG, UserType.PERMISSIVE)
    }

    @Test
    fun `fetch bare aosp secure delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.secure")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.STANDARD))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.BARE_AOSP, UserType.STANDARD)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.STANDARD)
    }

    @Test
    fun `fetch bare aosp unsafe delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.unsafe")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.PERMISSIVE))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.BARE_AOSP, UserType.PERMISSIVE)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.PERMISSIVE)
    }

    private fun dummyEvaluation(name: String) = Evaluation(
        name = name,
        packageName = "pkg",
        iconUrl = null,
        rating = 1,
        microg = 1,
        secure = 1,
        updatedAt = null,
        createdAt = null,
        publishedAt = null,
        versionName = null,
        brokenFeatures = null
    )
}
