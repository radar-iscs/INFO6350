package com.example.navigator.data.settings

enum class ProviderType {
    GOOGLE,
    OPENAI,
    OPEN_SOURCE
}

data class AppSettings(
    val provider: ProviderType = ProviderType.OPENAI,
    val baseUrl: String = "https://your-openai-proxy.example.com/",
    val defaultLanguage: String = "English"
)