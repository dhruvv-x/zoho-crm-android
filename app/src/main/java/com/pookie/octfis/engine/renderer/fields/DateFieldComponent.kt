package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.pookie.octfis.engine.metadata.FieldMetadata

@Composable
fun DateFieldComponent(
    field: FieldMetadata,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = { input ->
            // Auto-insert dashes: 2026 → 2026- → 2026-05- → 2026-05-16
            val digits = input.filter { it.isDigit() }.take(8)
            val formatted = buildString {
                digits.forEachIndexed { i, c ->
                    if (i == 4 || i == 6) append('-')
                    append(c)
                }
            }
            onValueChange(formatted)
        },
        label         = { Text(field.label + if (field.required) " *" else "") },
        placeholder   = { Text("YYYY-MM-DD") },
        trailingIcon  = { Icon(Icons.Default.CalendarToday, contentDescription = "Date") },
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction    = ImeAction.Next,
        ),
        modifier = modifier,
    )
}