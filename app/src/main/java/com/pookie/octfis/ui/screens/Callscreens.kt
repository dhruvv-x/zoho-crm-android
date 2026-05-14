package com.pookie.octfis.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.CrmCall
import com.pookie.octfis.data.repository.CallRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// List
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallListScreen(
    navController: NavController,
    vm: CallsViewModel = viewModel(),
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val uiState      by vm.uiState.collectAsState()
    val searchQuery  by vm.searchQuery.collectAsState()
    val actionState  by vm.actionState.collectAsState()
    val listState     = rememberLazyListState()
    var searchActive  by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val snackbarHost   = remember { SnackbarHostState() }

    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) { if (nearBottom && !searchActive) vm.loadNextPage() }
    LaunchedEffect(searchActive) { if (searchActive) focusRequester.requestFocus() }
    LaunchedEffect(actionState) {
        if (actionState is CallActionState.Error) {
            snackbarHost.showSnackbar((actionState as CallActionState.Error).message)
            vm.resetActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar    = { CrmBottomBar(navController, currentRoute) },
        floatingActionButton = {
            if (!searchActive) {
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.CreateCall.route) },
                    containerColor = CrmPrimary,
                    contentColor   = Color.White,
                    shape          = CircleShape,
                ) { Icon(Icons.Default.Add, "Create Call") }
            }
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (searchActive) {
                    IconButton(onClick = { searchActive = false; vm.setSearch("") }) {
                        Icon(Icons.Default.ArrowBack, "Close Search", tint = CrmOnSurface)
                    }
                    TextField(
                        value         = searchQuery,
                        onValueChange = { vm.setSearch(it) },
                        placeholder   = { Text("Search calls…", fontSize = 14.sp, color = CrmSubtext) },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {}),
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.setSearch("") }) {
                            Icon(Icons.Default.Close, "Clear", tint = CrmSubtext)
                        }
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Call, null, tint = CrmPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Calls", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    if (uiState is CallsUiState.Success) {
                        Text("${(uiState as CallsUiState.Success).calls.size} loaded", fontSize = 11.sp, color = CrmSubtext)
                    }
                    IconButton(onClick = { searchActive = true }) { Icon(Icons.Default.Search, "Search", tint = CrmSubtext) }
                    IconButton(onClick = { vm.load() }) { Icon(Icons.Default.Refresh, "Refresh", tint = CrmSubtext) }
                }
            }

            when (val s = uiState) {
                is CallsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CrmPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading from Zoho CRM…", color = CrmSubtext, fontSize = 13.sp)
                        }
                    }
                }

                is CallsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudOff, null, tint = CrmError, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(s.message, color = CrmSubtext, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = CrmPrimary)) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is CallsUiState.Success -> {
                    if (s.calls.isEmpty() && searchQuery.isNotBlank()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results for \"$searchQuery\"", color = CrmSubtext, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            state          = listState,
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            itemsIndexed(s.calls, key = { _, c -> c.zohoId }) { _, call ->
                                CallSwipeRow(
                                    call     = call,
                                    onDelete = { vm.deleteCall(call.zohoId) },
                                    onClick  = { navController.navigate(Screen.CallDetail.createRoute(call.zohoId)) },
                                    onEdit   = { navController.navigate(Screen.EditCall.createRoute(call.zohoId)) },
                                )
                                HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)
                            }
                            if (s.hasMore && searchQuery.isBlank()) {
                                item {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CrmPrimary, strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallSwipeRow(call: CrmCall, onDelete: () -> Unit, onClick: () -> Unit, onEdit: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) showDeleteDialog = true
            false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Call?") },
            text    = { Text("\"${call.subject}\" will be permanently deleted from Zoho CRM.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) { Text("Delete", color = CrmError) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    SwipeToDismissBox(
        state             = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) CrmError else Color.Transparent,
                label = "swipe-bg",
            )
            Box(Modifier.fillMaxSize().background(color).padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        },
    ) {
        CallRow(call, onClick, onEdit)
    }
}

@Composable
private fun CallRow(call: CrmCall, onClick: () -> Unit, onEdit: () -> Unit) {
    val typeColor = if (call.callType.lowercase() == "inbound") CrmSuccess else CrmPrimary

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (call.callType.lowercase() == "inbound") Icons.Default.CallReceived else Icons.Default.CallMade,
                contentDescription = null,
                tint     = typeColor,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(call.subject, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CrmOnSurface)
            Spacer(Modifier.height(2.dp))
            if (call.callStartTime.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = CrmSubtext, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(call.callStartTime.take(16).replace("T", " "), fontSize = 11.sp, color = CrmSubtext)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = typeColor.copy(alpha = 0.12f)) {
                    Text(call.callType, fontSize = 10.sp, color = typeColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                if (call.duration.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(call.duration, fontSize = 11.sp, color = CrmSubtext)
                }
                if (call.status.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text("• ${call.status}", fontSize = 11.sp, color = CrmSubtext)
                }
            }
        }

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit", tint = CrmSubtext, modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Detail
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailScreen(
    navController: NavController,
    callId: String,
    vm: CallsViewModel = viewModel(),
) {
    val call = CallRepository.cache.firstOrNull { it.zohoId == callId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val actionState by vm.actionState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is CallActionState.Done  -> { vm.resetActionState(); navController.popBackStack() }
            is CallActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetActionState() }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Call?") },
            text    = { Text("\"${call?.subject}\" will be permanently deleted from Zoho CRM.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; call?.let { vm.deleteCall(it.zohoId) } }) {
                    Text("Delete", color = CrmError)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = call?.subject?.ifEmpty { "Call Detail" } ?: "Call Detail",
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
                    if (call != null) {
                        IconButton(onClick = { navController.navigate(Screen.EditCall.createRoute(callId)) }) {
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
        if (call == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Call not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        if (actionState is CallActionState.Working) {
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
            SectionHeader("Call Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Subject",    call.subject.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Call Type",  call.callType.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Status",     call.status.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Start Time", call.callStartTime.replace("T", " ").ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Duration",   call.duration.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Owner",      call.ownerName.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact",    call.contactName.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Related To", call.relatedTo.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Description")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                FormRow("Description", call.description.ifEmpty { "—" })
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Create
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCallScreen(
    navController: NavController,
    vm: CallFormViewModel = viewModel(),
) {
    var subject     by remember { mutableStateOf("") }
    var startTime   by remember { mutableStateOf("") }
    var duration    by remember { mutableStateOf("") }
    var callType    by remember { mutableStateOf("Outbound") }
    var status      by remember { mutableStateOf("Scheduled") }
    var description by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf(Pair("", "-None-")) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is CallActionState.Done  -> navController.popBackStack()
            is CallActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Create Call", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is CallActionState.Working
                    Button(
                        onClick  = {
                            if (!saving) vm.create(subject, startTime, duration, callType, status, description, selectedOwner.first)
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
            SectionHeader("Call Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    ActivityTextField("Subject",    subject,   "Enter call subject", required = true) { subject = it }
                    ActivityDivider()
                    ActivityDropdown("Call Type", callType, vm.callTypes,  false) { callType = it }
                    ActivityDivider()
                    ActivityDropdown("Status",    status,   vm.statusList, false) { status = it }
                    ActivityDivider()
                    ActivityTextField("Start Time", startTime, "YYYY-MM-DDTHH:MM:SS") { startTime = it }
                    ActivityDivider()
                    ActivityTextField("Duration",   duration,  "e.g. 00:05:00")       { duration = it }
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

// ═══════════════════════════════════════════════════════════════════════════════
// Edit
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCallScreen(
    navController: NavController,
    callId: String,
    vm: CallFormViewModel = viewModel(),
) {
    val call = CallRepository.cache.firstOrNull { it.zohoId == callId }

    var subject     by remember { mutableStateOf(call?.subject ?: "") }
    var startTime   by remember { mutableStateOf(call?.callStartTime ?: "") }
    var duration    by remember { mutableStateOf(call?.duration ?: "") }
    var callType    by remember { mutableStateOf(call?.callType ?: "Outbound") }
    var status      by remember { mutableStateOf(call?.status ?: "Scheduled") }
    var description by remember { mutableStateOf(call?.description ?: "") }
    var selectedOwner by remember { mutableStateOf(Pair("", call?.ownerName ?: "-None-")) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is CallActionState.Done  -> navController.popBackStack()
            is CallActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Call", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is CallActionState.Working
                    Button(
                        onClick  = {
                            if (!saving && call != null)
                                vm.update(call.zohoId, subject, startTime, duration, callType, status, description, selectedOwner.first)
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
        if (call == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Call not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Call Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    ActivityTextField("Subject",    subject,   "Enter call subject", required = true) { subject = it }
                    ActivityDivider()
                    ActivityDropdown("Call Type", callType, vm.callTypes,  false) { callType = it }
                    ActivityDivider()
                    ActivityDropdown("Status",    status,   vm.statusList, false) { status = it }
                    ActivityDivider()
                    ActivityTextField("Start Time", startTime, "YYYY-MM-DDTHH:MM:SS") { startTime = it }
                    ActivityDivider()
                    ActivityTextField("Duration",   duration,  "e.g. 00:05:00")       { duration = it }
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