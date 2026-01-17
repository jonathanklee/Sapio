package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import javax.inject.Inject

open class FetchAppMicrogSecureEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(packageName: String): Result<Evaluation?> {
        return evaluationRepository.fetchMicrogSecureEvaluation(packageName)
    }
}
