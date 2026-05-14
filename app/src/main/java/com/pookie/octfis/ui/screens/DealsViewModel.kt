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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun setSearch(query: String) {
        _searchQuery.value = query
        val current = _uiState.value
        if (current is DealsUiState.Success) {
            _uiState.value = current.copy(deals = filter(allDeals, query))
        }
    }

    private fun filter(list: List<Deal>, query: String): List<Deal> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter {
            it.dealName.lowercase().contains(q) ||
                    it.accountName.lowercase().contains(q) ||
                    it.contactName.lowercase().contains(q) ||
                    it.stage.lowercase().contains(q) ||
                    it.amount.lowercase().contains(q)
        }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getDeals(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allDeals.addAll(newItems)
                currentPage    = page
                _uiState.value = DealsUiState.Success(filter(allDeals, _searchQuery.value), hasMore)
            },
            onFailure = { e ->
                _uiState.value = DealsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}