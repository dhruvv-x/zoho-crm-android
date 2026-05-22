package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Contact
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ContactsUiState {
    object Loading : ContactsUiState()
    data class Success(val contacts: List<Contact>, val hasMore: Boolean) : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
}

data class ContactFilterState(
    val leadSource  : String = "",
    val accountName : String = "",
) {
    val isActive get() = leadSource.isNotEmpty() || accountName.isNotEmpty()
}

class ContactsViewModel : ViewModel() {

    private val repo = ContactRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(ContactFilterState())
    val filterState: StateFlow<ContactFilterState> = _filterState.asStateFlow()

    private val _leadSources  = MutableStateFlow<List<String>>(emptyList())
    val leadSources: StateFlow<List<String>> = _leadSources.asStateFlow()

    private val _accountNames = MutableStateFlow<List<String>>(emptyList())
    val accountNames: StateFlow<List<String>> = _accountNames.asStateFlow()

    private val allContacts = mutableListOf<Contact>()
    private var currentPage = 1
    private var loadingMore = false

    init { load() }

    // Full refresh from Zoho — use for pull-to-refresh and manual refresh button only.
    fun load() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            allContacts.clear()
            currentPage = 1
            fetchPage(1)
        }
    }

    // ON_RESUME: always sync from cache so edits/creates are reflected immediately
    // without a full Zoho re-fetch (which has a propagation delay).
    fun loadIfEmpty() {
        val cache = ContactRepository.cache
        if (cache.isNotEmpty()) {
            allContacts.clear()
            allContacts.addAll(cache)
            rebuildFilterOptions()
            _uiState.value = ContactsUiState.Success(applyAll(allContacts), hasMore = false)
        } else {
            load()
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? ContactsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        recompute()
    }

    fun setFilter(filter: ContactFilterState) {
        _filterState.value = filter
        recompute()
    }

    fun clearFilter() {
        _filterState.value = ContactFilterState()
        recompute()
    }

    private fun recompute() {
        val current = _uiState.value
        if (current is ContactsUiState.Success) {
            _uiState.value = current.copy(contacts = applyAll(allContacts))
        }
    }

    private fun rebuildFilterOptions() {
        _leadSources.value  = allContacts.map { it.leadSource }.filter { it.isNotBlank() && it != "-None-" }.distinct().sorted()
        _accountNames.value = allContacts.map { it.accountName }.filter { it.isNotBlank() }.distinct().sorted()
    }

    private fun applyAll(list: List<Contact>): List<Contact> {
        val q = _searchQuery.value.trim().lowercase()
        val f = _filterState.value
        return list
            .filter { c ->
                if (q.isBlank()) true
                else c.fullName.lowercase().contains(q) ||
                        c.phone.lowercase().contains(q) ||
                        c.mobile.lowercase().contains(q) ||
                        c.email.lowercase().contains(q) ||
                        c.accountName.lowercase().contains(q)
            }
            .filter { c -> if (f.leadSource.isBlank())  true else c.leadSource  == f.leadSource }
            .filter { c -> if (f.accountName.isBlank()) true else c.accountName == f.accountName }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getContacts(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allContacts.addAll(newItems)
                currentPage = page
                rebuildFilterOptions()
                _uiState.value = ContactsUiState.Success(applyAll(allContacts), hasMore)
            },
            onFailure = { e ->
                _uiState.value = ContactsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}