package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepository
import javax.inject.Inject

class FetchAppBareAospRootEvaluationUseCase @Inject constructor() {

    @Inject
    lateinit var evaluationRepository: EvaluationRepository

    suspend operator fun invoke(packageName: String): Evaluation? {
        return evaluationRepository.fetchBareAospRootEvaluation(packageName)
    }
}
