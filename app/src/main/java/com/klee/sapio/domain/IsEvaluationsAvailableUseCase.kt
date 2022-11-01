package com.klee.sapio.domain

import com.klee.sapio.data.EvaluationRepositoryStrapi
import javax.inject.Inject

class IsEvaluationsAvailableUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepositoryStrapi

    operator fun invoke(): Boolean {
        return evaluationRepository.isAvailable()
    }
}