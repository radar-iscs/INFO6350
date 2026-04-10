package com.example.navigator.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.navigator.ui.screens.SettingsScreen
import com.example.navigator.ui.screens.SplashScreen
import com.example.navigator.ui.screens.TranslatorScreen
import com.example.navigator.ui.viewmodel.TranslatorViewModel
object Routes {
    const val SPLASH = "splash"
    const val TRANSLATOR = "translator"
    const val SETTINGS = "settings"
}
@Composable
fun AppNavGraph(navController: NavHostController) {
    val viewModel: TranslatorViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Routes.TRANSLATOR) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TRANSLATOR) {
            TranslatorScreen(
                viewModel = viewModel,
                onGoToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onSaveAndBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.TRANSLATOR) {
            TranslatorScreen(
                viewModel = viewModel,
                onGoToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onSaveAndBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}