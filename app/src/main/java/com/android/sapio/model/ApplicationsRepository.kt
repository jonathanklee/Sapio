package com.android.sapio.model

import androidx.annotation.VisibleForTesting
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationsRepository @Inject constructor() {

    @VisibleForTesting
    var query: ParseQuery<ParseObject> = ParseQuery.getQuery("LibreApps")

    private lateinit var applications: List<Application>
    private lateinit var foundApplications: List<Application>

    suspend fun refreshApplications(): List<Application> {
        withContext(Dispatchers.IO) {
            query.orderByDescending("updatedAt")
            applications = createApplicationList(query.find())
        }
        return applications
    }

    suspend fun searchApplications(pattern: String): List<Application> {
        withContext(Dispatchers.IO) {
            query.whereMatches("name", pattern, "i")
            foundApplications = createApplicationList(query.find())
        }
        return foundApplications
    }

    private fun createApplicationList(parseObjectList: List<ParseObject>): List<Application> {
        val resultList = mutableListOf<Application>()
        for (element in parseObjectList) {
            val application = Application(
                element.getString("name")!!,
                element.getString("package")!!,
                element.getParseFile("icon")?.url,
                element.getInt("rating"),
                element.getInt("microg"),
                element.getInt("rooted"),
                element.updatedAt
            )

            resultList.add(application)
        }

        return resultList
    }
}