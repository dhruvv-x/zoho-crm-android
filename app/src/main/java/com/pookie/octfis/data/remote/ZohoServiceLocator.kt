package com.pookie.octfis.data.remote

import android.content.Context

object ZohoServiceLocator {

    private lateinit var _tokenStore  : TokenStore
    private lateinit var _authManager : ZohoAuthManager
    private lateinit var _apiService  : ZohoApiService

    fun init(context: Context) {
        val app      = context.applicationContext
        _tokenStore  = TokenStore(app)
        _authManager = ZohoAuthManager(app, _tokenStore)
        _apiService  = ZohoApiClient.create(_authManager)
    }

    fun getTokenStore()  = _tokenStore
    fun getAuthManager() = _authManager
    fun getApiService()  = _apiService
}