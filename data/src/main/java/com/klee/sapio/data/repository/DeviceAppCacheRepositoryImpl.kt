package com.klee.sapio.data.repository

import com.klee.sapio.data.local.DeviceAppDao
import com.klee.sapio.data.local.DeviceAppEntity
import com.klee.sapio.domain.DeviceAppCacheRepository
import com.klee.sapio.domain.model.CachedDeviceApp
import javax.inject.Inject

class DeviceAppCacheRepositoryImpl @Inject constructor(
    private val deviceAppDao: DeviceAppDao
) : DeviceAppCacheRepository {

    override suspend fun getAll(): List<CachedDeviceApp> {
        return deviceAppDao.getAll().map { it.toDomain() }
    }

    override suspend fun replaceAll(items: List<CachedDeviceApp>) {
        deviceAppDao.deleteAll()
        deviceAppDao.upsertAll(items.map { it.toEntity() })
    }
}

private fun DeviceAppEntity.toDomain(): CachedDeviceApp = CachedDeviceApp(
    packageName = packageName,
    rating = rating,
    cachedAt = cachedAt
)

private fun CachedDeviceApp.toEntity(): DeviceAppEntity = DeviceAppEntity(
    packageName = packageName,
    rating = rating,
    cachedAt = cachedAt
)
