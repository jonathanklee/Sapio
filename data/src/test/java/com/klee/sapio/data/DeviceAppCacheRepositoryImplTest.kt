package com.klee.sapio.data

import com.klee.sapio.data.local.DeviceAppDao
import com.klee.sapio.data.local.DeviceAppEntity
import com.klee.sapio.data.repository.DeviceAppCacheRepositoryImpl
import com.klee.sapio.domain.model.CachedDeviceApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceAppCacheRepositoryImplTest {

    @Test
    fun `cached entities are mapped to the domain`() = runTest {
        val dao = FakeDao(listOf(DeviceAppEntity("pkg.one", 2, 1_500L)))
        val repository = DeviceAppCacheRepositoryImpl(dao)

        val cached = repository.getAll().single()

        assertEquals("pkg.one", cached.packageName)
        assertEquals(2, cached.rating)
        assertEquals(1_500L, cached.cachedAt)
    }

    @Test
    fun `an entity without a rating keeps a null rating`() = runTest {
        val dao = FakeDao(listOf(DeviceAppEntity("pkg.one", null, 1_500L)))

        assertNull(DeviceAppCacheRepositoryImpl(dao).getAll().single().rating)
    }

    @Test
    fun `an empty cache maps to an empty list`() = runTest {
        assertTrue(DeviceAppCacheRepositoryImpl(FakeDao(emptyList())).getAll().isEmpty())
    }

    @Test
    fun `replacing the cache clears it before inserting`() = runTest {
        val dao = FakeDao(listOf(DeviceAppEntity("pkg.old", 1, 1L)))
        val repository = DeviceAppCacheRepositoryImpl(dao)

        repository.replaceAll(listOf(CachedDeviceApp("pkg.new", 3, 2_000L)))

        assertEquals(listOf("deleteAll", "upsertAll"), dao.calls)
        assertEquals("pkg.new", dao.upserted.single().packageName)
        assertEquals(3, dao.upserted.single().rating)
        assertEquals(2_000L, dao.upserted.single().cachedAt)
    }

    @Test
    fun `replacing with nothing still clears the cache`() = runTest {
        val dao = FakeDao(listOf(DeviceAppEntity("pkg.old", 1, 1L)))

        DeviceAppCacheRepositoryImpl(dao).replaceAll(emptyList())

        assertEquals(listOf("deleteAll", "upsertAll"), dao.calls)
        assertTrue(dao.upserted.isEmpty())
    }

    private class FakeDao(private val entities: List<DeviceAppEntity>) : DeviceAppDao {
        val calls = mutableListOf<String>()
        var upserted: List<DeviceAppEntity> = emptyList()

        override suspend fun getAll(): List<DeviceAppEntity> {
            calls.add("getAll")
            return entities
        }

        override suspend fun upsertAll(items: List<DeviceAppEntity>) {
            calls.add("upsertAll")
            upserted = items
        }

        override suspend fun deleteAll() {
            calls.add("deleteAll")
        }
    }
}
