package com.pookie.octfis.ui.screens

data class PicklistOptions(
    val industries    : List<String>               = emptyList(),
    val gstTreatments : List<String>               = emptyList(),
    val leadSources   : List<String>               = emptyList(),
    val owners        : List<Pair<String, String>> = emptyList(),
)