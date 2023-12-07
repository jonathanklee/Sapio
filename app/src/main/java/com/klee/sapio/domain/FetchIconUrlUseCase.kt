package com.klee.sapio.domain

import javax.inject.Inject

class FetchIconUrlUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    suspend operator fun invoke(packageName: String): String {
        val icons = evaluationRepository.existingIcon("$packageName.png")
        if (icons.isEmpty()) {
            return ""
        }

        return icons[0].url
    }
}
