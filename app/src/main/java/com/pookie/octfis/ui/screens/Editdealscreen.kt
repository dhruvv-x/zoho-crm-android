package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.DealRepository
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Picklist options ──────────────────────────────────────────────────────────

data class DealPicklistOptions(
    val types       : List<String>              = listOf("-None-"),
    val stages      : List<String>              = listOf("-None-"),
    val leadSources : List<String>              = listOf("-None-"),
    val owners      : List<Pair<String, String>> = listOf(Pair("", "-None-")),
)

// ── Save state ────────────────────────────────────────────────────────────────

sealed class EditDealState {
    object Idle   : EditDealState()
    object Saving : EditDealState()
    object Saved  : EditDealState()
    data class Error(val message: String) : EditDealState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EditDealViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = DealRepository(api)

    private val _options        = MutableStateFlow(DealPicklistOptions())
    val options: StateFlow<DealPicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState      = MutableStateFlow<EditDealState>(EditDealState.Idle)
    val saveState: StateFlow<EditDealState> = _saveState.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fields = runCatching { api.getFields("Deals") }.getOrNull()
                val users  = runCatching { api.getUsers("AllUsers") }.getOrNull()
                val none   = listOf("-None-")
                _options.value = DealPicklistOptions(
                    types       = none + (fields?.fields?.firstOrNull { it.apiName == "Type" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    stages      = none + (fields?.fields?.firstOrNull { it.apiName == "Stage" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    leadSources = none + (fields?.fields?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    owners      = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.fullName ?: it.email ?: it.id) } ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = DealPicklistOptions()
            } finally {
                _optionsLoading.value = false
            }
        }
    }

    fun save(
        zohoId         : String,
        dealName       : String,
        accountName    : String,
        accountZohoId  : String,
        contactName    : String,
        contactZohoId  : String,
        amount         : String,
        closingDate    : String,
        type           : String,
        email          : String,
        ownerEntry     : Pair<String, String>,
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ) {
        if (dealName.isBlank()) { _saveState.value = EditDealState.Error("Deal Name is required"); return }
        viewModelScope.launch {
            _saveState.value = EditDealState.Saving
            repo.updateDeal(
                zohoId          = zohoId,
                dealName        = dealName,
                accountName     = accountName,
                accountZohoId   = accountZohoId,
                contactName     = contactName,
                contactZohoId   = contactZohoId,
                amount          = amount,
                closingDate     = closingDate,
                type            = if (type == "-None-") "" else type,
                email           = email,
                dealOwner       = ownerEntry.first,
                description     = description,
                stage           = stage,
                leadSource      = if (leadSource == "-None-") "" else leadSource,
                leadSourceDrill = leadSourceDrill,
            ).fold(
                onSuccess = { _saveState.value = EditDealState.Saved },
                onFailure = { e -> _saveState.value = EditDealState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = EditDealState.Idle }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDealScreen(
    navController: NavController,
    dealId: Int,
    vm: EditDealViewModel = viewModel(),
) {
    val deal = DealRepository.cache.firstOrNull { it.id == dealId }

    var dealName        by remember { mutableStateOf(deal?.dealName ?: "") }
    var accountName     by remember { mutableStateOf(deal?.accountName ?: "") }
    var contactName     by remember { mutableStateOf(deal?.contactName ?: "") }
    var amount          by remember { mutableStateOf(deal?.amount ?: "") }
    var closingDate     by remember { mutableStateOf(deal?.closingDate ?: "") }
    var type            by remember { mutableStateOf(deal?.type?.ifEmpty { "-None-" } ?: "-None-") }
    var email           by remember { mutableStateOf(deal?.email ?: "") }
    var selectedOwner   by remember { mutableStateOf(Pair("", deal?.dealOwner ?: "-None-")) }
    var description     by remember { mutableStateOf(deal?.description ?: "") }
    var stage           by remember { mutableStateOf(deal?.stage?.ifEmpty { "-None-" } ?: "-None-") }
    var leadSource      by remember { mutableStateOf(deal?.leadSource?.ifEmpty { "-None-" } ?: "-None-") }
    var leadSourceDrill by remember { mutableStateOf(deal?.leadSourceDrill ?: "") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val saveState      by vm.saveState.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is EditDealState.Saved -> navController.popBackStack()
            is EditDealState.Error -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Deal", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is EditDealState.Saving
                    Button(
                        onClick = {
                            if (!saving) vm.save(
                                zohoId          = deal?.zohoId ?: "",
                                dealName        = dealName,
                                accountName     = accountName,
                                accountZohoId   = deal?.accountZohoId ?: "",
                                contactName     = contactName,
                                contactZohoId   = deal?.contactZohoId ?: "",
                                amount          = amount,
                                closingDate     = closingDate,
                                type            = type,
                                email           = email,
                                ownerEntry      = selectedOwner,
                                description     = description,
                                stage           = stage,
                                leadSource      = leadSource,
                                leadSourceDrill = leadSourceDrill,
                            )
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving)
                            CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                        else
                            Text("Save", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
       containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (deal == null) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Deal not found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    EDTextField("Deal Name",    dealName,    "Deal Name")           { dealName = it }
                    EDDivider()
                    EDTextField("Account Name", accountName, "Enter Company name")  { accountName = it }
                    EDDivider()
                    EDTextField("Contact Name", contactName, "Enter Customer Name") { contactName = it }
                    EDDivider()
                    EDTextField("Amount",       amount,      "Enter Deal Amount")   { amount = it }
                    EDDivider()
                    EDTextField("Closing Date", closingDate, "YYYY-MM-DD")          { closingDate = it }
                    EDDivider()
                    EDDropdown("Type", type, options.types, optionsLoading)         { type = it }
                    EDDivider()
                    EDTextField("Email",        email,       "Enter Email ID")      { email = it }
                    EDDivider()
                    EDDropdown(
                        label   = "Deal Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    EDDivider()
                    EDTextField("Description",  description, "Short description")   { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Additional Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    EDDropdown("Stage",       stage,      options.stages,      optionsLoading) { stage = it }
                    EDDivider()
                    EDDropdown("Lead Source", leadSource, options.leadSources, optionsLoading) { leadSource = it }
                    EDDivider()
                    EDTextField("Lead Source Drill", leadSourceDrill, "Enter Source Reference") { leadSourceDrill = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun EDTextField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 13.sp) },
            singleLine    = true,
            modifier      = Modifier.weight(1f),
            colors        = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EDDropdown(label: String, value: String, options: List<String>, loading: Boolean, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { if (!loading) expanded = it },
        modifier         = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text     = value,
                    fontSize = 13.sp,
                    color    = if (value == "-None-") CrmSubtext else CrmOnSurface,
                    modifier = Modifier.weight(1f),
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text           = { Text(option, fontSize = 14.sp) },
                    onClick        = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun EDDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}