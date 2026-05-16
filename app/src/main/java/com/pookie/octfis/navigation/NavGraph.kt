package com.pookie.octfis.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pookie.octfis.ui.screens.*

@Composable
fun NavGraph(
    navController : NavHostController,
    onToggleTheme : () -> Unit,
    isDark        : Boolean,
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.SignIn.route,
    ) {
        composable(Screen.SignIn.route)    { SignInScreen(navController) }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController  = navController,
                onToggleTheme  = onToggleTheme,
                isDark         = isDark,
            )
        }

        // ── Accounts ──────────────────────────────────────────────────────
        composable(Screen.Accounts.route) {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Accounts",
                primaryField   = "Account_Name",
                secondaryField = "Phone",
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.AccountDetail.createRoute(zohoId))
                },
            )
        }
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

        // ── Contacts ──────────────────────────────────────────────────────
        composable(Screen.Contacts.route) {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Contacts",
                primaryField   = "Full_Name",
                secondaryField = "Email",
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.ContactDetail.createRoute(zohoId.toIntOrNull() ?: 0))
                },
            )
        }
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

        // ── Deals ─────────────────────────────────────────────────────────
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

        // ── Quotes ────────────────────────────────────────────────────────
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

        // ── Tasks ─────────────────────────────────────────────────────────
        composable(Screen.Tasks.route)      { TaskListScreen(navController) }
        composable(Screen.CreateTask.route) { CreateTaskScreen(navController) }
        composable(
            route     = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { back ->
            TaskDetailScreen(navController, back.arguments?.getString("taskId") ?: "")
        }
        composable(
            route     = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { back ->
            EditTaskScreen(navController, back.arguments?.getString("taskId") ?: "")
        }

        // ── Meetings ──────────────────────────────────────────────────────
        composable(Screen.Meetings.route)      { MeetingListScreen(navController) }
        composable(Screen.CreateMeeting.route) { CreateMeetingScreen(navController) }
        composable(
            route     = Screen.MeetingDetail.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType }),
        ) { back ->
            MeetingDetailScreen(navController, back.arguments?.getString("meetingId") ?: "")
        }
        composable(
            route     = Screen.EditMeeting.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType }),
        ) { back ->
            EditMeetingScreen(navController, back.arguments?.getString("meetingId") ?: "")
        }

        // ── Calls ─────────────────────────────────────────────────────────
        composable(Screen.Calls.route)      { CallListScreen(navController) }
        composable(Screen.CreateCall.route) { CreateCallScreen(navController) }
        composable(
            route     = Screen.CallDetail.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType }),
        ) { back ->
            CallDetailScreen(navController, back.arguments?.getString("callId") ?: "")
        }
        composable(
            route     = Screen.EditCall.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType }),
        ) { back ->
            EditCallScreen(navController, back.arguments?.getString("callId") ?: "")
        }

        // ── Dynamic Engine Routes ──────────────────────────────────────────
        composable(
            route     = Screen.DynamicList.route,
            arguments = listOf(navArgument("module") { type = NavType.StringType }),
        ) { back ->
            val module = back.arguments?.getString("module") ?: return@composable
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = module,
                showBackButton = true,
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.DynamicEdit.createRoute(module, zohoId))
                },
            )
        }

        composable(
            route     = Screen.DynamicCreate.route,
            arguments = listOf(navArgument("module") { type = NavType.StringType }),
        ) { back ->
            val module = back.arguments?.getString("module") ?: return@composable
            RecordFormScreenEntry(
                navController = navController,
                moduleName    = module,
                recordId      = null,
            )
        }

        composable(
            route     = Screen.DynamicEdit.route,
            arguments = listOf(
                navArgument("module")   { type = NavType.StringType },
                navArgument("recordId") { type = NavType.StringType },
            ),
        ) { back ->
            val module   = back.arguments?.getString("module")   ?: return@composable
            val recordId = back.arguments?.getString("recordId") ?: return@composable
            RecordFormScreenEntry(
                navController = navController,
                moduleName    = module,
                recordId      = recordId,
            )
        }
    }
}