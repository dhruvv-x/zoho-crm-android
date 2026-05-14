package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.CrmCall
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Action state ──────────────────────────────────────────────────────────────

sealed class CallActionState {
    object Idle    : CallActionState()
    object Working : CallActionState()
    object Done    : CallActionState()
    data class Error(val message: String) : CallActionState()
}

// ── List ──────────────────────────────────────────────────────────────────────

sealed class CallsUiState {
    object Loading : CallsUiState()
    data class Success(val calls: List<CrmCall>, val hasMore: Boolean) : CallsUiState()
    data class Error(val message: String) : CallsUiState()
}

class CallsViewModel : ViewModel() {

    private val repo = CallRepository(ZohoServiceLocator.getApiService())

    private val _uiState     = MutableStateFlow<CallsUiState>(CallsUiState.Loading)
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionState = MutableStateFlow<CallActionState>(CallActionState.Idle)
    val actionState: StateFlow<CallActionState> = _actionState.asStateFlow()

    private val all         = mutableListOf<CrmCall>()
    private var currentPage = 1
    private var loadingMore = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CallsUiState.Loading
            all.clear(); currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? CallsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(q: String) {
        _searchQuery.value = q
        val s = _uiState.value as? CallsUiState.Success ?: return
        _uiState.value = s.copy(calls = filter(all, q))
    }

    fun deleteCall(zohoId: String) {
        viewModelScope.launch {
            _actionState.value = CallActionState.Working
            repo.deleteCall(zohoId).fold(
                onSuccess = {
                    all.removeAll { it.zohoId == zohoId }
                    val s = _uiState.value as? CallsUiState.Success
                    if (s != null) _uiState.value = s.copy(calls = filter(all, _searchQuery.value))
                    _actionState.value = CallActionState.Done
                },
                onFailure = { _actionState.value = CallActionState.Error(it.message ?: "Delete failed") },
            )
        }
    }

    // Keep old name as alias
    fun delete(zohoId: String) = deleteCall(zohoId)

    fun resetActionState() { _actionState.value = CallActionState.Idle }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getCalls(page).fold(
            onSuccess = { (items, hasMore) ->
                all.addAll(items); currentPage = page
                _uiState.value = CallsUiState.Success(filter(all, _searchQuery.value), hasMore)
            },
            onFailure = { _uiState.value = CallsUiState.Error(it.message ?: "Unknown error") },
        )
        loadingMore = false
    }

    private fun filter(list: List<CrmCall>, q: String): List<CrmCall> {
        if (q.isBlank()) return list
        val lq = q.trim().lowercase()
        return list.filter {
            it.subject.lowercase().contains(lq) ||
                    it.status.lowercase().contains(lq) ||
                    it.callType.lowercase().contains(lq) ||
                    it.contactName.lowercase().contains(lq)
        }
    }
}

// ── Detail ────────────────────────────────────────────────────────────────────

sealed class CallDetailUiState {
    object Loading : CallDetailUiState()
    data class Success(val call: CrmCall) : CallDetailUiState()
    data class Error(val message: String) : CallDetailUiState()
}

class CallDetailViewModel(private val zohoId: String) : ViewModel() {

    private val repo = CallRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<CallDetailUiState>(CallDetailUiState.Loading)
    val uiState: StateFlow<CallDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CallDetailUiState.Loading
            repo.getCallById(zohoId).fold(
                onSuccess = { _uiState.value = CallDetailUiState.Success(it) },
                onFailure = { _uiState.value = CallDetailUiState.Error(it.message ?: "Error") },
            )
        }
    }

    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = CallDetailViewModel(zohoId) as T
    }
}

// ── Form ViewModel (unified Create + Edit, used by Callscreens.kt) ────────────

class CallFormViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = CallRepository(api)

    val callTypes  = listOf("Outbound", "Inbound")
    val statusList = listOf("Scheduled", "Completed", "Cancelled")

    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState     = MutableStateFlow<CallActionState>(CallActionState.Idle)
    val saveState: StateFlow<CallActionState> = _saveState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }

    fun create(subject: String, callStartTime: String, duration: String, callType: String,
               status: String, description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = CallActionState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = CallActionState.Working
            repo.createCall(subject, callStartTime, duration, callType, status, description, ownerId).fold(
                onSuccess = { _saveState.value = CallActionState.Done },
                onFailure = { _saveState.value = CallActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun update(zohoId: String, subject: String, callStartTime: String, duration: String,
               callType: String, status: String, description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = CallActionState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = CallActionState.Working
            repo.updateCall(zohoId, subject, callStartTime, duration, callType, status, description, ownerId).fold(
                onSuccess = { _saveState.value = CallActionState.Done },
                onFailure = { _saveState.value = CallActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = CallActionState.Idle }
}

// ── Legacy VMs kept for any direct references ─────────────────────────────────

sealed class CallSaveState {
    object Idle   : CallSaveState()
    object Saving : CallSaveState()
    object Saved  : CallSaveState()
    data class Error(val message: String) : CallSaveState()
}

class CreateCallViewModel : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = CallRepository(api)
    val callTypes    = listOf("Outbound", "Inbound")
    val callStatuses = listOf("Scheduled", "Completed", "Cancelled")
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<CallSaveState>(CallSaveState.Idle)
    val saveState: StateFlow<CallSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(subject: String, callStartTime: String, duration: String, callType: String,
             status: String, description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = CallSaveState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = CallSaveState.Saving
            repo.createCall(subject, callStartTime, duration, callType, status, description, ownerId).fold(
                onSuccess = { _saveState.value = CallSaveState.Saved },
                onFailure = { _saveState.value = CallSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = CallSaveState.Idle }
}

class EditCallViewModel(private val zohoId: String) : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = CallRepository(api)
    val callTypes    = listOf("Outbound", "Inbound")
    val callStatuses = listOf("Scheduled", "Completed", "Cancelled")
    private val _call          = MutableStateFlow<CrmCall?>(null)
    val call: StateFlow<CrmCall?> = _call.asStateFlow()
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<CallSaveState>(CallSaveState.Idle)
    val saveState: StateFlow<CallSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            repo.getCallById(zohoId).onSuccess { _call.value = it }
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(subject: String, callStartTime: String, duration: String, callType: String,
             status: String, description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = CallSaveState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = CallSaveState.Saving
            repo.updateCall(zohoId, subject, callStartTime, duration, callType, status, description, ownerId).fold(
                onSuccess = { _saveState.value = CallSaveState.Saved },
                onFailure = { _saveState.value = CallSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = CallSaveState.Idle }
    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = EditCallViewModel(zohoId) as T
    }
}