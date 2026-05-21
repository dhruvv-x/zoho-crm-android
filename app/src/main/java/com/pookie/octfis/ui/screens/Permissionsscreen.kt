package com.pookie.octfis.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pookie.octfis.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    navController             : NavController,
    phoneStateGranted         : Boolean,
    callLogGranted            : Boolean,
    notifGranted              : Boolean,
    overlayGranted            : Boolean,
    onSetPhoneStateEnabled    : (Boolean) -> Unit,
    onSetCallLogEnabled       : (Boolean) -> Unit,
    onSetNotifEnabled         : (Boolean) -> Unit,
    onSetOverlayEnabled       : (Boolean) -> Unit,
) {
    // Mirror the same local-enabled state logic as the drawer
    var phoneStateEnabled by remember(phoneStateGranted) { mutableStateOf(phoneStateGranted) }
    var callLogEnabled    by remember(callLogGranted)    { mutableStateOf(callLogGranted) }
    var notifEnabled      by remember(notifGranted)      { mutableStateOf(notifGranted) }
    var overlayEnabled    by remember(overlayGranted)    { mutableStateOf(overlayGranted) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Permissions", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 16.dp),
        ) {
            item {
                Text(
                    text      = "Manage which permissions Octfis CRM can use. Toggling a switch opens the system settings page where you can grant or revoke access.",
                    fontSize  = 13.sp,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier  = Modifier.padding(bottom = 4.dp),
                )
            }

            item {
                PermissionCard(
                    icon        = Icons.Default.Phone,
                    label       = "Phone State",
                    description = "Allows the app to detect incoming and outgoing calls so they can be logged automatically.",
                    granted     = phoneStateGranted,
                    enabled     = phoneStateEnabled && phoneStateGranted,
                    onToggle    = { enabled ->
                        onSetPhoneStateEnabled(enabled)
                        if (enabled && phoneStateGranted) phoneStateEnabled = true
                    },
                )
            }

            item {
                PermissionCard(
                    icon        = Icons.Default.PhoneCallback,
                    label       = "Call Log",
                    description = "Allows the app to read your call history to associate calls with CRM contacts.",
                    granted     = callLogGranted,
                    enabled     = callLogEnabled && callLogGranted,
                    onToggle    = { enabled ->
                        onSetCallLogEnabled(enabled)
                        if (enabled && callLogGranted) callLogEnabled = true
                    },
                )
            }

            item {
                PermissionCard(
                    icon        = Icons.Default.Notifications,
                    label       = "Notifications",
                    description = "Allows the app to send you reminders for tasks, meetings, and follow-ups.",
                    granted     = notifGranted,
                    enabled     = notifEnabled && notifGranted,
                    onToggle    = { enabled ->
                        onSetNotifEnabled(enabled)
                        if (enabled && notifGranted) notifEnabled = true
                    },
                )
            }

            item {
                PermissionCard(
                    icon        = Icons.Default.Layers,
                    label       = "Display Over Apps",
                    description = "Allows the app to show a quick call-log button on top of other apps after a call ends.",
                    granted     = overlayGranted,
                    enabled     = overlayEnabled && overlayGranted,
                    onToggle    = { enabled ->
                        onSetOverlayEnabled(enabled)
                        if (enabled && overlayGranted) overlayEnabled = true
                    },
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon        : ImageVector,
    label       : String,
    description : String,
    granted     : Boolean,
    enabled     : Boolean,
    onToggle    : (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (granted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(28.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 15.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = description,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = when {
                        !granted -> "Not granted — tap switch to open Settings"
                        enabled  -> "Active — tap switch to disable in Settings"
                        else     -> "Granted but disabled"
                    },
                    fontSize = 11.sp,
                    color    = if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                )
            }

            Switch(
                checked         = enabled,
                onCheckedChange = onToggle,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor       = CrmOnAccent,
                    checkedTrackColor       = CrmAccent,
                    uncheckedThumbColor     = if (isSystemInDarkTheme()) CrmSubtextDark else CrmSubtext,
                    uncheckedTrackColor     = if (isSystemInDarkTheme()) CrmSurfaceAltDark else CrmSurfaceAlt,
                    uncheckedBorderColor    = if (isSystemInDarkTheme()) CrmDividerDark else CrmDivider,
                ),
            )
        }
    }
}