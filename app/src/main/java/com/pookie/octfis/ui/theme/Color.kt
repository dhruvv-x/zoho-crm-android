package com.pookie.octfis.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand Amber — buttons, FAB, active chips, icons, progress indicators ──────
val CrmAccent          = Color(0xFFF5A623)
val CrmAccentDark      = Color(0xFFE8920A)
val CrmAccentLight     = Color(0xFFFFD166)
val CrmAccentContainer = Color(0xFFFFF3DC)
val CrmOnAccent        = Color(0xFF1A0F00)

// ── Screen-level aliases (used throughout all screens — DO NOT remove) ─────────
val CrmPrimary      = CrmAccent        // amber — buttons, icons, FAB, progress
val CrmPrimaryLight = CrmAccentLight   // lighter amber variant
val CrmPrimaryDark  = CrmAccentDark    // pressed/darker amber
val CrmOnPrimary    = CrmOnAccent      // near-black on amber

// ── Slate — table headers ONLY (not top bars in light mode) ───────────────────
val CrmSlate     = Color(0xFF2C3038)
val CrmSlateDark = Color(0xFF1C1F24)

// ── Light Theme ───────────────────────────────────────────────────────────────
val CrmBackground  = Color(0xFFF6F6F6)   // off-white page bg
val CrmSurface     = Color(0xFFFFFFFF)   // pure white — cards & TopAppBar
val CrmSurfaceAlt  = Color(0xFFF0F0F0)   // subtle grey alt rows / input fills
val CrmOnSurface   = Color(0xFF1C1C1E)   // near-black body text
val CrmSubtext     = Color(0xFF6B6B6B)   // secondary / caption text
val CrmDivider     = Color(0xFFE4E4E4)   // hairline dividers

// ── Dark Theme ────────────────────────────────────────────────────────────────
val CrmBackgroundDark = Color(0xFF17191C)
val CrmSurfaceDark    = Color(0xFF1E2124)
val CrmSurfaceAltDark = Color(0xFF26292D)
val CrmOnSurfaceDark  = Color(0xFFE8E3DC)
val CrmSubtextDark    = Color(0xFF9BA3AF)
val CrmDividerDark    = Color(0xFF2E3035)

// ── Table ─────────────────────────────────────────────────────────────────────
val CrmTableHeader     = Color(0xFF2C3038)   // slate — table headers both modes
val CrmTableHeaderDark = Color(0xFF252729)   // neutral dark (fixed — was amber-tinted)
val CrmRowAlt          = Color(0xFFF5F5F5)
val CrmRowAltDark      = Color(0xFF222426)

// ── Status ────────────────────────────────────────────────────────────────────
val CrmSuccess   = Color(0xFF43A047)
val CrmOnSuccess = Color(0xFFFFFFFF)
val CrmWarning   = Color(0xFFFB8C00)
val CrmOnWarning = Color(0xFF1A0F00)
val CrmError     = Color(0xFFEF5350)       // light mode
val CrmErrorDark = Color(0xFFFF7070)       // dark mode — better contrast on charcoal
val CrmOnError   = Color(0xFFFFFFFF)