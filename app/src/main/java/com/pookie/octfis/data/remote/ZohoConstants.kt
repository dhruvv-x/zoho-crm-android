package com.pookie.octfis.data.remote

object ZohoConstants {
    const val AUTH_BASE_URL = "https://accounts.zoho.com/oauth/v2/"
    const val API_BASE_URL  = "https://www.zohoapis.com/crm/v2/"
    const val REDIRECT_URI  = "com.pookie.octfis://oauth/callback"

    const val SCOPE =
        "ZohoCRM.modules.accounts.ALL," +
                "ZohoCRM.modules.contacts.ALL," +
                "ZohoCRM.modules.deals.ALL," +
                "ZohoCRM.modules.quotes.ALL," +
                "ZohoCRM.modules.Tasks.ALL," +
                "ZohoCRM.modules.Events.ALL," +
                "ZohoCRM.modules.Calls.ALL," +
                "ZohoCRM.settings.fields.READ," +
                "ZohoCRM.users.READ"

    const val PREFS_NAME        = "zoho_prefs"
    const val KEY_ACCESS_TOKEN  = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_EXPIRES_AT    = "expires_at"
    const val KEY_CODE_VERIFIER = "code_verifier"
}