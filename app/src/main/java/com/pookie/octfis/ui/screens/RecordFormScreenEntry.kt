package com.pookie.octfis.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.pookie.octfis.engine.form.RecordFormViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun RecordFormScreenEntry(
    navController : NavController,
    moduleName    : String,
    recordId      : String?  = null,
    cloneSourceId : String?  = null,
) {
    val holder  = hiltViewModel<RecordFormViewModelFactoryHolder>()

    RecordFormScreen(
        navController    = navController,
        moduleName       = moduleName,
        recordId         = recordId,
        cloneSourceId    = cloneSourceId,
        viewModelFactory = holder.factory,
    )
}

@HiltViewModel
class RecordFormViewModelFactoryHolder @Inject constructor(
    val factory: RecordFormViewModel.Factory,
) : ViewModel()