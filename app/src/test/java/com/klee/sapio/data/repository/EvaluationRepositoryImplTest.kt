package com.klee.sapio.data.repository

import android.os.Build
import androidx.room.Room
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.dto.Evaluation
import com.klee.sapio.data.dto.IconAnswer
import com.klee.sapio.data.local.AppDatabase
import com.klee.sapio.data.local.EvaluationDao
import com.klee.sapio.data.local.IconDao
import com.klee.sapio.data.mapper.toEntity
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
class EvaluationRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var evaluationDao: EvaluationDao
    private lateinit var iconDao: IconDao
    private lateinit var evaluationService: EvaluationService
    private lateinit var repository: EvaluationRepositoryImpl

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        evaluationDao = database.evaluationDao()
        iconDao = database.iconDao()
        evaluationService = Mockito.mock(EvaluationService::class.java)
        repository = EvaluationRepositoryImpl(evaluationService, evaluationDao, iconDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun listLatestEvaluations_remoteSuccessCachesResults() = runTest {
        val remote = listOf(
            Evaluation(
                name = "App One",
                packageName = "com.app.one",
                iconUrl = "/icon.png",
                rating = 1,
                microg = 1,
                secure = 0,
                updatedAt = Date(2),
                createdAt = Date(1),
                publishedAt = Date(1),
                versionName = "1.0"
            )
        )
        Mockito.`when`(evaluationService.listLatestEvaluations(1))
            .thenReturn(Result.success(remote))

        val result = repository.listLatestEvaluations(1)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(1, evaluationDao.listLatestEvaluations(10, 0).size)
    }

    @Test
    fun listLatestEvaluations_remoteFailureFallsBackToCache() = runTest {
        val cached = Evaluation(
            name = "Cached App",
            packageName = "com.cached.app",
            iconUrl = null,
            rating = 2,
            microg = 0,
            secure = 1,
            updatedAt = Date(2),
            createdAt = Date(1),
            publishedAt = Date(1),
            versionName = "1.0"
        )
        evaluationDao.upsertAll(listOf(cached.toEntity(System.currentTimeMillis())))
        Mockito.`when`(evaluationService.listLatestEvaluations(1))
            .thenReturn(Result.failure(IllegalStateException("Network error")))

        val result = repository.listLatestEvaluations(1)

        assertTrue(result.isSuccess)
        assertEquals("com.cached.app", result.getOrThrow().first().packageName)
    }

    @Test
    fun existingIcon_networkFirstCachesWhenSuccessful() = runTest {
        val now = System.currentTimeMillis()
        iconDao.upsertAll(
            listOf(
                iconAnswer(
                    id = 12,
                    name = "com.app.one.png",
                    url = "/icon.png"
                ).toEntity(now)
            )
        )
        val remote = listOf(
            iconAnswer(
                id = 13,
                name = "com.app.one.png",
                url = "/remote.png"
            )
        )
        Mockito.`when`(evaluationService.existingIcon("com.app.one.png"))
            .thenReturn(Result.success(remote))

        val result = repository.existingIcon("com.app.one.png")

        assertTrue(result.isSuccess)
        assertEquals("/remote.png", result.getOrThrow().first().url)
    }

    @Test
    fun existingIcon_fallsBackToCacheOnFailure() = runTest {
        val cachedAt = System.currentTimeMillis() - 1000
        iconDao.upsertAll(
            listOf(
                iconAnswer(
                    id = 22,
                    name = "com.app.two.png",
                    url = "/old.png"
                ).toEntity(cachedAt)
            )
        )
        Mockito.`when`(evaluationService.existingIcon("com.app.two.png"))
            .thenReturn(Result.failure(IllegalStateException("Network error")))

        val result = repository.existingIcon("com.app.two.png")

        assertTrue(result.isSuccess)
        assertEquals("/old.png", result.getOrThrow().first().url)
    }

    private fun iconAnswer(id: Int, name: String, url: String): IconAnswer {
        return IconAnswer(
            id = id,
            name = name,
            alternativeText = null,
            caption = null,
            width = 64,
            height = 64,
            formats = null,
            hash = "hash",
            ext = ".png",
            mime = "image/png",
            size = 10,
            url = url,
            previewUrl = null,
            provider = "local",
            provider_metadata = null,
            createdAt = Date(0),
            updatedAt = Date(0)
        )
    }
}
