package com.klee.sapio.domain

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationRecord
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation

interface EvaluationRepository {

    suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>>

    suspend fun searchEvaluations(pattern: String): Result<List<Evaluation>>

    suspend fun addEvaluation(evaluation: UploadEvaluation): Result<Unit>

    suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int): Result<Unit>

    suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int): Result<Evaluation?>

    suspend fun existingEvaluations(packageName: String): Result<List<EvaluationRecord>>

    suspend fun uploadIcon(app: InstalledApplication): Result<List<Icon>>

    suspend fun existingIcon(iconName: String): Result<List<Icon>>

    suspend fun deleteIcon(id: Int): Result<Unit>
}
