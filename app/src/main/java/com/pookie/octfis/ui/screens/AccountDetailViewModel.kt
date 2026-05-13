package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Account
import com.pookie.octfis.data.model.Contact
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.AccountRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AccountDetailUiState {
    object Loading : AccountDetailUiState()
    data class Success(
        val account         : Account,
        val relatedContacts : List<Contact>,
    ) : AccountDetailUiState()
    data class Error(val message: String) : AccountDetailUiState()
}

class AccountDetailViewModel(private val zohoId: String) : ViewModel() {

    private val repo = AccountRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<AccountDetailUiState>(AccountDetailUiState.Loading)
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AccountDetailUiState.Loading
            val detailDeferred   = async { repo.getAccountDetail(zohoId) }
            val contactsDeferred = async { repo.getAccountContacts(zohoId) }
            val detailResult     = detailDeferred.await()
            val contactsResult   = contactsDeferred.await()
            detailResult.fold(
                onSuccess = { account ->
                    _uiState.value = AccountDetailUiState.Success(
                        account         = account,
                        relatedContacts = contactsResult.getOrDefault(emptyList()),
                    )
                },
                onFailure = { e ->
                    _uiState.value = AccountDetailUiState.Error(e.message ?: "Failed to load")
                }
            )
        }
    }
}