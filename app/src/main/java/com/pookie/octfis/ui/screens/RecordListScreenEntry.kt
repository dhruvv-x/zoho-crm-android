// ui/screens/RecordListScreenEntry.kt
package com.pookie.octfis.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.pookie.octfis.engine.list.RecordListViewModel
import com.pookie.octfis.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun RecordListScreenEntry(
    navController : NavController,
    moduleName    : String,
    primaryField  : String  = "Name",
    secondaryField: String? = null,
    avatarField   : String? = null,
    showBackButton: Boolean = false,
    onRecordClick : (zohoId: String) -> Unit = {},
) {
    val holder    = hiltViewModel<RecordListViewModelFactoryHolder>()
    val viewModel = remember(moduleName) { holder.factory.create(moduleName) }

    RecordListScreen(
        navController        = navController,
        moduleName           = moduleName,
        viewModel            = viewModel,
        connectivityObserver = holder.connectivityObserver,
        primaryField         = primaryField,
        secondaryField       = secondaryField,
        avatarField          = avatarField,
        showBackButton       = showBackButton,
        onRecordClick        = onRecordClick,
    )
}

@HiltViewModel
class RecordListViewModelFactoryHolder @Inject constructor(
    val factory              : RecordListViewModel.Factory,
    val connectivityObserver : ConnectivityObserver,
) : ViewModel()