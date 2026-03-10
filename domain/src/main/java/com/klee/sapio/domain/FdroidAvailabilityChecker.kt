package com.klee.sapio.domain

interface FdroidAvailabilityChecker {
    suspend fun isAvailable(packageName: String): Boolean
}
