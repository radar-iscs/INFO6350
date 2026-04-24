package com.example.epay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.epay.screens.CardSetupScreen
import com.example.epay.screens.HistoryScreen
import com.example.epay.screens.LoginScreen
import com.example.epay.screens.PaymentScreen
import com.example.epay.ui.EPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = (application as EPayApp).repository

        setContent {
            EPayTheme {
                Surface(Modifier.fillMaxSize()) {
                    val nav = rememberNavController()
                    NavHost(nav, startDestination = "login") {
                        composable("login") {
                            LoginScreen(onSignedIn = {
                                nav.navigate("card_setup") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("card_setup") {
                            CardSetupScreen(
                                onContinue = { nav.navigate("payment") },
                                onHistory  = { nav.navigate("history") },
                                onSignOut  = {
                                    nav.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable("payment") {
                            PaymentScreen(
                                repository = repo,
                                onBack = { nav.popBackStack() },
                                onHistory = { nav.navigate("history") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                repository = repo,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}