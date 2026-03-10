package com.klee.sapio.data.fdroid

import android.util.Log
import com.klee.sapio.domain.FdroidAvailabilityChecker
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class KtorFdroidAvailabilityChecker @Inject constructor() : FdroidAvailabilityChecker {

    companion object {
        private const val HTTP_200_SUCCESS = 200
    }

    private val client = HttpClient(Android) {
        install(HttpTimeout)
    }

    override suspend fun isAvailable(packageName: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                client.get(
                    "https://f-droid.org/api/v1/packages/$packageName"
                ).status.value == HTTP_200_SUCCESS
            }
        } catch (e: IOException) {
            Log.e("KtorFdroidChecker", "Failed to reach F-Droid for $packageName", e)
            false
        }
    }
}
