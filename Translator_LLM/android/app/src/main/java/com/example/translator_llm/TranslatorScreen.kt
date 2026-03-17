package com.example.translator_llm

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@Composable
fun TranslatorScreen(apiKey: String) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Initialize the Gemini Model
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(text = "English → Chinese", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter English text") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        try {
                            // Prompt engineering for accurate translation
                            val prompt = "Translate the following English text to Simplified Chinese. Only output the translated text and nothing else:\n$inputText"
                            val response = generativeModel.generateContent(prompt)

                            // .trim() removes any accidental whitespace Gemini might add
                            resultText = response.text?.trim() ?: "Translation failed."
                        } catch (e: Exception) {
                            Log.e("TranslateApp", "Error", e)
                            resultText = "Error: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Translate")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (resultText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chinese Translation:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SelectionContainer {
                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}