package com.klee.sapio.data.di

import com.klee.sapio.data.fdroid.CachedFdroidAvailabilityChecker
import com.klee.sapio.data.repository.DeviceAppCacheRepositoryImpl
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.data.system.Settings
import com.klee.sapio.domain.AppSettings
import com.klee.sapio.domain.DeviceAppCacheRepository
import com.klee.sapio.domain.DeviceInfo
import com.klee.sapio.domain.FdroidAvailabilityChecker
import com.klee.sapio.domain.InstalledApplicationsDataSource
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
        impl: CachedFdroidAvailabilityChecker
    ): FdroidAvailabilityChecker

    @Binds
    @Singleton
    abstract fun bindDeviceInfo(
        impl: DeviceConfiguration
    ): DeviceInfo

    @Binds
    @Singleton
    abstract fun bindInstalledApplicationsDataSource(
        impl: InstalledApplicationsRepository
    ): InstalledApplicationsDataSource

    @Binds
    @Singleton
    abstract fun bindAppSettings(
        impl: Settings
    ): AppSettings

    @Binds
    @Singleton
    abstract fun bindDeviceAppCacheRepository(
        impl: DeviceAppCacheRepositoryImpl
    ): DeviceAppCacheRepository
}
