package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.AppEvents
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.ActivityRepository
import com.pookie.octfis.data.repository.DashboardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {

    private val repo = ActivityRepository(ZohoServiceLocator.getApiService())

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
        // Auto-reload whenever a call is successfully logged from anywhere in the app
        AppEvents.callLogged
            .onEach { load() }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            repo.getDashboardData().fold(
                onSuccess = { _uiState.value = DashboardUiState.Success(it) },
                onFailure = { _uiState.value = DashboardUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }
}