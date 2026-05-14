package com.pookie.octfis.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pookie.octfis.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.SignIn.route,
    ) {
        composable(Screen.SignIn.route)     { SignInScreen(navController) }
        composable(Screen.Dashboard.route)  { DashboardScreen(navController) }

        // ── Accounts ──────────────────────────────────────────────────────────
        composable(Screen.Accounts.route)      { AccountsScreen(navController) }
        composable(Screen.CreateAccount.route) { CreateAccountScreen(navController) }
        composable(
            route     = Screen.AccountDetail.route,
            arguments = listOf(navArgument("zohoId") { type = NavType.StringType }),
        ) { back ->
            AccountDetailScreen(navController, back.arguments?.getString("zohoId") ?: "")
        }
        composable(
            route     = Screen.EditAccount.route,
            arguments = listOf(navArgument("zohoId") { type = NavType.StringType }),
        ) { back ->
            EditAccountScreen(navController, back.arguments?.getString("zohoId") ?: "")
        }

        // ── Contacts ──────────────────────────────────────────────────────────
        composable(Screen.Contacts.route)      { ContactsScreen(navController) }
        composable(Screen.CreateContact.route) { CreateContactScreen(navController) }
        composable(
            route     = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType }),
        ) { back ->
            ContactDetailScreen(navController, back.arguments?.getInt("contactId") ?: 0)
        }
        composable(
            route     = Screen.EditContact.route,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType }),
        ) { back ->
            EditContactScreen(navController, back.arguments?.getInt("contactId") ?: 0)
        }

        // ── Deals ─────────────────────────────────────────────────────────────
        composable(Screen.Deals.route)      { DealsScreen(navController) }
        composable(Screen.CreateDeal.route) { CreateDealScreen(navController) }
        composable(
            route     = Screen.DealDetail.route,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType }),
        ) { back ->
            DealDetailScreen(navController, back.arguments?.getInt("dealId") ?: 0)
        }
        composable(
            route     = Screen.EditDeal.route,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType }),
        ) { back ->
            EditDealScreen(navController, back.arguments?.getInt("dealId") ?: 0)
        }

        // ── Quotes ────────────────────────────────────────────────────────────
        composable(Screen.Quotes.route)      { QuotesScreen(navController) }
        composable(Screen.CreateQuote.route) { CreateQuoteScreen(navController) }
        composable(
            route     = Screen.QuoteDetail.route,
            arguments = listOf(navArgument("quoteId") { type = NavType.IntType }),
        ) { back ->
            QuoteDetailScreen(navController, back.arguments?.getInt("quoteId") ?: 0)
        }
        composable(
            route     = Screen.EditQuote.route,
            arguments = listOf(navArgument("quoteId") { type = NavType.IntType }),
        ) { back ->
            EditQuoteScreen(navController, back.arguments?.getInt("quoteId") ?: 0)
        }
    }
}