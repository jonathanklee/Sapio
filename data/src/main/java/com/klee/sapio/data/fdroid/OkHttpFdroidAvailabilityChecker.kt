package com.klee.sapio.data.fdroid

import android.util.Log
import com.klee.sapio.domain.FdroidAvailabilityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class OkHttpFdroidAvailabilityChecker @Inject constructor() : FdroidAvailabilityChecker {

    private val client = OkHttpClient()

    override suspend fun isAvailable(packageName: String): Boolean {
        val request = Request.Builder()
            .url("https://f-droid.org/api/v1/packages/$packageName")
            .build()
        return try {
            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    response.code == HTTP_200_SUCCESS
                }
            }
        } catch (e: IOException) {
            Log.e("OkHttpFdroidChecker", "Failed to reach F-Droid for $packageName", e)
            false
        }
    }

    companion object {
        private const val HTTP_200_SUCCESS = 200
    }
}
