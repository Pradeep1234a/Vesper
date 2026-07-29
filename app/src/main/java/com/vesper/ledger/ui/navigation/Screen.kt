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
    object AddTransaction : Screen("add_transaction")
    object Accounts : Screen("accounts")
    object AddAccount : Screen("add_account")
    object Analytics : Screen("analytics")
    object AddBudget : Screen("add_budget")
    object SplitGroups : Screen("split_groups")
    object CreateSplitGroup : Screen("create_split_group")
    object AddSplitExpense : Screen("add_split_expense")
    object AddSavingsGoal : Screen("add_savings_goal")
    object ProfileManagement : Screen("profile_management")
    object SplitHistory : Screen("split_history")
}
