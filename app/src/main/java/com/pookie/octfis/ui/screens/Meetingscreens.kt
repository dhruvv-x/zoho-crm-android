package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.MeetingRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

// ── Detail ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    navController: NavController,
    meetingId: String,
    vm: MeetingsViewModel = viewModel(),
) {
    val meeting = MeetingRepository.cache.firstOrNull { it.id == meetingId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val actionState by vm.actionState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is MeetingActionState.Done  -> { vm.resetActionState(); navController.popBackStack() }
            is MeetingActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetActionState() }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Meeting?") },
            text    = { Text("\"${meeting?.title}\" will be permanently deleted from Zoho CRM.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; meeting?.let { vm.deleteMeeting(it.id) } }) {
                    Text("Delete", color = CrmError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = meeting?.title?.ifEmpty { "Meeting Detail" } ?: "Meeting Detail",
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
                    if (meeting != null) {
                        IconButton(onClick = { navController.navigate(Screen.EditMeeting.createRoute(meetingId)) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = CrmError)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        if (meeting == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Meeting not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        if (actionState is MeetingActionState.Working) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrmPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Meeting Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Title",       meeting.title.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Start",       meeting.startDateTime.replace("T", " ").ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("End",         meeting.endDateTime.replace("T", " ").ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Location",    meeting.location.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Owner",       meeting.owner.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Participants", meeting.participants.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Description")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                FormRow("Description", meeting.description.ifEmpty { "—" })
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Create ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingScreen(
    navController: NavController,
    vm: MeetingFormViewModel = viewModel(),
) {
    var title         by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf("") }
    var endDateTime   by remember { mutableStateOf("") }
    var location      by remember { mutableStateOf("") }
    var description   by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf(Pair("", "-None-")) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is MeetingActionState.Done  -> navController.popBackStack()
            is MeetingActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Create Meeting", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is MeetingActionState.Working
                    Button(
                        onClick  = {
                            if (!saving) vm.create(title, startDateTime, endDateTime, location, description, selectedOwner.first)
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Meeting Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    ActivityTextField("Title",          title,         "Enter meeting title", required = true) { title = it }
                    ActivityDivider()
                    ActivityTextField("Start Date/Time", startDateTime, "YYYY-MM-DDTHH:MM:SS", required = true) { startDateTime = it }
                    ActivityDivider()
                    ActivityTextField("End Date/Time",   endDateTime,   "YYYY-MM-DDTHH:MM:SS", required = true) { endDateTime = it }
                    ActivityDivider()
                    ActivityTextField("Location",        location,      "Enter location")                        { location = it }
                    ActivityDivider()
                    ActivityDropdown(
                        label   = "Owner",
                        value   = selectedOwner.second,
                        options = owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = owners.firstOrNull { it.second == name } ?: Pair("", name) }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Additional Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                ActivityTextField("Description", description, "Short description", multiline = true) { description = it }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Edit ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingScreen(
    navController: NavController,
    meetingId: String,
    vm: MeetingFormViewModel = viewModel(),
) {
    val meeting = MeetingRepository.cache.firstOrNull { it.id == meetingId }

    var title         by remember { mutableStateOf(meeting?.title ?: "") }
    var startDateTime by remember { mutableStateOf(meeting?.startDateTime ?: "") }
    var endDateTime   by remember { mutableStateOf(meeting?.endDateTime ?: "") }
    var location      by remember { mutableStateOf(meeting?.location ?: "") }
    var description   by remember { mutableStateOf(meeting?.description ?: "") }
    var selectedOwner by remember { mutableStateOf(Pair("", meeting?.owner ?: "-None-")) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is MeetingActionState.Done  -> navController.popBackStack()
            is MeetingActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Meeting", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is MeetingActionState.Working
                    Button(
                        onClick  = {
                            if (!saving && meeting != null)
                                vm.update(meeting.id, title, startDateTime, endDateTime, location, description, selectedOwner.first)
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        if (meeting == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Meeting not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Meeting Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    ActivityTextField("Title",          title,         "Enter meeting title", required = true) { title = it }
                    ActivityDivider()
                    ActivityTextField("Start Date/Time", startDateTime, "YYYY-MM-DDTHH:MM:SS", required = true) { startDateTime = it }
                    ActivityDivider()
                    ActivityTextField("End Date/Time",   endDateTime,   "YYYY-MM-DDTHH:MM:SS", required = true) { endDateTime = it }
                    ActivityDivider()
                    ActivityTextField("Location",        location,      "Enter location")                        { location = it }
                    ActivityDivider()
                    ActivityDropdown(
                        label   = "Owner",
                        value   = selectedOwner.second,
                        options = owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = owners.firstOrNull { it.second == name } ?: Pair("", name) }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Additional Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                ActivityTextField("Description", description, "Short description", multiline = true) { description = it }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}