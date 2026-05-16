package com.klee.sapio.data

import com.klee.sapio.data.fdroid.CachedFdroidAvailabilityChecker
import com.klee.sapio.data.fdroid.OkHttpFdroidAvailabilityChecker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CachedFdroidAvailabilityCheckerTest {

    @Mock
    private lateinit var delegate: OkHttpFdroidAvailabilityChecker

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `returns true when delegate reports available`() = runTest {
        `when`(delegate.isAvailable("com.test.app")).thenReturn(true)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        assertTrue(checker.isAvailable("com.test.app")!!)
    }

    @Test
    fun `returns false when delegate reports unavailable`() = runTest {
        `when`(delegate.isAvailable("com.test.app")).thenReturn(false)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        assertFalse(checker.isAvailable("com.test.app")!!)
    }

    @Test
    fun `returns null when delegate reports network error`() = runTest {
        `when`(delegate.isAvailable("com.test.app")).thenReturn(null)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        assertNull(checker.isAvailable("com.test.app"))
    }

    @Test
    fun `does not cache network errors - retries delegate on next call`() = runTest {
        `when`(delegate.isAvailable("com.test.app"))
            .thenReturn(null)
            .thenReturn(true)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        checker.isAvailable("com.test.app")
        val second = checker.isAvailable("com.test.app")

        verify(delegate, times(2)).isAvailable("com.test.app")
        assertTrue(second!!)
    }

    @Test
    fun `caches true results - does not call delegate again`() = runTest {
        `when`(delegate.isAvailable("com.test.app")).thenReturn(true)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        checker.isAvailable("com.test.app")
        checker.isAvailable("com.test.app")

        verify(delegate, times(1)).isAvailable("com.test.app")
    }

    @Test
    fun `caches false results - does not call delegate again`() = runTest {
        `when`(delegate.isAvailable("com.test.app")).thenReturn(false)
        val checker = CachedFdroidAvailabilityChecker(delegate)

        checker.isAvailable("com.test.app")
        checker.isAvailable("com.test.app")

        verify(delegate, times(1)).isAvailable("com.test.app")
    }
}
