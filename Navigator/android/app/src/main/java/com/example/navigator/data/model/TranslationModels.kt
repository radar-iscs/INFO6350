package com.example.navigator.data.model

data class TranslationRequest(
    val text: String,
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en",
    val provider: String
)

data class TranslationResponse(
    val translatedText: String,
    val providerUsed: String? = null,
    val detectedLanguage: String? = null
)
