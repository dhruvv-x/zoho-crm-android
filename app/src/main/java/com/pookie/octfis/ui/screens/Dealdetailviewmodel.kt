package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.DealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StageUpdateState {
    object Idle    : StageUpdateState()
    object Saving  : StageUpdateState()
    object Success : StageUpdateState()
    data class Error(val message: String) : StageUpdateState()
}

class DealDetailViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = DealRepository(api)

    // Live deal — updated after a successful stage change
    private val _deal = MutableStateFlow<Deal?>(null)
    val deal: StateFlow<Deal?> = _deal.asStateFlow()

    // All stages from Zoho settings/fields API
    private val _stages = MutableStateFlow<List<String>>(emptyList())
    val stages: StateFlow<List<String>> = _stages.asStateFlow()

    // Stage update status
    private val _stageUpdateState = MutableStateFlow<StageUpdateState>(StageUpdateState.Idle)
    val stageUpdateState: StateFlow<StageUpdateState> = _stageUpdateState.asStateFlow()

    fun init(dealId: Int) {
        // Load deal from cache
        _deal.value = DealRepository.cache.firstOrNull { it.id == dealId }
        // Fetch stages from Zoho settings/fields
        viewModelScope.launch {
            runCatching { api.getFields("Deals") }.getOrNull()
                ?.fields
                ?.firstOrNull { it.apiName == "Stage" }
                ?.pickListValues
                ?.map { it.actualValue }
                ?.filter { it.isNotBlank() && it != "-None-" }
                ?.let { _stages.value = it }
        }
    }

    fun updateStage(newStage: String) {
        val current = _deal.value ?: return
        if (current.stage == newStage) return

        viewModelScope.launch {
            _stageUpdateState.value = StageUpdateState.Saving

            // Send ONLY the Stage field — avoids Owner/lookup field validation errors
            runCatching {
                api.updateDeal(
                    id   = current.zohoId,
                    body = mapOf("data" to listOf(mapOf("Stage" to newStage))),
                )
            }.fold(
                onSuccess = { response ->
                    val result = response.data?.firstOrNull()
                    if (result?.status == "success") {
                        _deal.value = current.copy(stage = newStage)
                        // Sync cache too
                        val idx = DealRepository.cache.indexOfFirst { it.zohoId == current.zohoId }
                        if (idx >= 0) DealRepository.cache[idx] = DealRepository.cache[idx].copy(stage = newStage)
                        _stageUpdateState.value = StageUpdateState.Success
                    } else {
                        val field = result?.details?.apiName ?: "unknown"
                        _stageUpdateState.value = StageUpdateState.Error(
                            "${result?.message ?: "Stage update failed"} [field: $field]"
                        )
                    }
                },
                onFailure = { e ->
                    _stageUpdateState.value = StageUpdateState.Error(e.message ?: "Stage update failed")
                }
            )
        }
    }

    fun resetUpdateState() {
        _stageUpdateState.value = StageUpdateState.Idle
    }
}