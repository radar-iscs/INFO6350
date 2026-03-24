package com.example.speech2translator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isOnline by rememberNetworkConnectivityState()

    if (currentUser == null) {
        LoginScreen(
            isOnline = isOnline,
            onLoginSuccess = { name, email, idToken ->
                currentUser = UserProfile(name, email, idToken)
            }
        )
    } else {
        SpeechTranslatorScreen(
            user = currentUser!!,
            isOnline = isOnline,
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