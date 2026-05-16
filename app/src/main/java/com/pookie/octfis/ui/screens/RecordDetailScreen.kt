// ui/screens/RecordDetailScreen.kt
package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.detail.DetailUiState
import com.pookie.octfis.engine.detail.RecordDetailSkeleton
import com.pookie.octfis.engine.detail.RecordDetailViewModel
import com.pookie.octfis.engine.detail.RelatedRecordsSection
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    navController   : NavController,
    moduleName      : String,
    recordId        : String,
    viewModelFactory: RecordDetailViewModel.Factory,
    repository      : ZohoRecordRepository,
) {
    val viewModel = remember(moduleName, recordId) {
        viewModelFactory.create(moduleName, recordId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text     = moduleName,
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
                },
            )
        },
        floatingActionButton = {
            if (uiState is DetailUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate(
                            Screen.ModuleEdit.createRoute(moduleName, recordId)
                        )
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Edit") },
                )
            }
        },
    ) { padding ->

        when (val state = uiState) {

            is DetailUiState.Loading -> {
                RecordDetailSkeleton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            is DetailUiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Default.Warning,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }
                }
            }

            is DetailUiState.Success -> {
                DetailContent(
                    moduleName    = moduleName,
                    recordId      = recordId,
                    fields        = state.fields,
                    values        = state.values,
                    repository    = repository,
                    navController = navController,
                    modifier      = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
    navController : NavController,
    modifier      : Modifier = Modifier,
) {
    val avatarText = run {
        val nameField = fields.firstOrNull { f ->
            f.apiName == "Name" || f.apiName == "Subject" ||
                    f.apiName == "Full_Name" || f.apiName.endsWith("_Name")
        }
        val raw = nameField?.let { values[it.apiName]?.toString() } ?: moduleName
        raw.trim().split(" ").take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .joinToString("").ifEmpty { "?" }
    }
    val titleText = run {
        val nameField = fields.firstOrNull { f ->
            f.apiName == "Name" || f.apiName == "Subject" ||
                    f.apiName == "Full_Name" || f.apiName.endsWith("_Name")
        }
        nameField?.let { values[it.apiName]?.toString() } ?: "Record"
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {

        // ── Avatar header ─────────────────────────────────────────────────────
        Surface(
            modifier       = Modifier.fillMaxWidth(),
            color          = CrmPrimary,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier         = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = avatarText,
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = titleText,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text  = moduleName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Field sections ────────────────────────────────────────────────────
        val sections = fields
            .filter { it.apiName != "id" }
            .groupBy { it.sectionName }

        sections.forEach { (sectionName, sectionFields) ->
            Text(
                text     = sectionName,
                style    = MaterialTheme.typography.labelMedium,
                color    = CrmPrimary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
            )

            Surface(
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape          = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
            ) {
                Column {
                    sectionFields.forEachIndexed { index, field ->
                        val display = formatFieldValue(field, values[field.apiName])
                        if (display.isBlank() && !field.required) return@forEachIndexed
                        DetailFieldRow(
                            label  = field.label,
                            value  = display,
                            type   = field.type,
                            isLast = index == sectionFields.lastIndex,
                        )
                    }
                }
            }
        }

        // ── Related records ───────────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))

        Text(
            text     = "Related",
            style    = MaterialTheme.typography.labelMedium,
            color    = CrmPrimary,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        RelatedRecordsSection(
            parentModule  = moduleName,
            parentId      = recordId,
            repository    = repository,
            onRecordClick = { relatedModule, relatedId ->
                navController.navigate(
                    Screen.ModuleDetail.createRoute(relatedModule, relatedId)
                )
            },
        )

        Spacer(Modifier.height(88.dp))
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(120.dp),
            )
            Spacer(Modifier.width(8.dp))
            val valueColor = when (type) {
                FieldType.EMAIL, FieldType.PHONE, FieldType.URL -> CrmPrimary
                else -> MaterialTheme.colorScheme.onSurface
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
                modifier  = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

// ── Value formatter ───────────────────────────────────────────────────────────

private fun formatFieldValue(field: FieldMetadata, raw: Any?): String {
    if (raw == null) return ""
    // Hide raw map objects that aren't lookup/owner (e.g. Layout field)
    if (raw is Map<*, *> &&
        field.type != FieldType.LOOKUP &&
        field.type != FieldType.OWNER) return ""
    // Hide empty collections (e.g. Tag = [])
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
        else -> raw.toString()
    }
}