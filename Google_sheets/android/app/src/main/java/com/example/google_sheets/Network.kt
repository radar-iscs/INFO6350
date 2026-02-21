package com.example.google_sheets

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// The API Interface
interface ApiService {
    @POST("/api/record")
    suspend fun submitRecord(@Body record: RecordData): Response<ApiResponse>
}

// The Retrofit Singleton
object RetrofitClient {
    // 10.0.2.2 connects the Android Emulator to computer's localhost
    private const val BASE_URL = "http://10.0.2.2:5000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}