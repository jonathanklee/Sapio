package com.klee.sapio.model

import android.graphics.drawable.Drawable
import retrofit2.Response
import javax.inject.Inject

class EvaluationRepository @Inject constructor() {

    @Inject
    lateinit var retrofitService: EvaluationService

    fun isAvailable(): Boolean {
        return retrofitService.hasConnectivity()
    }

    suspend fun getEvaluations(): List<Evaluation> {
        return retrofitService.getAllEvaluations()
    }

    suspend fun getEvaluationsRawData(): List<StrapiElement> {
        return retrofitService.getEvaluationsRawData()
    }

    suspend fun searchEvaluations(pattern: String): List<Evaluation> {
        return retrofitService.searchEvaluation(pattern)
    }

    suspend fun addEvaluation(app: UploadEvaluation): Response<UploadAnswer> {
        return retrofitService.addEvaluation(app)
    }

    suspend fun updateEvaluation(app: UploadEvaluation, id: Int): Response<UploadAnswer> {
        return retrofitService.updateEvaluation(app, id)
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>> {
        return retrofitService.uploadIcon(icon)
    }
}
