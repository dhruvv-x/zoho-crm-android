package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType

@Composable
fun NumberFieldComponent(
    field: FieldMetadata,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    val prefix: String? = if (field.type == FieldType.CURRENCY) "₹" else null
    val suffix: String? = if (field.type == FieldType.PERCENT) "%" else null

    OutlinedTextField(
        value         = value,
        onValueChange = { input ->
            // Allow digits, one dot, optional leading minus
            val filtered = input.filter { it.isDigit() || it == '.' || it == '-' }
            onValueChange(filtered)
        },
        label         = { Text(field.label + if (field.required) " *" else "") },
        prefix        = prefix?.let { { Text(it) } },
        suffix        = suffix?.let { { Text(it) } },
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (field.type == FieldType.INTEGER) KeyboardType.Number else KeyboardType.Decimal,
            imeAction    = ImeAction.Next,
        ),
        modifier = modifier,
    )
}