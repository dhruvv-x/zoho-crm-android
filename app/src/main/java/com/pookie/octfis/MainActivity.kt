package com.pookie.octfis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.pookie.octfis.data.remote.AuthState
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.navigation.NavGraph
import com.pookie.octfis.ui.theme.OctfisCRMTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("OctfisAuth", "onCreate — intent data: ${intent?.data}")
        handleIntent(intent)

        val themePrefs = ZohoServiceLocator.themePrefs

        setContent {
            val isDark by themePrefs.isDarkTheme.collectAsState(initial = false)
            val scope  = rememberCoroutineScope()

            OctfisCRMTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(
                    navController   = navController,
                    onToggleTheme   = { scope.launch { themePrefs.setDarkTheme(!isDark) } },
                    isDark          = isDark,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("OctfisAuth", "onNewIntent — intent data: ${intent.data}")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        Log.d("OctfisAuth", "handleIntent — uri: $uri  scheme: ${uri?.scheme}  host: ${uri?.host}")
        if (uri == null) return

        if (uri.scheme == "com.pookie.octfis" && uri.host == "oauth") {
            Log.d("OctfisAuth", "OAuth callback received — code: ${uri.getQueryParameter("code")?.take(10)}…")
            lifecycleScope.launch {
                val success = ZohoServiceLocator.getAuthManager().handleCallback(uri)
                Log.d("OctfisAuth", "handleCallback result: $success")
                if (success) AuthState.onLoginSuccess()
            }
        }
    }
}