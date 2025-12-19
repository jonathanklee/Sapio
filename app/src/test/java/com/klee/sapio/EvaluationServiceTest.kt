package com.klee.sapio

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.klee.sapio.data.EvaluationApi
import com.klee.sapio.data.EvaluationService
import com.klee.sapio.data.StrapiAnswer
import com.klee.sapio.data.StrapiElement
import com.klee.sapio.data.StrapiMeta
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.Settings
import com.klee.sapio.data.IconAnswer
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.UploadAnswer
import com.klee.sapio.data.UploadEvaluation
import com.klee.sapio.data.UploadEvaluationHeader
import kotlinx.coroutines.CompletableDeferred
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
        val deferred = CompletableDeferred<StrapiAnswer>()
        val errorBody = "error".toResponseBody("text/plain".toMediaTypeOrNull())
        deferred.completeExceptionally(HttpException(Response.error<StrapiAnswer>(500, errorBody)))
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val api = object : EvaluationApi by failingApi() {
            override fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) = deferred
        }
        setApi(api)

        val result = service.listLatestEvaluations(pageNumber = 1)
        assertTrue(result.isEmpty())
    }

    @Test
    fun listLatestEvaluations_happyPathDistinctAndSorted() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val evalNewer = StrapiElement(1, createEvaluation("AppOne", "pkg1", updatedAtOffset = 2))
        val evalOlder = StrapiElement(2, createEvaluation("AppTwo", "pkg1", updatedAtOffset = 1)) // same package, should be dropped
        val answer = StrapiAnswer(arrayListOf(evalNewer, evalOlder), StrapiMeta(null))
        val deferred = CompletableDeferred(answer)
        val api = object : EvaluationApi by failingApi() {
            override fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) = deferred
        }
        setApi(api)

        val result = service.listLatestEvaluations(pageNumber = 0)
        assertEquals(1, result.size)
        assertEquals("pkg1", result.first().packageName)
        assertEquals("AppOne", result.first().name)
    }

    @Test
    fun listLatestEvaluations_sortsByUpdatedAtAcrossPackages() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val older = StrapiElement(1, createEvaluation("Old", "pkg.old", updatedAtOffset = 1))
        val newer = StrapiElement(2, createEvaluation("New", "pkg.new", updatedAtOffset = 5))
        val answer = StrapiAnswer(arrayListOf(older, newer), StrapiMeta(null))
        val deferred = CompletableDeferred(answer)
        setApi(object : EvaluationApi by failingApi() {
            override fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) = deferred
        })

        val result = service.listLatestEvaluations(pageNumber = 0)
        assertEquals(listOf("pkg.new", "pkg.old"), result.map { it.packageName })
    }

    @Test
    fun listLatestEvaluations_respectsPageNumberParameter() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(1)
        val page1 = CompletableDeferred(StrapiAnswer(arrayListOf(StrapiElement(1, createEvaluation("P1", "p1"))), StrapiMeta(null)))
        val page2 = CompletableDeferred(StrapiAnswer(arrayListOf(StrapiElement(2, createEvaluation("P2", "p2"))), StrapiMeta(null)))

        setApi(object : EvaluationApi by failingApi() {
            override fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) =
                if (pageNumber == 2) page2 else page1
        })

        val first = service.listLatestEvaluations(pageNumber = 1)
        val second = service.listLatestEvaluations(pageNumber = 2)

        assertEquals("p1", first.first().packageName)
        assertEquals("p2", second.first().packageName)
    }

    @Test
    fun searchEvaluation_returnsEmptyOnIOException() = runBlocking {
        val deferred = CompletableDeferred<StrapiAnswer>()
        deferred.completeExceptionally(IOException("boom"))
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val api = object : EvaluationApi by failingApi() {
            override fun searchAsync(name: String, packageName: String, rooted: Int) = deferred
        }
        setApi(api)

        val result = service.searchEvaluation("pattern")
        assertTrue(result.isEmpty())
    }

    @Test
    fun searchEvaluation_happyPathDistinctByPackage() = runBlocking {
        Mockito.`when`(mockSettings.getRootConfigurationLevel()).thenReturn(0)
        val evalA = StrapiElement(1, createEvaluation("A", "pkgA"))
        val evalADuplicate = StrapiElement(2, createEvaluation("A2", "pkgA"))
        val answer = StrapiAnswer(arrayListOf(evalA, evalADuplicate), StrapiMeta(null))
        val deferred = CompletableDeferred(answer)
        val api = object : EvaluationApi by failingApi() {
            override fun searchAsync(name: String, packageName: String, rooted: Int) = deferred
        }
        setApi(api)

        val result = service.searchEvaluation("a")
        assertEquals(1, result.size)
        assertEquals("pkgA", result.first().packageName)
        assertEquals("A", result.first().name)
    }

    @Test
    fun fetchEvaluation_returnsNullWhenNoData() = runBlocking {
        val answer = StrapiAnswer(arrayListOf(), StrapiMeta(null))
        val deferred = CompletableDeferred(answer)
        val api = object : EvaluationApi by failingApi() {
            override fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int) = deferred
        }
        setApi(api)

        val result = service.fetchEvaluation("pkg", microG = 0, rooted = 0)
        assertNull(result)
    }

    @Test
    fun existingEvaluations_returnsList() = runBlocking {
        val eval = StrapiElement(10, createEvaluation("B", "pkgB"))
        val answer = StrapiAnswer(arrayListOf(eval), StrapiMeta(null))
        val deferred = CompletableDeferred(answer)
        val api = object : EvaluationApi by failingApi() {
            override fun existingEvaluationsAsync(packageName: String) = deferred
        }
        setApi(api)

        val result = service.existingEvaluations("pkgB")
        assertEquals(1, result.size)
        assertEquals(10, result.first().id)
        assertEquals("pkgB", result.first().attributes.packageName)
    }

    @Test
    fun existingIcon_returnsNullOnIOException() = runBlocking {
        val deferred = CompletableDeferred<List<IconAnswer>>()
        deferred.completeExceptionally(IOException("fail"))
        val api = object : EvaluationApi by failingApi() {
            override fun existingIconAsync(iconName: String) = deferred
        }
        setApi(api)

        val result = service.existingIcon("icon.png")
        assertNull(result)
    }

    @Test
    fun existingIcon_returnsListOnSuccess() = runBlocking {
        val answer = emptyList<IconAnswer>()
        val deferred = CompletableDeferred(answer)
        val api = object : EvaluationApi by failingApi() {
            override fun existingIconAsync(iconName: String) = deferred
        }
        setApi(api)

        val result = service.existingIcon("icon.png")
        assertTrue(result?.isEmpty() == true)
    }

    @Test
    fun fetchEvaluation_returnsFirstElement() = runBlocking {
        val eval = StrapiElement(5, createEvaluation("PkgEval", "pkg.eval"))
        val deferred = CompletableDeferred(StrapiAnswer(arrayListOf(eval), StrapiMeta(null)))
        val api = object : EvaluationApi by failingApi() {
            override fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int) = deferred
        }
        setApi(api)

        val result = service.fetchEvaluation("pkg.eval", 0, 0)
        assertEquals("pkg.eval", result?.packageName)
    }

    @Test
    fun addEvaluation_returnsNullOnIOException() = runBlocking {
        val call = ThrowingCall<UploadAnswer>(IOException("add failed"))
        val api = object : EvaluationApi by failingApi() {
            override fun addEvaluation(evaluation: UploadEvaluationHeader) = call
        }
        setApi(api)

        val result = service.addEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)))
        assertNull(result)
    }

    @Test
    fun addEvaluation_returnsResponseOnSuccess() = runBlocking {
        val response = UploadAnswer(
            StrapiElement(1, createEvaluation("Success", "pkg.s")),
            StrapiMeta(null)
        )
        val call = ImmediateCall(response)
        val api = object : EvaluationApi by failingApi() {
            override fun addEvaluation(evaluation: UploadEvaluationHeader) = call
        }
        setApi(api)

        val result = service.addEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)))
        assertEquals(1, result?.body()?.data?.id)
    }

    @Test
    fun updateEvaluation_returnsNullOnIOException() = runBlocking {
        val call = ThrowingCall<UploadAnswer>(IOException("update failed"))
        val api = object : EvaluationApi by failingApi() {
            override fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int) = call
        }
        setApi(api)

        val result = service.updateEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)), 1)
        assertNull(result)
    }

    @Test
    fun updateEvaluation_returnsResponseOnSuccess() = runBlocking {
        val response = UploadAnswer(
            StrapiElement(2, createEvaluation("Updated", "pkg.u")),
            StrapiMeta(null)
        )
        val call = ImmediateCall(response)
        val api = object : EvaluationApi by failingApi() {
            override fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int) = call
        }
        setApi(api)

        val result = service.updateEvaluation(UploadEvaluationHeader(UploadEvaluation("a", "p", 1, 1, 0, 0)), 2)
        assertEquals(2, result?.body()?.data?.id)
    }

    @Test
    fun uploadIcon_returnsResponseOnSuccess() = runBlocking {
        val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
        val drawable = android.graphics.drawable.BitmapDrawable(
            ApplicationProvider.getApplicationContext<android.content.Context>().resources,
            bitmap
        )
        val app = InstalledApplication("n", "pkg.upload", drawable)
        val call = ImmediateCall(arrayListOf(createIconAnswer()))
        val api = object : EvaluationApi by failingApi() {
            override fun addIcon(image: okhttp3.MultipartBody.Part) = call
        }
        setApi(api)

        val result = service.uploadIcon(app)
        assertEquals(1, result?.body()?.size)
    }

    @Test
    fun deleteIcon_returnsResponseOnSuccess() = runBlocking {
        val call = ImmediateCall(createIconAnswer())
        val api = object : EvaluationApi by failingApi() {
            override fun deleteIcon(id: Int) = call
        }
        setApi(api)

        val result = service.deleteIcon(99)
        assertEquals(1, result?.body()?.id)
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
        val call = ThrowingCall<ArrayList<IconAnswer>>(IOException("net down"))
        val api = object : EvaluationApi by failingApi() {
            override fun addIcon(image: okhttp3.MultipartBody.Part) = call
        }
        setApi(api)

        val result = service.uploadIcon(app)
        assertNull(result)
    }

    @Test
    fun deleteIcon_returnsNullOnIOException() = runBlocking {
        val call = ThrowingCall<IconAnswer>(IOException("delete failed"))
        val api = object : EvaluationApi by failingApi() {
            override fun deleteIcon(id: Int) = call
        }
        setApi(api)

        val result = service.deleteIcon(123)
        assertNull(result)
    }

    private fun setField(name: String, value: Any) {
        org.robolectric.util.ReflectionHelpers.setField(service, name, value)
    }

    private fun setApi(api: EvaluationApi) {
        setField("evaluationsApi", api)
    }

    private fun failingApi(): EvaluationApi = object : EvaluationApi {
        override fun listLatestEvaluationsAsync(root: Int, pageNumber: Int) = throw NotImplementedError()
        override fun searchAsync(name: String, packageName: String, rooted: Int) = throw NotImplementedError()
        override fun existingEvaluationsAsync(packageName: String) = throw NotImplementedError()
        override fun addEvaluation(evaluation: UploadEvaluationHeader) = throw NotImplementedError()
        override fun updateEvaluation(evaluation: UploadEvaluationHeader, id: Int) = throw NotImplementedError()
        override fun addIcon(image: okhttp3.MultipartBody.Part) = throw NotImplementedError()
        override fun existingIconAsync(iconName: String) = throw NotImplementedError()
        override fun deleteIcon(id: Int) = throw NotImplementedError()
        override fun getSingleEvaluationAsync(packageName: String, microG: Int, rooted: Int) = throw NotImplementedError()
    }

    private class ThrowingCall<T>(private val error: IOException) : retrofit2.Call<T> {
        override fun clone(): retrofit2.Call<T> = ThrowingCall(error)
        override fun execute(): retrofit2.Response<T> { throw error }
        override fun enqueue(callback: retrofit2.Callback<T>) { callback.onFailure(this, error) }
        override fun isExecuted(): Boolean = false
        override fun cancel() {}
        override fun isCanceled(): Boolean = false
        override fun request(): okhttp3.Request = okhttp3.Request.Builder().url("http://localhost").build()
        override fun timeout(): okio.Timeout = okio.Timeout.NONE
    }

    private class ImmediateCall<T>(private val value: T) : retrofit2.Call<T> {
        override fun clone(): retrofit2.Call<T> = ImmediateCall(value)
        override fun execute(): retrofit2.Response<T> = retrofit2.Response.success(value)
        override fun enqueue(callback: retrofit2.Callback<T>) { callback.onResponse(this, execute()) }
        override fun isExecuted(): Boolean = true
        override fun cancel() {}
        override fun isCanceled(): Boolean = false
        override fun request(): okhttp3.Request = okhttp3.Request.Builder().url("http://localhost").build()
        override fun timeout(): okio.Timeout = okio.Timeout.NONE
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
            iconUrl = null,
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
