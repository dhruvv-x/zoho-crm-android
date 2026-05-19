package com.pookie.octfis.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Zoho CRM Shape System ─────────────────────────────────────────────────────
// Zoho uses restrained, business-appropriate rounding — nothing pill-shaped.
// Material3 defaults are too round for a professional CRM feel.
//
// Usage map:
//   extraSmall → badges, small chips, tooltip bubbles         (4dp)
//   small      → input fields, snackbars, menu items          (6dp)
//   medium     → cards, list items, dialogs                   (8dp)
//   large      → bottom sheets (top), side sheets, drawers    (12dp)
//   extraLarge → full-screen modals, large overlays           (16dp)

val CrmShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // badges, status chips
    small      = RoundedCornerShape(6.dp),   // input fields, compact cards
    medium     = RoundedCornerShape(8.dp),   // standard cards, dialogs, FAB
    large      = RoundedCornerShape(12.dp),  // bottom sheets, drawers
    extraLarge = RoundedCornerShape(16.dp),  // large modals
)

// ── Semantic shape aliases ────────────────────────────────────────────────────
// Use these in composables for intent-clarity instead of raw dp values

val ShapeCard        = RoundedCornerShape(8.dp)
val ShapeButton      = RoundedCornerShape(8.dp)   // Zoho buttons — NOT pill-shaped
val ShapeInputField  = RoundedCornerShape(6.dp)   // OutlinedTextField
val ShapeBadge       = RoundedCornerShape(4.dp)   // status badges, count chips
val ShapeChip        = RoundedCornerShape(6.dp)   // filter chips, tag chips
val ShapeDialog      = RoundedCornerShape(12.dp)  // alert dialogs
val ShapeBottomSheet = RoundedCornerShape(        // top corners only
    topStart = 16.dp,
    topEnd   = 16.dp,
    bottomStart = 0.dp,
    bottomEnd   = 0.dp,
)
val ShapeNavDrawer   = RoundedCornerShape(
    topEnd    = 0.dp,
    bottomEnd = 0.dp,
)  // flush left edge like Zoho sidebar