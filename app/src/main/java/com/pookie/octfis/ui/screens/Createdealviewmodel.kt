package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.AccountRepository
import com.pookie.octfis.data.repository.ContactRepository
import com.pookie.octfis.data.repository.DealRepository
import com.pookie.octfis.ui.components.LookupItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateDealViewModel : ViewModel() {

    private val api         = ZohoServiceLocator.getApiService()
    private val repo        = DealRepository(api)
    private val accountRepo = AccountRepository(api)
    private val contactRepo = ContactRepository(api)

    private val _options = MutableStateFlow(DealPicklistOptions())
    val options: StateFlow<DealPicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _createState = MutableStateFlow<CreateDealState>(CreateDealState.Idle)
    val createState: StateFlow<CreateDealState> = _createState.asStateFlow()

    private val _accountItems = MutableStateFlow<List<LookupItem>>(emptyList())
    val accountItems: StateFlow<List<LookupItem>> = _accountItems.asStateFlow()

    private val _contactItems = MutableStateFlow<List<LookupItem>>(emptyList())
    val contactItems: StateFlow<List<LookupItem>> = _contactItems.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fieldsDeferred = async { runCatching { api.getFields("Deals") }.getOrNull() }
                val usersDeferred  = async { runCatching { api.getUsers("AllUsers") }.getOrNull() }
                val fields = fieldsDeferred.await()
                val users  = usersDeferred.await()
                val none   = listOf("-None-")

                _options.value = DealPicklistOptions(
                    stages = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Stage" }
                        ?.pickListValues?.map { it.actualValue } ?: emptyList()),

                    types = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Type" }
                        ?.pickListValues?.map { it.actualValue } ?: emptyList()),

                    leadSources = none + (fields?.fields
                        ?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.actualValue } ?: emptyList()),

                    owners = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.displayName) }
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

        viewModelScope.launch {
            val cached = AccountRepository.cache
            if (cached.isNotEmpty())
                _accountItems.value = cached.map { LookupItem(it.zohoId, it.name, it.phone) }
            else
                accountRepo.getAccounts().getOrNull()?.first
                    ?.let { _accountItems.value = it.map { a -> LookupItem(a.zohoId, a.name, a.phone) } }
        }

        viewModelScope.launch {
            val cached = ContactRepository.cache
            if (cached.isNotEmpty())
                _contactItems.value = cached.map { LookupItem(it.zohoId, it.fullName, it.email) }
            else
                contactRepo.getContacts().getOrNull()?.first
                    ?.let { _contactItems.value = it.map { c -> LookupItem(c.zohoId, c.fullName, c.email) } }
        }
    }

    fun save(
        dealName       : String,
        accountName    : String,
        accountZohoId  : String,
        contactName    : String,
        contactZohoId  : String,
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
        if (dealName.isBlank()) { _createState.value = CreateDealState.Error("Deal Name is required"); return }
        if (closingDate.isBlank()) { _createState.value = CreateDealState.Error("Closing Date is required"); return }
        if (stage.isBlank() || stage == "-None-") { _createState.value = CreateDealState.Error("Stage is required"); return }
        viewModelScope.launch {
            _createState.value = CreateDealState.Saving
            repo.createDeal(
                dealName        = dealName,
                accountName     = accountName,
                accountZohoId   = accountZohoId,
                contactName     = contactName,
                contactZohoId   = contactZohoId,
                amount          = amount,
                closingDate     = closingDate,
                type            = type,
                email           = email,
                dealOwner       = ownerEntry.first,
                dealOwnerName   = ownerEntry.second,
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