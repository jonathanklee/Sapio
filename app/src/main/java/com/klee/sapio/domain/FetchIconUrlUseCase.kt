package com.klee.sapio.domain

import javax.inject.Inject

open class FetchIconUrlUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(packageName: String): Result<String> {
        return evaluationRepository.existingIcon("$packageName.png")
            .map { icons -> icons.firstOrNull()?.url.orEmpty() }
    }
}
