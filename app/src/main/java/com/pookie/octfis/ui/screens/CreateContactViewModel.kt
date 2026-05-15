package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.ContactRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContactPicklistOptions(
    val leadSources : List<String>               = emptyList(),
    val owners      : List<Pair<String, String>> = emptyList(),
)

sealed class CreateContactState {
    object Idle   : CreateContactState()
    object Saving : CreateContactState()
    data class Saved(val zohoId: String)  : CreateContactState()
    data class Error(val message: String) : CreateContactState()
}

class CreateContactViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = ContactRepository(api)

    private val _options = MutableStateFlow(ContactPicklistOptions())
    val options: StateFlow<ContactPicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _createState = MutableStateFlow<CreateContactState>(CreateContactState.Idle)
    val createState: StateFlow<CreateContactState> = _createState.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fieldsDeferred = async { runCatching { api.getFields("Contacts") }.getOrNull() }
                val usersDeferred  = async { runCatching { api.getUsers("AllUsers") }.getOrNull() }
                val fields = fieldsDeferred.await()
                val users  = usersDeferred.await()

                val none = listOf("-None-")

                _options.value = ContactPicklistOptions(
                    leadSources = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    owners = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
                                ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = ContactPicklistOptions(
                    leadSources = listOf("-None-"),
                    owners      = listOf(Pair("", "-None-")),
                )
            } finally {
                _optionsLoading.value = false
            }
        }
    }

    fun save(
        firstName     : String,
        lastName      : String,
        phone         : String,
        email         : String,
        accountName   : String,
        title         : String,
        department    : String,
        ownerEntry    : Pair<String, String>,
        leadSource    : String,
        description   : String,
        mailingStreet : String,
        mailingCity   : String,
        mailingState  : String,
        mailingZip    : String,
        mailingCountry: String,
    ) {
        if (lastName.isBlank()) {
            _createState.value = CreateContactState.Error("Last Name is required")
            return
        }
        viewModelScope.launch {
            _createState.value = CreateContactState.Saving
            repo.createContact(
                firstName      = firstName,
                lastName       = lastName,
                phone          = phone,
                email          = email,
                accountName    = accountName,
                title          = title,
                department     = department,
                contactOwner   = ownerEntry.first,
                leadSource     = leadSource.takeIf { it != "-None-" } ?: "",
                description    = description,
                mailingStreet  = mailingStreet,
                mailingCity    = mailingCity,
                mailingState   = mailingState,
                mailingZip     = mailingZip,
                mailingCountry = mailingCountry,
            ).fold(
                onSuccess = { id -> _createState.value = CreateContactState.Saved(id) },
                onFailure = { e  -> _createState.value = CreateContactState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _createState.value = CreateContactState.Idle }
}