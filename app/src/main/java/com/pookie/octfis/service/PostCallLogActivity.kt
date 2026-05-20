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
 * Transparent, dialog-themed Activity launched by the floating overlay button.
 * Uses the same ContactCallViewModel + CallRepository as the in-app flow.
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

    // Compute duration once
    val durationDisplay = remember {
        val start = CallStateHolder.callStartMillis.takeIf { it > 0L }
            ?: CallStateHolder.callInitiatedAtMillis
        val end = CallStateHolder.callEndMillis.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val totalSecs = ((end - start) / 1000).coerceAtLeast(0)
        val m = totalSecs / 60
        val s = totalSecs % 60
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    // Auto-dismiss on success
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