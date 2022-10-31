package com.klee.sapio.model

import android.graphics.drawable.Drawable
import retrofit2.Response
import javax.inject.Inject

class RemoteEvaluationRepository @Inject constructor() {

    private val retrofitService = EvaluationService()

    suspend fun getApplicationsFromStrapi(): List<RemoteEvaluation> {
        return retrofitService.getAllEvaluations()
    }

    suspend fun getApplicationRawData(): List<StrapiElement> {
        return retrofitService.getEvaluationsRawData()
    }

    suspend fun searchApplicationsFromStrapi(pattern: String): List<RemoteEvaluation> {
        return retrofitService.searchEvaluation(pattern)
    }

    suspend fun addApplication(app: UploadEvaluation): Response<UploadAnswer> {
        return retrofitService.addEvaluation(app)
    }

    suspend fun updateApplication(app: UploadEvaluation, id: Int): Response<UploadAnswer> {
        return retrofitService.updateEvaluation(app, id)
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>> {
        return retrofitService.uploadIcon(icon)
    }
}
