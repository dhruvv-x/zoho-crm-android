package com.pookie.octfis.engine.renderer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType
import com.pookie.octfis.engine.renderer.fields.BooleanFieldComponent
import com.pookie.octfis.engine.renderer.fields.DateFieldComponent
import com.pookie.octfis.engine.renderer.fields.DropdownFieldComponent
import com.pookie.octfis.engine.renderer.fields.LookupFieldComponent
import com.pookie.octfis.engine.renderer.fields.MultiSelectFieldComponent
import com.pookie.octfis.engine.renderer.fields.NumberFieldComponent
import com.pookie.octfis.engine.renderer.fields.ReadOnlyFieldComponent
import com.pookie.octfis.engine.renderer.fields.TextFieldComponent

@Composable
fun DynamicFieldComponent(
    field          : FieldMetadata,
    value          : String,
    onValueChange  : (String) -> Unit,
    error          : String?,
    modifier       : Modifier = Modifier,
    onLookupSearch : (suspend (module: String, query: String)
    -> List<Pair<String, String>>)? = null,
) {
    // ── Immutable system fields — render as locked display, never editable ──
    if (field.readOnly) {
        ReadOnlyFieldComponent(field = field, value = value, modifier = modifier)
        return
    }

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

        // ── Single select ─────────────────────────────────────────────────
        FieldType.PICKLIST -> DropdownFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Multi select — chip based ─────────────────────────────────────  ← CHANGED
        FieldType.MULTI_SELECT -> MultiSelectFieldComponent(
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

        // ── Lookup / Owner — search picker ────────────────────────────────  ← CHANGED
        FieldType.LOOKUP,
        FieldType.OWNER -> LookupFieldComponent(
            field         = field,
            value         = value,
            onValueChange = onValueChange,
            error         = error,
            onSearch      = onLookupSearch ?: { _, _ -> emptyList() },
            modifier      = modifier.fillMaxWidth(),
        )

        // ── Formula / Unknown — skip silently ─────────────────────────────
        FieldType.FORMULA,
        FieldType.UNKNOWN -> Unit
    }
}