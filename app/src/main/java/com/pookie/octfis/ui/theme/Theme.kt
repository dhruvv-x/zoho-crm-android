package com.pookie.octfis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary              = CrmAccent,
    onPrimary            = CrmOnAccent,
    primaryContainer     = CrmAccentContainer,
    onPrimaryContainer   = CrmAccentDark,
    secondary            = CrmNavActive,
    onSecondary          = Color.White,
    secondaryContainer   = CrmAccentLight,
    onSecondaryContainer = CrmAccentDark,
    background           = CrmBackground,
    onBackground         = CrmOnSurface,
    surface              = CrmSurface,
    onSurface            = CrmOnSurface,
    surfaceVariant       = CrmSurfaceAlt,
    onSurfaceVariant     = CrmSubtext,
    outline              = CrmDivider,
    error                = CrmError,
    onError              = CrmOnError,
    errorContainer       = Color(0xFFFFECE8),
    onErrorContainer     = Color(0xFF5C1400),
)

private val DarkColors = darkColorScheme(
    primary              = CrmAccentLight,
    onPrimary            = Color(0xFF1A2D8A),
    primaryContainer     = Color(0xFF2F44C0),
    onPrimaryContainer   = CrmAccentLight,
    secondary            = CrmNavActive,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF1B2A7A),
    onSecondaryContainer = CrmOnSurfaceDark,
    background           = CrmBackgroundDark,
    onBackground         = CrmOnSurfaceDark,
    surface              = CrmSurfaceDark,
    onSurface            = CrmOnSurfaceDark,
    surfaceVariant       = CrmSurfaceAltDark,
    onSurfaceVariant     = CrmSubtextDark,
    outline              = CrmDividerDark,
    error                = CrmErrorDark,
    onError              = CrmOnError,
    errorContainer       = Color(0xFF7A1500),
    onErrorContainer     = Color(0xFFFFDAD4),
)

@Composable
fun OctfisCRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content,
    )
}