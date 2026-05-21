package com.pookie.octfis

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.pookie.octfis.data.remote.AuthState
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.navigation.NavGraph
import com.pookie.octfis.service.CallMonitorService
import com.pookie.octfis.ui.theme.OctfisCRMTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    // After user returns from the overlay settings screen, just start the service.
    // We never check canDrawOverlays() again — MIUI lies about it.
    private val overlaySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        prefs.edit().putBoolean(KEY_OVERLAY_ASKED, true).apply()
        CallMonitorService.start(this)
    }

    /** Requests READ_PHONE_STATE + READ_CALL_LOG together. */
    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it }
        if (granted) CallMonitorService.start(this)
    }

    private fun requestPhonePermissionsIfNeeded() {
        val perms = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            phonePermissionLauncher.launch(missing.toTypedArray())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("octfis_prefs", MODE_PRIVATE)
        Log.d("OctfisAuth", "onCreate — intent data: ${intent?.data}")
        handleIntent(intent)
        requestPhonePermissionsIfNeeded()

        val themePrefs   = ZohoServiceLocator.themePrefs
        // Has the user already seen the overlay permission dialog?
        val overlayAsked = prefs.getBoolean(KEY_OVERLAY_ASKED, false)

        setContent {
            val isDark by themePrefs.isDarkTheme.collectAsState(initial = false)
            val scope  = rememberCoroutineScope()

            // Show permission rationale dialog only on first run
            var showOverlayDialog by remember { mutableStateOf(!overlayAsked) }

            OctfisCRMTheme(darkTheme = isDark) {

                if (showOverlayDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Enable call logging") },
                        text  = {
                            Text(
                                "Octfis needs \"Display over other apps\" permission to show " +
                                        "a quick-log button after calls made outside the app.\n\n" +
                                        "Tap Allow, find Octfis in the list, and turn it on."
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                showOverlayDialog = false
                                openOverlaySettings()
                            }) { Text("Allow") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showOverlayDialog = false
                                prefs.edit().putBoolean(KEY_OVERLAY_ASKED, true).apply()
                                // Start service anyway — overlay button just won't show
                                CallMonitorService.start(this@MainActivity)
                            }) { Text("Skip") }
                        },
                    )
                }

                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    onToggleTheme = { scope.launch { themePrefs.setDarkTheme(!isDark) } },
                    isDark        = isDark,
                )
            }

            // Always start the service on subsequent launches (overlayAsked == true)
            LaunchedEffect(Unit) {
                if (overlayAsked) {
                    CallMonitorService.start(this@MainActivity)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("OctfisAuth", "onNewIntent — intent data: ${intent.data}")
        handleIntent(intent)
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlaySettingsLauncher.launch(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.d("OctfisAuth", "handleIntent — uri: $uri")
        if (uri.scheme == "com.pookie.octfis" && uri.host == "oauth") {
            lifecycleScope.launch {
                val success = ZohoServiceLocator.getAuthManager().handleCallback(uri)
                Log.d("OctfisAuth", "handleCallback result: $success")
                if (success) AuthState.onLoginSuccess()
            }
        }
    }

    companion object {
        private const val KEY_OVERLAY_ASKED = "overlay_permission_asked"
    }
}