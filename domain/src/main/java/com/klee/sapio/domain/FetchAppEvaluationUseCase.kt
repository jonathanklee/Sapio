package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import javax.inject.Inject

open class FetchAppEvaluationUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(
        packageName: String,
        gmsType: Int,
        userType: Int
    ): Result<Evaluation?> {
        return evaluationRepository.fetchEvaluation(packageName, gmsType, userType)
    }
}
