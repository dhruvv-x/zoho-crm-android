package com.pookie.octfis.navigation

sealed class Screen(val route: String) {

    // ── Auth ──────────────────────────────────────────────────────────────────
    object SignIn    : Screen("sign_in")
    object Dashboard : Screen("dashboard")

    // ── Legacy static routes (kept alive — old detail/edit screens still work) ─
    object CreateAccount : Screen("create_account")
    object AccountDetail : Screen("account_detail/{zohoId}") {
        fun createRoute(zohoId: String) = "account_detail/$zohoId"
    }
    object EditAccount : Screen("edit_account/{zohoId}") {
        fun createRoute(zohoId: String) = "edit_account/$zohoId"
    }

    object CreateContact : Screen("create_contact")
    object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Int) = "contact_detail/$contactId"
    }
    object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: Int) = "edit_contact/$contactId"
    }

    object CreateDeal : Screen("create_deal")
    object DealDetail : Screen("deal_detail/{dealId}") {
        fun createRoute(dealId: Int) = "deal_detail/$dealId"
    }
    object EditDeal : Screen("edit_deal/{dealId}") {
        fun createRoute(dealId: Int) = "edit_deal/$dealId"
    }

    object CreateQuote : Screen("create_quote")
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: Int) = "quote_detail/$quoteId"
    }
    object EditQuote : Screen("edit_quote/{quoteId}") {
        fun createRoute(quoteId: Int) = "edit_quote/$quoteId"
    }

    object CreateTask : Screen("create_task")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }

    object CreateMeeting : Screen("create_meeting")
    object MeetingDetail : Screen("meeting_detail/{meetingId}") {
        fun createRoute(meetingId: String) = "meeting_detail/$meetingId"
    }
    object EditMeeting : Screen("edit_meeting/{meetingId}") {
        fun createRoute(meetingId: String) = "edit_meeting/$meetingId"
    }

    object CreateCall : Screen("create_call")
    object CallDetail : Screen("call_detail/{callId}") {
        fun createRoute(callId: String) = "call_detail/$callId"
    }
    object EditCall : Screen("edit_call/{callId}") {
        fun createRoute(callId: String) = "edit_call/$callId"
    }

    // ── Unified Dynamic Engine Routes ─────────────────────────────────────────
    // Single pattern handles ALL modules: Accounts, Contacts, Deals, Tasks, etc.

    object ModuleList : Screen("module/{moduleName}/list") {
        fun createRoute(moduleName: String) = "module/$moduleName/list"
    }

    object ModuleCreate : Screen("module/{moduleName}/create") {
        fun createRoute(moduleName: String) = "module/$moduleName/create"
    }

    object ModuleEdit : Screen("module/{moduleName}/edit/{recordId}") {
        fun createRoute(moduleName: String, recordId: String) =
            "module/$moduleName/edit/$recordId"
    }

    object ModuleDetail : Screen("module/{moduleName}/detail/{recordId}") {
        fun createRoute(moduleName: String, recordId: String) =
            "module/$moduleName/detail/$recordId"
    }

    // ── Legacy dynamic routes (kept for backward compat) ──────────────────────
    object DynamicList : Screen("dynamic_list/{module}") {
        fun createRoute(module: String) = "dynamic_list/$module"
    }
    object DynamicCreate : Screen("dynamic_create/{module}") {
        fun createRoute(module: String) = "dynamic_create/$module"
    }
    object DynamicEdit : Screen("dynamic_edit/{module}/{recordId}") {
        fun createRoute(module: String, recordId: String) = "dynamic_edit/$module/$recordId"
    }
}