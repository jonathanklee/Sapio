package com.klee.sapio.model

import android.graphics.drawable.Drawable
import androidx.annotation.VisibleForTesting
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class RemoteEvaluationRepository @Inject constructor() {

    @VisibleForTesting
    var query: ParseQuery<ParseObject> = ParseQuery.getQuery("LibreApps")

    private lateinit var feedEvaluations: List<RemoteEvaluation>
    private lateinit var foundEvaluations: List<RemoteEvaluation>
    private val retrofitService = ApplicationService()

    suspend fun getApplicationsFromStrapi(): List<RemoteEvaluation> {
        return retrofitService.getAllApplications()
    }

    suspend fun getApplicationRawData(): List<StrapiElement> {
        return retrofitService.getApplicationsRawData()
    }

    suspend fun searchApplicationsFromStrapi(pattern: String): List<RemoteEvaluation> {
        return retrofitService.searchApplication(pattern)
    }

    suspend fun getFeedApplications(): List<RemoteEvaluation> {
        withContext(Dispatchers.IO) {
            query.orderByDescending("updatedAt")
        }
        return feedEvaluations
    }

    suspend fun searchApplications(pattern: String): List<RemoteEvaluation> {
        withContext(Dispatchers.IO) {
            query.whereMatches("name", pattern, "i")
        }
        return foundEvaluations
    }

    suspend fun addApplication(app: UploadEvaluation): Response<UploadAnswer> {
        return retrofitService.addApplication(app)
    }

    suspend fun updateApplication(app: UploadEvaluation, id: Int): Response<UploadAnswer> {
        return retrofitService.updateApplication(app, id)
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>> {
        return retrofitService.uploadIcon(icon)
    }
}
