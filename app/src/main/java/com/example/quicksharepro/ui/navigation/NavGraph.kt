package com.example.quicksharepro.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500))
        }
    ) {
        composable(Screen.Splash.route) {
            com.example.quicksharepro.ui.screens.splash.SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            com.example.quicksharepro.ui.screens.home.HomeScreen(
                onNavigateToSend = { navController.navigate(Screen.FilePicker.route) },
                onNavigateToReceive = { navController.navigate(Screen.Receive.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Send.route) {
            com.example.quicksharepro.ui.screens.send.SendScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransfer = {
                    navController.navigate(Screen.TransferProgress.route) {
                        popUpTo(Screen.Send.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Receive.route) {
            com.example.quicksharepro.ui.screens.receive.ReceiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransfer = { 
                    navController.navigate(Screen.TransferProgress.route) {
                        popUpTo(Screen.Receive.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.FilePicker.route) {
            com.example.quicksharepro.ui.screens.filepicker.FilePickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiscovery = { navController.navigate(Screen.Send.route) }
            )
        }
        composable(Screen.TransferProgress.route) {
            com.example.quicksharepro.ui.screens.transfer.TransferProgressScreen(
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = { isReceiver ->
                    if (isReceiver) {
                        navController.navigate(Screen.Receive.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Screen.FilePicker.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                }
            )
        }
        composable(Screen.History.route) {
            com.example.quicksharepro.ui.screens.history.HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            com.example.quicksharepro.ui.screens.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
