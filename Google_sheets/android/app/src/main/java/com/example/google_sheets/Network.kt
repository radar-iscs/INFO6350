package com.example.google_sheets

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface ApiService {
    @POST("/api/record")
    suspend fun submitRecord(
        @Header("Authorization") token: String,
        @Body record: RecordData
    ): Response<ApiResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}