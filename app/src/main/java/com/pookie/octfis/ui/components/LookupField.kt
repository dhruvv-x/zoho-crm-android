package com.pookie.octfis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pookie.octfis.ui.theme.CrmOnSurface
import com.pookie.octfis.ui.theme.CrmPrimary
import com.pookie.octfis.ui.theme.CrmSubtext

// ── Data model ────────────────────────────────────────────────────────────────

/** A single selectable item in a lookup sheet. */
data class LookupItem(
    val zohoId: String,
    val name: String,
    val subtitle: String = "",   // e.g. phone, email, account – shown in grey below name
)

// ── Row in the form ───────────────────────────────────────────────────────────

/**
 * Renders a single form row that opens a bottom-sheet style lookup dialog
 * when tapped.  Matches the visual style of ECTextField / EDTextField / EQFormField.
 *
 * @param label       Left-side label (e.g. "Account Name")
 * @param value       Currently selected display name (empty = placeholder shown)
 * @param placeholder Hint text when nothing is selected
 * @param items       Full list of searchable items – pass from ViewModel state
 * @param loading     Show a spinner instead of the chevron while items load
 * @param onSelect    Called with the chosen LookupItem (name + zohoId)
 */
@Composable
fun LookupField(
    label: String,
    value: String,
    placeholder: String,
    items: List<LookupItem>,
    loading: Boolean = false,
    onSelect: (LookupItem) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !loading) { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp),
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = value.ifEmpty { placeholder },
                fontSize = 13.sp,
                color = if (value.isEmpty()) CrmSubtext.copy(alpha = 0.7f) else CrmOnSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (showDialog) {
        LookupDialog(
            title = "Select $label",
            items = items,
            onSelect = { item -> onSelect(item); showDialog = false },
            onDismiss = { showDialog = false },
        )
    }
}

// ── Search dialog ─────────────────────────────────────────────────────────────

@Composable
private fun LookupDialog(
    title: String,
    items: List<LookupItem>,
    onSelect: (LookupItem) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.72f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column {
                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                // ── Search bar ────────────────────────────────────────────────
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search…", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrmPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                LaunchedEffect(Unit) { focusRequester.requestFocus() }

                // ── Results ───────────────────────────────────────────────────
                if (filtered.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No results found",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filtered) { item ->
                            LookupRow(item = item, onClick = { onSelect(item) })
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LookupRow(item: LookupItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = item.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (item.subtitle.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}