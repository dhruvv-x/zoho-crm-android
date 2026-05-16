package com.pookie.octfis.engine.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    @Assisted("recordId") val recordId   : String?,   // null = create, non-null = edit
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
                    // Convert every value to String for FormStateManager
                    val initial = rawMap.mapValues { (_, v) ->
                        when (v) {
                            is Map<*, *> -> (v["name"] ?: v["id"] ?: "").toString()
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
        if (!formState.validateAll(currentFields)) return   // errors shown in UI

        viewModelScope.launch {
            _uiState.value = FormUiState.Submitting

            // Build payload — cast String values to correct types per FieldType
            val rawPayload  = formState.toPayload(currentFields)
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Zoho API expects typed values — integers as Int, booleans as Boolean etc.
     * Everything else stays as String.
     */
    private fun buildTypedPayload(
        raw: Map<String, String>,
        fields: List<FieldMetadata>,
    ): Map<String, Any> {
        val fieldMap = fields.associateBy { it.apiName }
        return raw.mapValues { (apiName, value) ->
            val type = fieldMap[apiName]?.type
            when (type) {
                com.pookie.octfis.engine.metadata.FieldType.INTEGER  ->
                    value.toLongOrNull() ?: value
                com.pookie.octfis.engine.metadata.FieldType.DECIMAL,
                com.pookie.octfis.engine.metadata.FieldType.CURRENCY,
                com.pookie.octfis.engine.metadata.FieldType.PERCENT  ->
                    value.toDoubleOrNull() ?: value
                com.pookie.octfis.engine.metadata.FieldType.BOOLEAN  ->
                    value.equals("true", ignoreCase = true)
                else -> value
            }
        }
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