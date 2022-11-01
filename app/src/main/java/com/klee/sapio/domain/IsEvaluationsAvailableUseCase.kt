package com.klee.sapio.domain

import com.klee.sapio.data.EvaluationRepository
import javax.inject.Inject

class IsEvaluationsAvailableUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    operator fun invoke(): Boolean {
        return evaluationRepository.isAvailable()
    }
}