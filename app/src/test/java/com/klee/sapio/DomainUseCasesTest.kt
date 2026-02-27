package com.klee.sapio

import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.UserType
import com.klee.sapio.domain.FetchAppEvaluationUseCase
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
    private lateinit var fetchEvaluationUseCase: FetchAppEvaluationUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fetchIconUrlUseCase = FetchIconUrlUseCase(evaluationRepository)
        fetchEvaluationUseCase = FetchAppEvaluationUseCase(evaluationRepository)
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
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.MICROG, UserType.SECURE))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.MICROG, UserType.SECURE)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.MICROG, UserType.SECURE)
    }

    @Test
    fun `fetch microg risky delegates to repository`() = runTest {
        val expected = dummyEvaluation("microg.risky")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.MICROG, UserType.RISKY))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.MICROG, UserType.RISKY)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.MICROG, UserType.RISKY)
    }

    @Test
    fun `fetch bare aosp secure delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.secure")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.SECURE))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.BARE_AOSP, UserType.SECURE)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.SECURE)
    }

    @Test
    fun `fetch bare aosp risky delegates to repository`() = runTest {
        val expected = dummyEvaluation("bare.risky")
        `when`(evaluationRepository.fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.RISKY))
            .thenReturn(Result.success(expected))

        val result = fetchEvaluationUseCase("pkg", GmsType.BARE_AOSP, UserType.RISKY)

        assertEquals(expected, result.getOrNull())
        verify(evaluationRepository).fetchEvaluation("pkg", GmsType.BARE_AOSP, UserType.RISKY)
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
