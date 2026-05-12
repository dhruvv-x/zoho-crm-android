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
        startDestination = Screen.SignIn.route
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.Accounts.route) {
            AccountsScreen(navController)
        }
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(navController)
        }
        composable(
            route     = Screen.AccountDetail.route,
            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: 0
            AccountDetailScreen(navController, accountId)
        }
        composable(Screen.Contacts.route) {
            ContactsScreen(navController)

        }
        composable(Screen.CreateContact.route) {
            CreateContactScreen(navController)
        }
        composable(Screen.Deals.route) {
            DealsScreen(navController)
        }
        composable(Screen.CreateDeal.route) {
            CreateDealScreen(navController)
        }
        composable(
            route     = Screen.DealDetail.route,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType })
        ) { backStackEntry ->
            val dealId = backStackEntry.arguments?.getInt("dealId") ?: 0
            DealDetailScreen(navController, dealId)
        }
        composable(Screen.Quotes.route) {
            QuotesScreen(navController)
        }
        composable(Screen.CreateQuote.route) {
            CreateQuoteScreen(navController)
        }
        composable(
            route     = Screen.QuoteDetail.route,
            arguments = listOf(navArgument("quoteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getInt("quoteId") ?: 0
            QuoteDetailScreen(navController, quoteId)
        }
    }
}