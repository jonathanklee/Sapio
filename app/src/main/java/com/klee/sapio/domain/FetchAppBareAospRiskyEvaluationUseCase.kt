package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import javax.inject.Inject

open class FetchAppBareAospRiskyEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    open suspend operator fun invoke(packageName: String): Evaluation? {
        return evaluationRepository.fetchBareAospRiskyEvaluation(packageName)
    }
}
