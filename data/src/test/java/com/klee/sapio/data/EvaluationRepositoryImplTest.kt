package com.klee.sapio.data

import android.content.Context
import android.os.Build
import org.robolectric.RuntimeEnvironment
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.dto.Evaluation as DtoEvaluation
import com.klee.sapio.data.dto.IconAnswer
import com.klee.sapio.data.dto.StrapiElement
import com.klee.sapio.data.dto.UploadEvaluationHeader
import com.klee.sapio.data.local.EvaluationDao
import com.klee.sapio.data.local.EvaluationEntity
import com.klee.sapio.data.local.IconDao
import com.klee.sapio.data.local.IconEntity
import com.klee.sapio.data.repository.EvaluationRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.M])
class EvaluationRepositoryImplTest {

    private lateinit var service: FakeEvaluationService
    private lateinit var evaluationDao: FakeEvaluationDao
    private lateinit var iconDao: FakeIconDao
    private lateinit var repository: EvaluationRepositoryImpl

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication() as Context
        service = FakeEvaluationService(context)
        evaluationDao = FakeEvaluationDao()
        iconDao = FakeIconDao()
        repository = EvaluationRepositoryImpl(service, evaluationDao, iconDao)
    }

    // region listLatestEvaluations

    @Test
    fun `listLatestEvaluations returns remote data on success`() = runTest {
        val dto = dtoEvaluation(name = "App", packageName = "com.app", iconUrl = "http://icon.png")
        service.listLatestResult = Result.success(listOf(dto))

        val result = repository.listLatestEvaluations(1)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("App", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `listLatestEvaluations caches remote data on success`() = runTest {
        val dto = dtoEvaluation(name = "App", packageName = "com.app", iconUrl = "http://icon.png")
        service.listLatestResult = Result.success(listOf(dto))

        repository.listLatestEvaluations(1)

        assertEquals(1, evaluationDao.upsertedItems.size)
    }

    @Test
    fun `listLatestEvaluations returns cache on remote failure when cache is non-empty`() = runTest {
        service.listLatestResult = Result.failure(RuntimeException("network error"))
        evaluationDao.listResult = listOf(evaluationEntity(name = "Cached App"))

        val result = repository.listLatestEvaluations(1)

        assertTrue(result.isSuccess)
        assertEquals("Cached App", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `listLatestEvaluations returns failure on remote failure when cache is empty`() = runTest {
        service.listLatestResult = Result.failure(RuntimeException("network error"))
        evaluationDao.listResult = emptyList()

        val result = repository.listLatestEvaluations(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `listLatestEvaluations fetches missing icon url via enrichment`() = runTest {
        val dto = dtoEvaluation(name = "App", packageName = "com.app", iconUrl = null)
        service.listLatestResult = Result.success(listOf(dto))
        service.existingIconResult = Result.success(listOf(iconAnswer(url = "http://fallback.png")))

        val result = repository.listLatestEvaluations(1)

        assertEquals("http://fallback.png", result.getOrNull()?.first()?.iconUrl)
    }

    // endregion

    // region searchEvaluations

    @Test
    fun `searchEvaluations returns remote data on success`() = runTest {
        val dto = dtoEvaluation(name = "Firefox", packageName = "org.mozilla.firefox", iconUrl = "http://icon.png")
        service.searchResult = Result.success(listOf(dto))

        val result = repository.searchEvaluations("firefox")

        assertTrue(result.isSuccess)
        assertEquals("Firefox", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `searchEvaluations caches remote results`() = runTest {
        service.searchResult = Result.success(listOf(dtoEvaluation(iconUrl = "http://icon.png")))

        repository.searchEvaluations("test")

        assertEquals(1, evaluationDao.upsertedItems.size)
    }

    @Test
    fun `searchEvaluations returns cache on remote failure when cache is non-empty`() = runTest {
        service.searchResult = Result.failure(RuntimeException())
        evaluationDao.searchResult = listOf(evaluationEntity(name = "Cached"))

        val result = repository.searchEvaluations("cached")

        assertTrue(result.isSuccess)
        assertEquals("Cached", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `searchEvaluations returns failure on remote failure when cache is empty`() = runTest {
        service.searchResult = Result.failure(RuntimeException())
        evaluationDao.searchResult = emptyList()

        val result = repository.searchEvaluations("nothing")

        assertTrue(result.isFailure)
    }

    // endregion

    // region addEvaluation and updateEvaluation

    @Test
    fun `addEvaluation returns success when service succeeds`() = runTest {
        service.addEvalResult = Result.success(Unit)

        val result = repository.addEvaluation(domainUploadEvaluation())

        assertTrue(result.isSuccess)
    }

    @Test
    fun `addEvaluation returns failure when service fails`() = runTest {
        service.addEvalResult = Result.failure(RuntimeException("server error"))

        val result = repository.addEvaluation(domainUploadEvaluation())

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateEvaluation returns success when service succeeds`() = runTest {
        service.updateEvalResult = Result.success(Unit)

        val result = repository.updateEvaluation(domainUploadEvaluation(), id = 1)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region fetchEvaluation

    @Test
    fun `fetchEvaluation returns evaluation when remote succeeds`() = runTest {
        val dto = dtoEvaluation(name = "App", packageName = "com.app")
        service.fetchEvalResult = Result.success(dto)

        val result = repository.fetchEvaluation("com.app", gmsType = 1, userType = 3)

        assertTrue(result.isSuccess)
        assertEquals("App", result.getOrNull()?.name)
    }

    @Test
    fun `fetchEvaluation caches evaluation when remote succeeds`() = runTest {
        service.fetchEvalResult = Result.success(dtoEvaluation())

        repository.fetchEvaluation("com.app", gmsType = 1, userType = 3)

        assertEquals(1, evaluationDao.upsertedItems.size)
    }

    @Test
    fun `fetchEvaluation returns null when remote returns null`() = runTest {
        service.fetchEvalResult = Result.success(null)

        val result = repository.fetchEvaluation("com.app", gmsType = 1, userType = 3)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `fetchEvaluation returns cache on remote failure when cached`() = runTest {
        service.fetchEvalResult = Result.failure(RuntimeException())
        evaluationDao.getEvaluationResult = evaluationEntity(name = "Cached App")

        val result = repository.fetchEvaluation("com.app", gmsType = 1, userType = 3)

        assertTrue(result.isSuccess)
        assertEquals("Cached App", result.getOrNull()?.name)
    }

    @Test
    fun `fetchEvaluation returns failure when remote fails and cache is empty`() = runTest {
        service.fetchEvalResult = Result.failure(RuntimeException())
        evaluationDao.getEvaluationResult = null

        val result = repository.fetchEvaluation("com.app", gmsType = 1, userType = 3)

        assertTrue(result.isFailure)
    }

    // endregion

    // region uploadIcon

    @Test
    fun `uploadIcon returns icons from remote on success`() = runTest {
        service.uploadIconResult = Result.success(listOf(iconAnswer(id = 5, url = "http://icon.png")))

        val result = repository.uploadIcon("com.test.app")

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.first()?.id)
    }

    @Test
    fun `uploadIcon caches icons on remote success`() = runTest {
        service.uploadIconResult = Result.success(listOf(iconAnswer()))

        repository.uploadIcon("com.test.app")

        assertEquals(1, iconDao.upsertedItems.size)
    }

    @Test
    fun `uploadIcon returns cached icons on remote failure`() = runTest {
        service.uploadIconResult = Result.failure(RuntimeException())
        iconDao.findByNameResult = listOf(iconEntity(id = 3, url = "http://cached.png"))

        val result = repository.uploadIcon("com.test.app")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.first()?.id)
    }

    @Test
    fun `uploadIcon returns failure when remote fails and cache is empty`() = runTest {
        service.uploadIconResult = Result.failure(RuntimeException())
        iconDao.findByNameResult = emptyList()

        val result = repository.uploadIcon("com.test.app")

        assertTrue(result.isFailure)
    }

    // endregion

    // region existingIcon

    @Test
    fun `existingIcon returns icons from remote on success`() = runTest {
        service.existingIconResult = Result.success(listOf(iconAnswer(url = "http://icon.png")))

        val result = repository.existingIcon("com.test.app.png")

        assertTrue(result.isSuccess)
        assertEquals("http://icon.png", result.getOrNull()?.first()?.url)
    }

    @Test
    fun `existingIcon caches icons on remote success`() = runTest {
        service.existingIconResult = Result.success(listOf(iconAnswer()))

        repository.existingIcon("com.test.app.png")

        assertEquals(1, iconDao.upsertedItems.size)
    }

    @Test
    fun `existingIcon returns cache on remote failure`() = runTest {
        service.existingIconResult = Result.failure(RuntimeException())
        iconDao.findByNameResult = listOf(iconEntity(url = "http://cached.png"))

        val result = repository.existingIcon("com.test.app.png")

        assertTrue(result.isSuccess)
        assertEquals("http://cached.png", result.getOrNull()?.first()?.url)
    }

    @Test
    fun `existingIcon returns failure when remote fails and cache is empty`() = runTest {
        service.existingIconResult = Result.failure(RuntimeException())
        iconDao.findByNameResult = emptyList()

        val result = repository.existingIcon("com.test.app.png")

        assertTrue(result.isFailure)
    }

    // endregion

    // region deleteIcon

    @Test
    fun `deleteIcon removes from local dao on remote success`() = runTest {
        service.deleteIconResult = Result.success(Unit)

        repository.deleteIcon(id = 10)

        assertTrue(iconDao.deletedIds.contains(10))
    }

    @Test
    fun `deleteIcon does not remove from local dao on remote failure`() = runTest {
        service.deleteIconResult = Result.failure(RuntimeException())

        repository.deleteIcon(id = 10)

        assertTrue(iconDao.deletedIds.isEmpty())
    }

    @Test
    fun `deleteIcon returns failure when remote fails`() = runTest {
        service.deleteIconResult = Result.failure(RuntimeException("error"))

        val result = repository.deleteIcon(id = 10)

        assertTrue(result.isFailure)
    }

    // endregion

    // region helpers

    private fun dtoEvaluation(
        name: String = "Test App",
        packageName: String = "com.test.app",
        iconUrl: String? = null,
        rating: Int = 1,
        microg: Int = 1,
        secure: Int = 3
    ): DtoEvaluation {
        val icon = iconUrl?.let {
            val imageData = com.klee.sapio.data.dto.RemoteImage(
                name = "test.png", alternativeText = null, caption = null,
                width = 100, height = 100, formats = null,
                hash = "hash", ext = ".png", mime = "image/png",
                size = 100, url = it, previewUrl = null,
                provider = null, provider_metadata = null,
                createdAt = Date(), updatedAt = Date()
            )
            com.klee.sapio.data.dto.Icon(com.klee.sapio.data.dto.StrapiImageElement(1, imageData))
        }
        return DtoEvaluation(
            name = name, packageName = packageName, icon = icon,
            rating = rating, microg = microg, secure = secure,
            updatedAt = null, createdAt = null, publishedAt = null, versionName = null
        )
    }

    private fun iconAnswer(
        id: Int = 1,
        name: String = "com.test.app.png",
        url: String = "http://icon/test.png"
    ) = IconAnswer(
        id = id, name = name, url = url,
        alternativeText = null, caption = null,
        width = 100, height = 100, formats = null,
        hash = "hash", ext = ".png", mime = "image/png",
        size = 100, previewUrl = null, provider = null,
        provider_metadata = null, createdAt = Date(), updatedAt = Date()
    )

    private fun evaluationEntity(
        name: String = "Test App",
        packageName: String = "com.test.app",
        microg: Int = 1,
        secure: Int = 3
    ) = EvaluationEntity(
        name = name, packageName = packageName, iconUrl = null,
        rating = 1, microg = microg, secure = secure,
        updatedAt = null, createdAt = null, publishedAt = null,
        versionName = null, cachedAt = 0L
    )

    private fun iconEntity(
        id: Int = 1,
        name: String = "com.test.app.png",
        url: String = "http://icon/test.png"
    ) = IconEntity(id = id, name = name, url = url, cachedAt = 0L)

    private fun domainUploadEvaluation() = com.klee.sapio.domain.model.UploadEvaluation(
        name = "Test App", packageName = "com.test.app", icon = 1,
        rating = 1, microg = 1, rooted = 3
    )

    // endregion

    // region fakes

    private class FakeEvaluationService(context: Context) : EvaluationService(context) {
        var listLatestResult: Result<List<DtoEvaluation>> = Result.success(emptyList())
        var searchResult: Result<List<DtoEvaluation>> = Result.success(emptyList())
        var addEvalResult: Result<Unit> = Result.success(Unit)
        var updateEvalResult: Result<Unit> = Result.success(Unit)
        var fetchEvalResult: Result<DtoEvaluation?> = Result.success(null)
        var uploadIconResult: Result<List<IconAnswer>> = Result.success(emptyList())
        var existingIconResult: Result<List<IconAnswer>> = Result.success(emptyList())
        var deleteIconResult: Result<Unit> = Result.success(Unit)
        var existingEvaluationsResult: Result<List<StrapiElement>> = Result.success(emptyList())

        override suspend fun listLatestEvaluations(pageNumber: Int) = listLatestResult
        override suspend fun searchEvaluation(pattern: String) = searchResult
        override suspend fun addEvaluation(app: UploadEvaluationHeader) = addEvalResult
        override suspend fun updateEvaluation(app: UploadEvaluationHeader, id: Int) = updateEvalResult
        override suspend fun fetchEvaluation(appPackageName: String, microG: Int, rooted: Int) = fetchEvalResult
        override suspend fun uploadIcon(packageName: String) = uploadIconResult
        override suspend fun existingIcon(iconName: String) = existingIconResult
        override suspend fun deleteIcon(id: Int) = deleteIconResult
        override suspend fun existingEvaluations(packageName: String) = existingEvaluationsResult
    }

    private class FakeEvaluationDao : EvaluationDao {
        var listResult: List<EvaluationEntity> = emptyList()
        var searchResult: List<EvaluationEntity> = emptyList()
        var getEvaluationResult: EvaluationEntity? = null
        val upsertedItems = mutableListOf<EvaluationEntity>()

        override suspend fun listLatestEvaluations(limit: Int, offset: Int) = listResult
        override suspend fun searchEvaluations(pattern: String) = searchResult
        override suspend fun getEvaluation(packageName: String, microg: Int, secure: Int) = getEvaluationResult
        override suspend fun upsertAll(items: List<EvaluationEntity>) { upsertedItems.addAll(items) }
    }

    private class FakeIconDao : IconDao {
        var findByNameResult: List<IconEntity> = emptyList()
        val upsertedItems = mutableListOf<IconEntity>()
        val deletedIds = mutableListOf<Int>()

        override suspend fun findByName(iconName: String) = findByNameResult
        override suspend fun upsertAll(items: List<IconEntity>) { upsertedItems.addAll(items) }
        override suspend fun deleteById(id: Int) { deletedIds.add(id) }
    }

    // endregion
}
