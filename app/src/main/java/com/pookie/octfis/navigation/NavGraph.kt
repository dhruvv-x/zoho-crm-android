// navigation/NavGraph.kt
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

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Screen.SignIn.route) { SignInScreen(navController) }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onToggleTheme = onToggleTheme,
                isDark        = isDark,
            )
        }

        // ══════════════════════════════════════════════════════════════════════
        // UNIFIED DYNAMIC ENGINE
        // All bottom-nav module taps land on ModuleList.
        // ModuleList → ModuleDetail (Day 8) or ModuleEdit.
        // ModuleCreate / ModuleEdit are generic for every module.
        // ══════════════════════════════════════════════════════════════════════

        // ── List ──────────────────────────────────────────────────────────────
        composable(
            route     = Screen.ModuleList.route,
            arguments = listOf(navArgument("moduleName") { type = NavType.StringType }),
        ) { back ->
            val module = back.arguments?.getString("moduleName") ?: return@composable
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = module,
                showBackButton = true,
                onRecordClick  = { zohoId ->
                    // Day 8: swap ModuleEdit → ModuleDetail once detail screen exists
                    navController.navigate(Screen.ModuleEdit.createRoute(module, zohoId))
                },
            )
        }

        // ── Create ────────────────────────────────────────────────────────────
        composable(
            route     = Screen.ModuleCreate.route,
            arguments = listOf(navArgument("moduleName") { type = NavType.StringType }),
        ) { back ->
            val module = back.arguments?.getString("moduleName") ?: return@composable
            RecordFormScreenEntry(
                navController = navController,
                moduleName    = module,
                recordId      = null,
            )
        }

        // ── Edit ──────────────────────────────────────────────────────────────
        composable(
            route     = Screen.ModuleEdit.route,
            arguments = listOf(
                navArgument("moduleName") { type = NavType.StringType },
                navArgument("recordId")   { type = NavType.StringType },
            ),
        ) { back ->
            val module   = back.arguments?.getString("moduleName") ?: return@composable
            val recordId = back.arguments?.getString("recordId")   ?: return@composable
            RecordFormScreenEntry(
                navController = navController,
                moduleName    = module,
                recordId      = recordId,
            )
        }

        // ── Detail (Day 8 replaces body with RecordDetailScreenEntry) ─────────
        composable(
            route     = Screen.ModuleDetail.route,
            arguments = listOf(
                navArgument("moduleName") { type = NavType.StringType },
                navArgument("recordId")   { type = NavType.StringType },
            ),
        ) { back ->
            val module   = back.arguments?.getString("moduleName") ?: return@composable
            val recordId = back.arguments?.getString("recordId")   ?: return@composable
            RecordFormScreenEntry(
                navController = navController,
                moduleName    = module,
                recordId      = recordId,
            )
        }

        // ══════════════════════════════════════════════════════════════════════
        // DYNAMIC BOTTOM NAV FALLBACK ROUTES
        // CrmBottomBar navigates to module/{name}/list for dynamic modules.
        // These 4 named routes are kept ONLY as legacy targets for old deep-links
        // and for the Dashboard's hardcoded nav items in case ModuleEngine fails.
        // They all now use RecordListScreenEntry (dynamic) — no more static screens.
        // ══════════════════════════════════════════════════════════════════════

        composable("accounts") {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Accounts",
                showBackButton = false,
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.ModuleEdit.createRoute("Accounts", zohoId))
                },
            )
        }

        composable("contacts") {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Contacts",
                showBackButton = false,
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.ModuleEdit.createRoute("Contacts", zohoId))
                },
            )
        }

        composable("deals") {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Deals",
                showBackButton = false,
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.ModuleEdit.createRoute("Deals", zohoId))
                },
            )
        }

        composable("quotes") {
            RecordListScreenEntry(
                navController  = navController,
                moduleName     = "Quotes",
                showBackButton = false,
                onRecordClick  = { zohoId ->
                    navController.navigate(Screen.ModuleEdit.createRoute("Quotes", zohoId))
                },
            )
        }

        // ══════════════════════════════════════════════════════════════════════
        // LEGACY STATIC ROUTES — kept alive, not deleted
        // Individual detail / edit / create screens still work if navigated to
        // directly. Remove only after Day 8 detail screen is verified.
        // ══════════════════════════════════════════════════════════════════════

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

        composable("tasks") { TaskListScreen(navController) }
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

        composable("meetings") { MeetingListScreen(navController) }
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

        composable("calls") { CallListScreen(navController) }
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

        // ── Legacy dynamic routes (backward compat) ───────────────────────────
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
                    navController.navigate(Screen.ModuleEdit.createRoute(module, zohoId))
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