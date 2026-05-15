package com.pookie.octfis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light — white surface, amber as the single brand/action colour ─────────────
// TopAppBar uses surface (white) by default in M3.
// The Dashboard's custom Row header uses CrmSlate directly (see DashboardScreen).
private val LightColors = lightColorScheme(
    primary              = CrmAccent,            // amber — FAB, buttons, active icons/chips
    onPrimary            = CrmOnAccent,          // near-black on amber
    primaryContainer     = CrmAccentContainer,   // very light amber tint
    onPrimaryContainer   = CrmOnSurface,

    secondary            = CrmAccent,
    onSecondary          = CrmOnAccent,
    secondaryContainer   = CrmAccentContainer,
    onSecondaryContainer = CrmOnSurface,

    background           = CrmBackground,        // off-white page
    onBackground         = CrmOnSurface,
    surface              = CrmSurface,           // white — cards & TopAppBar
    onSurface            = CrmOnSurface,
    surfaceVariant       = CrmSurfaceAlt,        // grey alt rows / input fills
    onSurfaceVariant     = CrmSubtext,

    outline              = CrmDivider,
    error                = CrmError,
    onError              = CrmOnError,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
)

// ── Dark — slate top bar, amber accent ────────────────────────────────────────
// primary = slate so the Dashboard Row header stays dark-grey in dark mode.
// secondary = amber so buttons/FAB/chips stay amber.
private val DarkColors = darkColorScheme(
    primary              = CrmSlate,             // slate — dark top bar background
    onPrimary            = Color(0xFFFFFFFF),    // white on slate
    primaryContainer     = CrmTableHeaderDark,   // neutral dark container
    onPrimaryContainer   = CrmOnSurfaceDark,

    secondary            = CrmAccentLight,       // light amber — buttons/FAB/chips
    onSecondary          = CrmOnAccent,
    secondaryContainer   = Color(0xFF3A2800),
    onSecondaryContainer = CrmOnSurfaceDark,

    background           = CrmBackgroundDark,
    onBackground         = CrmOnSurfaceDark,
    surface              = CrmSurfaceDark,
    onSurface            = CrmOnSurfaceDark,
    surfaceVariant       = CrmSurfaceAltDark,
    onSurfaceVariant     = CrmSubtextDark,

    outline              = CrmDividerDark,
    error                = CrmErrorDark,         // lightened for dark bg contrast
    onError              = CrmOnError,
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
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