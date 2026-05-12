package com.pookie.octfis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary        = CrmPrimary,
    onPrimary      = CrmOnPrimary,
    background     = CrmBackground,
    surface        = CrmSurface,
    onSurface      = CrmOnSurface,
    surfaceVariant = CrmRowAlt,
)

@Composable
fun OctfisCRMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content     = content,
    )
}