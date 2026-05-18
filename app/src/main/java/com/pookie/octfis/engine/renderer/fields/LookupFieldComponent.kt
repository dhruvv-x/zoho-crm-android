// engine/renderer/fields/LookupFieldComponent.kt
package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pookie.octfis.engine.metadata.FieldMetadata
import kotlinx.coroutines.delay

@Composable
fun LookupFieldComponent(
    field        : FieldMetadata,
    value        : String,   // stored as "id::name"
    onValueChange: (String) -> Unit,
    error        : String?,
    onSearch     : suspend (module: String, query: String) -> List<Pair<String, String>>,
    modifier     : Modifier = Modifier,
) {
    // If no lookupModule, render plain read-only field — nothing to search
    if (field.lookupModule == null) {
        OutlinedTextField(
            value         = value,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(field.label) },
            modifier      = modifier.fillMaxWidth(),
        )
        return
    }

    // Display only the "name" part after "::"
    val displayName = if (value.contains("::")) value.substringAfter("::") else value

    var showDialog  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var results     by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // ── Read-only display field with search icon ───────────────────────────────
    OutlinedTextField(
        value         = displayName,
        onValueChange = {},
        readOnly      = true,
        label         = { Text(field.label + if (field.required) " *" else "") },
        trailingIcon  = {
            IconButton(onClick = {
                searchQuery = ""
                results     = emptyList()
                showDialog  = true
            }) {
                Icon(
                    imageVector        = Icons.Default.Search,
                    contentDescription = "Search ${field.label}",
                )
            }
        },
        isError        = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier       = modifier.fillMaxWidth(),
    )

    // ── Picker dialog ─────────────────────────────────────────────────────────
    if (showDialog) {

        // 400ms debounce — fires search when query >= 2 chars
        LaunchedEffect(searchQuery) {
            if (searchQuery.length >= 2) {
                delay(400)
                isSearching = true
                results     = onSearch(field.lookupModule, searchQuery)
                isSearching = false
            } else {
                results = emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title            = { Text("Search ${field.label}") },
            text             = {
                Column {
                    // Search input
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { searchQuery = it },
                        label         = { Text("Type to search…") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                    )

                    // Loading indicator
                    if (isSearching) {
                        Box(
                            modifier         = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }

                    // Results list — clickable wraps ListItem directly
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(results) { (id, name) ->
                            ListItem(
                                headlineContent = { Text(name) },
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .clickable {           // ← FIXED: clickable on ListItem
                                        onValueChange("$id::$name")
                                        showDialog = false
                                    },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton  = {},
            dismissButton  = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}