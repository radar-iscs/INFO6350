package com.example.sso

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

// 1. Create a data class to hold the logged-in user's information
data class UserProfile(val name: String, val email: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppScreen()
                }
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    // 2. State hoisting: Track the user at the top level
    var currentUser by remember { mutableStateOf<UserProfile?>(null) }

    // 3. Conditional navigation based on login state
    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { name, email ->
                currentUser = UserProfile(name, email)
            }
        )
    } else {
        TranslatorScreen(
            user = currentUser!!,
            onLogout = { currentUser = null }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit) {
    // We need the Context and a CoroutineScope to launch the login request
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Translator App", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("You must be logged in to use the translator.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val credentialManager = CredentialManager.create(context)

                    val webClientId = "51840671724-nlk5dtk5h24ofb7qvugg5jkcsm5b7gsn.apps.googleusercontent.com"

                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    try {
                        // This triggers the Google Bottom Sheet UI!
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )

                        val credential = result.credential

                        // Parse the result into a Google ID Token
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                                val name = googleIdTokenCredential.displayName ?: "User"
                                val email = googleIdTokenCredential.id
                                val idToken = googleIdTokenCredential.idToken // <-- We will need this for Flask later!

                                Log.d("Auth", "Login Successful! Token: $idToken")

                                // Pass the data to update the UI
                                onLoginSuccess(name, email)

                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e("Auth", "Received an invalid google id token response", e)
                            }
                        }
                    } catch (e: GetCredentialException) {
                        Log.e("Auth", "Login failed or was cancelled by the user", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Sign in with Google")
        }
    }
}

@Composable
fun TranslatorScreen(user: UserProfile, onLogout: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 4. Add the User Profile Header and Logout Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Welcome, ${user.name}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
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
                        val translation = translateText(inputText)
                        resultText = translation
                        isLoading = false
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

suspend fun translateText(textToTranslate: String): String {
    val url = "http://10.0.2.2:5000/translate"

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val jsonBody = JSONObject()
            jsonBody.put("text", textToTranslate)
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                val jsonResponse = JSONObject(responseData)
                jsonResponse.getString("translated_text")
            } else if (response.code == 401) {
                // 5. Catch the specific Unauthorized error from Flask
                "Error: 401 Unauthorized. The Android app needs to send authentication data."
            } else {
                "Server Error: ${response.code}"
            }

        } catch (e: Exception) {
            Log.e("TranslateApp", "Error", e)
            "Failed to connect. Is the server running?"
        }
    }
}