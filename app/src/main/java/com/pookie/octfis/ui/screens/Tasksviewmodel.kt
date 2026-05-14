package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.Task
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Action state (used by list, detail, and form screens) ─────────────────────

sealed class TaskActionState {
    object Idle    : TaskActionState()
    object Working : TaskActionState()
    object Done    : TaskActionState()
    data class Error(val message: String) : TaskActionState()
}

// ── List ──────────────────────────────────────────────────────────────────────

sealed class TasksUiState {
    object Loading : TasksUiState()
    data class Success(val tasks: List<Task>, val hasMore: Boolean) : TasksUiState()
    data class Error(val message: String) : TasksUiState()
}

class TasksViewModel : ViewModel() {

    private val repo = TaskRepository(ZohoServiceLocator.getApiService())

    private val _uiState      = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _searchQuery  = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionState  = MutableStateFlow<TaskActionState>(TaskActionState.Idle)
    val actionState: StateFlow<TaskActionState> = _actionState.asStateFlow()

    private val all           = mutableListOf<Task>()
    private var currentPage   = 1
    private var loadingMore   = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading
            all.clear(); currentPage = 1
            fetchPage(1)
        }
    }

    fun loadNextPage() {
        if (loadingMore) return
        if ((_uiState.value as? TasksUiState.Success)?.hasMore != true) return
        viewModelScope.launch { fetchPage(currentPage + 1) }
    }

    fun setSearch(q: String) {
        _searchQuery.value = q
        val s = _uiState.value as? TasksUiState.Success ?: return
        _uiState.value = s.copy(tasks = filter(all, q))
    }

    fun deleteTask(zohoId: String) {
        viewModelScope.launch {
            _actionState.value = TaskActionState.Working
            repo.deleteTask(zohoId).fold(
                onSuccess = {
                    all.removeAll { it.zohoId == zohoId }
                    val s = _uiState.value as? TasksUiState.Success
                    if (s != null) _uiState.value = s.copy(tasks = filter(all, _searchQuery.value))
                    _actionState.value = TaskActionState.Done
                },
                onFailure = { _actionState.value = TaskActionState.Error(it.message ?: "Delete failed") },
            )
        }
    }

    // Keep old name as alias so nothing else breaks
    fun delete(zohoId: String) = deleteTask(zohoId)

    fun resetActionState() { _actionState.value = TaskActionState.Idle }

    private suspend fun fetchPage(page: Int) {
        loadingMore = true
        repo.getTasks(page).fold(
            onSuccess = { (items, hasMore) ->
                all.addAll(items); currentPage = page
                _uiState.value = TasksUiState.Success(filter(all, _searchQuery.value), hasMore)
            },
            onFailure = { _uiState.value = TasksUiState.Error(it.message ?: "Unknown error") },
        )
        loadingMore = false
    }

    private fun filter(list: List<Task>, q: String): List<Task> {
        if (q.isBlank()) return list
        val lq = q.trim().lowercase()
        return list.filter {
            it.subject.lowercase().contains(lq) ||
                    it.status.lowercase().contains(lq) ||
                    it.priority.lowercase().contains(lq) ||
                    it.dueDate.contains(lq)
        }
    }
}

// ── Detail ────────────────────────────────────────────────────────────────────

sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Success(val task: Task) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

class TaskDetailViewModel(private val zohoId: String) : ViewModel() {

    private val repo = TaskRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            repo.getTaskById(zohoId).fold(
                onSuccess = { _uiState.value = TaskDetailUiState.Success(it) },
                onFailure = { _uiState.value = TaskDetailUiState.Error(it.message ?: "Error") },
            )
        }
    }

    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = TaskDetailViewModel(zohoId) as T
    }
}

// ── Form ViewModel (unified Create + Edit, used by Taskformscreens.kt) ───────

class TaskFormViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = TaskRepository(api)

    private val _owners         = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()

    val statusList   = MutableStateFlow(listOf("Not Started", "In Progress", "Completed", "Waiting for input", "Deferred"))
    val priorityList = MutableStateFlow(listOf("High", "Highest", "Low", "Lowest", "Normal"))

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState      = MutableStateFlow<TaskActionState>(TaskActionState.Idle)
    val saveState: StateFlow<TaskActionState> = _saveState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }

    fun create(subject: String, dueDate: String, status: String, priority: String,
               description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = TaskActionState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = TaskActionState.Working
            repo.createTask(subject, dueDate, status, priority, description, ownerId).fold(
                onSuccess = { _saveState.value = TaskActionState.Done },
                onFailure = { _saveState.value = TaskActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun update(zohoId: String, subject: String, dueDate: String, status: String, priority: String,
               description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = TaskActionState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = TaskActionState.Working
            repo.updateTask(zohoId, subject, dueDate, status, priority, description, ownerId).fold(
                onSuccess = { _saveState.value = TaskActionState.Done },
                onFailure = { _saveState.value = TaskActionState.Error(it.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = TaskActionState.Idle }
}

// ── Legacy VMs kept for any direct references ─────────────────────────────────

class CreateTaskViewModel : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = TaskRepository(api)
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _statuses      = MutableStateFlow(listOf("Not Started","In Progress","Completed","Waiting for input","Deferred"))
    val statuses: StateFlow<List<String>> = _statuses.asStateFlow()
    private val _priorities    = MutableStateFlow(listOf("High","Highest","Low","Lowest","Normal"))
    val priorities: StateFlow<List<String>> = _priorities.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<TaskSaveState>(TaskSaveState.Idle)
    val saveState: StateFlow<TaskSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(subject: String, dueDate: String, status: String, priority: String,
             description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = TaskSaveState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = TaskSaveState.Saving
            repo.createTask(subject, dueDate, status, priority, description, ownerId).fold(
                onSuccess = { _saveState.value = TaskSaveState.Saved },
                onFailure = { _saveState.value = TaskSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = TaskSaveState.Idle }
}

class EditTaskViewModel(private val zohoId: String) : ViewModel() {
    private val api  = ZohoServiceLocator.getApiService()
    private val repo = TaskRepository(api)
    private val _task          = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()
    private val _owners        = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val owners: StateFlow<List<Pair<String, String>>> = _owners.asStateFlow()
    private val _statuses      = MutableStateFlow(listOf("Not Started","In Progress","Completed","Waiting for input","Deferred"))
    val statuses: StateFlow<List<String>> = _statuses.asStateFlow()
    private val _priorities    = MutableStateFlow(listOf("High","Highest","Low","Lowest","Normal"))
    val priorities: StateFlow<List<String>> = _priorities.asStateFlow()
    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()
    private val _saveState     = MutableStateFlow<TaskSaveState>(TaskSaveState.Idle)
    val saveState: StateFlow<TaskSaveState> = _saveState.asStateFlow()
    init {
        viewModelScope.launch {
            repo.getTaskById(zohoId).onSuccess { _task.value = it }
            runCatching { api.getUsers() }.getOrNull()?.users?.let { users ->
                _owners.value = listOf(Pair("", "-None-")) +
                        users.map { Pair(it.id, it.fullName ?: it.email ?: it.id) }
            }
            _optionsLoading.value = false
        }
    }
    fun save(subject: String, dueDate: String, status: String, priority: String,
             description: String, ownerId: String) {
        if (subject.isBlank()) { _saveState.value = TaskSaveState.Error("Subject is required"); return }
        viewModelScope.launch {
            _saveState.value = TaskSaveState.Saving
            repo.updateTask(zohoId, subject, dueDate, status, priority, description, ownerId).fold(
                onSuccess = { _saveState.value = TaskSaveState.Saved },
                onFailure = { _saveState.value = TaskSaveState.Error(it.message ?: "Save failed") },
            )
        }
    }
    fun resetState() { _saveState.value = TaskSaveState.Idle }
    class Factory(private val zohoId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = EditTaskViewModel(zohoId) as T
    }
}

// ── Legacy save state kept for old VM references ──────────────────────────────

sealed class TaskSaveState {
    object Idle   : TaskSaveState()
    object Saving : TaskSaveState()
    object Saved  : TaskSaveState()
    data class Error(val message: String) : TaskSaveState()
}