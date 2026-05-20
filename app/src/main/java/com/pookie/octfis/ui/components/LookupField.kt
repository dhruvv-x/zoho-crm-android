package com.pookie.octfis.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pookie.octfis.ui.theme.CrmOnSurface
import com.pookie.octfis.ui.theme.CrmPrimary
import com.pookie.octfis.ui.theme.CrmSubtext

data class LookupItem(
    val zohoId: String,
    val name: String,
    val subtitle: String = "",
)

@Composable
fun LookupField(
    label: String,
    value: String,
    placeholder: String,
    items: List<LookupItem>,
    loading: Boolean = false,
    onSelect: (LookupItem) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Trigger row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !loading) {
                    expanded = !expanded
                    if (!expanded) query = ""
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = label,
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(130.dp),
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text     = value.ifEmpty { placeholder },
                    fontSize = 13.sp,
                    color    = if (value.isEmpty()) CrmSubtext.copy(alpha = 0.7f) else CrmOnSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector        = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp),
                )
            }
        }

        // Inline dropdown panel
        AnimatedVisibility(
            visible = expanded && !loading,
            enter   = expandVertically(),
            exit    = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                // Search bar
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Search…", fontSize = 13.sp) },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    },
                    singleLine    = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    shape         = RoundedCornerShape(8.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = CrmPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                // Results list — capped at ~3 visible rows, scrollable
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No results found", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 210.dp)) {
                        items(filtered) { item ->
                            LookupRow(item = item) {
                                onSelect(item)
                                expanded = false
                                query = ""
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outline,
                                modifier  = Modifier.padding(horizontal = 12.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        if (item.subtitle.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(text = item.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}