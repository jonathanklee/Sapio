package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import javax.inject.Inject

class FetchAppBareAospRiskyEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    suspend operator fun invoke(packageName: String): Evaluation? {
        return evaluationRepository.fetchBareAospRiskyEvaluation(packageName)
    }
}
