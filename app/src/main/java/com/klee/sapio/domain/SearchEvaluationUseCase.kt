package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryImpl
import javax.inject.Inject

class SearchEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepositoryImpl

    suspend operator fun invoke(pattern: String): List<Evaluation> {
        return evaluationRepository.searchEvaluations(pattern)
    }
}
