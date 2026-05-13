package com.pookie.octfis.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that signals a successful OAuth login to the Compose UI.
 * MainActivity writes to it after handleCallback(); SignInScreen observes it.
 */
object AuthState {
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    fun onLoginSuccess() { _loginSuccess.value = true }
    fun reset()          { _loginSuccess.value = false }
}