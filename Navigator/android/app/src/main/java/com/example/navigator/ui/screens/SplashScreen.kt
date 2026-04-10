package com.example.navigator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    // wait 2 seconds then move to the translator screen
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateNext()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Translator App",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Jetpack Compose + MVVM + Retrofit",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
