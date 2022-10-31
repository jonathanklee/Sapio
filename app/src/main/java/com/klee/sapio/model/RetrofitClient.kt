package com.klee.sapio.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.ByteArrayOutputStream

object RetrofitClient {

    const val BASE_URL = "http://192.168.1.42:1337"

    val okHttpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    fun getClient(): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("$BASE_URL/api/")
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
}

interface EvaluationApi {
    @GET("sapio-applications?populate=*")
    fun getEvaluationsAsync(): Deferred<StrapiAnswer>

    @Headers("Content-Type: application/json")
    @POST("sapio-applications")
    fun addEvaluation(@Body evaluation: UploadEvaluation): Call<UploadAnswer>

    @Headers("Content-Type: application/json")
    @PUT("sapio-applications/{id}")
    fun updateEvaluation(
        @Body evaluation: UploadEvaluation,
        @Path(value = "id", encoded = false) id: Int
    ): Call<UploadAnswer>

    @Multipart
    @POST("upload")
    fun addIcon(@Part image: MultipartBody.Part): Call<ArrayList<UploadIconAnswer>>
}

class EvaluationService {

    private val retrofit = RetrofitClient.getClient()
    private val evaluationsApi = retrofit.create(EvaluationApi::class.java)

    suspend fun getAllEvaluations(): List<RemoteEvaluation> {
        val list = ArrayList<RemoteEvaluation>()
        val strapiAnswer = fetchEvaluations()

        strapiAnswer.data.map {
            list.add(it.attributes)
        }

        return list.sortedByDescending { it.updatedAt }
    }

    suspend fun searchEvaluation(pattern: String): List<RemoteEvaluation> {
        val list = ArrayList<RemoteEvaluation>()

        val strapiAnswer = fetchEvaluations()

        strapiAnswer.data.map {
            val app = it.attributes
            if (app.name.contains(pattern, true) || app.packageName.contains(pattern, true)) {
                list.add(app)
            }
        }

        return list
    }

    suspend fun getEvaluationsRawData(): List<StrapiElement> {
        val strapiAnswer = fetchEvaluations()
        val list = ArrayList<StrapiElement>()

        strapiAnswer.data.map {
            list.add(it)
        }

        return list
    }

    private suspend fun fetchEvaluations(): StrapiAnswer {
        var strapiAnswer: StrapiAnswer

        withContext(Dispatchers.IO) {
            strapiAnswer = evaluationsApi.getEvaluationsAsync().await()
        }

        return strapiAnswer
    }

    suspend fun addEvaluation(app: UploadEvaluation): Response<UploadAnswer> {
        var response: Response<UploadAnswer>

        withContext(Dispatchers.IO) {
            response = evaluationsApi.addEvaluation(app).execute()
        }

        return response
    }

    suspend fun updateEvaluation(app: UploadEvaluation, id: Int): Response<UploadAnswer> {
        var response: Response<UploadAnswer>

        withContext(Dispatchers.IO) {
            response = evaluationsApi.updateEvaluation(app, id).execute()
        }

        return response
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>> {
        var response: Response<ArrayList<UploadIconAnswer>>

        val bytes = fromDrawableToByArray(icon)
        val requestBody = bytes.toRequestBody(null, 0, bytes.size)
        val image = MultipartBody.Part.createFormData("files", "plop.png", requestBody)
        withContext(Dispatchers.IO) {
            response = evaluationsApi.addIcon(image).execute()
        }

        return response
    }

    private fun fromDrawableToByArray(drawable: Drawable): ByteArray {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
