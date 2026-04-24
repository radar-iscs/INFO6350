package com.example.epay.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEDC8),
    onPrimaryContainer = Color(0xFF0F2A12),
    secondary = Color(0xFF66BB6A),
    background = Color(0xFFF7FBF6),
    surface = Color.White,
    onSurface = Color(0xFF1B1F1B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0F2A12),
    primaryContainer = Color(0xFF2E4A33),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF101411),
    surface = Color(0xFF161B17),
    onSurface = Color(0xFFE2E7E3)
)

@Composable
fun EPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}