package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryImpl
import javax.inject.Inject

open class SearchEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepositoryImpl

    open suspend operator fun invoke(pattern: String): List<Evaluation> {
        return evaluationRepository.searchEvaluations(pattern)
    }
}
