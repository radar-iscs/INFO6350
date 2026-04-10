package com.example.navigator.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navigator.data.settings.AppSettings
import com.example.navigator.data.settings.ProviderType
import com.example.navigator.data.repository.TranslationRepository
import kotlinx.coroutines.launch
class TranslatorViewModel : ViewModel() {
    private val repository = TranslationRepository()

    var appSettings by mutableStateOf(
        AppSettings(
            provider = ProviderType.OPENAI,
            baseUrl = "http://10.0.2.2:8080/",
            defaultLanguage = "English"
        )
    )

    // Translator screen state
    var inputText by mutableStateOf("")
    var translatedText by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Update provider and URL from settings screen
    fun updateSettings(newSettings: AppSettings) {
        appSettings = newSettings
    }
    fun translate() {
        if (inputText.isBlank()) {
            errorMessage = "Please enter text to translate."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.translate(inputText, appSettings)
                translatedText = response.translatedText
            } catch (e: Exception) {
                translatedText = ""
                errorMessage = e.message ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }
}