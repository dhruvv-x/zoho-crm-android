package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.DealRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateDealViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = DealRepository(api)

    private val _options = MutableStateFlow(DealPicklistOptions())
    val options: StateFlow<DealPicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _createState = MutableStateFlow<CreateDealState>(CreateDealState.Idle)
    val createState: StateFlow<CreateDealState> = _createState.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fieldsDeferred = async { runCatching { api.getFields("Deals") }.getOrNull() }
                val usersDeferred  = async { runCatching { api.getUsers("AllUsers") }.getOrNull() }
                val fields = fieldsDeferred.await()
                val users  = usersDeferred.await()

                val none = listOf("-None-")

                _options.value = DealPicklistOptions(
                    stages = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Stage" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    types = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Type" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    leadSources = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),

                    owners = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
                                ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = DealPicklistOptions(
                    stages      = listOf("-None-"),
                    types       = listOf("-None-"),
                    leadSources = listOf("-None-"),
                    owners      = listOf(Pair("", "-None-")),
                )
            } finally {
                _optionsLoading.value = false
            }
        }
    }

    fun save(
        dealName       : String,
        accountName    : String,
        contactName    : String,
        amount         : String,
        closingDate    : String,
        type           : String,
        email          : String,
        ownerEntry     : Pair<String, String>,
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ) {
        if (dealName.isBlank()) {
            _createState.value = CreateDealState.Error("Deal Name is required")
            return
        }
        if (closingDate.isBlank()) {
            _createState.value = CreateDealState.Error("Closing Date is required")
            return
        }
        viewModelScope.launch {
            _createState.value = CreateDealState.Saving
            repo.createDeal(
                dealName        = dealName,
                accountName     = accountName,
                contactName     = contactName,
                amount          = amount,
                closingDate     = closingDate,
                type            = type,
                email           = email,
                dealOwner       = ownerEntry.first,
                description     = description,
                stage           = stage,
                leadSource      = leadSource,
                leadSourceDrill = leadSourceDrill,
            ).fold(
                onSuccess = { id -> _createState.value = CreateDealState.Saved(id) },
                onFailure = { e  -> _createState.value = CreateDealState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _createState.value = CreateDealState.Idle }
}