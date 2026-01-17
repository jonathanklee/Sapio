package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import javax.inject.Inject

open class ListLatestEvaluationsUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(pageNumber: Int): List<Evaluation> {
        return evaluationRepository.listLatestEvaluations(pageNumber)
    }
}
