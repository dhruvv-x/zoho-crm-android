package com.pookie.octfis.data.remote

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

class ThemePreferenceManager(private val context: Context) {

    private val isDarkKey = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data
        .map { prefs -> prefs[isDarkKey] ?: false }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[isDarkKey] = isDark
        }
    }
}