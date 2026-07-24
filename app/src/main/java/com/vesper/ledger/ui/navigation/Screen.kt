package com.vesper.ledger.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")

    object Savings : Screen("savings")
    object Settings : Screen("settings")
    object Budgets : Screen("budgets")
    object AuthWelcome : Screen("auth_welcome")
    object AuthSignIn : Screen("auth_sign_in")
    object AuthCreateAccount : Screen("auth_create_account")
    object AuthForgotPassword : Screen("auth_forgot_password")
    object CurrencySelector : Screen("currency_selector")
    object AddCategory : Screen("add_category")
}
