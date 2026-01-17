package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import javax.inject.Inject

open class ListLatestEvaluationsUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(pageNumber: Int): List<Evaluation> {
        return evaluationRepository.listLatestEvaluations(pageNumber)
    }
}
