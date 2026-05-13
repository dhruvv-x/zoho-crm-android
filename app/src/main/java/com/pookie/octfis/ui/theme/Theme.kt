package com.pookie.octfis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary            = CrmPrimary,
    onPrimary          = CrmOnPrimary,
    primaryContainer   = CrmPrimaryLight,
    onPrimaryContainer = CrmOnSurface,
    secondary          = CrmAccent,
    onSecondary        = CrmOnPrimary,
    background         = CrmBackground,
    onBackground       = CrmOnSurface,
    surface            = CrmSurface,
    onSurface          = CrmOnSurface,
    surfaceVariant     = CrmSurfaceAlt,
    onSurfaceVariant   = CrmSubtext,
    outline            = CrmDivider,
    error              = CrmError,
    onError            = CrmOnPrimary,
)

@Composable
fun OctfisCRMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content,
    )
}