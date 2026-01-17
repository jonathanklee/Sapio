package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation

interface EvaluationRepository {

    suspend fun listLatestEvaluations(pageNumber: Int): List<Evaluation>

    suspend fun searchEvaluations(pattern: String): List<Evaluation>

    suspend fun addEvaluation(evaluation: UploadEvaluation)

    suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int)

    suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Evaluation?

    suspend fun existingEvaluations(packageName: String): List<EvaluationRecord>

    suspend fun uploadIcon(app: InstalledApplication): List<Icon>?

    suspend fun existingIcon(iconName: String): List<Icon>

    suspend fun deleteIcon(id: Int)
}
