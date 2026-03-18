package com.klee.sapio

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.UserType
import com.klee.sapio.ui.viewmodel.EvaluateViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class EvaluateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val appContext = RuntimeEnvironment.getApplication()

    @Test
    fun `uiState initializes with device gmsType and userType`() {
        val vm = buildViewModel(gmsType = GmsType.MICROG, userType = UserType.SECURE)

        assertEquals(GmsType.MICROG, vm.uiState.value.gmsType)
        assertEquals(UserType.SECURE, vm.uiState.value.userType)
    }

    private fun buildViewModel(gmsType: Int = GmsType.BARE_AOSP, userType: Int = UserType.SECURE): EvaluateViewModel {
        val fakeDeviceConfig = object : DeviceConfiguration(appContext) {
            override fun getGmsType() = gmsType
            override fun isUnsafe() = userType
        }
        return EvaluateViewModel(fakeDeviceConfig)
    }
}
