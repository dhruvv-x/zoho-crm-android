package com.pookie.octfis.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary — Zoho CRM indigo-blue ───────────────────────────────────────────
// Exact match to site buttons, active nav, FAB
val CrmAccent          = Color(0xFF4361EE)   // primary button, FAB, active
val CrmAccentHover     = Color(0xFF3451D1)   // pressed / hover state
val CrmAccentDark      = Color(0xFF2C42B8)   // on-container text
val CrmAccentLight     = Color(0xFFEBEEFD)   // chip bg, tag bg, input focus ring
val CrmAccentContainer = Color(0xFFDDE3FB)   // badge bg, selected row tint
val CrmOnAccent        = Color(0xFFFFFFFF)

// Screen-level aliases (keep existing references working)
val CrmPrimary      = CrmAccent
val CrmPrimaryLight = CrmAccentLight
val CrmPrimaryDark  = CrmAccentDark
val CrmOnPrimary    = CrmOnAccent

// ── Sidebar / Nav — Zoho dark navy sidebar ────────────────────────────────────
val CrmNavBackground = Color(0xFF1A1F36)     // sidebar bg — deep navy
val CrmNavActive     = Color(0xFF4361EE)     // active pill — same as primary
val CrmNavText       = Color(0xFFB8C2D8)     // inactive nav label
val CrmNavTextActive = Color(0xFFFFFFFF)     // active nav label
val CrmNavIcon       = Color(0xFF7A89AA)     // inactive icon
val CrmNavIconActive = Color(0xFFFFFFFF)     // active icon

// ── Light Theme surfaces ──────────────────────────────────────────────────────
val CrmBackground    = Color(0xFFF0F2F8)     // page bg — cool gray-blue
val CrmSurface       = Color(0xFFFFFFFF)     // cards, dialogs — pure white
val CrmSurfaceAlt    = Color(0xFFF4F6FC)     // table alt rows, input bg
val CrmSurfaceCard   = Color(0xFFFFFFFF)     // explicit card bg
val CrmOnSurface     = Color(0xFF1A1F36)     // primary text — navy black
val CrmSubtext       = Color(0xFF6B7A99)     // secondary text — blue-gray
val CrmHint          = Color(0xFFA0ABBE)     // placeholder / hint text
val CrmDivider       = Color(0xFFE2E6F0)     // lines, borders — cool gray

// ── Dark Theme surfaces ───────────────────────────────────────────────────────
val CrmBackgroundDark = Color(0xFF0E1120)    // page bg dark
val CrmSurfaceDark    = Color(0xFF171B2D)    // card bg dark
val CrmSurfaceAltDark = Color(0xFF1D2236)    // alt row / input bg dark
val CrmOnSurfaceDark  = Color(0xFFDDE3F0)    // primary text dark
val CrmSubtextDark    = Color(0xFF8A96B8)    // secondary text dark
val CrmHintDark       = Color(0xFF5A6585)    // placeholder dark
val CrmDividerDark    = Color(0xFF252A42)    // dividers dark

// ── Slate aliases (nav/table headers) ────────────────────────────────────────
val CrmSlate          = Color(0xFF1A1F36)
val CrmSlateDark      = Color(0xFF101320)

// ── Table ─────────────────────────────────────────────────────────────────────
val CrmTableHeader     = Color(0xFF252A42)   // column header bg
val CrmTableHeaderDark = Color(0xFF181C2E)
val CrmRowAlt          = Color(0xFFF7F9FC)   // zebra stripe light
val CrmRowAltDark      = Color(0xFF1D2236)
val CrmRowSelected     = Color(0xFFEBEEFD)   // selected row highlight
val CrmRowSelectedDark = Color(0xFF252A42)

// ── Status — exact Zoho CRM colors ───────────────────────────────────────────
val CrmSuccess          = Color(0xFF00875A)
val CrmSuccessContainer = Color(0xFFE3F5EE)  // green badge bg
val CrmOnSuccess        = Color(0xFFFFFFFF)
val CrmOnSuccessContainer = Color(0xFF005235)

val CrmWarning          = Color(0xFFFF8B00)
val CrmWarningContainer = Color(0xFFFFF3E0)
val CrmOnWarning        = Color(0xFF1A0F00)
val CrmOnWarningContainer = Color(0xFF7A3E00)

val CrmError            = Color(0xFFDE350B)
val CrmErrorDark        = Color(0xFFFF5630)
val CrmErrorContainer   = Color(0xFFFFECE8)
val CrmOnError          = Color(0xFFFFFFFF)
val CrmOnErrorContainer = Color(0xFF5C1400)

// ── Link color ────────────────────────────────────────────────────────────────
val CrmLink     = Color(0xFF4361EE)          // clickable record names, URLs
val CrmLinkDark = Color(0xFF8CA4F5)