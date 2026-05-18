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

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Black,
        fontSize      = 48.sp,
        lineHeight    = 56.sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 48.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 13.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.4.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = PuviFont,
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.2.sp,
    ),
)