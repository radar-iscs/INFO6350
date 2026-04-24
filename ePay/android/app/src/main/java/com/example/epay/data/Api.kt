package com.example.epay.data

import com.example.epay.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class PaymentRequest(
    val amount: Long,
    val currency: String,
    val note: String,
    @Json(name = "customer_name")  val customerName: String,
    @Json(name = "customer_email") val customerEmail: String?,
    @Json(name = "google_email")   val googleEmail: String?
)

@JsonClass(generateAdapter = true)
data class PaymentResponse(
    val success: Boolean,
    @Json(name = "payment_intent_id") val paymentIntentId: String?,
    @Json(name = "client_secret") val clientSecret: String?,
    val status: String?,
    val message: String?,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LogRequest(
    @Json(name = "payment_intent_id") val paymentIntentId: String,
    val amount: Long, val currency: String, val note: String, val status: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "google_email") val googleEmail: String?,
    @Json(name = "error_message") val errorMessage: String?
)

@JsonClass(generateAdapter = true)
data class LogResponse(val success: Boolean, val message: String?)

// retrofit interface
interface ApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("payments/create")
    suspend fun createPayment(@Body req: PaymentRequest): PaymentResponse

    @POST("transactions/log")
    suspend fun logTransaction(@Body req: LogRequest): LogResponse
}

//  shared client
object RetrofitClient {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}