package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import javax.inject.Inject

open class SearchEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(pattern: String): Result<List<Evaluation>> {
        return evaluationRepository.searchEvaluations(pattern)
    }
}
