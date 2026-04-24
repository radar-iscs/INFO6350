package com.example.epay.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.epay.auth.signInWithGoogle
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("ePay",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Secure payments through Stripe",
            style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                if (loading) return@Button
                loading = true; error = null
                scope.launch {
                    signInWithGoogle(context)
                        .onSuccess { onSignedIn() }
                        .onFailure { error = it.localizedMessage ?: "Sign-in failed" }
                    loading = false
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading) CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
            ) else Text("Continue with Google", fontWeight = FontWeight.SemiBold)
        }

        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}