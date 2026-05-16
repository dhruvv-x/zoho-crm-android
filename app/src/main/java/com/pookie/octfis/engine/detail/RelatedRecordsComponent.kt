// engine/detail/RelatedRecordsComponent.kt
package com.pookie.octfis.engine.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.pookie.octfis.data.repository.RawRecord
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.ui.theme.CrmPrimary
import com.pookie.octfis.ui.theme.CrmSubtext
import kotlinx.coroutines.launch

// ── State ─────────────────────────────────────────────────────────────────────

private sealed class RelatedState {
    object Idle    : RelatedState()
    object Loading : RelatedState()
    data class Success(val records: List<RawRecord>) : RelatedState()
    data class Error(val message: String)            : RelatedState()
}

// ── Public config — which related modules to show per parent ──────────────────

/**
 * Defines one related-records section to show on a detail screen.
 *
 * @param relatedModule  Zoho API module name, e.g. "Contacts", "Deals"
 * @param label          Display label for the section header, e.g. "Contacts"
 * @param primaryField   Field key used as the row title, e.g. "Full_Name"
 * @param secondaryField Optional field key used as the row subtitle
 */
data class RelatedModuleConfig(
    val relatedModule : String,
    val label         : String,
    val primaryField  : String  = "Name",
    val secondaryField: String? = null,
)

/**
 * Returns which related modules to show for a given parent module.
 * Add more entries here as the app grows — no other code changes needed.
 */
fun relatedModulesFor(parentModule: String): List<RelatedModuleConfig> =
    when (parentModule) {
        "Accounts" -> listOf(
            RelatedModuleConfig("Contacts", "Contacts", "Full_Name", "Email"),
            RelatedModuleConfig("Deals",    "Deals",    "Deal_Name", "Stage"),
        )
        "Contacts" -> listOf(
            RelatedModuleConfig("Deals", "Deals", "Deal_Name", "Stage"),
        )
        "Deals" -> listOf(
            RelatedModuleConfig("Contacts", "Contacts", "Full_Name", "Email"),
        )
        else -> emptyList()
    }

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * Renders all related-record sections for [parentModule]/[parentId].
 * Each section is collapsible and lazy-loads on first expand.
 *
 * Drop this inside a Column in RecordDetailScreen — it handles its own
 * coroutine scope and state.
 */
@Composable
fun RelatedRecordsSection(
    parentModule : String,
    parentId     : String,
    repository   : ZohoRecordRepository,
    onRecordClick: (moduleName: String, recordId: String) -> Unit = { _, _ -> },
    modifier     : Modifier = Modifier,
) {
    val configs = remember(parentModule) { relatedModulesFor(parentModule) }
    if (configs.isEmpty()) return

    Column(modifier = modifier) {
        configs.forEach { config ->
            RelatedModuleCard(
                parentModule  = parentModule,
                parentId      = parentId,
                config        = config,
                repository    = repository,
                onRecordClick = onRecordClick,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Single collapsible card ───────────────────────────────────────────────────

@Composable
private fun RelatedModuleCard(
    parentModule : String,
    parentId     : String,
    config       : RelatedModuleConfig,
    repository   : ZohoRecordRepository,
    onRecordClick: (moduleName: String, recordId: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var state    by remember { mutableStateOf<RelatedState>(RelatedState.Idle) }
    val scope    = rememberCoroutineScope()

    // Load once when first expanded
    fun load() {
        scope.launch {
            state = RelatedState.Loading
            state = repository.listRelatedRecords(
                parentModule  = parentModule,
                parentId      = parentId,
                relatedModule = config.relatedModule,
                perPage       = 10,
            ).fold(
                onSuccess = { RelatedState.Success(it) },
                onFailure = { RelatedState.Error(it.message ?: "Failed to load") },
            )
        }
    }

    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape          = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
    ) {
        Column {

            // ── Section header row ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        if (expanded && state is RelatedState.Idle) load()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = config.label,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = CrmPrimary,
                    )
                    // Record count badge once loaded
                    if (state is RelatedState.Success) {
                        val count = (state as RelatedState.Success).records.size
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = CrmPrimary,
                        ) {
                            Text(
                                text     = count.toString(),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Icon(
                    imageVector        = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint               = CrmSubtext,
                )
            }

            // ── Expandable body ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp)

                    when (val s = state) {

                        is RelatedState.Idle, RelatedState.Loading -> {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color    = CrmPrimary,
                                )
                            }
                        }

                        is RelatedState.Error -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text  = s.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                TextButton(onClick = { load() }) { Text("Retry") }
                            }
                        }

                        is RelatedState.Success -> {
                            if (s.records.isEmpty()) {
                                Text(
                                    text     = "No ${config.label} found",
                                    style    = MaterialTheme.typography.bodySmall,
                                    color    = CrmSubtext,
                                    modifier = Modifier.padding(16.dp),
                                )
                            } else {
                                s.records.forEachIndexed { index, record ->
                                    RelatedRecordRow(
                                        record        = record,
                                        config        = config,
                                        isLast        = index == s.records.lastIndex,
                                        onClick       = {
                                            onRecordClick(config.relatedModule, record.id)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Single related record row ─────────────────────────────────────────────────

@Composable
private fun RelatedRecordRow(
    record : RawRecord,
    config : RelatedModuleConfig,
    isLast : Boolean,
    onClick: () -> Unit,
) {
    val primary = resolveDisplay(record, config.primaryField)
    val secondary = config.secondaryField?.let { resolveDisplay(record, it) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar initials circle
            val initials = primary.trim().split(" ").take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                .joinToString("").ifEmpty { "?" }

            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        Modifier.then(
                            androidx.compose.ui.Modifier
                                .clip(CircleShape)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = CrmPrimary.copy(alpha = 0.15f),
                    shape    = CircleShape,
                ) {}
                Text(
                    text       = initials,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CrmPrimary,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = primary.ifBlank { "—" },
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!secondary.isNullOrBlank()) {
                    Text(
                        text     = secondary,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = CrmSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(
                modifier  = Modifier.padding(start = 64.dp),
                thickness = 0.5.dp,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Resolves a field value from a raw record map.
 * Handles nested lookup objects { "name": "…" } and plain strings.
 */
private fun resolveDisplay(record: RawRecord, fieldKey: String): String {
    val raw = record.fields[fieldKey] ?: return ""
    @Suppress("UNCHECKED_CAST")
    return (raw as? Map<String, Any?>)?.get("name")?.toString()
        ?: raw.toString()
}