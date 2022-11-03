package com.klee.sapio.data

import android.graphics.drawable.Drawable
import com.klee.sapio.domain.EvaluationRepository
import retrofit2.Response
import javax.inject.Inject

class EvaluationRepositoryStrapi @Inject constructor() : EvaluationRepository {

    @Inject
    lateinit var retrofitService: EvaluationService

    override fun isAvailable(): Boolean {
        return retrofitService.hasConnectivity()
    }

    override suspend fun getEvaluations(): List<Evaluation> {
        return retrofitService.getAllEvaluations()
    }

    override suspend fun searchEvaluations(pattern: String): List<Evaluation> {
        return retrofitService.searchEvaluation(pattern)
    }

    override suspend fun addEvaluation(evaluation: UploadEvaluation) {
        val header = UploadEvaluationHeader(evaluation)
        retrofitService.addEvaluation(header)
    }

    override suspend fun updateEvaluation(evaluation: UploadEvaluation, id: Int) {
        val header = UploadEvaluationHeader(evaluation)
        retrofitService.updateEvaluation(header, id)
    }

    suspend fun getEvaluationsRawData(): List<StrapiElement> {
        return retrofitService.getEvaluationsRawData()
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>>? {
        return retrofitService.uploadIcon(icon)
    }
}
