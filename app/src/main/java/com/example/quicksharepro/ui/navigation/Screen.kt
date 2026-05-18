package com.example.quicksharepro.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Send : Screen("send")
    object Receive : Screen("receive")
    object FilePicker : Screen("filepicker")
    object TransferProgress : Screen("transfer_progress")
    object History : Screen("history")
    object Settings : Screen("settings")
}
