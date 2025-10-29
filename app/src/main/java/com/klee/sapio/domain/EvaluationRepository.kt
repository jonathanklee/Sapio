package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.IconAnswer
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.StrapiElement
import com.klee.sapio.data.UploadEvaluation
import retrofit2.Response

interface EvaluationRepository {

    suspend fun listLatestEvaluations(pageNumber: Int): List<Evaluation>

    suspend fun searchEvaluations(pattern: String): List<Evaluation>

    suspend fun addEvaluation(evaluation: UploadEvaluation)

    suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int)

    suspend fun fetchMicrogSecureEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchMicrogRiskyEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospSecureEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospRiskyEvaluation(appPackageName: String): Evaluation?

    suspend fun existingEvaluations(packageName: String): List<StrapiElement>

    suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<IconAnswer>>?

    suspend fun existingIcon(iconName: String): List<IconAnswer>

    suspend fun deleteIcon(id: Int): Response<IconAnswer>?
}
