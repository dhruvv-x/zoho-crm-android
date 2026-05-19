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
import com.pookie.octfis.data.repository.ContactRepository
import com.pookie.octfis.ui.components.LookupField
import com.pookie.octfis.ui.components.LookupItem
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

sealed class EditContactState {
    object Idle   : EditContactState()
    object Saving : EditContactState()
    object Saved  : EditContactState()
    data class Error(val message: String) : EditContactState()
}

class EditContactViewModel : ViewModel() {

    private val api  = ZohoServiceLocator.getApiService()
    private val repo = ContactRepository(api)
    private val accountRepo = AccountRepository(api)

    private val _options        = MutableStateFlow(ContactPicklistOptions())
    val options: StateFlow<ContactPicklistOptions> = _options.asStateFlow()

    private val _optionsLoading = MutableStateFlow(true)
    val optionsLoading: StateFlow<Boolean> = _optionsLoading.asStateFlow()

    private val _saveState      = MutableStateFlow<EditContactState>(EditContactState.Idle)
    val saveState: StateFlow<EditContactState> = _saveState.asStateFlow()

    // ── NEW: account lookup list ──────────────────────────────────────────────
    private val _accountItems   = MutableStateFlow<List<LookupItem>>(emptyList())
    val accountItems: StateFlow<List<LookupItem>> = _accountItems.asStateFlow()

    init { loadOptions() }

    private fun loadOptions() {
        viewModelScope.launch {
            _optionsLoading.value = true
            try {
                val fields = runCatching { api.getFields("Contacts") }.getOrNull()
                val users  = runCatching { api.getUsers("AllUsers") }.getOrNull()
                val none   = listOf("-None-")
                _options.value = ContactPicklistOptions(
                    leadSources = none + (fields?.fields?.firstOrNull { it.apiName == "Lead_Source" }
                        ?.pickListValues?.map { it.displayValue } ?: emptyList()),
                    owners      = listOf(Pair("", "-None-")) +
                            (users?.users?.map { Pair(it.id, it.fullName ?: it.email ?: it.id) } ?: emptyList()),
                )
            } catch (_: Exception) {
                _options.value = ContactPicklistOptions(
                    leadSources = listOf("-None-"),
                    owners      = listOf(Pair("", "-None-")),
                )
            } finally {
                _optionsLoading.value = false
            }

            // Load accounts for lookup (use cache if already populated)
            loadAccountLookup()
        }
    }

    // ── NEW ───────────────────────────────────────────────────────────────────
    private fun loadAccountLookup() {
        viewModelScope.launch {
            // Use whatever is already in cache; if empty, fetch page 1
            val cached = AccountRepository.cache
            if (cached.isNotEmpty()) {
                _accountItems.value = cached.map { LookupItem(it.zohoId, it.name, it.phone) }
            } else {
                runCatching { accountRepo.getAccounts(1) }
                    .getOrNull()
                    ?.getOrNull()
                    ?.first
                    ?.let { accounts ->
                        _accountItems.value = accounts.map { LookupItem(it.zohoId, it.name, it.phone) }
                    }
            }
        }
    }

    fun save(
        zohoId        : String,
        contactId     : Int,
        firstName     : String,
        lastName      : String,
        phone         : String,
        email         : String,
        accountName   : String,
        title         : String,
        department    : String,
        ownerEntry    : Pair<String, String>,
        leadSource    : String,
        description   : String,
        mailingStreet : String,
        mailingCity   : String,
        mailingState  : String,
        mailingZip    : String,
        mailingCountry: String,
    ) {
        viewModelScope.launch {
            _saveState.value = EditContactState.Saving
            repo.updateContact(
                zohoId         = zohoId,
                contactId      = contactId,
                firstName      = firstName,
                lastName       = lastName,
                phone          = phone,
                email          = email,
                accountName    = accountName,
                title          = title,
                department     = department,
                contactOwner   = ownerEntry.first,
                leadSource     = if (leadSource == "-None-") "" else leadSource,
                description    = description,
                mailingStreet  = mailingStreet,
                mailingCity    = mailingCity,
                mailingState   = mailingState,
                mailingZip     = mailingZip,
                mailingCountry = mailingCountry,
            ).fold(
                onSuccess = { _saveState.value = EditContactState.Saved },
                onFailure = { e -> _saveState.value = EditContactState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun resetState() { _saveState.value = EditContactState.Idle }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    contactId: Int,
    vm: EditContactViewModel = viewModel(),
) {
    val contact = ContactRepository.cache.firstOrNull { it.id == contactId }

    var firstName      by remember { mutableStateOf(contact?.firstName ?: "") }
    var lastName       by remember { mutableStateOf(contact?.lastName ?: "") }
    var phone          by remember { mutableStateOf(contact?.phone ?: "") }
    var email          by remember { mutableStateOf(contact?.email ?: "") }
    var accountName    by remember { mutableStateOf(contact?.accountName ?: "") }
    var title          by remember { mutableStateOf(contact?.title ?: "") }
    var department     by remember { mutableStateOf(contact?.department ?: "") }
    var selectedOwner  by remember { mutableStateOf(Pair("", contact?.contactOwner ?: "-None-")) }
    var leadSource     by remember { mutableStateOf(contact?.leadSource?.ifEmpty { "-None-" } ?: "-None-") }
    var description    by remember { mutableStateOf(contact?.description ?: "") }
    var mailingStreet  by remember { mutableStateOf(contact?.mailingStreet ?: "") }
    var mailingCity    by remember { mutableStateOf(contact?.mailingCity ?: "") }
    var mailingState   by remember { mutableStateOf(contact?.mailingState ?: "") }
    var mailingZip     by remember { mutableStateOf(contact?.mailingZip ?: "") }
    var mailingCountry by remember { mutableStateOf(contact?.mailingCountry ?: "") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val saveState      by vm.saveState.collectAsState()
    // NEW
    val accountItems   by vm.accountItems.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is EditContactState.Saved  -> navController.popBackStack()
            is EditContactState.Error  -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Contact", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = saveState is EditContactState.Saving
                    Button(
                        onClick = {
                            if (!saving) vm.save(
                                zohoId         = contact?.zohoId ?: "",
                                contactId      = contactId,
                                firstName      = firstName,
                                lastName       = lastName,
                                phone          = phone,
                                email          = email,
                                accountName    = accountName,
                                title          = title,
                                department     = department,
                                ownerEntry     = selectedOwner,
                                leadSource     = leadSource,
                                description    = description,
                                mailingStreet  = mailingStreet,
                                mailingCity    = mailingCity,
                                mailingState   = mailingState,
                                mailingZip     = mailingZip,
                                mailingCountry = mailingCountry,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    ECTextField("First Name",   firstName,   "Enter First Name")   { firstName = it }
                    ECDivider()
                    ECTextField("Last Name",    lastName,    "Enter Last Name")    { lastName = it }
                    ECDivider()
                    ECTextField("Phone",        phone,       "Enter Phone No")     { phone = it }
                    ECDivider()
                    ECTextField("Email",        email,       "Enter Email ID")     { email = it }
                    ECDivider()

                    // ── CHANGED: was ECTextField, now LookupField ─────────────
                    LookupField(
                        label       = "Account Name",
                        value       = accountName,
                        placeholder = "Select Account",
                        items       = accountItems,
                        loading     = optionsLoading && accountItems.isEmpty(),
                        onSelect    = { item -> accountName = item.name },
                    )
                    // ─────────────────────────────────────────────────────────

                    ECDivider()
                    ECTextField("Title",        title,       "Enter Job Title")    { title = it }
                    ECDivider()
                    ECTextField("Department",   department,  "Enter Department")   { department = it }
                    ECDivider()
                    ECDropdown(
                        label   = "Contact Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    ECDivider()
                    ECDropdown(
                        label   = "Lead Source",
                        value   = leadSource,
                        options = options.leadSources,
                        loading = optionsLoading,
                    ) { leadSource = it }
                    ECDivider()
                    ECTextField("Description",  description, "Short description")  { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    ECTextField("Mailing Street",  mailingStreet,  "Plot no, Building name") { mailingStreet = it }
                    ECDivider()
                    ECTextField("Mailing City",    mailingCity,    "Enter City Name")        { mailingCity = it }
                    ECDivider()
                    ECTextField("Mailing State",   mailingState,   "Enter State")            { mailingState = it }
                    ECDivider()
                    ECTextField("Mailing ZIP",     mailingZip,     "Enter ZIP Code")         { mailingZip = it }
                    ECDivider()
                    ECTextField("Mailing Country", mailingCountry, "Enter Country")          { mailingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ECTextField(
    label: String, value: String, placeholder: String, onValueChange: (String) -> Unit,
) {
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
private fun ECDropdown(
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
                    text    = { Text(option, fontSize = 14.sp) },
                    onClick = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun ECDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}