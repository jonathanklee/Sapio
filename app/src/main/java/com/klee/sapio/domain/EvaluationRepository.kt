package com.klee.sapio.domain

import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.StrapiElement
import com.klee.sapio.data.UploadEvaluation
import com.klee.sapio.data.IconAnswer
import retrofit2.Response

interface EvaluationRepository {

    suspend fun listLatestEvaluations(): List<Evaluation>

    suspend fun searchEvaluations(pattern: String): List<Evaluation>

    suspend fun addEvaluation(evaluation: UploadEvaluation)

    suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int)

    suspend fun fetchMicrogUserEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchMicrogRootEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospUserEvaluation(appPackageName: String): Evaluation?

    suspend fun fetchBareAospRootEvaluation(appPackageName: String): Evaluation?

    suspend fun existingEvaluations(packageName: String): List<StrapiElement>

    suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<IconAnswer>>?

    suspend fun existingIcon(iconName: String): List<IconAnswer>

    suspend fun deleteIcon(id: Int): Response<IconAnswer>?
}
