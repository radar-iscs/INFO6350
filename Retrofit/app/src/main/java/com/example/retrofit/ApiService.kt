package com.example.mamounclass3


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // GET /add?a=1&b=2
    @GET("add")
    fun addGet(
        @Query("a") a: Double,
        @Query("b") b: Double
    ): Call<AddResponse>

    // POST /add  { "a": 1, "b": 2 }
    @POST("add")
    fun addPost(
        @Body request: AddRequest
    ): Call<AddResponse>
}
