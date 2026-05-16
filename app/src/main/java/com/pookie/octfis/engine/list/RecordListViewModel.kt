// engine/list/RecordListViewModel.kt
package com.pookie.octfis.engine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.repository.RawRecord
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.MetadataEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class RecordListUiState {
    object Loading  : RecordListUiState()
    data class Success(
        val records : List<RawRecord>,
        val hasMore : Boolean,
    ) : RecordListUiState()
    data class Error(val message: String) : RecordListUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class RecordListViewModel @AssistedInject constructor(
    private val metadataEngine   : MetadataEngine,
    private val recordRepository : ZohoRecordRepository,
    @Assisted val moduleName     : String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordListUiState>(RecordListUiState.Loading)
    val uiState: StateFlow<RecordListUiState> = _uiState.asStateFlow()

    private val _fields = MutableStateFlow<List<FieldMetadata>>(emptyList())
    val fields: StateFlow<List<FieldMetadata>> = _fields.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * The api_name of the field used as the primary (title) line in each row.
     * Resolved automatically from metadata — no hardcoding at call-sites.
     */
    private val _primaryField = MutableStateFlow("Name")
    val primaryField: StateFlow<String> = _primaryField.asStateFlow()

    /**
     * The api_name of the field used as the subtitle line (nullable = no subtitle).
     */
    private val _secondaryField = MutableStateFlow<String?>(null)
    val secondaryField: StateFlow<String?> = _secondaryField.asStateFlow()

    // Internal pagination state
    private val allRecords  = mutableListOf<RawRecord>()
    private var currentPage = 1
    private var loadingMore = false

    init {
        loadMetadataThenRecords()
    }

    // ── Boot ──────────────────────────────────────────────────────────────────

    private fun loadMetadataThenRecords() {
        viewModelScope.launch {
            _uiState.value = RecordListUiState.Loading
            metadataEngine.getModuleMetadata(moduleName)
                .onSuccess { meta ->
                    _fields.value = meta
                    resolveDisplayFields(meta)
                    allRecords.clear()
                    currentPage = 1
                    fetchPage(1)
                }
                .onFailure { e ->
                    _uiState.value = RecordListUiState.Error("Failed to load fields: ${e.message}")
                }
        }
    }

    /**
     * Picks the best primary + secondary field from Zoho metadata.
     *
     * Priority for PRIMARY:
     *  1. Known name fields for common modules (Account_Name, Full_Name, Subject…)
     *  2. Any field whose api_name ends with "_Name"
     *  3. First text-like field in sequence order
     *
     * Priority for SECONDARY:
     *  1. Known secondary fields per module (Phone, Email, Amount…)
     *  2. First text-like field after primary
     */
    private fun resolveDisplayFields(meta: List<FieldMetadata>) {
        val apiNames = meta.map { it.apiName }.toSet()

        // ── Primary ───────────────────────────────────────────────────────
        val knownPrimary = when (moduleName) {
            "Accounts"    -> "Account_Name"
            "Contacts"    -> "Full_Name"
            "Leads"       -> "Full_Name"
            "Deals",
            "Potentials"  -> "Deal_Name"
            "Quotes"      -> "Subject"
            "SalesOrders" -> "Subject"
            "Invoices"    -> "Subject"
            "PurchaseOrders" -> "Subject"
            "Products"    -> "Product_Name"
            "Campaigns"   -> "Campaign_Name"
            "Cases"       -> "Subject"
            "Solutions"   -> "Solution_Title"
            "Vendors"     -> "Vendor_Name"
            "Tasks"       -> "Subject"
            "Events"      -> "Event_Title"
            else          -> null
        }

        _primaryField.value = when {
            knownPrimary != null && knownPrimary in apiNames -> knownPrimary
            "Name" in apiNames -> "Name"
            else -> meta.firstOrNull { it.apiName.endsWith("_Name") }?.apiName
                ?: meta.firstOrNull()?.apiName
                ?: "Name"
        }

        // ── Secondary ─────────────────────────────────────────────────────
        val knownSecondary = when (moduleName) {
            "Accounts"    -> "Phone"
            "Contacts"    -> "Email"
            "Leads"       -> "Email"
            "Deals",
            "Potentials"  -> "Amount"
            "Quotes"      -> "Grand_Total"
            "SalesOrders" -> "Grand_Total"
            "Invoices"    -> "Grand_Total"
            "Products"    -> "Unit_Price"
            "Campaigns"   -> "Status"
            "Cases"       -> "Status"
            "Tasks"       -> "Due_Date"
            "Events"      -> "Start_DateTime"
            else          -> null
        }

        _secondaryField.value = when {
            knownSecondary != null && knownSecondary in apiNames -> knownSecondary
            else -> meta
                .filter { it.apiName != _primaryField.value }
                .firstOrNull()
                ?.apiName
        }
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    fun refresh() {
        allRecords.clear()
        currentPage = 1
        viewModelScope.launch { fetchPage(1) }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? RecordListUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        recordRepository.listRecords(module = moduleName, page = page)
            .onSuccess { (newItems, hasMore) ->
                allRecords.addAll(newItems)
                currentPage = page
                _uiState.value = RecordListUiState.Success(
                    records = applySearch(allRecords),
                    hasMore = hasMore,
                )
            }
            .onFailure { e ->
                _uiState.value = RecordListUiState.Error(e.message ?: "Unknown error")
            }
        loadingMore = false
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun setSearch(query: String) {
        _searchQuery.value = query
        val current = _uiState.value
        if (current is RecordListUiState.Success) {
            _uiState.value = current.copy(records = applySearch(allRecords))
        }
    }

    private fun applySearch(list: List<RawRecord>): List<RawRecord> {
        val q = _searchQuery.value.trim().lowercase()
        if (q.isBlank()) return list
        return list.filter { record ->
            record.fields.values.any { v ->
                v?.toString()?.lowercase()?.contains(q) == true
            }
        }
    }

    // ── Assisted Factory ──────────────────────────────────────────────────────

    @AssistedFactory
    interface Factory {
        fun create(@Assisted moduleName: String): RecordListViewModel
    }
}