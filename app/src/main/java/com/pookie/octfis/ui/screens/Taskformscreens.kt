package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

// ── Create ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    vm: TaskFormViewModel = viewModel(),
) {
    var subject     by remember { mutableStateOf("") }
    var dueDate     by remember { mutableStateOf("") }
    var status      by remember { mutableStateOf("Not Started") }
    var priority    by remember { mutableStateOf("Normal") }
    var description by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf(Pair("", "-None-")) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val statusList     by vm.statusList.collectAsState()
    val priorityList   by vm.priorityList.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is TaskActionState.Done  -> navController.popBackStack()
            is TaskActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Create Task", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is TaskActionState.Working
                    Button(
                        onClick  = {
                            if (!saving) vm.create(subject, dueDate, status, priority, description, selectedOwner.first)
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving) CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                        else Text("Save", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Task Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    ActivityTextField("Subject",  subject,  "Enter task subject", required = true) { subject = it }
                    ActivityDivider()
                    ActivityTextField("Due Date", dueDate,  "YYYY-MM-DD")                          { dueDate = it }
                    ActivityDivider()
                    ActivityDropdown("Status",   status,   statusList,   optionsLoading)  { status = it }
                    ActivityDivider()
                    ActivityDropdown("Priority", priority, priorityList, optionsLoading)  { priority = it }
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
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                ActivityTextField("Description", description, "Short description", multiline = true) { description = it }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Edit ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    navController: NavController,
    taskId: String,
    vm: TaskFormViewModel = viewModel(),
    detailVm: TaskDetailViewModel = viewModel(factory = TaskDetailViewModel.Factory(taskId)),
) {
    val detailState by detailVm.uiState.collectAsState()
    val task = (detailState as? TaskDetailUiState.Success)?.task

    var subject     by remember { mutableStateOf("") }
    var dueDate     by remember { mutableStateOf("") }
    var status      by remember { mutableStateOf("Not Started") }
    var priority    by remember { mutableStateOf("Normal") }
    var description by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf(Pair("", "-None-")) }
    var fieldsInitialised by remember { mutableStateOf(false) }

    val saveState      by vm.saveState.collectAsState()
    val owners         by vm.owners.collectAsState()
    val statusList     by vm.statusList.collectAsState()
    val priorityList   by vm.priorityList.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(task, owners) {
        if (task != null && !fieldsInitialised) {
            subject     = task.subject
            dueDate     = task.dueDate
            status      = task.status
            priority    = task.priority
            description = task.description
            val ownerPair = owners.firstOrNull { it.second == task.ownerName }
            selectedOwner = ownerPair ?: Pair(task.ownerId, task.ownerName.ifEmpty { "-None-" })
            fieldsInitialised = true
        }
    }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is TaskActionState.Done  -> {
                // ✅ FIX: signal the detail screen to re-fetch instead of calling
                // detailVm.load() here (which fires on a dying VM scope and is discarded)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("shouldRefresh", true)
                navController.popBackStack()
            }
            is TaskActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Task", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is TaskActionState.Working
                    Button(
                        onClick = {
                            if (!saving && task != null)
                                vm.update(task.zohoId, subject, dueDate, status, priority, description, selectedOwner.first)
                        },
                        enabled  = !saving && task != null,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving) CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                        else Text("Save", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        if (detailState is TaskDetailUiState.Loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrmPrimary)
            }
            return@Scaffold
        }

        if (detailState is TaskDetailUiState.Error) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    text     = (detailState as TaskDetailUiState.Error).message,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Task Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    ActivityTextField("Subject",  subject,  "Enter task subject", required = true) { subject = it }
                    ActivityDivider()
                    ActivityTextField("Due Date", dueDate,  "YYYY-MM-DD")                          { dueDate = it }
                    ActivityDivider()
                    ActivityDropdown("Status",   status,   statusList,   optionsLoading) { status = it }
                    ActivityDivider()
                    ActivityDropdown("Priority", priority, priorityList, optionsLoading) { priority = it }
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
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                ActivityTextField("Description", description, "Short description", multiline = true) { description = it }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Shared form composables (Activity-scoped) ─────────────────────────────────

@Composable
internal fun ActivityTextField(
    label        : String,
    value        : String,
    placeholder  : String,
    required     : Boolean = false,
    multiline    : Boolean = false,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
    ) {
        Text(
            text     = if (required) "$label *" else label,
            fontSize = 13.sp,
            color    = if (required) CrmPrimary else CrmSubtext,
            modifier = Modifier.width(120.dp).padding(top = if (multiline) 14.dp else 0.dp),
        )
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 13.sp) },
            singleLine    = !multiline,
            minLines      = if (multiline) 3 else 1,
            modifier      = Modifier.weight(1f),
            colors        = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivityDropdown(
    label   : String,
    value   : String,
    options : List<String>,
    loading : Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { if (!loading) expanded = it },
        modifier         = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text     = value,
                    fontSize = 13.sp,
                    color    = if (value == "-None-") CrmSubtext else CrmOnSurface,
                    modifier = Modifier.weight(1f),
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text           = { Text(option, fontSize = 14.sp) },
                    onClick        = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
internal fun ActivityDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}