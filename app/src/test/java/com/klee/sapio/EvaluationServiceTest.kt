package com.klee.sapio

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.klee.sapio.data.api.EvaluationApi
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.dto.Evaluation
import com.klee.sapio.data.dto.IconAnswer
import com.klee.sapio.data.dto.StrapiAnswer
import com.klee.sapio.data.dto.StrapiElement
import com.klee.sapio.data.dto.StrapiMeta
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.data.dto.UploadAnswer
import com.klee.sapio.data.dto.UploadEvaluation
import com.klee.sapio.data.dto.UploadEvaluationHeader
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.P])
class EvaluationServiceTest {

    @Mock
    private lateinit var mockSettings: Settings

    private lateinit var service: EvaluationService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = EvaluationService(ApplicationProvider.getApplicationContext())
        setField("settings", mockSettings)
    }

    @Test
    fun listLatestEvaluations_returnsEmptyOnHttpException() = runBlocking {
        val errorBody = "error".toResponseBody("text/plain".toMediaTypeOrNull())
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val api = object : EvaluationApi by failingApi() {
            override suspend fun listLatestEvaluationsAsync(root: Int, pageNumber: Int): StrapiAnswer {
                throw HttpException(Response.error<StrapiAnswer>(500, errorBody))
            }
        }
        setApi(api)

        val result = service.listLatestEvaluations(pageNumber = 1)
        assertTrue(result.isFailure)
    }

    @Test
    fun listLatestEvaluations_happyPathSorted() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val evalNewer = StrapiElement(1, createEvaluation("AppOne", "pkg1", updatedAtOffset = 2))
        val evalOlder = StrapiElement(2, createEvaluation("AppTwo", "pkg1", updatedAtOffset = 1))
        val answer = StrapiAnswer(arrayListOf(evalOlder, evalNewer), StrapiMeta(null))
        val api = object : EvaluationApi by failingApi() {
            override suspend fun listLatestEvaluationsAsync(root: Int, pageNumber: Int): StrapiAnswer = answer
        }
        setApi(api)

        val result = service.listLatestEvaluations(pageNumber = 0).getOrThrow()
        assertEquals(2, result.size)
        assertEquals("AppOne", result.first().name)
        assertEquals("AppTwo", result.last().name)
    }

    @Test
    fun listLatestEvaluations_sortsByUpdatedAtAcrossPackages() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val older = StrapiElement(1, createEvaluation("Old", "pkg.old", updatedAtOffset = 1))
        val newer = StrapiElement(2, createEvaluation("New", "pkg.new", updatedAtOffset = 5))
        val answer = StrapiAnswer(arrayListOf(older, newer), StrapiMeta(null))
        setApi(object : EvaluationApi by failingApi() {
            override suspend fun listLatestEvaluationsAsync(root: Int, pageNumber: Int): StrapiAnswer = answer
        })

        val result = service.listLatestEvaluations(pageNumber = 0).getOrThrow()
        assertEquals(listOf("pkg.new", "pkg.old"), result.map { it.packageName })
    }

    @Test
    fun listLatestEvaluations_respectsPageNumberParameter() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val page1 = StrapiAnswer(arrayListOf(StrapiElement(1, createEvaluation("P1", "p1"))), StrapiMeta(null))
        val page2 = StrapiAnswer(arrayListOf(StrapiElement(2, createEvaluation("P2", "p2"))), StrapiMeta(null))

        setApi(object : EvaluationApi by failingApi() {
            override suspend fun listLatestEvaluationsAsync(root: Int, pageNumber: Int): StrapiAnswer =
                if (pageNumber == 2) page2 else page1
        })

        val first = service.listLatestEvaluations(pageNumber = 1).getOrThrow()
        val second = service.listLatestEvaluations(pageNumber = 2).getOrThrow()

        assertEquals("p1", first.first().packageName)
        assertEquals("p2", second.first().packageName)
    }

    @Test
    fun searchEvaluation_returnsEmptyOnIOException() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val api = object : EvaluationApi by failingApi() {
            override suspend fun searchAsync(name: String, packageName: String, rooted: Int): StrapiAnswer {
                throw IOException("boom")
            }
        }
        setApi(api)

        val result = service.searchEvaluation("pattern")
        assertTrue(result.isFailure)
    }

    @Test
    fun searchEvaluation_happyPathDistinctByPackage() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val evalA = StrapiElement(1, createEvaluation("A", "pkgA"))
        val evalADuplicate = StrapiElement(2, createEvaluation("A2", "pkgA"))
        val answer = StrapiAnswer(arrayListOf(evalA, evalADuplicate), StrapiMeta(null))
        val api = object : EvaluationApi by failingApi() {
            override suspend fun searchAsync(name: String, packageName: String, rooted: Int): StrapiAnswer = answer
        }
        setApi(api)

        val result = service.searchEvaluation("a").getOrThrow()
        assertEquals(1, result.size)
        assertEquals("pkgA", result.first().packageName)
        assertEquals("A", result.first().name)
    }

    @Test
    fun fetchEvaluation_returnsNullWhenNoData() = runBlocking {
        val answer = StrapiAnswer(arrayListOf(), StrapiMeta(null))
        val api = object : EvaluationApi by failingApi() {
            override suspend fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int): StrapiAnswer = answer
        }
        setApi(api)

        val result = service.fetchEvaluation("pkg", microG = 0, rooted = 0).getOrThrow()
        assertNull(result)
    }

    @Test
    fun existingEvaluations_returnsList() = runBlocking {
        val eval = StrapiElement(10, createEvaluation("B", "pkgB"))
        val answer = StrapiAnswer(arrayListOf(eval), StrapiMeta(null))
        val api = object : EvaluationApi by failingApi() {
            override suspend fun existingEvaluationsAsync(packageName: String): StrapiAnswer = answer
        }
        setApi(api)

        val result = service.existingEvaluations("pkgB").getOrThrow()
        assertEquals(1, result.size)
        assertEquals(10, result.first().id)
        assertEquals("pkgB", result.first().attributes.packageName)
    }

    @Test
    fun existingIcon_returnsNullOnIOException() = runBlocking {
        val api = object : EvaluationApi by failingApi() {
            override suspend fun existingIconAsync(iconName: String): List<IconAnswer> {
                throw IOException("fail")
            }
        }
        setApi(api)

        val result = service.existingIcon("icon.png")
        assertTrue(result.isFailure)
    }

    @Test
    fun existingIcon_returnsListOnSuccess() = runBlocking {
        val answer = emptyList<IconAnswer>()
        val api = object : EvaluationApi by failingApi() {
            override suspend fun existingIconAsync(iconName: String): List<IconAnswer> = answer
        }
        setApi(api)

        val result = service.existingIcon("icon.png").getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun fetchEvaluation_returnsFirstElement() = runBlocking {
        val eval = StrapiElement(5, createEvaluation("PkgEval", "pkg.eval"))
        val api = object : EvaluationApi by failingApi() {
            override suspend fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int): StrapiAnswer =
                StrapiAnswer(arrayListOf(eval), StrapiMeta(null))
        }
        setApi(api)

        val result = service.fetchEvaluation("pkg.eval", 0, 0).getOrThrow()
        assertEquals("pkg.eval", result?.packageName)
    }

    @Test
    fun addEvaluation_returnsNullOnIOException() = runBlocking {
        val api = object : EvaluationApi by failingApi() {
            override suspend fun addEvaluation(evaluation: UploadEvaluationHeader): UploadAnswer {
                throw IOException("add failed")
            }
        }
        setApi(api)

        val result = service.addEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)))
        assertTrue(result.isFailure)
    }

    @Test
    fun addEvaluation_returnsResponseOnSuccess() = runBlocking {
        val response = UploadAnswer(
            StrapiElement(1, createEvaluation("Success", "pkg.s")),
            StrapiMeta(null)
        )
        val api = object : EvaluationApi by failingApi() {
            override suspend fun addEvaluation(evaluation: UploadEvaluationHeader): UploadAnswer = response
        }
        setApi(api)

        val result = service.addEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)))
        assertTrue(result.isSuccess)
    }

    @Test
    fun updateEvaluation_returnsNullOnIOException() = runBlocking {
        val api = object : EvaluationApi by failingApi() {
            override suspend fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int): UploadAnswer {
                throw IOException("update failed")
            }
        }
        setApi(api)

        val result = service.updateEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)), 1)
        assertTrue(result.isFailure)
    }

    @Test
    fun updateEvaluation_returnsResponseOnSuccess() = runBlocking {
        val response = UploadAnswer(
            StrapiElement(2, createEvaluation("Updated", "pkg.u")),
            StrapiMeta(null)
        )
        val api = object : EvaluationApi by failingApi() {
            override suspend fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int): UploadAnswer = response
        }
        setApi(api)

        val result = service.updateEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)), 2)
        assertTrue(result.isSuccess)
    }

    @Test
    fun uploadIcon_returnsResponseOnSuccess() = runBlocking {
        val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
        val drawable = android.graphics.drawable.BitmapDrawable(
            ApplicationProvider.getApplicationContext<android.content.Context>().resources,
            bitmap
        )
        val app = InstalledApplication("n", "pkg.upload", drawable)
        val api = object : EvaluationApi by failingApi() {
            override suspend fun addIcon(image: okhttp3.MultipartBody.Part): ArrayList<IconAnswer> =
                arrayListOf(createIconAnswer())
        }
        setApi(api)

        val result = service.uploadIcon(app)
        assertEquals(1, result.getOrThrow().size)
    }

    @Test
    fun deleteIcon_returnsResponseOnSuccess() = runBlocking {
        val api = object : EvaluationApi by failingApi() {
            override suspend fun deleteIcon(id: Int): IconAnswer = createIconAnswer()
        }
        setApi(api)

        val result = service.deleteIcon(99)
        assertTrue(result.isSuccess)
    }

    @Test
    fun uploadIcon_returnsNullOnIOException() = runBlocking {
        // Force drawable -> byte array conversion without hitting network by mocking upload to throw
        val bitmap = android.graphics.Bitmap.createBitmap(10, 10, android.graphics.Bitmap.Config.ARGB_8888)
        val drawable = android.graphics.drawable.BitmapDrawable(
            ApplicationProvider.getApplicationContext<android.content.Context>().resources,
            bitmap
        )
        val app = InstalledApplication("n", "pkg.upload", drawable)
        val api = object : EvaluationApi by failingApi() {
            override suspend fun addIcon(image: okhttp3.MultipartBody.Part): ArrayList<IconAnswer> {
                throw IOException("net down")
            }
        }
        setApi(api)

        val result = service.uploadIcon(app)
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteIcon_returnsNullOnIOException() = runBlocking {
        val api = object : EvaluationApi by failingApi() {
            override suspend fun deleteIcon(id: Int): IconAnswer {
                throw IOException("delete failed")
            }
        }
        setApi(api)

        val result = service.deleteIcon(123)
        assertTrue(result.isFailure)
    }

    private fun setField(name: String, value: Any) {
        org.robolectric.util.ReflectionHelpers.setField(service, name, value)
    }

    private fun setApi(api: EvaluationApi) {
        setField("evaluationsApi", api)
    }

    private fun failingApi(): EvaluationApi = object : EvaluationApi {
        override suspend fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) =
            throw NotImplementedError()
        override suspend fun searchAsync(name: String, packageName: String, rooted: Int) =
            throw NotImplementedError()
        override suspend fun existingEvaluationsAsync(packageName: String) = throw NotImplementedError()
        override suspend fun addEvaluation(evaluation: UploadEvaluationHeader) = throw NotImplementedError()
        override suspend fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int) = throw NotImplementedError()
        override suspend fun addIcon(image: okhttp3.MultipartBody.Part) = throw NotImplementedError()
        override suspend fun existingIconAsync(iconName: String) = throw NotImplementedError()
        override suspend fun deleteIcon(id: Int) = throw NotImplementedError()
        override suspend fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int) =
            throw NotImplementedError()
    }

    private fun createEvaluation(
        name: String,
        packageName: String,
        updatedAtOffset: Long = 0
    ): Evaluation {
        val date = Date(System.currentTimeMillis() + updatedAtOffset)
        return Evaluation(
            name = name,
            packageName = packageName,
            icon = null,
            rating = 1,
            microg = 0,
            secure = 0,
            updatedAt = date,
            createdAt = date,
            publishedAt = date,
            versionName = "1.0"
        )
    }

    private fun createIconAnswer(): IconAnswer {
        val now = Date()
        return IconAnswer(
            id = 1,
            name = "icon",
            alternativeText = null,
            caption = null,
            width = 10,
            height = 10,
            formats = null,
            hash = "hash",
            ext = ".png",
            mime = "image/png",
            size = 1,
            url = "http://localhost/icon.png",
            previewUrl = null,
            provider = null,
            provider_metadata = null,
            createdAt = now,
            updatedAt = now
        )
    }
}
