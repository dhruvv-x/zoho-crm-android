package com.pookie.octfis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.LocalIndication

// ── Light colour scheme ───────────────────────────────────────────────────────
// Keeps cards pure white — no Material3 tonal surface tinting (like Zoho site)
private val LightColors = lightColorScheme(
    primary              = CrmAccent,
    onPrimary            = CrmOnAccent,
    primaryContainer     = CrmAccentContainer,
    onPrimaryContainer   = CrmAccentDark,

    secondary            = CrmNavActive,
    onSecondary          = Color.White,
    secondaryContainer   = CrmAccentLight,
    onSecondaryContainer = CrmAccentDark,

    tertiary             = CrmSuccess,
    onTertiary           = CrmOnSuccess,
    tertiaryContainer    = CrmSuccessContainer,
    onTertiaryContainer  = CrmOnSuccessContainer,

    background           = CrmBackground,
    onBackground         = CrmOnSurface,

    surface              = CrmSurface,
    onSurface            = CrmOnSurface,
    surfaceVariant       = CrmSurfaceAlt,
    onSurfaceVariant     = CrmSubtext,
    surfaceTint          = Color.Transparent,   // disables M3 blue card tinting

    outline              = CrmDivider,
    outlineVariant       = Color(0xFFEEF1F8),

    error                = CrmError,
    onError              = CrmOnError,
    errorContainer       = CrmErrorContainer,
    onErrorContainer     = CrmOnErrorContainer,

    scrim                = Color(0x991A1F36),    // 60% navy — modal overlays
    inverseSurface       = Color(0xFF1A1F36),
    inverseOnSurface     = Color(0xFFEEF1F8),
    inversePrimary       = CrmAccentLight,
)

// ── Dark colour scheme ────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary              = Color(0xFF8CA4F5),
    onPrimary            = Color(0xFF1A2D8A),
    primaryContainer     = Color(0xFF2C42B8),
    onPrimaryContainer   = CrmAccentLight,

    secondary            = Color(0xFF6B85F0),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF1E2D8A),
    onSecondaryContainer = CrmOnSurfaceDark,

    tertiary             = Color(0xFF2DB87A),
    onTertiary           = Color(0xFF003820),
    tertiaryContainer    = Color(0xFF00502E),
    onTertiaryContainer  = Color(0xFFB7F5D8),

    background           = CrmBackgroundDark,
    onBackground         = CrmOnSurfaceDark,

    surface              = CrmSurfaceDark,
    onSurface            = CrmOnSurfaceDark,
    surfaceVariant       = CrmSurfaceAltDark,
    onSurfaceVariant     = CrmSubtextDark,
    surfaceTint          = Color.Transparent,

    outline              = CrmDividerDark,
    outlineVariant       = Color(0xFF1E2338),

    error                = CrmErrorDark,
    onError              = CrmOnError,
    errorContainer       = Color(0xFF7A1500),
    onErrorContainer     = Color(0xFFFFDAD4),

    scrim                = Color(0x99000000),
    inverseSurface       = Color(0xFFDDE3F0),
    inverseOnSurface     = Color(0xFF1A1F36),
    inversePrimary       = CrmAccent,
)

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun OctfisCRMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit,
) {
    // Indigo ripple — matches Zoho's subtle indigo touch feedback on rows/buttons
    // Uses CrmAccent at 10% alpha in light, 14% alpha in dark
    val crmRipple = ripple(
        color   = if (darkTheme) Color(0xFF8CA4F5) else CrmAccent,
        radius  = androidx.compose.ui.unit.Dp.Unspecified,  // fills the component naturally
        bounded = true,
    )

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        shapes      = CrmShapes,          // ← Zoho-tuned shape system
        content     = {
            // Override default black ripple → indigo ripple globally
            CompositionLocalProvider(
                LocalIndication provides crmRipple,
            ) {
                content()
            }
        },
    )
}