package com.example.google_sheets

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecordViewModel : ViewModel() {
    var submissionStatus = mutableStateOf("")
        private set

    fun submitData(
        idToken: String,
        firstName: String,
        lastName: String,
        timeIn: String,
        timeOut: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val record = RecordData(firstName, lastName, timeIn, timeOut, notes)
                val bearerToken = "Bearer $idToken"
                val response = RetrofitClient.apiService.submitRecord(bearerToken, record)
                if (response.isSuccessful) {
                    submissionStatus.value = "Success: ${response.body()?.message}"
                    onSuccess()
                } else {
                    submissionStatus.value = "Error: Server returned code ${response.code()}"
                }
            } catch (e: Exception) {
                submissionStatus.value = "Exception: ${e.message}"
            }
        }
    }
}