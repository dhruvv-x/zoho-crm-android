package com.pookie.octfis.navigation

sealed class Screen(val route: String) {
    object SignIn        : Screen("sign_in")
    object Dashboard     : Screen("dashboard")

    object Accounts      : Screen("accounts")
    object CreateAccount : Screen("create_account")
    object AccountDetail : Screen("account_detail/{zohoId}") {
        fun createRoute(zohoId: String) = "account_detail/$zohoId"
    }
    object EditAccount   : Screen("edit_account/{zohoId}") {
        fun createRoute(zohoId: String) = "edit_account/$zohoId"
    }

    object Contacts      : Screen("contacts")
    object CreateContact : Screen("create_contact")
    object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Int) = "contact_detail/$contactId"
    }
    object EditContact   : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: Int) = "edit_contact/$contactId"
    }

    object Deals         : Screen("deals")
    object CreateDeal    : Screen("create_deal")
    object DealDetail    : Screen("deal_detail/{dealId}") {
        fun createRoute(dealId: Int) = "deal_detail/$dealId"
    }
    object EditDeal      : Screen("edit_deal/{dealId}") {
        fun createRoute(dealId: Int) = "edit_deal/$dealId"
    }

    object Quotes        : Screen("quotes")
    object CreateQuote   : Screen("create_quote")
    object QuoteDetail   : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: Int) = "quote_detail/$quoteId"
    }
    object EditQuote     : Screen("edit_quote/{quoteId}") {
        fun createRoute(quoteId: Int) = "edit_quote/$quoteId"
    }
}