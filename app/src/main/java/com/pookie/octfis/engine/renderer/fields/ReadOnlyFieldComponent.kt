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
import com.pookie.octfis.engine.metadata.FieldType
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders an immutable system field (Created Time, Modified Time, Created By, etc.)
 * as a styled read-only display — no interaction possible.
 */
@Composable
fun ReadOnlyFieldComponent(
    field    : FieldMetadata,
    value    : String,
    modifier : Modifier = Modifier,
) {
    val displayValue = formatForDisplay(value, field.type)

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

/**
 * Converts raw Zoho values into human-readable strings.
 *
 * - DATETIME  "2024-10-16T15:45:00+05:30"  →  "16 Oct 2024, 3:45 PM"
 * - DATE      "2024-10-16"                  →  "16 Oct 2024"
 * - LOOKUP/OWNER  "447559::User1"           →  "User1"
 * - Everything else                         →  as-is, or "—" if blank
 */
private fun formatForDisplay(raw: String, type: FieldType): String {
    if (raw.isBlank()) return "—"

    return when (type) {
        FieldType.DATETIME -> runCatching {
            val odt = OffsetDateTime.parse(raw)
            odt.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH))
        }.getOrElse { raw }

        FieldType.DATE -> runCatching {
            val ld = LocalDate.parse(raw)
            ld.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
        }.getOrElse { raw }

        FieldType.LOOKUP,
        FieldType.OWNER -> raw.substringAfterLast("::").ifBlank { raw }

        else -> raw
    }
}