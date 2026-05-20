package com.pookie.octfis.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.remote.CallStateHolder
import com.pookie.octfis.data.repository.ContactRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(navController: NavController, contactId: Int) {

    val contact      = ContactRepository.cache.firstOrNull { it.id == contactId }
    val context      = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val callVm: ContactCallViewModel = viewModel()
    val logState by callVm.logState.collectAsState()

    var showPostCallDialog by remember { mutableStateOf(false) }
    var description        by remember { mutableStateOf("") }

    // ── Core fix: detect return from dialer via onResume ──────────────────────
    //
    // When the user taps the call button we:
    //   1. Write contactZohoId / contactName into CallStateHolder
    //   2. Record callInitiatedAtMillis  ← the "we left the app" timestamp
    //   3. Set isCallActive = true
    //   4. Fire ACTION_CALL → dialer takes the foreground
    //
    // When the user finishes the call and comes back:
    //   onResume fires → we see isCallActive == true → show the dialog.
    //
    // This works on ALL Android versions with zero permissions beyond CALL_PHONE.
    //
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && CallStateHolder.isCallActive) {
                // Record when we got back as the end-of-call proxy
                CallStateHolder.callEndMillis = System.currentTimeMillis()
                CallStateHolder.isCallActive  = false
                showPostCallDialog = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Close dialog automatically after a successful save
    LaunchedEffect(logState) {
        if (logState is LogCallState.Done) {
            showPostCallDialog = false
            description        = ""
            callVm.resetState()
            CallStateHolder.reset()
        }
    }

    // ── Permission launcher ───────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) return@rememberLauncherForActivityResult
        val number = contact?.mobile?.ifBlank { contact.phone } ?: return@rememberLauncherForActivityResult
        if (number.isBlank()) return@rememberLauncherForActivityResult
        launchCall(context, contact.zohoId, contact.fullName, number)
    }

    fun initiateCall() {
        val number = contact?.mobile?.ifBlank { contact.phone } ?: return
        if (number.isBlank()) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCall(context, contact.zohoId, contact.fullName, number)
        } else {
            permissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // ── Post-call dialog ──────────────────────────────────────────────────────
    if (showPostCallDialog) {
        AlertDialog(
            onDismissRequest = { /* force explicit choice */ },
            title = { Text("Log Call to Zoho") },
            text  = {
                Column {
                    Text(
                        text  = "Call with ${CallStateHolder.contactName} ended.",
                        style = MaterialTheme.typography.bodyMedium,
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
                    showPostCallDialog = false
                    description        = ""
                    callVm.resetState()
                    CallStateHolder.reset()
                }) { Text("Skip") }
            },
        )
    }

    // ── Scaffold / UI ─────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = contact?.fullName?.ifEmpty { "Contact Detail" } ?: "Contact Detail",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { initiateCall() }) {
                        Icon(Icons.Default.Call, "Call", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.EditContact.createRoute(contactId))
                    }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        if (contact == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Contact not found",
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    FormRow("Full Name",     contact.fullName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("First Name",    contact.firstName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Last Name",     contact.lastName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Phone",         contact.phone.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Mobile",        contact.mobile.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Email",         contact.email.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Name",  contact.accountName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Title",         contact.title.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Department",    contact.department.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Lead Source",   contact.leadSource.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact Owner", contact.contactOwner.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",   contact.description.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    FormRow("Mailing Street",  contact.mailingStreet.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Mailing City",    contact.mailingCity.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Mailing State",   contact.mailingState.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Mailing ZIP",     contact.mailingZip.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Mailing Country", contact.mailingCountry.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helper (top-level, not inside the composable) ─────────────────────────────
/**
 * Writes to CallStateHolder and fires ACTION_CALL.
 * Called both from the permission-granted branch and the already-granted branch.
 */
private fun launchCall(
    context: android.content.Context,
    zohoId: String,
    name: String,
    number: String,
) {
    CallStateHolder.contactZohoId        = zohoId
    CallStateHolder.contactName          = name
    CallStateHolder.callInitiatedAtMillis = System.currentTimeMillis()
    CallStateHolder.callStartMillis      = CallStateHolder.callInitiatedAtMillis
    CallStateHolder.callEndMillis        = 0L
    CallStateHolder.isCallActive         = true
    context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
}