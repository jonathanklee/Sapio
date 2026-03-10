package com.klee.sapio.data.di

import com.klee.sapio.data.fdroid.KtorFdroidAvailabilityChecker
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.domain.DeviceInfo
import com.klee.sapio.domain.FdroidAvailabilityChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFdroidAvailabilityChecker(
        impl: KtorFdroidAvailabilityChecker
    ): FdroidAvailabilityChecker

    @Binds
    @Singleton
    abstract fun bindDeviceInfo(
        impl: DeviceConfiguration
    ): DeviceInfo
}
