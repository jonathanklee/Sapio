package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.UploadEvaluation

interface EvaluationRepository {

    fun isAvailable(): Boolean

    suspend fun listLatestEvaluations(): List<Evaluation>

    suspend fun searchEvaluations(pattern: String): List<Evaluation>

    suspend fun addEvaluation(evaluation: UploadEvaluation)

    suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int)

    suspend fun fetchMicrogUserEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchMicrogRootEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospUserEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospRootEvaluation(appPackageName: String): Evaluation?
}
