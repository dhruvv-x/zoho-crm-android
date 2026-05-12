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
            PlaceholderScreen(navController, "Contacts")
        }
        composable(Screen.Deals.route) {
            PlaceholderScreen(navController, "Deals")
        }
        composable(Screen.Quotes.route) {
            PlaceholderScreen(navController, "Quotes")
        }
    }
}