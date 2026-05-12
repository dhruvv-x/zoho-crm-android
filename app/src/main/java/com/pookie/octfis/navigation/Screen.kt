package com.pookie.octfis.navigation

sealed class Screen(val route: String) {
    object SignIn        : Screen("sign_in")
    object Dashboard     : Screen("dashboard")
    object Accounts      : Screen("accounts")
    object CreateAccount : Screen("create_account")
    object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: Int) = "account_detail/$accountId"
    }
    object Contacts      : Screen("contacts")
    object Deals         : Screen("deals")
    object Quotes        : Screen("quotes")
}