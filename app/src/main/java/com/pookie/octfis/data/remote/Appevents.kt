package com.pookie.octfis.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide one-shot events. Any component can emit; any ViewModel can subscribe.
 */
object AppEvents {
    private val _callLogged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val callLogged = _callLogged.asSharedFlow()

    fun notifyCallLogged() { _callLogged.tryEmit(Unit) }
}