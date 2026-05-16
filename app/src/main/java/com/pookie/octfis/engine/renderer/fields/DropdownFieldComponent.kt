package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pookie.octfis.engine.metadata.FieldMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFieldComponent(
    field: FieldMetadata,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = field.pickListValues
        .firstOrNull { it.actualValue == value }
        ?.displayValue
        ?: value

    ExposedDropdownMenuBox(
        expanded       = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier       = modifier,
    ) {
        OutlinedTextField(
            value         = selectedLabel,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(field.label + if (field.required) " *" else "") },
            trailingIcon  = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
            isError        = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier       = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )

        ExposedDropdownMenu(
            expanded        = expanded,
            onDismissRequest = { expanded = false },
        ) {
            // Blank "none selected" option
            DropdownMenuItem(
                text    = { Text("— Select —", style = MaterialTheme.typography.bodyMedium) },
                onClick = {
                    onValueChange("")
                    expanded = false
                },
            )
            field.pickListValues.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(option.displayValue) },
                    onClick = {
                        onValueChange(option.actualValue)
                        expanded = false
                    },
                )
            }
        }
    }
}