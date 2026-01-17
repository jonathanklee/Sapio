package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import javax.inject.Inject

open class FetchAppBareAospSecureEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(packageName: String): Evaluation? {
        return evaluationRepository.fetchBareAospSecureEvaluation(packageName)
    }
}
