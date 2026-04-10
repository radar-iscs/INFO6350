package com.example.navigator.data.remote

import com.example.navigator.data.model.TranslationRequest
import com.example.navigator.data.model.TranslationResponse
import retrofit2.http.Body
import retrofit2.http.POST
interface TranslationApi {
    @POST("translate")
    suspend fun translateText(
        @Body request: TranslationRequest
    ): TranslationResponse
}
