package com.klee.sapio.data.fdroid

import com.klee.sapio.domain.FdroidAvailabilityChecker
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class CachedFdroidAvailabilityChecker @Inject constructor(
    private val delegate: OkHttpFdroidAvailabilityChecker
) : FdroidAvailabilityChecker {

    private data class CacheEntry(val isAvailable: Boolean, val cachedAt: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun isAvailable(packageName: String): Boolean {
        val entry = cache[packageName]
        if (entry != null && System.currentTimeMillis() - entry.cachedAt < CACHE_VALIDITY_MS) {
            return entry.isAvailable
        }
        val result = delegate.isAvailable(packageName)
        cache[packageName] = CacheEntry(result, System.currentTimeMillis())
        return result
    }

    companion object {
        private const val CACHE_VALIDITY_MS = 86_400_000L // 24 hours
    }
}
