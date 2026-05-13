package com.pookie.octfis.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("zoho_token_store")

class TokenStore(private val context: Context) {

    private val accessKey  = stringPreferencesKey(ZohoConstants.KEY_ACCESS_TOKEN)
    private val refreshKey = stringPreferencesKey(ZohoConstants.KEY_REFRESH_TOKEN)
    private val expiresKey = longPreferencesKey(ZohoConstants.KEY_EXPIRES_AT)

    suspend fun save(accessToken: String, refreshToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000L - 60_000L
        context.dataStore.edit { prefs ->
            prefs[accessKey]  = accessToken
            prefs[refreshKey] = refreshToken
            prefs[expiresKey] = expiresAt
        }
    }

    suspend fun saveAccessOnly(accessToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000L - 60_000L
        context.dataStore.edit { prefs ->
            prefs[accessKey]  = accessToken
            prefs[expiresKey] = expiresAt
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[accessKey] }.firstOrNull()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[refreshKey] }.firstOrNull()

    suspend fun isExpired(): Boolean {
        val exp = context.dataStore.data.map { it[expiresKey] ?: 0L }.firstOrNull() ?: 0L
        return System.currentTimeMillis() > exp
    }

    suspend fun isLoggedIn(): Boolean = getRefreshToken() != null

    suspend fun clear() = context.dataStore.edit { it.clear() }
}