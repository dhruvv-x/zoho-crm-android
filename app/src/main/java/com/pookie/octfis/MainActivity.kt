package com.pookie.octfis

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
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

    // ── Permission state (observed by the drawer) ──────────────────────────
    private val phoneStateGranted  = mutableStateOf(false)
    private val callLogGranted     = mutableStateOf(false)
    private val notifGranted       = mutableStateOf(false)
    private val overlayGranted     = mutableStateOf(false)

    // ── Launchers ──────────────────────────────────────────────────────────

    private val overlaySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        prefs.edit().putBoolean(KEY_OVERLAY_ASKED, true).apply()
        overlayGranted.value = Settings.canDrawOverlays(this)
        CallMonitorService.start(this)
    }

    private val phonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        phoneStateGranted.value =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED
        callLogGranted.value =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) ==
                    PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifGranted.value =
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
        }
        if (grants.values.any { it }) CallMonitorService.start(this)
    }

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted.value = granted
    }

    private val singlePhonePermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        phoneStateGranted.value =
            grants[Manifest.permission.READ_PHONE_STATE] == true ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        callLogGranted.value =
            grants[Manifest.permission.READ_CALL_LOG] == true ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        if (phoneStateGranted.value || callLogGranted.value) CallMonitorService.start(this)
    }

    // ── Launcher for opening app settings (to let user revoke perms) ───────
    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // User returned from Settings — re-check all permission states
        refreshPermissionStates()
    }

    // ── Launcher for opening notification settings directly ────────────────
    private val notifSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissionStates()
    }

    // ── Public callbacks passed to the drawer ──────────────────────────────

    fun requestPhoneAndCallLogPerms() {
        singlePhonePermLauncher.launch(
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
        )
    }

    fun requestNotifPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestOverlayPerm() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlaySettingsLauncher.launch(intent)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        appSettingsLauncher.launch(intent)   // use launcher so we catch the return
    }

    fun openNotifSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        notifSettingsLauncher.launch(intent)
    }

    // Opens Settings → Apps → Octfis → Permissions where Phone can be toggled
    fun openPhonePermSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        appSettingsLauncher.launch(intent)
    }

    // Opens Settings → Apps → Octfis → Permissions where Call Log can be toggled
    fun openCallLogPermSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        appSettingsLauncher.launch(intent)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun refreshPermissionStates() {
        phoneStateGranted.value =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED
        callLogGranted.value =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) ==
                    PackageManager.PERMISSION_GRANTED
        notifGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        else true
        overlayGranted.value = Settings.canDrawOverlays(this)
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
        if (missing.isNotEmpty()) phonePermissionLauncher.launch(missing.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("octfis_prefs", MODE_PRIVATE)
        Log.d("OctfisAuth", "onCreate — intent data: ${intent?.data}")
        handleIntent(intent)
        refreshPermissionStates()
        requestPhonePermissionsIfNeeded()

        val themePrefs   = ZohoServiceLocator.themePrefs
        val overlayAsked = prefs.getBoolean(KEY_OVERLAY_ASKED, false)

        setContent {
            val isDark by themePrefs.isDarkTheme.collectAsState(initial = false)
            val scope  = rememberCoroutineScope()

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
                                requestOverlayPerm()
                            }) { Text("Allow") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showOverlayDialog = false
                                prefs.edit().putBoolean(KEY_OVERLAY_ASKED, true).apply()
                                CallMonitorService.start(this@MainActivity)
                            }) { Text("Skip") }
                        },
                    )
                }

                val navController = rememberNavController()
                NavGraph(
                    navController           = navController,
                    onToggleTheme           = { scope.launch { themePrefs.setDarkTheme(!isDark) } },
                    isDark                  = isDark,
                    phoneStateGranted       = phoneStateGranted.value,
                    callLogGranted          = callLogGranted.value,
                    notifGranted            = notifGranted.value,
                    overlayGranted          = overlayGranted.value,
                    onRequestPhonePerms     = ::requestPhoneAndCallLogPerms,
                    onRequestNotif          = ::requestNotifPerm,
                    onRequestOverlay        = ::requestOverlayPerm,
                    onOpenAppSettings       = ::openAppSettings,
                    onOpenNotifSettings     = ::openNotifSettings,
                    onOpenPhonePermSettings = ::openPhonePermSettings,
                    onOpenCallLogPermSettings = ::openCallLogPermSettings,
                )
            }

            LaunchedEffect(Unit) {
                if (overlayAsked) CallMonitorService.start(this@MainActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStates()   // re-check all perms when returning from anywhere
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("OctfisAuth", "onNewIntent — intent data: ${intent.data}")
        handleIntent(intent)
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