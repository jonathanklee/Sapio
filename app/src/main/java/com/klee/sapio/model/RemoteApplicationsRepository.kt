package com.klee.sapio.model

import androidx.annotation.VisibleForTesting
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteApplicationsRepository @Inject constructor() {

    @VisibleForTesting
    var query: ParseQuery<ParseObject> = ParseQuery.getQuery("LibreApps")

    private lateinit var feedApplications: List<RemoteApplication>
    private lateinit var foundApplications: List<RemoteApplication>
    private val retrofitService = ApplicationService()

    suspend fun getApplicationsFromStrapi(): List<RemoteApplication> {
        return retrofitService.getAllApplications()
    }

    suspend fun searchApplicationsFromStrapi(pattern: String): List<RemoteApplication> {
        return retrofitService.searchApplication(pattern)
    }

    suspend fun getFeedApplications(): List<RemoteApplication> {
        withContext(Dispatchers.IO) {
            query.orderByDescending("updatedAt")
        }
        return feedApplications
    }

    suspend fun searchApplications(pattern: String): List<RemoteApplication> {
        withContext(Dispatchers.IO) {
            query.whereMatches("name", pattern, "i")
        }
        return foundApplications
    }
}