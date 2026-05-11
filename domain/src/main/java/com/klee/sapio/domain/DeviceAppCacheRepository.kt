package com.klee.sapio.domain

import com.klee.sapio.domain.model.CachedDeviceApp

interface DeviceAppCacheRepository {
    suspend fun getAll(): List<CachedDeviceApp>

    suspend fun replaceAll(items: List<CachedDeviceApp>)
}
