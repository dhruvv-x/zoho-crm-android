package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType

@Composable
fun TextFieldComponent(
    field: FieldMetadata,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    val keyboardType = when (field.type) {
        FieldType.EMAIL -> KeyboardType.Email
        FieldType.PHONE -> KeyboardType.Phone
        FieldType.URL   -> KeyboardType.Uri
        else            -> KeyboardType.Text
    }

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(field.label + if (field.required) " *" else "") },
        placeholder   = field.tooltip?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine    = field.type != FieldType.TEXTAREA,
        minLines      = if (field.type == FieldType.TEXTAREA) 3 else 1,
        maxLines      = if (field.type == FieldType.TEXTAREA) 6 else 1,
        keyboardOptions = KeyboardOptions(
            keyboardType   = keyboardType,
            capitalization = if (field.type == FieldType.TEXT) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            imeAction      = ImeAction.Next,
        ),
        modifier = modifier,
    )
}