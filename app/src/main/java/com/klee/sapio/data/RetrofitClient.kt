package com.klee.sapio.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

interface EvaluationApi {
    @GET("sapio-applications?populate=*&pagination[pageSize]=100&sort=updatedAt:Desc")
    fun listLatestEvaluationsAsync(): Deferred<StrapiAnswer>

    @GET("sapio-applications?populate=*&sort=name")
    fun searchAsync(
        @Query("filters[\$or][0][name][\$contains]") name: String,
        @Query("filters[\$or][1][packageName][\$contains]") packageName: String
    ): Deferred<StrapiAnswer>

    @GET("sapio-applications?")
    fun existingEvaluationsAsync(
        @Query("filters[packageName][\$eq]") packageName: String
    ): Deferred<StrapiAnswer>

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

    @GET("upload/files?")
    fun existingIconAsync(
        @Query("filters[name][\$eq]") iconName: String
    ): Deferred<List<UploadIconAnswer>>

    @GET("sapio-applications?")
    fun getSingleEvaluationAsync(
        @Query("filters[\$and][0][packageName][\$contains]") packageName: String,
        @Query("filters[\$and][1][microG][\$contains]") microG: Int,
        @Query("filters[\$and][2][rooted][\$contains]") rooted: Int
    ): Deferred<StrapiAnswer>
}

class EvaluationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val BASE_URL = "https://sapio.ovh"
    }

    private var retrofit: Retrofit
    private var evaluationsApi: EvaluationApi

    init {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("$BASE_URL/api/")
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        evaluationsApi = retrofit.create(EvaluationApi::class.java)
    }

    suspend fun listLatestEvaluations(): List<Evaluation> {
        val list = ArrayList<Evaluation>()
        val strapiAnswer = listEvaluationsForFeed() ?: return ArrayList()

        strapiAnswer.data.map {
            list.add(it.attributes)
        }

        return list.sortedByDescending { it.updatedAt }
    }

    suspend fun searchEvaluation(pattern: String): List<Evaluation> {
        val list = ArrayList<Evaluation>()

        val strapiAnswer = searchEvaluations(pattern) ?: return ArrayList()
        strapiAnswer.data.map {
            list.add(it.attributes)
        }

        return list
    }

    suspend fun existingEvaluations(packageName: String): List<StrapiElement> {
        val strapiAnswer = getExistingEvaluations(packageName) ?: return ArrayList()

        val list = ArrayList<StrapiElement>()
        strapiAnswer.data.map {
            list.add(it)
        }

        return list
    }

    private suspend fun listEvaluationsForFeed(): StrapiAnswer? {
        var strapiAnswer: StrapiAnswer? = null

        withContext(Dispatchers.IO) {
            try {
                strapiAnswer = evaluationsApi.listLatestEvaluationsAsync().await()
            } catch (_: IOException) {}
        }

        return strapiAnswer
    }

    private suspend fun searchEvaluations(pattern: String): StrapiAnswer? {
        var strapiAnswer: StrapiAnswer? = null

        withContext(Dispatchers.IO) {
            try {
                strapiAnswer = evaluationsApi.searchAsync(pattern, pattern).await()
            } catch (_: IOException) {}
        }

        return strapiAnswer
    }

    private suspend fun getExistingEvaluations(packageName: String): StrapiAnswer? {
        var strapiAnswer: StrapiAnswer? = null

        withContext(Dispatchers.IO) {
            try {
                strapiAnswer = evaluationsApi.existingEvaluationsAsync(packageName).await()
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

    suspend fun uploadIcon(app: InstalledApplication): Response<ArrayList<UploadIconAnswer>>? {
        var response: Response<ArrayList<UploadIconAnswer>>? = null

        val bytes = fromDrawableToByArray(app.icon)
        val requestBody = bytes.toRequestBody(null, 0, bytes.size)
        val image = MultipartBody.Part.createFormData(
            "files",
            "${app.packageName}.png",
            requestBody
        )

        withContext(Dispatchers.IO) {
            try {
                response = evaluationsApi.addIcon(image).execute()
            } catch (_: IOException) {}
        }

        return response
    }

    suspend fun existingIcon(iconName: String): List<UploadIconAnswer> {
        var remotesImage: List<UploadIconAnswer>? = null

        withContext(Dispatchers.IO) {
            try {
                remotesImage = evaluationsApi.existingIconAsync(iconName).await()
            } catch (_: IOException) {}
        }

        return remotesImage!!
    }

    suspend fun fetchEvaluation(appPackageName: String, microG: Int, rooted: Int): Evaluation? {
        var answer: StrapiAnswer? = null

        withContext(Dispatchers.IO) {
            try {
                answer = evaluationsApi.getSingleEvaluationAsync(
                    appPackageName,
                    microG,
                    rooted
                ).await()
            } catch (_: IOException) {
            }
        }

        if (answer?.data?.size!! <= 0) {
            return null
        }

        return answer?.data?.get(0)?.attributes
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
