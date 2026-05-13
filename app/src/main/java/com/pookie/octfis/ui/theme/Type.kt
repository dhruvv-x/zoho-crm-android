package com.pookie.octfis.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Screen titles
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp,
        lineHeight = 28.sp,
        color      = CrmOnSurface,
    ),
    // Section / card titles
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        color      = CrmOnSurface,
    ),
    // List item primary text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        color      = CrmOnSurface,
    ),
    // Form labels / secondary text
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 18.sp,
        color      = CrmSubtext,
    ),
    // Captions / timestamps
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        color      = CrmSubtext,
    ),
    // Buttons
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
    ),
    // Bottom nav labels
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
    ),
)