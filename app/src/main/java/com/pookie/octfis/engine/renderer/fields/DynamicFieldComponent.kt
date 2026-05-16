package com.pookie.octfis.engine.renderer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType
import com.pookie.octfis.engine.renderer.fields.BooleanFieldComponent
import com.pookie.octfis.engine.renderer.fields.DateFieldComponent
import com.pookie.octfis.engine.renderer.fields.DropdownFieldComponent
import com.pookie.octfis.engine.renderer.fields.NumberFieldComponent
import com.pookie.octfis.engine.renderer.fields.TextFieldComponent

@Composable
fun DynamicFieldComponent(
    field: FieldMetadata,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    when (field.type) {

        // ── Text family ───────────────────────────────────────────────────
        FieldType.TEXT,
        FieldType.EMAIL,
        FieldType.PHONE,
        FieldType.URL,
        FieldType.TEXTAREA,
        FieldType.RICH_TEXT -> TextFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Numeric family ────────────────────────────────────────────────
        FieldType.INTEGER,
        FieldType.DECIMAL,
        FieldType.CURRENCY,
        FieldType.PERCENT -> NumberFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Selection ─────────────────────────────────────────────────────
        FieldType.PICKLIST,
        FieldType.MULTI_SELECT -> DropdownFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Boolean / toggle ──────────────────────────────────────────────
        FieldType.BOOLEAN -> BooleanFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Date / DateTime ───────────────────────────────────────────────
        FieldType.DATE,
        FieldType.DATETIME -> DateFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Lookup / Owner — read-only display for now ────────────────────
        FieldType.LOOKUP,
        FieldType.OWNER -> OutlinedTextField(
            value         = value,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(field.label) },
            supportingText = { Text("Lookup — coming soon", style = MaterialTheme.typography.bodySmall) },
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Formula / Unknown — skip silently ─────────────────────────────
        FieldType.FORMULA,
        FieldType.UNKNOWN -> Unit
    }
}