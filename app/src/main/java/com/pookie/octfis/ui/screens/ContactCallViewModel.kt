package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.remote.CallStateHolder
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

sealed class LogCallState {
    object Idle    : LogCallState()
    object Saving  : LogCallState()
    object Done    : LogCallState()
    data class Error(val message: String) : LogCallState()
}

class ContactCallViewModel : ViewModel() {

    private val repo = CallRepository(ZohoServiceLocator.getApiService())

    private val _logState = MutableStateFlow<LogCallState>(LogCallState.Idle)
    val logState: StateFlow<LogCallState> = _logState

    fun logCallToZoho(description: String) {
        val contactZohoId = CallStateHolder.contactZohoId
        val contactName   = CallStateHolder.contactName
        val startMillis   = CallStateHolder.callStartMillis
        val endMillis     = CallStateHolder.callEndMillis

        val durationSeconds = ((endMillis - startMillis) / 1000).coerceAtLeast(0)
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val durationStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val startTimeStr = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()
        ).format(Date(startMillis))
        Log.d("CALL_LOG_DEBUG", "whoId=$contactZohoId | name=$contactName | start=$startTimeStr | duration=$durationStr")


        viewModelScope.launch {
            _logState.value = LogCallState.Saving
            repo.createCall(
                subject       = "Outgoing call to $contactName",
                callStartTime = startTimeStr,
                duration      = durationStr,
                callType      = "Outbound",
                status        = "Completed",
                description   = description,
                ownerId       = "",
                whoId         = contactZohoId,
            ).fold(
                onSuccess = { _logState.value = LogCallState.Done },
                onFailure = { _logState.value = LogCallState.Error(it.message ?: "Failed to log call") },
            )
        }
    }

    fun resetState() { _logState.value = LogCallState.Idle }
}