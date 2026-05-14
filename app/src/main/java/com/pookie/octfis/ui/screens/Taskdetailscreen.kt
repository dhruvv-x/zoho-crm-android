package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.TaskRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: String,
    vm: TasksViewModel = viewModel(),
) {
    val task = TaskRepository.cache.firstOrNull { it.id == taskId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val actionState by vm.actionState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is TaskActionState.Done  -> { vm.resetActionState(); navController.popBackStack() }
            is TaskActionState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetActionState() }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Task?") },
            text    = { Text("\"${task?.subject}\" will be permanently deleted from Zoho CRM.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; task?.let { vm.deleteTask(it.id) } }) {
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
                        text       = task?.subject?.ifEmpty { "Task Detail" } ?: "Task Detail",
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
                    if (task != null) {
                        IconButton(onClick = { navController.navigate(Screen.EditTask.createRoute(taskId)) }) {
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
        if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Task not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        if (actionState is TaskActionState.Working) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
            SectionHeader("Task Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Subject",    task.subject.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Due Date",   task.dueDate.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Status",     task.status.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Priority",   task.priority.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Owner",      task.owner.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact",    task.contactName.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Related To", task.relatedTo.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Description")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                FormRow("Description", task.description.ifEmpty { "—" })
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}