package com.klee.sapio

import com.klee.sapio.domain.FetchAppBareAospRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospSecureEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogSecureEvaluationUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.Icon
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

    private lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase
    private lateinit var fetchMicrogSecure: FetchAppMicrogSecureEvaluationUseCase
    private lateinit var fetchMicrogRisky: FetchAppMicrogRiskyEvaluationUseCase
    private lateinit var fetchBareSecure: FetchAppBareAospSecureEvaluationUseCase
    private lateinit var fetchBareRisky: FetchAppBareAospRiskyEvaluationUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fetchIconUrlUseCase = FetchIconUrlUseCase(evaluationRepository)
        fetchMicrogSecure = FetchAppMicrogSecureEvaluationUseCase(evaluationRepository)
        fetchMicrogRisky = FetchAppMicrogRiskyEvaluationUseCase(evaluationRepository)
        fetchBareSecure = FetchAppBareAospSecureEvaluationUseCase(evaluationRepository)
        fetchBareRisky = FetchAppBareAospRiskyEvaluationUseCase(evaluationRepository)
    }

    @Test
    fun `fetch icon url returns first url when icons exist`() = runTest {
        val icon = Icon(
            id = 1,
            name = "icon.png",
            url = "https://example.com/icon.png"
        )

        `when`(evaluationRepository.existingIcon("com.test.png")).thenReturn(Result.success(listOf(icon)))

        val result = fetchIconUrlUseCase("com.test")

        assertEquals("https://example.com/icon.png", result.getOrNull())
        verify(evaluationRepository).existingIcon("com.test.png")
    }

    @Test
    fun `fetch icon url returns empty string when no icons`() = runTest {
        `when`(evaluationRepository.existingIcon("com.empty.png")).thenReturn(Result.success(emptyList()))

        val result = fetchIconUrlUseCase("com.empty")

        assertEquals("", result.getOrNull())
        verify(evaluationRepository).existingIcon("com.empty.png")
    }

    @Test
    fun `fetch microg secure delegates to repository`() = runTest {
        val expected = dummyEvaluation("microg.secure")
        `when`(evaluationRepository.fetchMicrogSecureEvaluation("pkg")).thenReturn(Result.success(expected))

        val result = fetchMicrogSecure("pkg")

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchMicrogSecureEvaluation("pkg")
    }

    @Test
    fun `fetch microg risky delegates to repository`() = runTest {
        val expected = dummyEvaluation("microg.risky")
        `when`(evaluationRepository.fetchMicrogRiskyEvaluation("pkg")).thenReturn(Result.success(expected))

        val result = fetchMicrogRisky("pkg")

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchMicrogRiskyEvaluation("pkg")
    }

    @Test
    fun `fetch bare aosp secure delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.secure")
        `when`(evaluationRepository.fetchBareAospSecureEvaluation("pkg")).thenReturn(Result.success(expected))

        val result = fetchBareSecure("pkg")

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchBareAospSecureEvaluation("pkg")
    }

    @Test
    fun `fetch bare aosp risky delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.risky")
        `when`(evaluationRepository.fetchBareAospRiskyEvaluation("pkg")).thenReturn(Result.success(expected))

        val result = fetchBareRisky("pkg")

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchBareAospRiskyEvaluation("pkg")
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
        versionName = null
    )
}
