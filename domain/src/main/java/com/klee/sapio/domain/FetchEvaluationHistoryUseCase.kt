package com.klee.sapio.domain

import com.klee.sapio.domain.model.Environment
import com.klee.sapio.domain.model.EvaluationHistory
import javax.inject.Inject

open class FetchEvaluationHistoryUseCase @Inject constructor(
    private val evaluationRepository: EvaluationRepository
) {

    open suspend operator fun invoke(packageName: String): Result<Map<Environment, EvaluationHistory>> {
        return evaluationRepository.fetchEvaluationHistory(packageName).map { evaluations ->
            evaluations
                .groupBy { Environment(it.microg, it.secure) }
                .mapNotNull { (environment, group) ->
                    EvaluationHistory.of(group)?.let { environment to it }
                }
                .toMap()
        }
    }
}
