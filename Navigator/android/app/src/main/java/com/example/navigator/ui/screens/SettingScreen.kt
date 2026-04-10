package com.example.navigator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigator.data.settings.AppSettings
import com.example.navigator.data.settings.ProviderType
import com.example.navigator.ui.viewmodel.TranslatorViewModel
@Composable
fun SettingsScreen(
    viewModel: TranslatorViewModel,
    onSaveAndBack: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(viewModel.appSettings.provider) }
    var baseUrl by remember { mutableStateOf(viewModel.appSettings.baseUrl) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Choose Provider")
        ProviderOption(
            title = "Google",
            selected = selectedProvider == ProviderType.GOOGLE,
            onClick = {
                selectedProvider = ProviderType.GOOGLE
                baseUrl = "http://10.0.2.2:8080/"
            }
        )
        ProviderOption(
            title = "ChatGPT / OpenAI",
            selected = selectedProvider == ProviderType.OPENAI,
            onClick = {
                selectedProvider = ProviderType.OPENAI
                baseUrl = "http://10.0.2.2:8080/"
            }
        )
        ProviderOption(
            title = "Open Source",
            selected = selectedProvider == ProviderType.OPEN_SOURCE,
            onClick = {
                selectedProvider = ProviderType.OPEN_SOURCE
                baseUrl = "http://10.0.2.2:8080/"
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Default Language: English")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.updateSettings(
                    AppSettings(
                        provider = selectedProvider,
                        baseUrl = baseUrl,
                        defaultLanguage = "English"
                    )
                )
                onSaveAndBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
@Composable
private fun ProviderOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = title,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
