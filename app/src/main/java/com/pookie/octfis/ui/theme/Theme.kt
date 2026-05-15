package com.pookie.octfis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColors = darkColorScheme(
    primary            = CrmPrimaryLight,    // Lighter indigo — readable on dark bg
    onPrimary          = CrmOnSurface,       // Dark text on light primary button
    primaryContainer   = CrmPrimaryDark,
    onPrimaryContainer = CrmOnSurfaceDark,
    secondary          = CrmAccent,
    onSecondary        = CrmOnPrimary,
    background         = CrmBackgroundDark,
    onBackground       = CrmOnSurfaceDark,
    surface            = CrmSurfaceDark,
    onSurface          = CrmOnSurfaceDark,
    surfaceVariant     = CrmSurfaceAltDark,
    onSurfaceVariant   = CrmSubtextDark,
    outline            = CrmDividerDark,
    error              = CrmError,
    onError            = CrmOnPrimary,
)

@Composable
fun OctfisCRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // auto-follows device setting
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content,
    )
}