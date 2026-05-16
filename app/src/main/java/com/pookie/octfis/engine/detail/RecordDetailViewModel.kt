// engine/detail/RecordDetailViewModel.kt
package com.pookie.octfis.engine.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.MetadataEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val fields : List<FieldMetadata>,
        val values : Map<String, Any?>,    // raw Zoho values — display layer converts
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class RecordDetailViewModel @AssistedInject constructor(
    private val metadataEngine   : MetadataEngine,
    private val recordRepository : ZohoRecordRepository,
    @Assisted("module")   val moduleName : String,
    @Assisted("recordId") val recordId   : String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Fetch metadata + record in parallel
                val metaDeferred   = async { metadataEngine.getModuleMetadata(moduleName) }
                val recordDeferred = async { recordRepository.getRecord(moduleName, recordId) }

                val metaResult   = metaDeferred.await()
                val recordResult = recordDeferred.await()

                // Both must succeed
                val fields = metaResult.getOrElse {
                    _uiState.value = DetailUiState.Error("Failed to load fields: ${it.message}")
                    return@launch
                }
                val rawValues = recordResult.getOrElse {
                    _uiState.value = DetailUiState.Error("Failed to load record: ${it.message}")
                    return@launch
                }

                // Include ALL fields (read-only too) — detail is display-only
                val displayFields = fields.sortedBy { it.sequence }

                _uiState.value = DetailUiState.Success(
                    fields = displayFields,
                    values = rawValues,
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() = load()

    // ── Assisted Factory ──────────────────────────────────────────────────────

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("module")   moduleName: String,
            @Assisted("recordId") recordId  : String,
        ): RecordDetailViewModel
    }
}