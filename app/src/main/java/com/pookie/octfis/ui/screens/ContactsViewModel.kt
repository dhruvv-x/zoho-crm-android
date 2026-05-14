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

class ContactsViewModel : ViewModel() {

    private val repo = ContactRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allContacts = mutableListOf<Contact>()
    private var currentPage  = 1
    private var loadingMore  = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            allContacts.clear()
            currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? ContactsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        val current = _uiState.value
        if (current is ContactsUiState.Success) {
            _uiState.value = current.copy(contacts = filter(allContacts, query))
        }
    }

    private fun filter(list: List<Contact>, query: String): List<Contact> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter {
            it.fullName.lowercase().contains(q) ||
                    it.phone.lowercase().contains(q) ||
                    it.mobile.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.accountName.lowercase().contains(q)
        }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getContacts(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allContacts.addAll(newItems)
                currentPage    = page
                _uiState.value = ContactsUiState.Success(filter(allContacts, _searchQuery.value), hasMore)
            },
            onFailure = { e ->
                _uiState.value = ContactsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}