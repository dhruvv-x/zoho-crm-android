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

    object CreateContact : Screen("create_contact")
    object Deals         : Screen("deals")
    object CreateDeal    : Screen("create_deal")
    object DealDetail    : Screen("deal_detail/{dealId}") {
        fun createRoute(dealId: Int) = "deal_detail/$dealId"
    }
    object Quotes        : Screen("quotes")
    object CreateQuote   : Screen("create_quote")
    object QuoteDetail   : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: Int) = "quote_detail/$quoteId"
    }
}