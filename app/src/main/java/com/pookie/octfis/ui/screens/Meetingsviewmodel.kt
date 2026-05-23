package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Meeting
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Action state ──────────────────────────────────────────────────────────────

sealed class MeetingActionState {
    object Idle    : MeetingActionState()
    object Working : MeetingActionState()
    object Done    : MeetingActionState()
    data class Error(val message: String) : MeetingActionState()
}

// ── List ──────────────────────────────────────────────────────────────────────

sealed class MeetingsUiState {
    object Loading : MeetingsUiState()
    data class Success(val meetings: List<Meeting>, val hasMore: Boolean) : MeetingsUiState()
    data class Error(val message: String) : MeetingsUiState()
}

class MeetingsViewModel : ViewModel() {

    private val repo = MeetingRepository(ZohoServiceLocator.getApiService())

    private val _uiState     = MutableStateFlow<MeetingsUiState>(MeetingsUiState.Loading)
    val uiState: StateFlow<MeetingsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionState = MutableStateFlow<MeetingActionState>(MeetingActionState.Idle)
    val actionState: StateFlow<MeetingActionState> = _actionState.asStateFlow()

    private val all         = mutableListOf<Meeting>()
    private var currentPage = 1
    private var loadingMore = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MeetingsUiState.Loading
            all.clear(); currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? MeetingsUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(q: String) {
        _searchQuery.value = q
        val s = _uiState.value as? MeetingsUiState.Success ?: return
        _uiState.value = s.copy(meetings = filter(all, q))
    }

    fun deleteMeeting(zohoId: String) {
        viewModelScope.launch {
            _actionState.value = MeetingActionState.Working
            repo.deleteMeeting(zohoId).fold(
                onSuccess = {
                    all.removeAll { it.zohoId == zohoId }
                    val s = _uiState.value as? MeetingsUiState.Success
                    if (s != null) _uiState.value = s.copy(meetings = filter(all, _searchQuery.value))
                    _actionState.value = MeetingActionState.Done
                },
                onFailure = { _actionState.value = MeetingActionState.Error(it.message ?: "Delete failed") },
            )
        }
    }

    // Keep old name as alias
    fun delete(zohoId: String) = deleteMeeting(zohoId)

    fun resetActionState() { _actionState.value = MeetingActionState.Idle }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getMeetings(page).fold(
            onSuccess = { (items, hasMore) ->
                all.addAll(items); currentPage = page
                _uiState.value = MeetingsUiState.Success(filter(all, _searchQuery.value), hasMore)
            },
            onFailure = { _uiState.value = MeetingsUiState.Error(it.message ?: "Unknown error") },
        )
        loadingMore = false
    }

    private fun filter(list: List<Meeting>, q: String): List<Meeting> {
        if (q.isBlank()) return list
        val lq = q.trim().lowercase()
        return list.filter {
            it.title.lowercase().contains(lq) ||
                    it.location.lowercase().contains(lq) ||
                    it.startDateTime.contains(lq)
        }
    }
}

// ── Detail ────────────────────────────────────────────────────────────────────

sealed class MeetingDetailUiState {
    object Loading : MeetingDetailUiState()
    data class Success(val meeting: Meeting) : MeetingDetailUiState()
    data class Error(val message: String) : MeetingDetailUiState()
}

class MeetingDetailViewModel(private val zohoId: String) : ViewModel() {

    private val repo = MeetingRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<MeetingDetailUiState>(MeetingDetailUiState.Loading)
    val uiState: StateFlow<MeetingDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MeetingDetailUiState.Loading
            repo.getMeetingById(zohoId).fold(
                onSuccess = { _uiState.value = MeetingDetailUiState.Success(it) },
                onFailure = { _uiState.value = MeetingDetailUiState.Error(it.message ?: "Error") },
            )
        }
    }

    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = MeetingDetailViewModel(zohoId) as T
    }
}

// ── Form ViewModel (unified Create + Edit, used by Meetingscreens.kt) ────────

class MeetingFormViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = MeetingRepository(api)

    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState     = MutableStateFlow<MeetingActionState>(MeetingActionState.Idle)
    val saveState: StateFlow<MeetingActionState> = _saveState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.displayName) }
            }
            _optionsLoading.value = false
        }
    }

    fun create(title: String, startDateTime: String, endDateTime: String,
               location: String, description: String, ownerId: String) {
        if (title.isBlank()) { _saveState.value = MeetingActionState.Error("Title is required"); return }
        if (startDateTime.isBlank()) { _saveState.value = MeetingActionState.Error("Start date/time is required"); return }
        viewModelScope.launch {
            _saveState.value = MeetingActionState.Working
            repo.createMeeting(title, startDateTime, endDateTime, description, location, ownerId).fold(
                onSuccess = { _saveState.value = MeetingActionState.Done },
                onFailure = { _saveState.value = MeetingActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun update(zohoId: String, title: String, startDateTime: String, endDateTime: String,
               location: String, description: String, ownerId: String) {
        if (title.isBlank()) { _saveState.value = MeetingActionState.Error("Title is required"); return }
        viewModelScope.launch {
            _saveState.value = MeetingActionState.Working
            repo.updateMeeting(zohoId, title, startDateTime, endDateTime, description, location, ownerId).fold(
                onSuccess = { _saveState.value = MeetingActionState.Done },
                onFailure = { _saveState.value = MeetingActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = MeetingActionState.Idle }
}

// ── Legacy VMs kept for any direct references ─────────────────────────────────

sealed class MeetingSaveState {
    object Idle   : MeetingSaveState()
    object Saving : MeetingSaveState()
    object Saved  : MeetingSaveState()
    data class Error(val message: String) : MeetingSaveState()
}

class CreateMeetingViewModel : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = MeetingRepository(api)
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<MeetingSaveState>(MeetingSaveState.Idle)
    val saveState: StateFlow<MeetingSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.displayName) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(title: String, startDateTime: String, endDateTime: String,
             description: String, location: String, ownerId: String) {
        if (title.isBlank()) { _saveState.value = MeetingSaveState.Error("Title is required"); return }
        viewModelScope.launch {
            _saveState.value = MeetingSaveState.Saving
            repo.createMeeting(title, startDateTime, endDateTime, description, location, ownerId).fold(
                onSuccess = { _saveState.value = MeetingSaveState.Saved },
                onFailure = { _saveState.value = MeetingSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = MeetingSaveState.Idle }
}

class EditMeetingViewModel(private val zohoId: String) : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = MeetingRepository(api)
    private val _meeting       = MutableStateFlow<Meeting?>(null)
    val meeting: StateFlow<Meeting?> = _meeting.asStateFlow()
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<MeetingSaveState>(MeetingSaveState.Idle)
    val saveState: StateFlow<MeetingSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            repo.getMeetingById(zohoId).onSuccess { _meeting.value = it }
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.displayName) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(title: String, startDateTime: String, endDateTime: String,
             description: String, location: String, ownerId: String) {
        if (title.isBlank()) { _saveState.value = MeetingSaveState.Error("Title is required"); return }
        viewModelScope.launch {
            _saveState.value = MeetingSaveState.Saving
            repo.updateMeeting(zohoId, title, startDateTime, endDateTime, description, location, ownerId).fold(
                onSuccess = { _saveState.value = MeetingSaveState.Saved },
                onFailure = { _saveState.value = MeetingSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = MeetingSaveState.Idle }
    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = EditMeetingViewModel(zohoId) as T
    }
}