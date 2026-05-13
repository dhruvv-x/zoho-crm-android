package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.DealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DealsUiState {
    object Loading : DealsUiState()
    data class Success(val deals: List<Deal>, val hasMore: Boolean) : DealsUiState()
    data class Error(val message: String) : DealsUiState()
}

class DealsViewModel : ViewModel() {

    private val repo = DealRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<DealsUiState>(DealsUiState.Loading)
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    private val allDeals   = mutableListOf<Deal>()
    private var currentPage = 1
    private var loadingMore = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DealsUiState.Loading
            allDeals.clear()
            currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? DealsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getDeals(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allDeals.addAll(newItems)
                currentPage    = page
                _uiState.value = DealsUiState.Success(allDeals.toList(), hasMore)
            },
            onFailure = { e ->
                _uiState.value = DealsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}