package com.example.aiwallpaper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.generator.GeneratorScreen
import com.example.aiwallpaper.ui.history.HistoryScreen
import com.example.aiwallpaper.ui.onboarding.OnboardingScreen
import com.example.aiwallpaper.ui.result.ResultScreen

private object Routes {
    const val ONBOARDING = "onboarding"
    const val GENERATOR = "generator"
    const val RESULT = "result/{historyId}"
    const val HISTORY = "history"

    fun result(historyId: Long) = "result/$historyId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val hasKey = remember { AppContainer.apiKeyRepository().hasApiKey() }
    val start = if (hasKey) Routes.GENERATOR else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onSetupComplete = {
                    navController.navigate(Routes.GENERATOR) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.GENERATOR) {
            GeneratorScreen(
                onNavigateToResult = { historyId ->
                    navController.navigate(Routes.result(historyId))
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.RESULT) { backStack ->
            val historyId = backStack.arguments?.getString("historyId")?.toLongOrNull() ?: -1L
            ResultScreen(
                historyId = historyId,
                onBack = { navController.popBackStack() },
                onRegenerate = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onItemClick = { historyId ->
                    navController.navigate(Routes.result(historyId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
