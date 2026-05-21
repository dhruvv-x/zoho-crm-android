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
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

    val detailVm: ContactDetailViewModel = viewModel()
    val contactCalls by detailVm.calls.collectAsState()
    val callsLoading by detailVm.loading.collectAsState()

    // ISSUE 1 FIX: Post-call logging is handled entirely by PostCallLogActivity,
    // which is launched by CallMonitorService the moment CALL_STATE_IDLE fires.
    // This screen has NO onResume dialog logic and NO inline call-logging dialog.

    // Load linked calls whenever contact zohoId is available
    LaunchedEffect(contact?.zohoId) {
        contact?.zohoId?.let { detailVm.loadCallsForContact(it) }
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

            // ── Closed Activities ─────────────────────────────────────────────
            SectionHeader("Closed Activities")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                if (callsLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                } else if (contactCalls.isEmpty()) {
                    Text(
                        text = "No logged calls yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    Column {
                        contactCalls.forEach { call ->
                            val icon = if (call.callType.equals("Inbound", ignoreCase = true))
                                Icons.Default.CallReceived else Icons.Default.CallMade
                            ListItem(
                                headlineContent = {
                                    Text(call.subject, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                supportingContent = {
                                    // ISSUE 4 FIX: call.duration is already "MM:SS" (converted
                                    // from Call_Duration_In_Seconds in CallRepository.map())
                                    Text(
                                        text = "${call.callType} · ${call.duration} · ${call.callStartTime.take(10)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                leadingContent = {
                                    Icon(icon, contentDescription = call.callType, tint = CrmPrimary)
                                },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helper (top-level function, not inside the composable) ────────────────────
/**
 * Sets CallStateHolder and fires ACTION_CALL.
 *
 * ISSUE 3 FIX:
 *   callInitiatedAtMillis = now  (fallback if service never fires OFFHOOK)
 *   callStartMillis       = 0L   (will be set precisely by CALL_STATE_OFFHOOK in service)
 *   callEndMillis         = 0L   (will be set precisely by CALL_STATE_IDLE in service)
 *   isCallActive          = true (tells service to launch PostCallLogActivity immediately on IDLE)
 */
private fun launchCall(
    context: android.content.Context,
    zohoId: String,
    name: String,
    number: String,
) {
    CallStateHolder.contactZohoId         = zohoId
    CallStateHolder.contactName           = name
    CallStateHolder.callInitiatedAtMillis = System.currentTimeMillis()
    CallStateHolder.callStartMillis       = 0L   // set precisely by CALL_STATE_OFFHOOK
    CallStateHolder.callEndMillis         = 0L   // set precisely by CALL_STATE_IDLE
    CallStateHolder.isCallActive          = true
    context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
}