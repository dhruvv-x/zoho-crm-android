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
import com.pookie.octfis.data.repository.AccountRepository
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

sealed class EditAccountState {
    object Idle   : EditAccountState()
    object Saving : EditAccountState()
    object Saved  : EditAccountState()
    data class Error(val message: String) : EditAccountState()
}

class EditAccountViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = AccountRepository(api)

    private val _options        = MutableStateFlow(PicklistOptions())
    val options: StateFlow<PicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState      = MutableStateFlow<EditAccountState>(EditAccountState.Idle)
    val saveState: StateFlow<EditAccountState> = _saveState.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fields = runCatching { api.getFields("Accounts") }.getOrNull()
                val users  = runCatching { api.getUsers("AllUsers") }.getOrNull()
                val none   = listOf("-None-")
                _options.value = PicklistOptions(
                    industries    = none + (fields?.fields?.firstOrNull { it.apiName == "Industry" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    gstTreatments = none + (fields?.fields?.firstOrNull { it.apiName == "GST_Treatment" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    leadSources   = none + (fields?.fields?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    owners        = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.fullName ?: it.email ?: it.id) } ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = PicklistOptions(
                    industries    = listOf("-None-"),
                    gstTreatments = listOf("-None-"),
                    leadSources   = listOf("-None-"),
                    owners        = listOf(Pair("", "-None-")),
                )
            } finally {
                _optionsLoading.value = false
            }
        }
    }

    fun save(
        zohoId        : String,
        name          : String,
        phone         : String,
        website       : String,
        industry      : String,
        gstTreatment  : String,
        gstin         : String,
        leadSource    : String,
        ownerEntry    : Pair<String, String>,
        description   : String,
        billingStreet : String,
        billingCity   : String,
        billingState  : String,
        billingCode   : String,
        billingCountry: String,
    ) {
        if (name.isBlank()) { _saveState.value = EditAccountState.Error("Account Name is required"); return }
        viewModelScope.launch {
            _saveState.value = EditAccountState.Saving
            repo.updateAccount(
                zohoId         = zohoId,
                name           = name,
                phone          = phone,
                website        = website,
                industry       = if (industry == "-None-") "" else industry,
                gstTreatment   = if (gstTreatment == "-None-") "" else gstTreatment,
                gstin          = gstin,
                leadSource     = if (leadSource == "-None-") "" else leadSource,
                accountOwner   = ownerEntry.first,
                description    = description,
                billingStreet  = billingStreet,
                billingCity    = billingCity,
                billingState   = billingState,
                billingCode    = billingCode,
                billingCountry = billingCountry,
            ).fold(
                onSuccess = { _saveState.value = EditAccountState.Saved },
                onFailure = { e -> _saveState.value = EditAccountState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = EditAccountState.Idle }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    navController: NavController,
    zohoId: String,
    vm: EditAccountViewModel = viewModel(),
) {
    val account = AccountRepository.cache.firstOrNull { it.zohoId == zohoId }

    var accountName    by remember { mutableStateOf(account?.name ?: "") }
    var phone          by remember { mutableStateOf(account?.phone ?: "") }
    var website        by remember { mutableStateOf(account?.website ?: "") }
    var industry       by remember { mutableStateOf(account?.industry?.ifEmpty { "-None-" } ?: "-None-") }
    var gstTreatment   by remember { mutableStateOf(account?.gstTreatment?.ifEmpty { "-None-" } ?: "-None-") }
    var gstin          by remember { mutableStateOf(account?.gstin ?: "") }
    var leadSource     by remember { mutableStateOf(account?.leadSource?.ifEmpty { "-None-" } ?: "-None-") }
    var selectedOwner  by remember { mutableStateOf(Pair("", account?.accountOwner ?: "-None-")) }
    var description    by remember { mutableStateOf(account?.description ?: "") }
    var billingStreet  by remember { mutableStateOf(account?.billingStreet ?: "") }
    var billingCity    by remember { mutableStateOf(account?.billingCity ?: "") }
    var billingState   by remember { mutableStateOf(account?.billingState ?: "") }
    var billingCode    by remember { mutableStateOf(account?.billingCode ?: "") }
    var billingCountry by remember { mutableStateOf(account?.billingCountry ?: "") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val saveState      by vm.saveState.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is EditAccountState.Saved        -> navController.popBackStack()
            is EditAccountState.Error        -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Account", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is EditAccountState.Saving
                    Button(
                        onClick = {
                            if (!saving) vm.save(
                                zohoId         = zohoId,
                                name           = accountName,
                                phone          = phone,
                                website        = website,
                                industry       = industry,
                                gstTreatment   = gstTreatment,
                                gstin          = gstin,
                                leadSource     = leadSource,
                                ownerEntry     = selectedOwner,
                                description    = description,
                                billingStreet  = billingStreet,
                                billingCity    = billingCity,
                                billingState   = billingState,
                                billingCode    = billingCode,
                                billingCountry = billingCountry,
                            )
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving)
                            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else
                            Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    EATextField("Account Name",  accountName,  "Enter Company name") { accountName = it }
                    EADivider()
                    EATextField("Phone",         phone,        "Enter Phone no")     { phone = it }
                    EADivider()
                    EATextField("Website",       website,      "www.example.com")    { website = it }
                    EADivider()
                    EADropdown("Industry",      industry,      options.industries,    optionsLoading) { industry = it }
                    EADivider()
                    EADropdown("GST Treatment", gstTreatment, options.gstTreatments, optionsLoading) { gstTreatment = it }
                    EADivider()
                    EATextField("GSTIN",         gstin,        "Enter GST Number")   { gstin = it }
                    EADivider()
                    EADropdown("Lead Source",   leadSource,   options.leadSources,   optionsLoading) { leadSource = it }
                    EADivider()
                    EADropdown(
                        label   = "Account Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    EADivider()
                    EATextField("Description",   description,  "Short description")  { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    EATextField("Billing Street",   billingStreet,  "Plot no, Building name") { billingStreet = it }
                    EADivider()
                    EATextField("Billing City",     billingCity,    "Enter City Name")        { billingCity = it }
                    EADivider()
                    EATextField("Billing State",    billingState,   "Enter State")            { billingState = it }
                    EADivider()
                    EATextField("Billing Code",     billingCode,    "PIN / ZIP code")         { billingCode = it }
                    EADivider()
                    EATextField("Billing Country",  billingCountry, "Enter Country")          { billingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EATextField(
    label: String, value: String, placeholder: String, onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = CrmSubtext.copy(alpha = 0.7f), fontSize = 13.sp) },
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
private fun EADropdown(
    label: String, value: String, options: List<String>, loading: Boolean, onSelect: (String) -> Unit,
) {
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
            Text(label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = CrmSubtext)
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
                    text    = { Text(option, fontSize = 14.sp) },
                    onClick = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun EADivider() {
    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}