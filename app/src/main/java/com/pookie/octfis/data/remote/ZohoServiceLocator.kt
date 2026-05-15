package com.pookie.octfis.data.remote

import android.content.Context

object ZohoServiceLocator {

    private lateinit var appContext  : Context
    private lateinit var _tokenStore : TokenStore
    private lateinit var _authManager: ZohoAuthManager
    private lateinit var _apiService : ZohoApiService

    val themePrefs by lazy { ThemePreferenceManager(appContext) }

    fun init(context: Context) {
        appContext    = context.applicationContext
        _tokenStore   = TokenStore(appContext)
        _authManager  = ZohoAuthManager(appContext, _tokenStore)
        _apiService   = ZohoApiClient.create(_authManager)
    }

    fun getTokenStore()  = _tokenStore
    fun getAuthManager() = _authManager
    fun getApiService()  = _apiService
}