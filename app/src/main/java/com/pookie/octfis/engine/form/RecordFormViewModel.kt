package com.pookie.octfis.engine.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

sealed class FormUiState {
    object Idle : FormUiState()
    object LoadingMetadata : FormUiState()
    object LoadingRecord : FormUiState()
    object Submitting : FormUiState()
    object Success : FormUiState()
    data class Error(val message: String) : FormUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class RecordFormViewModel @AssistedInject constructor(
    private val metadataEngine    : MetadataEngine,
    private val recordRepository  : ZohoRecordRepository,
    @Assisted("module")   val moduleName : String,
    @Assisted("recordId") val recordId   : String?,
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _fields = MutableStateFlow<List<FieldMetadata>>(emptyList())
    val fields: StateFlow<List<FieldMetadata>> = _fields.asStateFlow()

    val formState = FormStateManager()

    val isEditMode get() = recordId != null

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadMetadata()
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    private fun loadMetadata() {
        viewModelScope.launch {
            _uiState.value = FormUiState.LoadingMetadata
            metadataEngine.getModuleMetadata(moduleName)
                .onSuccess { fields ->
                    _fields.value = fields
                    if (recordId != null) {
                        loadRecord(recordId)
                    } else {
                        _uiState.value = FormUiState.Idle
                    }
                }
                .onFailure { e ->
                    _uiState.value = FormUiState.Error("Failed to load fields: ${e.message}")
                }
        }
    }

    // ── Load existing record (edit mode) ──────────────────────────────────────

    private fun loadRecord(id: String) {
        viewModelScope.launch {
            _uiState.value = FormUiState.LoadingRecord
            recordRepository.getRecord(moduleName, id)
                .onSuccess { rawMap ->
                    val initial = rawMap.mapValues { (_, v) ->
                        when (v) {
                            // ── CHANGED: store LOOKUP/OWNER as "id::name" ──
                            is Map<*, *> -> {
                                val rid  = v["id"]?.toString() ?: ""
                                val name = v["name"]?.toString() ?: ""
                                if (rid.isNotBlank()) "$rid::$name" else name
                            }
                            is List<*>   -> v.joinToString(";")
                            else         -> v?.toString() ?: ""
                        }
                    }
                    formState.initialize(initial)
                    _uiState.value = FormUiState.Idle
                }
                .onFailure { e ->
                    _uiState.value = FormUiState.Error("Failed to load record: ${e.message}")
                }
        }
    }

    // ── Field change ──────────────────────────────────────────────────────────

    fun onFieldChange(apiName: String, value: String) {
        val field = _fields.value.firstOrNull { it.apiName == apiName } ?: return
        formState.onFieldChange(apiName, value, field)
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    fun submit() {
        val currentFields = _fields.value
        if (!formState.validateAll(currentFields)) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Submitting

            val rawPayload   = formState.toPayload(currentFields)
            val typedPayload = buildTypedPayload(rawPayload, currentFields)

            val result = if (isEditMode) {
                recordRepository.updateRecord(moduleName, recordId!!, typedPayload)
            } else {
                recordRepository.createRecord(moduleName, typedPayload)
            }

            result
                .onSuccess { _uiState.value = FormUiState.Success }
                .onFailure { e -> _uiState.value = FormUiState.Error(e.message ?: "Unknown error") }
        }
    }

    // ── Typed payload builder ─────────────────────────────────────────────────

    private fun buildTypedPayload(
        raw   : Map<String, String>,
        fields: List<FieldMetadata>,
    ): Map<String, Any> {
        val fieldMap = fields.associateBy { it.apiName }
        return raw.mapValues { (apiName, value) ->
            val type = fieldMap[apiName]?.type
            when (type) {
                FieldType.INTEGER  ->
                    value.toLongOrNull() ?: value
                FieldType.DECIMAL,
                FieldType.CURRENCY,
                FieldType.PERCENT  ->
                    value.toDoubleOrNull() ?: value
                FieldType.BOOLEAN  ->
                    value.equals("true", ignoreCase = true)
                // ── CHANGED: unwrap "id::name" → {id: "..."} map for Zoho ──
                FieldType.LOOKUP,
                FieldType.OWNER    -> {
                    val id = value.substringBefore("::")
                    when {
                        id.isNotBlank() && id != value -> mapOf("id" to id)
                        value.isNotBlank()             -> mapOf("id" to value)
                        else                           -> value
                    }
                }
                else -> value
            }
        }
    }

    // ── Lookup search (delegates to repository) ───────────────────────────────

    // ── ADDED ──
    suspend fun searchLookup(module: String, query: String): List<Pair<String, String>> =
        try {
            recordRepository.searchRecords(module, query)
        } catch (e: Exception) {
            emptyList()
        }

    fun resetState() {
        _uiState.value = FormUiState.Idle
    }

    // ── Assisted Factory ──────────────────────────────────────────────────────

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("module")   moduleName: String,
            @Assisted("recordId") recordId  : String?,
        ): RecordFormViewModel
    }
}