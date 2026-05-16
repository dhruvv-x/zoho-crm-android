package com.pookie.octfis.engine.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pookie.octfis.engine.metadata.FieldMetadata

@Composable
fun DynamicFormRenderer(
    fields: List<FieldMetadata>,
    values: Map<String, String>,
    errors: Map<String, String>,
    onValueChange: (apiName: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
) {
    val columnModifier = if (scrollable)
        modifier.verticalScroll(rememberScrollState())
    else
        modifier

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = columnModifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Group fields by section
        val sections = fields.groupBy { it.sectionName }

        sections.forEach { (sectionName, sectionFields) ->

            // Section header
            if (sections.size > 1) {
                Text(
                    text  = sectionName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                HorizontalDivider()
            }

            // Fields in this section
            sectionFields.forEach { field ->
                DynamicFieldComponent(
                    field         = field,
                    value         = values[field.apiName] ?: field.defaultValue ?: "",
                    onValueChange = { newValue -> onValueChange(field.apiName, newValue) },
                    error         = errors[field.apiName],
                    modifier      = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}