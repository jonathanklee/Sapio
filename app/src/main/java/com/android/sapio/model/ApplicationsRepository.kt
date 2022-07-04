package com.android.sapio.model

import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationsRepository @Inject constructor() {

    private lateinit var _applications: List<Application>
    var applications: List<Application>
        get() = _applications
        set(value) {
            _applications = value
        }

    private lateinit var _foundApplications: List<Application>
    var foundApplications: List<Application>
        get() = _foundApplications
        set(value) {
            _foundApplications = value
        }

    suspend fun refreshApplications() {
        withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.orderByDescending("updatedAt")
            applications = createApplicationList(query.find())
        }
    }

    suspend fun searchApplications(pattern: String) {
        withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereMatches("name", pattern, "i")
            foundApplications = createApplicationList(query.find())
        }
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