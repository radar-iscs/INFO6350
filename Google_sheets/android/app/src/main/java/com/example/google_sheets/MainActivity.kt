package com.example.google_sheets

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

data class UserProfile(val name: String, val email: String, val idToken: String)

// A simple wrapper to help our UI know if the API call was successful
data class ApiResult(val isSuccess: Boolean, val message: String)

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
    var currentUser by remember { mutableStateOf<UserProfile?>(null) }
    // add context and a coroutine scope to handle the logout action
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { name, email, idToken ->
                currentUser = UserProfile(name, email, idToken)
            }
        )
    } else {
        RecordFormScreen(
            user = currentUser!!,
            onLogout = {
                coroutineScope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        Log.d("Auth", "Successfully cleared credential state.")
                    } catch (e: Exception) {
                        Log.e("Auth", "Failed to clear credential state", e)
                    } finally {
                        currentUser = null
                    }
                }
            }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (String, String, String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sheets Logger App", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("You must be logged in to submit records.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val credentialManager = CredentialManager.create(context)
                    val webClientId = BuildConfig.WEB_CLIENT_ID

                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    try {
                        val result = credentialManager.getCredential(request = request, context = context)
                        val credential = result.credential

                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val name = googleIdTokenCredential.displayName ?: "User"
                                val email = googleIdTokenCredential.id
                                val idToken = googleIdTokenCredential.idToken

                                onLoginSuccess(name, email, idToken)
                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e("Auth", "Invalid google id token response", e)
                            }
                        }
                    } catch (e: GetCredentialException) {
                        Log.e("Auth", "Login failed", e)
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
fun RecordFormScreen(user: UserProfile, onLogout: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var timeIn by remember { mutableStateOf("") }
    var timeOut by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var resultMessage by remember { mutableStateOf("") }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
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

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Log New Record", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = timeIn, onValueChange = { timeIn = it }, label = { Text("Time In (HH:MM)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = timeOut, onValueChange = { timeOut = it }, label = { Text("Time Out (HH:MM)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = submitRecordToSheets(user.idToken, firstName, lastName, timeIn, timeOut, notes)
                    resultMessage = result.message
                    isSuccessMessage = result.isSuccess

                    if (result.isSuccess) {
                        // Clear the form fields upon successful submission
                        firstName = ""
                        lastName = ""
                        timeIn = ""
                        timeOut = ""
                        notes = ""
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Submit to Sheets")
            }
        }

        if (resultMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = resultMessage,
                color = if (isSuccessMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

suspend fun submitRecordToSheets(
    idToken: String,
    firstName: String,
    lastName: String,
    timeIn: String,
    timeOut: String,
    notes: String
): ApiResult {
    val url = "http://10.0.2.2:5000/api/record"

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val jsonBody = JSONObject().apply {
                put("first_name", firstName)
                put("last_name", lastName)
                put("time_in", timeIn)
                put("time_out", timeOut)
                put("notes", notes)
                put("resource", "Android")
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $idToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                val jsonResponse = JSONObject(responseData)
                ApiResult(true, jsonResponse.getString("message"))
            } else if (response.code == 401) {
                ApiResult(false, "Error: 401 Unauthorized. Token may be invalid.")
            } else {
                ApiResult(false, "Server Error: ${response.code}")
            }

        } catch (e: Exception) {
            Log.e("SheetsApp", "Error", e)
            ApiResult(false, "Failed to connect. Is the Flask server running?")
        }
    }
}