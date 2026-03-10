package com.klee.sapio.domain

import javax.inject.Inject

open class CheckFdroidAvailabilityUseCase @Inject constructor(
    private val fdroidAvailabilityChecker: FdroidAvailabilityChecker
) {
    open suspend operator fun invoke(packageName: String): Boolean {
        return fdroidAvailabilityChecker.isAvailable(packageName)
    }
}
