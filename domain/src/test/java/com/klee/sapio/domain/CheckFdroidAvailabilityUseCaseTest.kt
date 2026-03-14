package com.klee.sapio.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckFdroidAvailabilityUseCaseTest {

    @Test
    fun `returns true when checker reports available`() = runTest {
        val useCase = CheckFdroidAvailabilityUseCase(FakeFdroidChecker(available = true))

        assertTrue(useCase("com.test.app"))
    }

    @Test
    fun `returns false when checker reports unavailable`() = runTest {
        val useCase = CheckFdroidAvailabilityUseCase(FakeFdroidChecker(available = false))

        assertFalse(useCase("com.test.app"))
    }

    @Test
    fun `passes package name to checker`() = runTest {
        val checker = RecordingFdroidChecker()
        val useCase = CheckFdroidAvailabilityUseCase(checker)

        useCase("org.mozilla.firefox")

        assertTrue(checker.lastCheckedPackage == "org.mozilla.firefox")
    }

    private class FakeFdroidChecker(private val available: Boolean) : FdroidAvailabilityChecker {
        override suspend fun isAvailable(packageName: String) = available
    }

    private class RecordingFdroidChecker : FdroidAvailabilityChecker {
        var lastCheckedPackage: String = ""
        override suspend fun isAvailable(packageName: String): Boolean {
            lastCheckedPackage = packageName
            return false
        }
    }
}
