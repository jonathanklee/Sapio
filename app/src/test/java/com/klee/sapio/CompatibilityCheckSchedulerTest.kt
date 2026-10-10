package com.klee.sapio

import android.os.Build
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.klee.sapio.work.CompatibilityCheckScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CompatibilityCheckSchedulerTest {

    private val context = RuntimeEnvironment.getApplication()

    private fun initialiseWorkManager() {
        val configuration = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun `schedule enqueues a unique periodic check`() {
        initialiseWorkManager()

        CompatibilityCheckScheduler.schedule(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
            .get()

        assertEquals(1, work.size)
        assertEquals(WorkInfo.State.ENQUEUED, work.single().state)
    }

    @Test
    fun `scheduling twice keeps a single periodic check`() {
        initialiseWorkManager()

        CompatibilityCheckScheduler.schedule(context)
        CompatibilityCheckScheduler.schedule(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
            .get()

        assertEquals(1, work.size)
    }

    @Test
    fun `the periodic check waits for a network`() {
        initialiseWorkManager()

        CompatibilityCheckScheduler.schedule(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(PERIODIC_WORK_NAME)
            .get()
            .single()

        assertEquals(NetworkType.CONNECTED, work.constraints.requiredNetworkType)
    }

    @Test
    fun `runNow enqueues a one time check`() {
        initialiseWorkManager()

        CompatibilityCheckScheduler.runNow(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ONE_TIME_WORK_NAME)
            .get()

        assertEquals(1, work.size)
        assertEquals(NetworkType.CONNECTED, work.single().constraints.requiredNetworkType)
    }

    @Test
    fun `runNow replaces a previous one time check`() {
        initialiseWorkManager()

        CompatibilityCheckScheduler.runNow(context)
        CompatibilityCheckScheduler.runNow(context)

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ONE_TIME_WORK_NAME)
            .get()

        assertTrue(work.count { it.state == WorkInfo.State.ENQUEUED } <= 1)
    }

    private companion object {
        const val PERIODIC_WORK_NAME = "compatibility_check"
        const val ONE_TIME_WORK_NAME = "compatibility_check_now"
    }
}
