package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import javax.inject.Inject

open class SearchEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(pattern: String): List<Evaluation> {
        return evaluationRepository.searchEvaluations(pattern)
    }
}
