package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepository
import javax.inject.Inject

class SearchEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    suspend operator fun invoke(pattern: String): List<Evaluation> {
        return evaluationRepository.searchEvaluations(pattern)
    }
}