// engine/renderer/fields/LookupFieldComponent.kt
package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

    val displayName = if (value.contains("::")) value.substringAfter("::") else value
    val hasValue    = displayName.isNotBlank()

    var isExpanded  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var results     by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, isExpanded) {
        if (isExpanded && searchQuery.length >= 2) {
            delay(400)
            isSearching = true
            results     = onSearch(field.lookupModule, searchQuery)
            isSearching = false
        } else {
            results = emptyList()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value         = if (isExpanded) searchQuery else displayName,
            onValueChange = { if (isExpanded) searchQuery = it },
            readOnly      = !isExpanded,
            label         = { Text(field.label + if (field.required) " *" else "") },
            placeholder   = if (isExpanded) ({ Text("Type to search…") }) else null,
            trailingIcon  = {
                // Show ✕ when value is selected (and not searching), else show 🔍
                if (hasValue && !isExpanded) {
                    IconButton(onClick = {
                        onValueChange("")
                        searchQuery = ""
                        results     = emptyList()
                    }) {
                        Icon(
                            imageVector        = Icons.Default.Clear,
                            contentDescription = "Clear ${field.label}",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    IconButton(onClick = {
                        isExpanded  = !isExpanded
                        searchQuery = ""
                        results     = emptyList()
                    }) {
                        Icon(
                            imageVector        = Icons.Default.Search,
                            contentDescription = "Search ${field.label}",
                            tint = if (isExpanded)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            isError        = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine     = true,
            modifier       = Modifier.fillMaxWidth(),
        )

        if (isExpanded) {
            Surface(
                tonalElevation  = 3.dp,
                shadowElevation = 2.dp,
                shape           = MaterialTheme.shapes.medium,
                modifier        = Modifier.fillMaxWidth(),
            ) {
                Column {
                    if (isSearching) {
                        Box(
                            modifier         = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }

                    if (!isSearching && searchQuery.length >= 2 && results.isEmpty()) {
                        Text(
                            text  = "No results found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        results.forEach { (id, name) ->
                            ListItem(
                                headlineContent = { Text(name) },
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange("$id::$name")
                                        isExpanded  = false
                                        searchQuery = ""
                                        results     = emptyList()
                                    },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}