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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Debounce job for server-side search
    private var searchJob: Job? = null

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

        // Types that are never useful as a display title or subtitle.
        // LOOKUP/OWNER render as nested objects; FORMULA/UNKNOWN are unreliable.
        val nonDisplayTypes = setOf(
            FieldType.LOOKUP,
            FieldType.OWNER,
            FieldType.FORMULA,
            FieldType.UNKNOWN,
            FieldType.BOOLEAN,
            FieldType.MULTI_SELECT,
            FieldType.RICH_TEXT,
        )

        val displayable = sorted.filter { it.type !in nonDisplayTypes }

        // ── Primary field (record title) ──────────────────────────────────────
        val primary =
            // 1. Field literally named "Name" — all custom modules use this
            displayable.firstOrNull { it.apiName == "Name" }
            // 2. Field literally named "Subject" — Quotes, Tasks, Meetings, etc.
                ?: displayable.firstOrNull { it.apiName == "Subject" }
                // 3. First TEXT field whose apiName ends with "_Name" or "_Title"
                ?: displayable.firstOrNull {
                    it.type == FieldType.TEXT &&
                            (it.apiName.endsWith("_Name") || it.apiName.endsWith("_Title"))
                }
                // 4. First non-readOnly TEXT/EMAIL/PHONE field by sequence
                ?: displayable.firstOrNull {
                    !it.readOnly &&
                            it.type in listOf(FieldType.TEXT, FieldType.EMAIL, FieldType.PHONE)
                }
                // 5. Absolute fallback: first displayable field
                ?: displayable.firstOrNull()
                // 6. Last resort: truly first field regardless of type
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
                displayable.firstOrNull { field ->
                    field.type == preferredType &&
                            field.apiName != _primaryField.value
                }
            }

        _secondaryField.value = secondary?.apiName
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    fun refresh() {
        // Cancel any pending search and reset query so we go back to list mode
        searchJob?.cancel()
        searchJob = null
        _searchQuery.value = ""
        allRecords.clear()
        currentPage = 1
        viewModelScope.launch { fetchPage(1) }
    }

    fun loadNextPage() {
        // Don't paginate while a search is active — server search returns its own set
        if (_searchQuery.value.trim().length >= 3) return
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
                // If user typed something short while we were loading, apply client filter
                _uiState.value = RecordListUiState.Success(
                    records = applyClientSearch(allRecords),
                    hasMore = hasMore,
                )
            }
            .onFailure { e ->
                _uiState.value = RecordListUiState.Error(e.message ?: "Unknown error")
            }
        loadingMore = false
    }

    // ── Search ────────────────────────────────────────────────────────────────

    /**
     * Called by the UI on every keystroke.
     *
     * • query.length < 3  → cancel any server job, apply client-side filter
     *                        over the locally-cached [allRecords] immediately.
     * • query.length >= 3 → debounce 300 ms, then call [searchRecords] on the
     *                        server. If the server returns empty, fall back to
     *                        the client-side filter so the list isn't jarring.
     */
    fun setSearch(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        val trimmed = query.trim()

        if (trimmed.length < 3) {
            // Short query → fast local filter, no network call
            val current = _uiState.value
            if (current is RecordListUiState.Success) {
                _uiState.value = current.copy(records = applyClientSearch(allRecords))
            } else {
                // Re-apply over allRecords even if we were in a previous search state
                _uiState.value = RecordListUiState.Success(
                    records = applyClientSearch(allRecords),
                    hasMore = false,
                )
            }
            return
        }

        // Long-enough query → debounced server search
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.value = RecordListUiState.Loading
            val results = recordRepository.searchRecords(module = moduleName, query = trimmed)
            if (results.isNotEmpty()) {
                // Convert List<Pair<id,name>> → List<RawRecord> using the name key
                // we resolved for this module so the list item renders correctly
                val primaryKey = _primaryField.value
                val serverRecords = results.map { (id, displayName) ->
                    RawRecord(
                        id     = id,
                        fields = mapOf("id" to id, primaryKey to displayName),
                    )
                }
                _uiState.value = RecordListUiState.Success(
                    records = serverRecords,
                    hasMore = false,       // server search returns a flat result set
                )
            } else {
                // Server returned nothing → fall back to client-side filter
                val clientFiltered = applyClientSearch(allRecords)
                _uiState.value = RecordListUiState.Success(
                    records = clientFiltered,
                    hasMore = false,
                )
            }
        }
    }

    // ── Client-side filter (fallback / short queries) ─────────────────────────

    private fun applyClientSearch(list: List<RawRecord>): List<RawRecord> {
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