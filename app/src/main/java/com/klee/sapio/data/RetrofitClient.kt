package com.klee.sapio.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.graphics.drawable.toBitmap
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
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
import java.io.IOException
import javax.inject.Inject

interface EvaluationApi {
    @GET("sapio-applications?populate=*")
    fun getEvaluationsAsync(): Deferred<StrapiAnswer>

    @Headers("Content-Type: application/json")
    @POST("sapio-applications")
    fun addEvaluation(@Body evaluation: UploadEvaluationHeader): Call<UploadAnswer>

    @Headers("Content-Type: application/json")
    @PUT("sapio-applications/{id}")
    fun updateEvaluation(
        @Body evaluation: UploadEvaluationHeader,
        @Path(value = "id", encoded = false) id: Int
    ): Call<UploadAnswer>

    @Multipart
    @POST("upload")
    fun addIcon(@Part image: MultipartBody.Part): Call<ArrayList<UploadIconAnswer>>
}

class EvaluationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val TAG = "EvaluationService"
        const val BASE_URL = "https://sapio.ovh"
    }

    private var retrofit: Retrofit
    private var evaluationsApi: EvaluationApi

    init {
        val certificatePinner = CertificatePinner.Builder()
            .add("sapio.ovh", "sha256/H6WgKeLXoWQUBdPJZWJNzD5X/vW/9TN2+HQ3mWD7pek=")
            .build()

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .certificatePinner(certificatePinner)
            .build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("$BASE_URL/api/")
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        evaluationsApi = retrofit.create(EvaluationApi::class.java)
    }

    suspend fun getAllEvaluations(): List<Evaluation> {
        val list = ArrayList<Evaluation>()
        val strapiAnswer = fetchEvaluations() ?: return ArrayList()

        strapiAnswer.data.map {
            list.add(it.attributes)
        }

        return list.sortedByDescending { it.updatedAt }
    }

    suspend fun searchEvaluation(pattern: String): List<Evaluation> {
        val list = ArrayList<Evaluation>()

        val strapiAnswer = fetchEvaluations() ?: return ArrayList()

        strapiAnswer.data.map {
            val app = it.attributes
            if (app.name.contains(pattern, true) || app.packageName.contains(pattern, true)) {
                list.add(app)
            }
        }

        return list
    }

    suspend fun getEvaluationsRawData(): List<StrapiElement> {
        val strapiAnswer = fetchEvaluations() ?: return ArrayList()

        val list = ArrayList<StrapiElement>()

        strapiAnswer.data.map {
            list.add(it)
        }

        return list
    }

    private suspend fun fetchEvaluations(): StrapiAnswer? {
        var strapiAnswer: StrapiAnswer? = null

        withContext(Dispatchers.IO) {
            try {
                strapiAnswer = evaluationsApi.getEvaluationsAsync().await()
            } catch (_: IOException) {}
        }

        return strapiAnswer
    }

    suspend fun addEvaluation(app: UploadEvaluationHeader): Response<UploadAnswer>? {
        var response: Response<UploadAnswer>? = null

        withContext(Dispatchers.IO) {
            try {
                response = evaluationsApi.addEvaluation(app).execute()
            } catch (_: IOException) {}
        }

        return response
    }

    suspend fun updateEvaluation(app: UploadEvaluationHeader, id: Int): Response<UploadAnswer>? {
        var response: Response<UploadAnswer>? = null

        withContext(Dispatchers.IO) {
            try {
                response = evaluationsApi.updateEvaluation(app, id).execute()
            } catch (_: IOException) {}
        }

        return response
    }

    suspend fun uploadIcon(icon: Drawable): Response<ArrayList<UploadIconAnswer>>? {
        var response: Response<ArrayList<UploadIconAnswer>>? = null

        val bytes = fromDrawableToByArray(icon)
        val requestBody = bytes.toRequestBody(null, 0, bytes.size)
        val image = MultipartBody.Part.createFormData("files", "plop.png", requestBody)

        withContext(Dispatchers.IO) {
            try {
                response = evaluationsApi.addIcon(image).execute()
            } catch (_: IOException) {}
        }

        return response
    }

    fun hasConnectivity(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val currentNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
            ?: return false

        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return true
        }

        return false
    }

    private fun fromDrawableToByArray(drawable: Drawable): ByteArray {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
