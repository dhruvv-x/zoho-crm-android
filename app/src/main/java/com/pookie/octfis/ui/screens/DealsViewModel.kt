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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class DealsUiState {
    object Loading : DealsUiState()
    data class Success(val deals: List<Deal>, val hasMore: Boolean) : DealsUiState()
    data class Error(val message: String) : DealsUiState()
}

enum class ClosingDateFilter(val label: String) {
    ALL("All"),
    THIS_MONTH("This Month"),
    THIS_QUARTER("This Quarter"),
}

data class DealFilterState(
    val stage       : String            = "",
    val accountName : String            = "",
    val closingDate : ClosingDateFilter = ClosingDateFilter.ALL,
) {
    val isActive get() = stage.isNotEmpty() || accountName.isNotEmpty() || closingDate != ClosingDateFilter.ALL
}

class DealsViewModel : ViewModel() {

    private val repo = DealRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<DealsUiState>(DealsUiState.Loading)
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(DealFilterState())
    val filterState: StateFlow<DealFilterState> = _filterState.asStateFlow()

    private val _stages      = MutableStateFlow<List<String>>(emptyList())
    val stages: StateFlow<List<String>> = _stages.asStateFlow()

    private val _accountNames = MutableStateFlow<List<String>>(emptyList())
    val accountNames: StateFlow<List<String>> = _accountNames.asStateFlow()

    private val allDeals   = mutableListOf<Deal>()
    private var currentPage = 1
    private var loadingMore = false

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        recompute()
    }

    fun setFilter(filter: DealFilterState) {
        _filterState.value = filter
        recompute()
    }

    fun clearFilter() {
        _filterState.value = DealFilterState()
        recompute()
    }

    private fun recompute() {
        val current = _uiState.value
        if (current is DealsUiState.Success) {
            _uiState.value = current.copy(deals = applyAll(allDeals))
        }
    }

    private fun applyAll(list: List<Deal>): List<Deal> {
        val q     = _searchQuery.value.trim().lowercase()
        val f     = _filterState.value
        val today = LocalDate.now()

        return list
            .filter { d ->
                if (q.isBlank()) true
                else d.dealName.lowercase().contains(q) ||
                        d.accountName.lowercase().contains(q) ||
                        d.contactName.lowercase().contains(q) ||
                        d.stage.lowercase().contains(q) ||
                        d.amount.lowercase().contains(q)
            }
            .filter { d -> if (f.stage.isBlank())       true else d.stage       == f.stage }
            .filter { d -> if (f.accountName.isBlank()) true else d.accountName == f.accountName }
            .filter { d ->
                when (f.closingDate) {
                    ClosingDateFilter.ALL         -> true
                    ClosingDateFilter.THIS_MONTH  -> {
                        runCatching {
                            val date = LocalDate.parse(d.closingDate, dateFormatter)
                            date.year == today.year && date.month == today.month
                        }.getOrDefault(false)
                    }
                    ClosingDateFilter.THIS_QUARTER -> {
                        runCatching {
                            val date    = LocalDate.parse(d.closingDate, dateFormatter)
                            val qStart  = today.withMonth(((today.monthValue - 1) / 3) * 3 + 1).withDayOfMonth(1)
                            val qEnd    = qStart.plusMonths(2).withDayOfMonth(qStart.plusMonths(2).lengthOfMonth())
                            date in qStart..qEnd
                        }.getOrDefault(false)
                    }
                }
            }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getDeals(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allDeals.addAll(newItems)
                currentPage     = page
                _stages.value       = allDeals.map { it.stage }.filter { it.isNotBlank() && it != "-None-" }.distinct().sorted()
                _accountNames.value = allDeals.map { it.accountName }.filter { it.isNotBlank() }.distinct().sorted()
                _uiState.value  = DealsUiState.Success(applyAll(allDeals), hasMore)
            },
            onFailure = { e ->
                _uiState.value = DealsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}