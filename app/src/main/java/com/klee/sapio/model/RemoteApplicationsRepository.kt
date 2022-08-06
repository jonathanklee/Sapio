package com.klee.sapio.model

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import javax.inject.Inject

class RemoteApplicationsRepository @Inject constructor() {

    @VisibleForTesting
    var query: ParseQuery<ParseObject> = ParseQuery.getQuery("LibreApps")


    private lateinit var feedApplications: List<RemoteApplication>
    private lateinit var foundApplications: List<RemoteApplication>
    private val retrofitService = ApplicationService()

    suspend fun getApplicationsFromStrapi(): List<RemoteApplication> {
        return retrofitService.getRemoteApplications()
    }

    suspend fun getFeedApplications(): List<RemoteApplication> {
        withContext(Dispatchers.IO) {
            query.orderByDescending("updatedAt")
            //feedApplications = createApplicationList(query.find())
        }
        return feedApplications
    }

    suspend fun searchApplications(pattern: String): List<RemoteApplication> {
        withContext(Dispatchers.IO) {
            query.whereMatches("name", pattern, "i")
            //foundApplications = createApplicationList(query.find())
        }
        return foundApplications
    }

    /*private fun createApplicationList(parseObjectList: List<ParseObject>): List<RemoteApplication> {
        val resultList = mutableListOf<RemoteApplication>()
        for (element in parseObjectList) {
            val remoteApplication = RemoteApplication(
                element.getString("name")!!,
                element.getString("package")!!,
                element.getParseFile("icon")?.url,
                element.getInt("rating"),
                element.getInt("microg"),
                element.getInt("rooted"),
                element.updatedAt
            )

            resultList.add(remoteApplication)
        }

        return resultList
    }*/
}