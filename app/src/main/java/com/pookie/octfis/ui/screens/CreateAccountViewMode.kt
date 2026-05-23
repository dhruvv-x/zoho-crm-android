package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.AccountRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateAccountState {
    object Idle   : CreateAccountState()
    object Saving : CreateAccountState()
    data class Saved(val zohoId: String)  : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
}

class CreateAccountViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = AccountRepository(api)

    private val _options        = MutableStateFlow(PicklistOptions())
    val options: StateFlow<PicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _createState    = MutableStateFlow<CreateAccountState>(CreateAccountState.Idle)
    val createState: StateFlow<CreateAccountState> = _createState.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fieldsDeferred = async { runCatching { api.getFields("Accounts") }.getOrNull() }
                val usersDeferred  = async { runCatching { api.getUsers("AllUsers") }.getOrNull() }
                val fields = fieldsDeferred.await()
                val users  = usersDeferred.await()

                val none = listOf("-None-")

                _options.value = PicklistOptions(
                    industries = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Industry" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    gstTreatments = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "GST_Treatment" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    leadSources = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    owners = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.displayName) }
                                ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = PicklistOptions(
                    industries    = listOf("-None-"),
                    gstTreatments = listOf("-None-"),
                    leadSources   = listOf("-None-"),
                    owners        = listOf(Pair("", "-None-")),
                )
            } finally {
                _optionsLoading.value = false
            }
        }
    }

    fun save(
        name          : String,
        phone         : String,
        website       : String,
        industry      : String,
        gstTreatment  : String,
        gstin         : String,
        leadSource    : String,
        ownerEntry    : Pair<String, String>,
        description   : String,
        billingStreet : String,
        billingStreet2: String,
        billingCity   : String,
        billingState  : String,
        billingCode   : String,
        billingCountry: String,
    ) {
        if (name.isBlank()) {
            _createState.value = CreateAccountState.Error("Account Name is required")
            return
        }
        viewModelScope.launch {
            _createState.value = CreateAccountState.Saving
            repo.createAccount(
                name           = name,
                phone          = phone,
                website        = website,
                industry       = industry,
                gstTreatment   = gstTreatment,
                gstin          = gstin,
                leadSource     = leadSource,
                accountOwner   = ownerEntry.first,
                description    = description,
                billingStreet  = billingStreet,
                billingStreet2 = billingStreet2,
                billingCity    = billingCity,
                billingState   = billingState,
                billingCode    = billingCode,
                billingCountry = billingCountry,
            ).fold(
                onSuccess = { id -> _createState.value = CreateAccountState.Saved(id) },
                onFailure = { e  -> _createState.value = CreateAccountState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _createState.value = CreateAccountState.Idle }
}