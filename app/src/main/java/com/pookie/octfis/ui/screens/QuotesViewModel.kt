package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuotesUiState {
    object Loading : QuotesUiState()
    data class Success(val quotes: List<Quote>, val hasMore: Boolean) : QuotesUiState()
    data class Error(val message: String) : QuotesUiState()
}

class QuotesViewModel : ViewModel() {

    private val repo = QuoteRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<QuotesUiState>(QuotesUiState.Loading)
    val uiState: StateFlow<QuotesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allQuotes  = mutableListOf<Quote>()
    private var currentPage = 1
    private var loadingMore = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = QuotesUiState.Loading
            allQuotes.clear()
            currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? QuotesUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        val current = _uiState.value
        if (current is QuotesUiState.Success) {
            _uiState.value = current.copy(quotes = filter(allQuotes, query))
        }
    }

    private fun filter(list: List<Quote>, query: String): List<Quote> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter {
            it.subject.lowercase().contains(q) ||
                    it.accountName.lowercase().contains(q) ||
                    it.contactName.lowercase().contains(q) ||
                    it.quoteStage.lowercase().contains(q)
        }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getQuotes(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allQuotes.addAll(newItems)
                currentPage    = page
                _uiState.value = QuotesUiState.Success(filter(allQuotes, _searchQuery.value), hasMore)
            },
            onFailure = { e ->
                _uiState.value = QuotesUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}