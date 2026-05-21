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
    navController             : NavHostController,
    onToggleTheme             : () -> Unit,
    isDark                    : Boolean,
    phoneStateGranted         : Boolean,
    callLogGranted            : Boolean,
    notifGranted              : Boolean,
    overlayGranted            : Boolean,
    onRequestPhonePerms       : () -> Unit,
    onRequestNotif            : () -> Unit,
    onRequestOverlay          : () -> Unit,
    onOpenAppSettings         : () -> Unit,
    onOpenNotifSettings       : () -> Unit,
    onOpenPhonePermSettings   : () -> Unit,
    onOpenCallLogPermSettings : () -> Unit,
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.SignIn.route,
    ) {

        // ── Auth ──────────────────────────────────────────────────────────
        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        // ── Dashboard ─────────────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController             = navController,
                onToggleTheme             = onToggleTheme,
                isDark                    = isDark,
                phoneStateGranted         = phoneStateGranted,
                callLogGranted            = callLogGranted,
                notifGranted              = notifGranted,
                overlayGranted            = overlayGranted,
                onRequestPhonePerms       = onRequestPhonePerms,
                onRequestNotif            = onRequestNotif,
                onRequestOverlay          = onRequestOverlay,
                onOpenAppSettings         = onOpenAppSettings,
                onOpenNotifSettings       = onOpenNotifSettings,
                onOpenPhonePermSettings   = onOpenPhonePermSettings,
                onOpenCallLogPermSettings = onOpenCallLogPermSettings,
            )
        }

        // ── Accounts ──────────────────────────────────────────────────────
        composable(Screen.Accounts.route) {
            AccountsScreen(navController = navController)
        }
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(navController = navController)
        }
        composable(
            route     = Screen.AccountDetail.route,
            arguments = listOf(navArgument("zohoId") { type = NavType.StringType }),
        ) { backStack ->
            val zohoId = backStack.arguments?.getString("zohoId") ?: ""
            AccountDetailScreen(navController = navController, zohoId = zohoId)
        }
        composable(
            route     = Screen.EditAccount.route,
            arguments = listOf(navArgument("zohoId") { type = NavType.StringType }),
        ) { backStack ->
            val zohoId = backStack.arguments?.getString("zohoId") ?: ""
            EditAccountScreen(navController = navController, zohoId = zohoId)
        }

        // ── Contacts ──────────────────────────────────────────────────────
        composable(Screen.Contacts.route) {
            ContactsScreen(navController = navController)
        }
        composable(Screen.CreateContact.route) {
            CreateContactScreen(navController = navController)
        }
        composable(
            route     = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType }),
        ) { backStack ->
            val contactId = backStack.arguments?.getInt("contactId") ?: 0
            ContactDetailScreen(navController = navController, contactId = contactId)
        }
        composable(
            route     = Screen.EditContact.route,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType }),
        ) { backStack ->
            val contactId = backStack.arguments?.getInt("contactId") ?: 0
            EditContactScreen(navController = navController, contactId = contactId)
        }

        // ── Deals ─────────────────────────────────────────────────────────
        composable(Screen.Deals.route) {
            DealsScreen(navController = navController)
        }
        composable(Screen.CreateDeal.route) {
            CreateDealScreen(navController = navController)
        }
        composable(
            route     = Screen.DealDetail.route,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType }),
        ) { backStack ->
            val dealId = backStack.arguments?.getInt("dealId") ?: 0
            DealDetailScreen(navController = navController, dealId = dealId)
        }
        composable(
            route     = Screen.EditDeal.route,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType }),
        ) { backStack ->
            val dealId = backStack.arguments?.getInt("dealId") ?: 0
            EditDealScreen(navController = navController, dealId = dealId)
        }

        // ── Quotes ────────────────────────────────────────────────────────
        composable(Screen.Quotes.route) {
            QuotesScreen(navController = navController)
        }
        composable(Screen.CreateQuote.route) {
            CreateQuoteScreen(navController = navController)
        }
        composable(
            route     = Screen.QuoteDetail.route,
            arguments = listOf(navArgument("quoteId") { type = NavType.IntType }),
        ) { backStack ->
            val quoteId = backStack.arguments?.getInt("quoteId") ?: 0
            QuoteDetailScreen(navController = navController, quoteId = quoteId)
        }
        composable(
            route     = Screen.EditQuote.route,
            arguments = listOf(navArgument("quoteId") { type = NavType.IntType }),
        ) { backStack ->
            val quoteId = backStack.arguments?.getInt("quoteId") ?: 0
            EditQuoteScreen(navController = navController, quoteId = quoteId)
        }
        composable(Screen.AddQuoteItem.route) {
            AddQuoteItemScreen(navController = navController)
        }

        // ── Tasks ─────────────────────────────────────────────────────────
        composable(Screen.Tasks.route) {
            TaskListScreen(navController = navController)
        }
        composable(Screen.CreateTask.route) {
            CreateTaskScreen(navController = navController)
        }
        composable(
            route     = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { backStack ->
            val taskId = backStack.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(navController = navController, taskId = taskId)
        }
        composable(
            route     = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { backStack ->
            val taskId = backStack.arguments?.getString("taskId") ?: ""
            EditTaskScreen(navController = navController, taskId = taskId)
        }

        // ── Meetings ──────────────────────────────────────────────────────
        composable(Screen.Meetings.route) {
            MeetingListScreen(navController = navController)
        }
        composable(Screen.CreateMeeting.route) {
            CreateMeetingScreen(navController = navController)
        }
        composable(
            route     = Screen.MeetingDetail.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType }),
        ) { backStack ->
            val meetingId = backStack.arguments?.getString("meetingId") ?: ""
            MeetingDetailScreen(navController = navController, meetingId = meetingId)
        }
        composable(
            route     = Screen.EditMeeting.route,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType }),
        ) { backStack ->
            val meetingId = backStack.arguments?.getString("meetingId") ?: ""
            EditMeetingScreen(navController = navController, meetingId = meetingId)
        }

        // ── Permissions ───────────────────────────────────────────────────
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                navController          = navController,
                phoneStateGranted      = phoneStateGranted,
                callLogGranted         = callLogGranted,
                notifGranted           = notifGranted,
                overlayGranted         = overlayGranted,
                onSetPhoneStateEnabled = { enabled ->
                    if (!phoneStateGranted || !enabled) onOpenPhonePermSettings()
                    // enabling when already granted is handled inside screen
                },
                onSetCallLogEnabled    = { enabled ->
                    if (!callLogGranted || !enabled) onOpenCallLogPermSettings()
                },
                onSetNotifEnabled      = { enabled ->
                    if (!notifGranted || !enabled) onOpenNotifSettings()
                },
                onSetOverlayEnabled    = { enabled ->
                    if (!overlayGranted || !enabled) onOpenAppSettings()
                },
            )
        }

        // ── Calls ─────────────────────────────────────────────────────────
        composable(Screen.Calls.route) {
            CallListScreen(navController = navController)
        }
        composable(Screen.CreateCall.route) {
            CreateCallScreen(navController = navController)
        }
        composable(
            route     = Screen.CallDetail.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType }),
        ) { backStack ->
            val callId = backStack.arguments?.getString("callId") ?: ""
            CallDetailScreen(navController = navController, callId = callId)
        }
        composable(
            route     = Screen.EditCall.route,
            arguments = listOf(navArgument("callId") { type = NavType.StringType }),
        ) { backStack ->
            val callId = backStack.arguments?.getString("callId") ?: ""
            EditCallScreen(navController = navController, callId = callId)
        }
    }
}