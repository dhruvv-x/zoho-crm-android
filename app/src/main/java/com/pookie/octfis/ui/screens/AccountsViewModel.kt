package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Account
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AccountsUiState {
    object Loading : AccountsUiState()
    data class Success(val accounts: List<Account>, val hasMore: Boolean) : AccountsUiState()
    data class Error(val message: String) : AccountsUiState()
}

data class AccountFilterState(
    val industry  : String = "",
    val leadSource: String = "",
) {
    val isActive get() = industry.isNotEmpty() || leadSource.isNotEmpty()
}

class AccountsViewModel : ViewModel() {

    private val repo = AccountRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(AccountFilterState())
    val filterState: StateFlow<AccountFilterState> = _filterState.asStateFlow()

    // Unique values for chips — populated after first load
    private val _industries  = MutableStateFlow<List<String>>(emptyList())
    val industries: StateFlow<List<String>> = _industries.asStateFlow()

    private val _leadSources = MutableStateFlow<List<String>>(emptyList())
    val leadSources: StateFlow<List<String>> = _leadSources.asStateFlow()

    private val allAccounts = mutableListOf<Account>()
    private var currentPage  = 1
    private var loadingMore  = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AccountsUiState.Loading
            allAccounts.clear()
            currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? AccountsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        recompute()
    }

    fun setFilter(filter: AccountFilterState) {
        _filterState.value = filter
        recompute()
    }

    fun clearFilter() {
        _filterState.value = AccountFilterState()
        recompute()
    }

    private fun recompute() {
        val current = _uiState.value
        if (current is AccountsUiState.Success) {
            _uiState.value = current.copy(accounts = applyAll(allAccounts))
        }
    }

    private fun applyAll(list: List<Account>): List<Account> {
        val q = _searchQuery.value.trim().lowercase()
        val f = _filterState.value
        return list
            .filter { a ->
                if (q.isBlank()) true
                else a.name.lowercase().contains(q) ||
                        a.phone.lowercase().contains(q) ||
                        a.accountNo.lowercase().contains(q) ||
                        a.industry.lowercase().contains(q)
            }
            .filter { a -> if (f.industry.isBlank())   true else a.industry   == f.industry }
            .filter { a -> if (f.leadSource.isBlank()) true else a.leadSource == f.leadSource }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getAccounts(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allAccounts.addAll(newItems)
                currentPage = page
                // Rebuild unique filter options from all loaded data
                _industries.value  = allAccounts.map { it.industry }.filter { it.isNotBlank() && it != "-None-" }.distinct().sorted()
                _leadSources.value = allAccounts.map { it.leadSource }.filter { it.isNotBlank() && it != "-None-" }.distinct().sorted()
                _uiState.value = AccountsUiState.Success(applyAll(allAccounts), hasMore)
            },
            onFailure = { e ->
                _uiState.value = AccountsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}