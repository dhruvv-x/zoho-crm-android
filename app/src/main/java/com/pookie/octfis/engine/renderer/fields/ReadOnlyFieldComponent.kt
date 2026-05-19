package com.pookie.octfis.engine.renderer.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pookie.octfis.engine.metadata.FieldMetadata

/**
 * Renders an immutable system field (Created Time, Modified Time, Created By, etc.)
 * as a styled read-only display — no interaction possible.
 *
 * Shown only in edit mode; hidden in create mode (see RecordFormViewModel).
 */
@Composable
fun ReadOnlyFieldComponent(
    field    : FieldMetadata,
    value    : String,
    modifier : Modifier = Modifier,
) {
    val displayValue = value
        .substringAfterLast("::")   // "4475594000000267001::User1" → "User1"
        .ifBlank { "—" }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape    = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text  = field.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
        }
    }
}