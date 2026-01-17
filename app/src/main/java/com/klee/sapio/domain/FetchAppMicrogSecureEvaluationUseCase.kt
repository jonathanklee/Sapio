package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import javax.inject.Inject

open class FetchAppMicrogSecureEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(packageName: String): Evaluation? {
        return evaluationRepository.fetchMicrogSecureEvaluation(packageName)
    }
}
