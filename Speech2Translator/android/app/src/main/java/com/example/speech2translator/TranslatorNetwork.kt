package com.example.speech2translator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun translateText(textToTranslate: String, user: UserProfile): String {
    val url = "http://10.0.2.2:5000/translate"

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            val jsonBody = JSONObject()
            jsonBody.put("text", textToTranslate)

            val userJson = JSONObject()
            userJson.put("name", user.name)
            userJson.put("email", user.email)

            jsonBody.put("user", userJson)

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer ${user.idToken}")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                val jsonResponse = JSONObject(responseData)
                jsonResponse.getString("translated_text")
            } else {
                "Server Error: ${response.code}"
            }

        } catch (e: Exception) {
            Log.e("TranslateApp", "Error", e)
            "Failed to connect."
        }
    }
}