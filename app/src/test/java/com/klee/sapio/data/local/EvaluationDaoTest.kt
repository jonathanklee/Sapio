package com.klee.sapio.data.local

import android.os.Build
import androidx.room.Room
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
class EvaluationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var evaluationDao: EvaluationDao
    private lateinit var iconDao: IconDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        evaluationDao = database.evaluationDao()
        iconDao = database.iconDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun listLatestEvaluations_ordersByUpdatedAt() = runTest {
        val older = EvaluationEntity(
            name = "Old",
            packageName = "com.old",
            iconUrl = null,
            rating = 1,
            microg = 0,
            secure = 1,
            updatedAt = Date(100),
            createdAt = Date(50),
            publishedAt = Date(60),
            versionName = "1.0",
            cachedAt = 1
        )
        val newer = EvaluationEntity(
            name = "New",
            packageName = "com.new",
            iconUrl = null,
            rating = 2,
            microg = 1,
            secure = 0,
            updatedAt = Date(200),
            createdAt = Date(80),
            publishedAt = Date(90),
            versionName = "1.1",
            cachedAt = 1
        )
        evaluationDao.upsertAll(listOf(older, newer))

        val results = evaluationDao.listLatestEvaluations(limit = 1, offset = 0)

        assertEquals(1, results.size)
        assertEquals("com.new", results.first().packageName)
    }

    @Test
    fun searchEvaluations_matchesNameOrPackage() = runTest {
        val target = EvaluationEntity(
            name = "Target App",
            packageName = "com.target.app",
            iconUrl = null,
            rating = 1,
            microg = 0,
            secure = 1,
            updatedAt = Date(100),
            createdAt = Date(50),
            publishedAt = Date(60),
            versionName = "1.0",
            cachedAt = 1
        )
        val other = EvaluationEntity(
            name = "Other",
            packageName = "com.other",
            iconUrl = null,
            rating = 1,
            microg = 0,
            secure = 1,
            updatedAt = Date(100),
            createdAt = Date(50),
            publishedAt = Date(60),
            versionName = "1.0",
            cachedAt = 1
        )
        evaluationDao.upsertAll(listOf(target, other))

        val results = evaluationDao.searchEvaluations("%target%")

        assertEquals(1, results.size)
        assertEquals("com.target.app", results.first().packageName)
    }

    @Test
    fun getEvaluation_usesCompositeKey() = runTest {
        val target = EvaluationEntity(
            name = "Keyed",
            packageName = "com.keyed",
            iconUrl = null,
            rating = 1,
            microg = 1,
            secure = 0,
            updatedAt = Date(100),
            createdAt = Date(50),
            publishedAt = Date(60),
            versionName = "1.0",
            cachedAt = 1
        )
        evaluationDao.upsertAll(listOf(target))

        val result = evaluationDao.getEvaluation("com.keyed", microg = 1, secure = 0)

        assertNotNull(result)
        assertEquals("com.keyed", result?.packageName)
    }

    @Test
    fun findIconByName_filtersByCachedAt() = runTest {
        val expired = IconEntity(
            id = 1,
            name = "com.app.png",
            url = "/expired.png",
            cachedAt = 10
        )
        val fresh = IconEntity(
            id = 2,
            name = "com.app.png",
            url = "/fresh.png",
            cachedAt = 200
        )
        iconDao.upsertAll(listOf(expired, fresh))

        val results = iconDao.findByName("com.app.png", minCachedAt = 100)

        assertEquals(1, results.size)
        assertTrue(results.first().url.contains("fresh"))
    }
}
