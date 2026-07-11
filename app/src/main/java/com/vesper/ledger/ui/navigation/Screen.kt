package com.vesper.ledger.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction?type={type}&id={id}") {
        fun createRoute(type: String, id: Long? = null) =
            if (id != null) "add_transaction?type=$type&id=$id"
            else "add_transaction?type=$type"
    }
    object Savings : Screen("savings")
    object Settings : Screen("settings")
    object Reports : Screen("reports")
}
