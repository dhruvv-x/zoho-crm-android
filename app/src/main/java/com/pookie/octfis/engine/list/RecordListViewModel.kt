// engine/list/RecordListViewModel.kt
package com.pookie.octfis.engine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.repository.RawRecord
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType
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

    private val _primaryField = MutableStateFlow("Name")
    val primaryField: StateFlow<String> = _primaryField.asStateFlow()

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

    // ── Display field heuristics (zero hardcoded module names) ────────────────

    private fun resolveDisplayFields(meta: List<FieldMetadata>) {
        val sorted = meta.sortedBy { it.sequence }

        // ── Primary field (record title) ──────────────────────────────────────
        val primary =
            // 1. Field literally named "Name" — all custom modules use this
            sorted.firstOrNull { it.apiName == "Name" }
            // 2. First TEXT field whose apiName ends with "_Name"
                ?: sorted.firstOrNull {
                    it.type == FieldType.TEXT && it.apiName.endsWith("_Name")
                }
                // 3. First TEXT field named "Subject" or ending with "_Title"
                ?: sorted.firstOrNull {
                    it.type == FieldType.TEXT &&
                            (it.apiName == "Subject" || it.apiName.endsWith("_Title"))
                }
                // 4. First non-readOnly TEXT/EMAIL/PHONE field by sequence
                ?: sorted.firstOrNull {
                    !it.readOnly &&
                            it.type in listOf(FieldType.TEXT, FieldType.EMAIL, FieldType.PHONE)
                }
                // 5. Absolute fallback: first field in metadata
                ?: sorted.firstOrNull()

        _primaryField.value = primary?.apiName ?: "Name"

        // ── Secondary field (subtitle) ────────────────────────────────────────
        val secondaryTypePriority = listOf(
            FieldType.EMAIL,
            FieldType.PHONE,
            FieldType.CURRENCY,
            FieldType.DECIMAL,
            FieldType.PERCENT,
            FieldType.DATE,
            FieldType.DATETIME,
            FieldType.PICKLIST,
            FieldType.TEXT,
        )

        val secondary = secondaryTypePriority
            .firstNotNullOfOrNull { preferredType ->
                sorted.firstOrNull { field ->
                    field.type == preferredType &&
                            field.apiName != _primaryField.value
                }
            }

        _secondaryField.value = secondary?.apiName
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