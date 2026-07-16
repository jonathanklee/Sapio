package com.klee.sapio.data.system

import android.content.Context
import android.content.pm.PackageManager
import com.klee.sapio.data.attestation.HardwareAttestationClient
import com.klee.sapio.domain.DeviceInfo
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType
import com.klee.sapio.domain.model.VerifiedBootState
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DeviceConfiguration @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val attestationClient: HardwareAttestationClient = HardwareAttestationClient()
) : DeviceInfo {
    companion object {
        const val GMS_SERVICES_PACKAGE_NAME = "com.google.android.gms"
    }

    private var packageManager: PackageManager = mContext.packageManager

    override fun getGmsType(): Int = cachedGmsType

    private val cachedGmsType: Int by lazy { computeGmsType() }

    private val cachedBootloaderState: VerifiedBootState by lazy {
        attestationClient.readVerifiedBootState()
    }

    private fun computeGmsType(): Int {
        val apps = try {
            packageManager.getInstalledApplications(0)
        } catch (e: RuntimeException) {
            return GmsType.BARE_AOSP
        }
        val gmsApp = apps.firstOrNull { it.packageName == GMS_SERVICES_PACKAGE_NAME }
            ?: return GmsType.BARE_AOSP

        return try {
            if (packageManager.getApplicationLabel(gmsApp).toString().contains("Google", true)) {
                GmsType.GOOGLE_PLAY_SERVICES
            } else {
                GmsType.MICROG
            }
        } catch (e: RuntimeException) {
            GmsType.BARE_AOSP
        }
    }

    override fun isPermissive(): Int {
        return if (isRooted() && !isBootloaderLocked()) {
            UserType.PERMISSIVE
        } else {
            UserType.STANDARD
        }
    }

    protected open fun isRooted(): Boolean = RootBeer(mContext).isRooted

    protected open fun isBootloaderLocked(): Boolean {
        return cachedBootloaderState == VerifiedBootState.GREEN ||
            cachedBootloaderState == VerifiedBootState.YELLOW
    }
}
