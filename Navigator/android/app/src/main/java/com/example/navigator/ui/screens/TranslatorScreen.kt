package com.example.navigator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigator.ui.viewmodel.TranslatorViewModel

@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel,
    onGoToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Translator",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Provider: ${viewModel.appSettings.provider}")
        Text(text = "Base URL: ${viewModel.appSettings.baseUrl}")
        Text(text = "Default language: ${viewModel.appSettings.defaultLanguage}")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.inputText,
            onValueChange = { viewModel.inputText = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.translate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Translate")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onGoToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            Text("Loading...")
        }
        viewModel.errorMessage?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
        }
        if (viewModel.translatedText.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Translated Result",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = viewModel.translatedText)
                }
            }
        }
    }
}