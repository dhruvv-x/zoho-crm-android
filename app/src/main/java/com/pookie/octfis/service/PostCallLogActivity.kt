package com.pookie.octfis.service

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pookie.octfis.data.remote.CallStateHolder
import com.pookie.octfis.ui.screens.ContactCallViewModel
import com.pookie.octfis.ui.screens.LogCallState
import com.pookie.octfis.ui.theme.OctfisCRMTheme
import java.util.Locale

/**
 * Transparent, dialog-themed Activity launched by CallMonitorService immediately
 * when CALL_STATE_IDLE fires — regardless of which app is currently in foreground.
 *
 * ISSUE 1 FIX: This Activity is always launched by the service on IDLE.
 * ContactDetailScreen has NO onResume dialog logic.
 *
 * ISSUE 3 FIX: durationDisplay reads callStartMillis (set by OFFHOOK) first,
 * then callInitiatedAtMillis, then System.currentTimeMillis() as last resort.
 */
class PostCallLogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OctfisCRMTheme {
                PostCallLogDialog(
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@Composable
private fun PostCallLogDialog(onDismiss: () -> Unit) {
    val callVm: ContactCallViewModel = viewModel()
    val logState by callVm.logState.collectAsState()

    var description by remember { mutableStateOf("") }

    // Compute duration once when the dialog opens.
    // Priority: OFFHOOK time → initiated time → now (last resort).
    val durationDisplay = remember {
        val start = CallStateHolder.callStartMillis.takeIf { it > 0L }
            ?: CallStateHolder.callInitiatedAtMillis.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val end = CallStateHolder.callEndMillis.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val totalSecs = ((end - start) / 1000).coerceAtLeast(0)
        val m = totalSecs / 60
        val s = totalSecs % 60
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    // Auto-dismiss on successful save
    LaunchedEffect(logState) {
        if (logState is LogCallState.Done) {
            CallStateHolder.reset()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { /* force explicit choice */ },
        title = { Text("Log Call to Zoho") },
        text  = {
            Column {
                Text(
                    text  = buildString {
                        val dir = if (CallStateHolder.callDirection == "Inbound")
                            "Incoming call from" else "Outgoing call to"
                        append("$dir ${CallStateHolder.contactName}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Duration: $durationDisplay",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 3,
                )
                if (logState is LogCallState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = (logState as LogCallState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { callVm.logCallToZoho(description) },
                enabled = logState !is LogCallState.Saving,
            ) {
                if (logState is LogCallState.Saving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White,
                    )
                } else {
                    Text("Save to Zoho")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                callVm.resetState()
                CallStateHolder.reset()
                onDismiss()
            }) { Text("Skip") }
        },
    )
}