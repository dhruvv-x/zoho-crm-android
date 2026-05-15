package com.pookie.octfis.ui.screens

data class DealPicklistOptions(
    val stages      : List<String>               = emptyList(),
    val types       : List<String>               = emptyList(),
    val leadSources : List<String>               = emptyList(),
    val owners      : List<Pair<String, String>> = emptyList(),
)

sealed class CreateDealState {
    object Idle   : CreateDealState()
    object Saving : CreateDealState()
    data class Saved(val zohoId: String)  : CreateDealState()
    data class Error(val message: String) : CreateDealState()
}