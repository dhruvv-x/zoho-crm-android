// ui/components/NetworkMonitor.kt
package com.pookie.octfis.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ── Connectivity flow ─────────────────────────────────────────────────────────

/**
 * Emits `true` when the device has an active internet-capable network,
 * `false` when it doesn't. Uses [callbackFlow] so the callback is
 * automatically unregistered when the collector cancels.
 */
fun connectivityFlow(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Emit current state immediately so the UI doesn't start in an unknown state
    trySend(cm.isCurrentlyConnected())

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network)  { trySend(true)  }
        override fun onLost(network: Network)        { trySend(cm.isCurrentlyConnected()) }
        override fun onUnavailable()                 { trySend(false) }
    }

    cm.registerNetworkCallback(request, callback)
    awaitClose { cm.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()

private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
    val network = activeNetwork ?: return false
    val caps    = getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

// ── isOnline composable state ─────────────────────────────────────────────────

/**
 * Returns a [State<Boolean>] that tracks whether the device is online.
 * Automatically starts/stops the connectivity observer with the composition.
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = true) {
        connectivityFlow(context).collect { value = it }
    }
}

// ── OfflineBanner ─────────────────────────────────────────────────────────────

/**
 * Animated banner that slides in from the top when [isOnline] is false
 * and slides back out when connectivity is restored.
 *
 * Usage — place once at the top of your root scaffold content:
 *
 * ```kotlin
 * val isOnline by rememberIsOnline()
 * Column {
 *     OfflineBanner(isOnline = isOnline)
 *     NavGraph(...)
 * }
 * ```
 */
@Composable
fun OfflineBanner(
    isOnline: Boolean,
    modifier : Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !isOnline,
        enter   = expandVertically(expandFrom = Alignment.Top),
        exit    = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB00020))          // error red — intentional hardcode for max contrast
                .statusBarsPadding()                    // respect edge-to-edge status bar
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment      = Alignment.CenterVertically,
            horizontalArrangement  = Arrangement.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.WifiOff,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "No internet connection",
                color      = Color.White,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}