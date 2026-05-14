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
import com.pookie.octfis.data.repository.TaskRepository
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
                        if (saving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Task Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    ActivityTextField("Subject",     subject,     "Enter task subject", required = true) { subject = it }
                    ActivityDivider()
                    ActivityTextField("Due Date",    dueDate,     "YYYY-MM-DD")                          { dueDate = it }
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
fun EditTaskScreen(
    navController: NavController,
    taskId: String,
    vm: TaskFormViewModel = viewModel(),
) {
    val task = TaskRepository.cache.firstOrNull { it.id == taskId }

    var subject     by remember { mutableStateOf(task?.subject ?: "") }
    var dueDate     by remember { mutableStateOf(task?.dueDate ?: "") }
    var status      by remember { mutableStateOf(task?.status ?: "Not Started") }
    var priority    by remember { mutableStateOf(task?.priority ?: "Normal") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedOwner by remember { mutableStateOf(Pair("", task?.owner ?: "-None-")) }

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
                title = { Text("Edit Task", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is TaskActionState.Working
                    Button(
                        onClick  = {
                            if (!saving && task != null)
                                vm.update(task.id, subject, dueDate, status, priority, description, selectedOwner.first)
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
        if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Task not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Task Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
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
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
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
            placeholder   = { Text(placeholder, color = CrmSubtext.copy(alpha = 0.7f), fontSize = 13.sp) },
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
                .menuAnchor()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(120.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = CrmSubtext)
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
    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}