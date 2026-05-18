package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pookie.octfis.engine.form.FormUiState
import com.pookie.octfis.engine.form.RecordFormViewModel
import com.pookie.octfis.engine.renderer.DynamicFormRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormScreen(
    navController    : NavController,
    moduleName       : String,
    recordId         : String?  = null,
    screenTitle      : String?  = null,
    viewModelFactory : RecordFormViewModel.Factory,
) {
    // ── ViewModel via Assisted Inject ─────────────────────────────────────
    val viewModel: RecordFormViewModel = remember(moduleName, recordId) {
        viewModelFactory.create(moduleName, recordId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fields  by viewModel.fields.collectAsStateWithLifecycle()

    // ── Navigate back on success ──────────────────────────────────────────
    LaunchedEffect(uiState) {
        if (uiState is FormUiState.Success) {
            navController.popBackStack()
        }
    }

    // ── Error snackbar ────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is FormUiState.Error) {
            snackbarHostState.showSnackbar(
                message  = (uiState as FormUiState.Error).message,
                duration = SnackbarDuration.Long,
            )
            viewModel.resetState()
        }
    }

    val title = screenTitle
        ?: if (viewModel.isEditMode) "Edit $moduleName" else "New $moduleName"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.submit() },
                        enabled = uiState !is FormUiState.Submitting &&
                                uiState !is FormUiState.LoadingMetadata &&
                                uiState !is FormUiState.LoadingRecord,
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState !is FormUiState.LoadingMetadata &&
                uiState !is FormUiState.LoadingRecord) {
                ExtendedFloatingActionButton(
                    onClick  = { viewModel.submit() },
                    icon     = { Icon(Icons.Default.Check, contentDescription = null) },
                    text     = {
                        Text(
                            if (uiState is FormUiState.Submitting) "Saving…"
                            else if (viewModel.isEditMode) "Update" else "Create"
                        )
                    },
                    expanded = true,
                )
            }
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {

                // ── Loading metadata or record ────────────────────────────
                is FormUiState.LoadingMetadata,
                is FormUiState.LoadingRecord -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = if (uiState is FormUiState.LoadingMetadata)
                                "Loading form…" else "Loading record…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Form ready (Idle / Submitting / Error) ────────────────
                else -> {
                    Box {
                        DynamicFormRenderer(
                            fields         = fields,
                            values         = viewModel.formState.values,
                            errors         = viewModel.formState.visibleErrors(),
                            onValueChange  = { apiName, value ->
                                viewModel.onFieldChange(apiName, value)
                            },
                            scrollable     = true,
                            modifier       = Modifier.fillMaxSize(),
                            onLookupSearch = { module, query ->   // ← ADDED
                                viewModel.searchLookup(module, query)
                            },
                        )

                        // Submitting overlay
                        if (uiState is FormUiState.Submitting) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}