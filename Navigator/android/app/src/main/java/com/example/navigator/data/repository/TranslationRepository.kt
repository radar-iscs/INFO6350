package com.example.navigator.data.repository

import com.example.navigator.data.settings.AppSettings
import com.example.navigator.data.model.TranslationRequest
import com.example.navigator.data.model.TranslationResponse
import com.example.navigator.data.remote.RetrofitProvider

class TranslationRepository {
    suspend fun translate(
        text: String,
        settings: AppSettings
    ): TranslationResponse {
        val api = RetrofitProvider.create(settings.baseUrl)
        val providerName = when (settings.provider) {
            com.example.navigator.data.settings.ProviderType.GOOGLE -> "Google"
            com.example.navigator.data.settings.ProviderType.OPENAI -> "ChatGPT/OpenAI"
            com.example.navigator.data.settings.ProviderType.OPEN_SOURCE -> "Open Source"
        }
        return api.translateText(
            TranslationRequest(
                text = text,
                targetLanguage = "en",
                provider = providerName
            )
        )
    }
}
