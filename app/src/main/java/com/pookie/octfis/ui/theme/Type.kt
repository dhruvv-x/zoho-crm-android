package com.pookie.octfis.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pookie.octfis.R

val PuviFont = FontFamily(
    Font(R.font.puvi_thin,            FontWeight.Thin),
    Font(R.font.puvi_extralight,      FontWeight.ExtraLight),
    Font(R.font.puvi_light,           FontWeight.Light),
    Font(R.font.puvi_regular,         FontWeight.Normal),
    Font(R.font.puvi_regular_italic,  FontWeight.Normal,   FontStyle.Italic),
    Font(R.font.puvi_medium,          FontWeight.Medium),
    Font(R.font.puvi_semibold,        FontWeight.SemiBold),
    Font(R.font.puvi_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.puvi_bold,            FontWeight.Bold),
    Font(R.font.puvi_bold_italic,     FontWeight.Bold,     FontStyle.Italic),
    Font(R.font.puvi_extrabold,       FontWeight.ExtraBold),
    Font(R.font.puvi_black,           FontWeight.Black),
)

// ── Typography — tuned to match Zoho CRM's crisp, professional density ────────
//
// Key decisions vs old scale:
//  • Negative letter-spacing on all headings/titles → crisp, premium CRM feel
//  • bodyMedium bumped 13 → 14sp → more readable on dense list rows
//  • titleMedium bumped 13 → 15sp → section headers breathe properly
//  • labelLarge letter-spacing tightened → matches Zoho button label rendering
//  • All lineHeights tuned for ~1.4–1.5× ratio — professional document density

val Typography = Typography(

    // ── Display — sign-in hero text, onboarding ───────────────────────────────
    displayLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Black,
        fontSize      = 48.sp,
        lineHeight    = 56.sp,
        letterSpacing = (-1.0).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 48.sp,
        letterSpacing = (-0.8).sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 34.sp,
        lineHeight    = 42.sp,
        letterSpacing = (-0.5).sp,
    ),

    // ── Headline — screen titles, dashboard section headers ───────────────────
    headlineLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 22.sp,
        lineHeight    = 30.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.2).sp,
    ),

    // ── Title — top app bar, card titles, section labels ──────────────────────
    titleLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.0.sp,
    ),

    // ── Body — record field values, list row content ──────────────────────────
    bodyLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 17.sp,
        letterSpacing = 0.0.sp,
    ),

    // ── Label — buttons, chips, field labels, badges ──────────────────────────
    labelLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.0.sp,   // Zoho buttons have no extra tracking
    ),
    labelMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.1.sp,
    ),
)