package com.klee.sapio.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CompatibilityCheckScheduler {
    private const val UNIQUE_WORK_NAME = "compatibility_check"
    private const val UNIQUE_WORK_NOW_NAME = "compatibility_check_now"
    private const val REPEAT_INTERVAL_DAYS = 7L
    private const val FLEX_INTERVAL_DAYS = 1L

    fun schedule(context: Context) {
        if (!WorkManager.isInitialized()) {
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<CompatibilityCheckWorker>(
            REPEAT_INTERVAL_DAYS,
            TimeUnit.DAYS,
            FLEX_INTERVAL_DAYS,
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun runNow(context: Context) {
        if (!WorkManager.isInitialized()) {
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = androidx.work.OneTimeWorkRequestBuilder<CompatibilityCheckWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_WORK_NOW_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                request
            )
    }
}
