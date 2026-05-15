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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class QuotesUiState {
    object Loading : QuotesUiState()
    data class Success(val quotes: List<Quote>, val hasMore: Boolean) : QuotesUiState()
    data class Error(val message: String) : QuotesUiState()
}

enum class ValidityDateFilter(val label: String) {
    ALL("All"),
    THIS_MONTH("This Month"),
    THIS_QUARTER("This Quarter"),
}

data class QuoteFilterState(
    val quoteStage   : String              = "",
    val accountName  : String              = "",
    val validityDate : ValidityDateFilter  = ValidityDateFilter.ALL,
) {
    val isActive get() = quoteStage.isNotEmpty() || accountName.isNotEmpty() || validityDate != ValidityDateFilter.ALL
}

class QuotesViewModel : ViewModel() {

    private val repo = QuoteRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<QuotesUiState>(QuotesUiState.Loading)
    val uiState: StateFlow<QuotesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(QuoteFilterState())
    val filterState: StateFlow<QuoteFilterState> = _filterState.asStateFlow()

    private val _quoteStages  = MutableStateFlow<List<String>>(emptyList())
    val quoteStages: StateFlow<List<String>> = _quoteStages.asStateFlow()

    private val _accountNames = MutableStateFlow<List<String>>(emptyList())
    val accountNames: StateFlow<List<String>> = _accountNames.asStateFlow()

    private val allQuotes  = mutableListOf<Quote>()
    private var currentPage = 1
    private var loadingMore = false

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        recompute()
    }

    fun setFilter(filter: QuoteFilterState) {
        _filterState.value = filter
        recompute()
    }

    fun clearFilter() {
        _filterState.value = QuoteFilterState()
        recompute()
    }

    private fun recompute() {
        val current = _uiState.value
        if (current is QuotesUiState.Success) {
            _uiState.value = current.copy(quotes = applyAll(allQuotes))
        }
    }

    private fun applyAll(list: List<Quote>): List<Quote> {
        val q     = _searchQuery.value.trim().lowercase()
        val f     = _filterState.value
        val today = LocalDate.now()

        return list
            .filter { qt ->
                if (q.isBlank()) true
                else qt.subject.lowercase().contains(q) ||
                        qt.accountName.lowercase().contains(q) ||
                        qt.contactName.lowercase().contains(q) ||
                        qt.quoteStage.lowercase().contains(q)
            }
            .filter { qt -> if (f.quoteStage.isBlank())  true else qt.quoteStage  == f.quoteStage }
            .filter { qt -> if (f.accountName.isBlank()) true else qt.accountName == f.accountName }
            .filter { qt ->
                when (f.validityDate) {
                    ValidityDateFilter.ALL          -> true
                    ValidityDateFilter.THIS_MONTH   -> {
                        runCatching {
                            val date = LocalDate.parse(qt.validUntil, dateFormatter)
                            date.year == today.year && date.month == today.month
                        }.getOrDefault(false)
                    }
                    ValidityDateFilter.THIS_QUARTER -> {
                        runCatching {
                            val date   = LocalDate.parse(qt.validUntil, dateFormatter)
                            val qStart = today.withMonth(((today.monthValue - 1) / 3) * 3 + 1).withDayOfMonth(1)
                            val qEnd   = qStart.plusMonths(2).withDayOfMonth(qStart.plusMonths(2).lengthOfMonth())
                            date in qStart..qEnd
                        }.getOrDefault(false)
                    }
                }
            }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getQuotes(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allQuotes.addAll(newItems)
                currentPage      = page
                _quoteStages.value  = allQuotes.map { it.quoteStage }.filter { it.isNotBlank() }.distinct().sorted()
                _accountNames.value = allQuotes.map { it.accountName }.filter { it.isNotBlank() }.distinct().sorted()
                _uiState.value   = QuotesUiState.Success(applyAll(allQuotes), hasMore)
            },
            onFailure = { e ->
                _uiState.value = QuotesUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}