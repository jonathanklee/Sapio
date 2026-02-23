package com.klee.sapio.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.UserType
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.model.Rating
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

class CompatibilityCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CompatibilityWorkerEntryPoint::class.java
        )
        val deviceConfiguration = entryPoint.deviceConfiguration()
        val gmsType = deviceConfiguration.getGmsType()

        if (gmsType == GmsType.GOOGLE_PLAY_SERVICES) {
            return Result.success()
        }

        val installedApps = entryPoint.installedApplicationsRepository()
            .getAppList(applicationContext)

        val evaluationRepository = entryPoint.evaluationRepository()
        val userType = UserType.SECURE

        val badApps = mutableListOf<InstalledApplication>()
        val averageApps = mutableListOf<InstalledApplication>()
        for (app in installedApps) {
            when {
                isBadCompatibility(evaluationRepository, app, gmsType, userType) -> {
                    badApps.add(app)
                }
                isAverageCompatibility(evaluationRepository, app, gmsType, userType) -> {
                    averageApps.add(app)
                }
            }
        }

        if (badApps.isNotEmpty()) {
            val badApp = badApps[Random.nextInt(badApps.size)]
            CompatibilityNotificationManager(applicationContext).show(badApp)
        } else if (averageApps.isNotEmpty()) {
            val averageApp = averageApps[Random.nextInt(averageApps.size)]
            CompatibilityNotificationManager(applicationContext).show(averageApp)
        }

        return Result.success()
    }

    private suspend fun isBadCompatibility(
        evaluationRepository: EvaluationRepository,
        app: InstalledApplication,
        gmsType: Int,
        userType: Int
    ): Boolean {
        val evaluation = when (gmsType) {
            GmsType.MICROG -> {
                if (userType == UserType.RISKY) {
                    evaluationRepository.fetchMicrogRiskyEvaluation(app.packageName).getOrNull()
                } else {
                    evaluationRepository.fetchMicrogSecureEvaluation(app.packageName).getOrNull()
                }
            }
            GmsType.BARE_AOSP -> {
                if (userType == UserType.RISKY) {
                    evaluationRepository.fetchBareAospRiskyEvaluation(app.packageName).getOrNull()
                } else {
                    evaluationRepository.fetchBareAospSecureEvaluation(app.packageName).getOrNull()
                }
            }
            else -> null
        }

        return evaluation?.rating == Rating.BAD
    }

    private suspend fun isAverageCompatibility(
        evaluationRepository: EvaluationRepository,
        app: InstalledApplication,
        gmsType: Int,
        userType: Int
    ): Boolean {
        val evaluation = when (gmsType) {
            GmsType.MICROG -> {
                if (userType == UserType.RISKY) {
                    evaluationRepository.fetchMicrogRiskyEvaluation(app.packageName).getOrNull()
                } else {
                    evaluationRepository.fetchMicrogSecureEvaluation(app.packageName).getOrNull()
                }
            }
            GmsType.BARE_AOSP -> {
                if (userType == UserType.RISKY) {
                    evaluationRepository.fetchBareAospRiskyEvaluation(app.packageName).getOrNull()
                } else {
                    evaluationRepository.fetchBareAospSecureEvaluation(app.packageName).getOrNull()
                }
            }
            else -> null
        }

        return evaluation?.rating == Rating.AVERAGE
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CompatibilityWorkerEntryPoint {
    fun installedApplicationsRepository(): InstalledApplicationsRepository
    fun evaluationRepository(): EvaluationRepository
    fun deviceConfiguration(): DeviceConfiguration
}
