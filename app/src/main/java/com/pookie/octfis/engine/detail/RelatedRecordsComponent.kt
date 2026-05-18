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
import com.pookie.octfis.data.remote.dto.ZohoRelatedList
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

// ── Config ────────────────────────────────────────────────────────────────────

/**
 * Defines one related-records section to show on a detail screen.
 *
 * Built from [ZohoRelatedList] returned by GET /settings/related_lists.
 *
 * @param relatedModule  Zoho API module name used for fetching related records
 * @param label          Display label for the section header
 * @param primaryField   Field key used as the row title
 * @param secondaryField Optional field key used as the row subtitle
 */
data class RelatedModuleConfig(
    val relatedModule : String,
    val label         : String,
    val primaryField  : String  = "Name",
    val secondaryField: String? = null,
)

/**
 * Converts a [ZohoRelatedList] from the settings API into a [RelatedModuleConfig].
 *
 * The `module` field on [ZohoRelatedList] is the actual API module name used for
 * record fetching (e.g. "Contacts"). `api_name` is the relationship name and may
 * differ (e.g. "Contacts_1"). We prefer `module` for fetching and `display_label`
 * for the UI label.
 *
 * Primary/secondary field heuristics are intentionally generic — they work for
 * all standard and custom Zoho modules without hardcoding module names.
 */
private fun ZohoRelatedList.toConfig(): RelatedModuleConfig? {
    // `module` is the fetchable API name; fall back to api_name if absent
    val fetchModule = module?.takeIf { it.isNotBlank() } ?: apiName.takeIf { it.isNotBlank() }
    ?: return null
    val displayLabel = displayLabel?.takeIf { it.isNotBlank() } ?: fetchModule

    // Heuristic primary/secondary fields — cover the vast majority of Zoho modules
    val (primary, secondary) = when (fetchModule) {
        "Contacts"            -> "Full_Name"  to "Email"
        "Deals", "Potentials" -> "Deal_Name"  to "Stage"
        "Leads"               -> "Full_Name"  to "Company"
        "Cases"               -> "Case_Subject" to "Status"
        "Tasks"               -> "Subject"    to "Due_Date"
        "Events"              -> "Event_Title" to "Start_DateTime"
        "Calls"               -> "Subject"    to "Call_Start_Time"
        "Quotes"              -> "Subject"    to "Quote_Stage"
        "Invoices"            -> "Subject"    to "Status"
        "SalesOrders"         -> "Subject"    to "Status"
        "PurchaseOrders"      -> "Subject"    to "Status"
        "Products"            -> "Product_Name" to "Unit_Price"
        "Vendors"             -> "Vendor_Name" to "Email"
        "Campaigns"           -> "Campaign_Name" to "Status"
        else                  -> "Name"       to null
    }
    return RelatedModuleConfig(
        relatedModule  = fetchModule,
        label          = displayLabel,
        primaryField   = primary,
        secondaryField = secondary,
    )
}

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * Renders all related-record sections for [parentModule]/[parentId].
 *
 * Accepts [relatedLists] from [RecordDetailViewModel] — these come from
 * GET /settings/related_lists and reflect what Zoho CRM has actually configured
 * for this module. Each section is collapsible and lazy-loads on first expand.
 *
 * Pass an empty list and this composable renders nothing.
 */
@Composable
fun RelatedRecordsSection(
    parentModule  : String,
    parentId      : String,
    repository    : ZohoRecordRepository,
    relatedLists  : List<ZohoRelatedList>,
    onRecordClick : (moduleName: String, recordId: String) -> Unit = { _, _ -> },
    modifier      : Modifier = Modifier,
) {
    val configs = remember(relatedLists) {
        relatedLists.mapNotNull { it.toConfig() }
    }
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
                                        record  = record,
                                        config  = config,
                                        isLast  = index == s.records.lastIndex,
                                        onClick = {
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
    val primary   = resolveDisplay(record, config.primaryField)
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
                    .clip(CircleShape),
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
                    text       = primary.ifBlank { "—" },
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
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