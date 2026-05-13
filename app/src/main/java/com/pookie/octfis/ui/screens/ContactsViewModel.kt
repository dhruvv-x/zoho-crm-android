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

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getContacts(page).fold(
            onSuccess = { (newItems, hasMore) ->
                allContacts.addAll(newItems)
                currentPage    = page
                _uiState.value = ContactsUiState.Success(allContacts.toList(), hasMore)
            },
            onFailure = { e ->
                _uiState.value = ContactsUiState.Error(e.message ?: "Unknown error")
            }
        )
        loadingMore = false
    }
}