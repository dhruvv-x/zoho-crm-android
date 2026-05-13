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

class AccountsViewModel : ViewModel() {

    private val repo = AccountRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

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

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getAccounts(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allAccounts.addAll(newItems)
                currentPage   = page
                _uiState.value = AccountsUiState.Success(allAccounts.toList(), hasMore)
            },
            onFailure = { e ->
                _uiState.value = AccountsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}