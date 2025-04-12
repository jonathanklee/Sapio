package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryImpl
import com.klee.sapio.data.Settings
import javax.inject.Inject

class ListLatestEvaluationsUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepositoryImpl

    @Inject
    lateinit var settings: Settings

    suspend operator fun invoke(pageNumber: Int): List<Evaluation> {
        return evaluationRepository.listLatestEvaluations(pageNumber)
    }
}
