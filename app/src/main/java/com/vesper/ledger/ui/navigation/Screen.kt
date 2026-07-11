package com.vesper.ledger.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction?type={type}") {
        fun createRoute(type: String) = "add_transaction?type=$type"
    }
    object Savings : Screen("savings")
    object Settings : Screen("settings")
    object Reports : Screen("reports")
}
