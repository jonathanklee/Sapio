package com.klee.sapio.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.klee.sapio.data.dto.Evaluation
import com.klee.sapio.data.dto.IconAnswer
import com.klee.sapio.data.dto.StrapiAnswer
import com.klee.sapio.data.dto.StrapiElement
import com.klee.sapio.data.dto.UploadAnswer
import com.klee.sapio.data.dto.UploadEvaluationHeader
import com.klee.sapio.data.system.Settings
import com.klee.sapio.domain.model.InstalledApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withTimeout
import okhttp3.Cache
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import javax.inject.Inject

interface EvaluationApi {
    @GET("sapio-applications?pagination[pageSize]=10&sort=updatedAt:Desc")
    suspend fun listLatestEvaluationsAsync(
        @Query("filters[rooted][\$lte]") root: Int,
        @Query("pagination[page]") pageNumber: Int
    ): StrapiAnswer

    @GET("sapio-applications?sort=name")
    suspend fun searchAsync(
        @Query("filters[\$or][0][name][\$contains]") name: String,
        @Query("filters[\$or][1][packageName][\$contains]") packageName: String,
        @Query("filters[\$and][2][rooted][\$lte]") rooted: Int
    ): StrapiAnswer

    @GET("sapio-applications?")
    suspend fun existingEvaluationsAsync(
        @Query("filters[packageName][\$eq]") packageName: String
    ): StrapiAnswer

    @Headers("Content-Type: application/json")
    @POST("sapio-applications")
    suspend fun addEvaluation(@Body evaluation: UploadEvaluationHeader): UploadAnswer

    @Headers("Content-Type: application/json")
    @PUT("sapio-applications/{id}")
    suspend fun updateEvaluation(
        @Body evaluation: UploadEvaluationHeader,
        @Path(value = "id", encoded = false) id: Int
    ): UploadAnswer

    @Multipart
    @POST("upload")
    suspend fun addIcon(@Part image: MultipartBody.Part): ArrayList<IconAnswer>

    @GET("upload/files?sort=updatedAt")
    suspend fun existingIconAsync(
        @Query("filters[name][\$eq]") iconName: String
    ): List<IconAnswer>

    @DELETE("upload/files/{id}")
    suspend fun deleteIcon(
        @Path(value = "id", encoded = false) id: Int
    ): IconAnswer

    @GET("sapio-applications?sort=updatedAt:Desc")
    suspend fun getSingleEvaluationAsync(
        @Query("filters[\$and][0][packageName][\$eq]") packageName: String,
        @Query("filters[\$and][1][microG][\$contains]") microG: Int,
        @Query("filters[\$and][2][rooted][\$contains]") rooted: Int
    ): StrapiAnswer
}

class EvaluationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val BASE_URL = "https://server.sapio.ovh"
        const val COMPRESSION_QUALITY = 100
        const val UPLOAD_TIMEOUT_MS: Long = 10000
        const val CACHE_MAX_SIZE = 10 * 1024 * 1024L
    }

    @Inject
    lateinit var settings: Settings
    private var retrofit: Retrofit
    private var evaluationsApi: EvaluationApi

    init {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .cache(Cache(context.cacheDir, CACHE_MAX_SIZE))
            .build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("$BASE_URL/api/")
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        evaluationsApi = retrofit.create(EvaluationApi::class.java)
    }

    suspend fun listLatestEvaluations(pageNumber: Int): Result<List<Evaluation>> =
        runCatching {
            val strapiAnswer = evaluationsApi.listLatestEvaluationsAsync(
                settings.getRootConfigurationLevel(),
                pageNumber
            )

            strapiAnswer.data.map { it.attributes }
                .distinctBy { it.packageName }
                .sortedByDescending { it.updatedAt }
        }.onFailure { exception ->
            if (exception is HttpException) {
                Log.i("EvaluationService", "HttpException: $exception")
            }
        }

    suspend fun searchEvaluation(pattern: String): Result<List<Evaluation>> =
        runCatching {
            val strapiAnswer = evaluationsApi.searchAsync(
                pattern,
                pattern,
                settings.getRootConfigurationLevel()
            )

            strapiAnswer.data.map { it.attributes }
                .distinctBy { it.packageName }
        }

    suspend fun existingEvaluations(packageName: String): Result<List<StrapiElement>> =
        runCatching {
            val strapiAnswer = evaluationsApi.existingEvaluationsAsync(packageName)
            strapiAnswer.data.toList()
        }

    suspend fun addEvaluation(app: UploadEvaluationHeader): Result<Unit> =
        runCatching {
            evaluationsApi.addEvaluation(app)
            Unit
        }

    suspend fun updateEvaluation(app: UploadEvaluationHeader, id: Int): Result<Unit> =
        runCatching {
            evaluationsApi.updateEvaluation(app, id)
            Unit
        }

    suspend fun uploadIcon(app: InstalledApplication): Result<List<IconAnswer>> {
        val bytes = fromDrawableToByArray(app.icon)
        val requestBody = bytes.toRequestBody(null, 0, bytes.size)
        val image = MultipartBody.Part.createFormData(
            "files",
            "${app.packageName}.png",
            requestBody
        )

        return runCatching {
            withTimeout(UPLOAD_TIMEOUT_MS) {
                evaluationsApi.addIcon(image)
            }
        }
    }

    suspend fun existingIcon(iconName: String): Result<List<IconAnswer>> =
        runCatching {
            evaluationsApi.existingIconAsync(iconName)
        }

    suspend fun deleteIcon(id: Int): Result<Unit> =
        runCatching {
            evaluationsApi.deleteIcon(id)
            Unit
        }

    suspend fun fetchEvaluation(appPackageName: String, microG: Int, rooted: Int): Result<Evaluation?> =
        runCatching {
            val answer = evaluationsApi.getSingleEvaluationAsync(
                appPackageName,
                microG,
                rooted
            )

            answer.data.firstOrNull()?.attributes
        }

    private fun fromDrawableToByArray(drawable: Drawable): ByteArray {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream)
        return stream.toByteArray()
    }
}
