// ui/screens/RecordDetailScreenEntry.kt
package com.pookie.octfis.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.ZohoRecordRepository
import com.pookie.octfis.engine.detail.RecordDetailViewModel
import com.pookie.octfis.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun RecordDetailScreenEntry(
    navController : NavController,
    moduleName    : String,
    recordId      : String,
) {
    val holder = hiltViewModel<RecordDetailViewModelFactoryHolder>()

    RecordDetailScreen(
        navController        = navController,
        moduleName           = moduleName,
        recordId             = recordId,
        viewModelFactory     = holder.factory,
        repository           = holder.repository,
        connectivityObserver = holder.connectivityObserver,
    )
}

@HiltViewModel
class RecordDetailViewModelFactoryHolder @Inject constructor(
    val factory              : RecordDetailViewModel.Factory,
    val repository           : ZohoRecordRepository,
    val connectivityObserver : ConnectivityObserver,
) : ViewModel()