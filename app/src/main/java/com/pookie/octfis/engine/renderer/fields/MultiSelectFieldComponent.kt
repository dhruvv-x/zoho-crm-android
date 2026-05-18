// engine/renderer/fields/MultiSelectFieldComponent.kt
package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pookie.octfis.engine.metadata.FieldMetadata

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectFieldComponent(
    field        : FieldMetadata,
    value        : String,
    onValueChange: (String) -> Unit,
    error        : String?,
    modifier     : Modifier = Modifier,
) {
    // Value is stored as semicolon-separated: "Val1;Val2;Val3"
    val selected = remember(value) {
        value.split(";").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    Column(modifier = modifier.fillMaxWidth()) {

        // ── Field label ───────────────────────────────────────────────────────
        Text(
            text  = field.label + if (field.required) " *" else "",
            style = MaterialTheme.typography.labelMedium,
            color = if (error != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── Chips ─────────────────────────────────────────────────────────────
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            field.pickListValues.forEach { pickItem ->
                val isSelected = pickItem.actualValue in selected
                FilterChip(
                    selected = isSelected,
                    onClick  = {
                        val newSet = selected.toMutableSet()
                        if (isSelected) newSet.remove(pickItem.actualValue)
                        else            newSet.add(pickItem.actualValue)
                        onValueChange(newSet.joinToString(";"))
                    },
                    label    = { Text(pickItem.displayValue) },
                    modifier = Modifier.padding(end = 6.dp, bottom = 4.dp),
                )
            }
        }

        // ── Error text ────────────────────────────────────────────────────────
        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}