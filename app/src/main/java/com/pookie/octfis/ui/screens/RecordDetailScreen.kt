// ui/screens/RecordDetailScreen.kt
package com.pookie.octfis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.detail.DetailUiState
import com.pookie.octfis.engine.detail.RecordDetailSkeleton
import com.pookie.octfis.engine.detail.RecordDetailViewModel
import com.pookie.octfis.data.remote.dto.ZohoRelatedList
import com.pookie.octfis.engine.detail.RelatedRecordsSection
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*
import com.pookie.octfis.util.ConnectivityObserver
import kotlinx.coroutines.launch

// ── Error classifier ──────────────────────────────────────────────────────────

private enum class DetailErrorKind { NETWORK, AUTH, UNKNOWN }

private fun classifyDetailError(message: String): DetailErrorKind = when {
    message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect",      ignoreCase = true) ||
            message.contains("timeout",                ignoreCase = true) ||
            message.contains("SocketTimeout",          ignoreCase = true) ||
            message.contains("UnknownHost",            ignoreCase = true) ||
            message.contains("Network",                ignoreCase = true) -> DetailErrorKind.NETWORK

    message.contains("401", ignoreCase = true) ||
            message.contains("403", ignoreCase = true) ||
            message.contains("unauthorized", ignoreCase = true) ||
            message.contains("token",        ignoreCase = true) -> DetailErrorKind.AUTH

    else -> DetailErrorKind.UNKNOWN
}

private data class DetailErrorDisplay(
    val icon    : ImageVector,
    val title   : String,
    val subtitle: String,
)

private fun detailErrorDisplay(kind: DetailErrorKind): DetailErrorDisplay = when (kind) {
    DetailErrorKind.NETWORK -> DetailErrorDisplay(
        icon     = Icons.Default.WifiOff,
        title    = "No connection",
        subtitle = "Check your internet and try again.",
    )
    DetailErrorKind.AUTH -> DetailErrorDisplay(
        icon     = Icons.Default.Lock,
        title    = "Session expired",
        subtitle = "Your session has expired. Please sign in again.",
    )
    DetailErrorKind.UNKNOWN -> DetailErrorDisplay(
        icon     = Icons.Default.ErrorOutline,
        title    = "Something went wrong",
        subtitle = "We couldn't load this record. Please try again.",
    )
}

@Composable
private fun DetailErrorState(
    message : String,
    onRetry : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kind    = classifyDetailError(message)
    val display = detailErrorDisplay(kind)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                imageVector        = display.icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text      = display.title,
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = display.subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Retry")
            }
        }
    }
}

// ── RecordDetailScreen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    navController        : NavController,
    moduleName           : String,
    recordId             : String,
    viewModelFactory     : RecordDetailViewModel.Factory,
    repository           : ZohoRecordRepository,
    connectivityObserver : ConnectivityObserver,
) {
    val viewModel = remember(moduleName, recordId) {
        viewModelFactory.create(moduleName, recordId)
    }
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by connectivityObserver.isOnline.collectAsStateWithLifecycle(initialValue = true)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteInProgress by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleteInProgress) showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector        = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text("Delete record?") },
            text  = {
                Text(
                    "This will permanently delete this $moduleName record from Zoho CRM. " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    enabled = !deleteInProgress,
                    onClick = {
                        deleteInProgress = true
                        scope.launch {
                            repository.deleteRecord(moduleName, recordId)
                                .onSuccess {
                                    showDeleteDialog = false
                                    deleteInProgress = false
                                    navController.popBackStack()
                                }
                                .onFailure { e ->
                                    deleteInProgress = false
                                    showDeleteDialog  = false
                                    snackbarHostState.showSnackbar(
                                        message  = "Delete failed: ${e.message ?: "Unknown error"}",
                                        duration = SnackbarDuration.Long,
                                    )
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    if (deleteInProgress) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            color       = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !deleteInProgress,
                    onClick = { showDeleteDialog = false },
                ) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text     = "$moduleName Details",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (uiState is DetailUiState.Error) {
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                        if (uiState is DetailUiState.Success) {
                            // Edit moved into TopBar — no FAB
                            IconButton(onClick = {
                                navController.navigate(
                                    Screen.ModuleEdit.createRoute(moduleName, recordId)
                                )
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                navController.navigate(
                                    Screen.ModuleClone.createRoute(moduleName, recordId)
                                )
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Clone")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector        = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint               = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    },
                )
                AnimatedVisibility(
                    visible = !isOnline,
                    enter   = expandVertically(),
                    exit    = shrinkVertically(),
                ) {
                    OfflineBanner()
                }
            }
        },
    ) { padding ->

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                RecordDetailSkeleton(
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
            is DetailUiState.Error -> {
                DetailErrorState(
                    message  = state.message,
                    onRetry  = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
            is DetailUiState.Success -> {
                DetailContent(
                    moduleName    = moduleName,
                    recordId      = recordId,
                    fields        = state.fields,
                    relatedLists  = state.relatedLists,
                    values        = state.values,
                    repository    = repository,
                    navController = navController,
                    modifier      = Modifier.fillMaxSize().padding(padding),
                )
            }
        }
    }
}

// ── Detail content ────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    moduleName    : String,
    recordId      : String,
    fields        : List<FieldMetadata>,
    values        : Map<String, Any?>,
    repository    : ZohoRecordRepository,
    relatedLists  : List<ZohoRelatedList>,
    navController : NavController,
    modifier      : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(CrmBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        val sections = fields
            .filter { it.apiName != "id" }
            .groupBy { it.sectionName }

        sections.forEach { (sectionName, sectionFields) ->

            // ── Skip sections with no displayable fields ───────────────────
            val visibleFieldsCheck = sectionFields.filter { field ->
                formatFieldValue(field, values[field.apiName]).isNotBlank() || field.required
            }
            if (visibleFieldsCheck.isEmpty()) return@forEach

            // ── Blue banner section header ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(CrmPrimary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text       = sectionName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }

            // ── White card with rows ───────────────────────────────────────
            Surface(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape           = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                color           = CrmSurface,
                shadowElevation = 1.dp,
            ) {
                Column {
                    visibleFieldsCheck.forEachIndexed { index, field ->
                        DetailFieldRow(
                            label  = field.label,
                            value  = formatFieldValue(field, values[field.apiName]),
                            type   = field.type,
                            isLast = index == visibleFieldsCheck.lastIndex,
                        )
                    }
                }
            }
        }

        // ── Related section header ─────────────────────────────────────────
        if (relatedLists.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(CrmPrimary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text       = "Related",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }

            RelatedRecordsSection(
                parentModule  = moduleName,
                parentId      = recordId,
                repository    = repository,
                relatedLists  = relatedLists,
                onRecordClick = { relatedModule, relatedId ->
                    navController.navigate(
                        Screen.ModuleDetail.createRoute(relatedModule, relatedId)
                    )
                },
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Single field row ──────────────────────────────────────────────────────────

@Composable
private fun DetailFieldRow(
    label : String,
    value : String,
    type  : FieldType,
    isLast: Boolean,
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = CrmOnSurface,
                modifier   = Modifier.width(130.dp),
            )
            val valueColor = when (type) {
                FieldType.EMAIL, FieldType.PHONE, FieldType.URL -> CrmPrimary
                else -> CrmSubtext
            }
            Text(
                text     = value.ifBlank { "—" },
                style    = MaterialTheme.typography.bodyMedium,
                color    = valueColor,
                modifier = Modifier.weight(1f),
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = CrmDivider,
            )
        }
    }
}

// ── Value formatter ───────────────────────────────────────────────────────────

private fun formatFieldValue(field: FieldMetadata, raw: Any?): String {
    if (raw == null) return ""
    if (raw is Map<*, *> &&
        field.type != FieldType.LOOKUP &&
        field.type != FieldType.OWNER) return ""
    if (raw is List<*> &&
        field.type != FieldType.MULTI_SELECT) return ""
    return when (field.type) {
        FieldType.BOOLEAN ->
            if (raw.toString().equals("true", ignoreCase = true)) "Yes" else "No"
        FieldType.LOOKUP, FieldType.OWNER -> {
            @Suppress("UNCHECKED_CAST")
            (raw as? Map<String, Any?>)?.get("name")?.toString() ?: raw.toString()
        }
        FieldType.MULTI_SELECT -> when (raw) {
            is List<*> -> raw.joinToString(", ")
            else       -> raw.toString().replace(";", ", ")
        }
        FieldType.CURRENCY, FieldType.DECIMAL, FieldType.PERCENT ->
            raw.toString().toDoubleOrNull()?.let { "%.2f".format(it) } ?: raw.toString()
        FieldType.DATETIME -> runCatching {
            val odt = java.time.OffsetDateTime.parse(raw.toString())
            odt.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", java.util.Locale.ENGLISH))
        }.getOrElse { raw.toString() }
        FieldType.DATE -> runCatching {
            val ld = java.time.LocalDate.parse(raw.toString())
            ld.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH))
        }.getOrElse { raw.toString() }
        else -> raw.toString()
    }
}