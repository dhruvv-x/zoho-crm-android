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
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.service.CallMonitorService
import com.pookie.octfis.ui.theme.OctfisCRMTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    // ── Permission state (observed by the drawer & permissions screen) ─────
    private val phoneStateGranted = mutableStateOf(false)
    private val callLogGranted    = mutableStateOf(false)
    private val notifGranted      = mutableStateOf(false)
    private val overlayGranted    = mutableStateOf(false)

    // ── Launchers ──────────────────────────────────────────────────────────

    private val overlaySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        overlayGranted.value = Settings.canDrawOverlays(this)
        if (overlayGranted.value) CallMonitorService.start(this)
    }

    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // User returned from Settings — re-check all permission states
        refreshPermissionStates()
    }

    private val notifSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissionStates()
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

    // ── Public callbacks passed to NavGraph / screens ──────────────────────

    fun requestPhoneAndCallLogPerms() {
        singlePhonePermLauncher.launch(
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
        )
    }

    fun requestNotifPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Direct runtime request for notifications
            singlePhonePermLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
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
        appSettingsLauncher.launch(intent)
    }

    fun openNotifSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        notifSettingsLauncher.launch(intent)
    }

    // Opens Settings → Apps → Octfis → Permissions (Phone)
    fun openPhonePermSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        appSettingsLauncher.launch(intent)
    }

    // Opens Settings → Apps → Octfis → Permissions (Call Log)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("octfis_prefs", MODE_PRIVATE)
        Log.d("OctfisAuth", "onCreate — intent data: ${intent?.data}")
        handleIntent(intent)
        refreshPermissionStates()   // just read states, never auto-request

        val themePrefs = ZohoServiceLocator.themePrefs

        setContent {
            val isDark by themePrefs.isDarkTheme.collectAsState(initial = false)
            val scope  = rememberCoroutineScope()

            OctfisCRMTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(
                    navController             = navController,
                    onToggleTheme             = { scope.launch { themePrefs.setDarkTheme(!isDark) } },
                    isDark                    = isDark,
                    phoneStateGranted         = phoneStateGranted.value,
                    callLogGranted            = callLogGranted.value,
                    notifGranted              = notifGranted.value,
                    overlayGranted            = overlayGranted.value,
                    onRequestPhonePerms       = ::requestPhoneAndCallLogPerms,
                    onRequestNotif            = ::requestNotifPerm,
                    onRequestOverlay          = ::requestOverlayPerm,
                    onOpenAppSettings         = ::openAppSettings,
                    onOpenNotifSettings       = ::openNotifSettings,
                    onOpenPhonePermSettings   = ::openPhonePermSettings,
                    onOpenCallLogPermSettings = ::openCallLogPermSettings,
                )

                LaunchedEffect(Unit) {
                    // Start service only if permissions already granted from a previous session
                    if (phoneStateGranted.value || callLogGranted.value) {
                        CallMonitorService.start(this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStates()   // re-check all perms when returning from Settings
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
}