package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.AccountRepository
import com.pookie.octfis.data.repository.ContactRepository
import com.pookie.octfis.data.repository.DealRepository
import com.pookie.octfis.data.repository.QuoteRepository
import com.pookie.octfis.ui.components.LookupField
import com.pookie.octfis.ui.components.LookupItem
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Save State ────────────────────────────────────────────────────────────────

sealed class SaveState {
    object Idle    : SaveState()
    object Saving  : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EditQuoteViewModel : ViewModel() {

    private val api         = ZohoServiceLocator.getApiService()
    private val accountRepo = AccountRepository(api)
    private val contactRepo = ContactRepository(api)
    private val dealRepo    = DealRepository(api)

    private val _accountItems  = MutableStateFlow<List<LookupItem>>(emptyList())
    val accountItems: StateFlow<List<LookupItem>> = _accountItems.asStateFlow()

    private val _contactItems  = MutableStateFlow<List<LookupItem>>(emptyList())
    val contactItems: StateFlow<List<LookupItem>> = _contactItems.asStateFlow()

    private val _dealItems     = MutableStateFlow<List<LookupItem>>(emptyList())
    val dealItems: StateFlow<List<LookupItem>> = _dealItems.asStateFlow()

    private val _lookupLoading = MutableStateFlow(true)
    val lookupLoading: StateFlow<Boolean> = _lookupLoading.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init { loadLookups() }

    private fun loadLookups() {
        viewModelScope.launch {
            _lookupLoading.value = true

            val cachedAccounts = AccountRepository.cache
            if (cachedAccounts.isNotEmpty()) {
                _accountItems.value = cachedAccounts.map { LookupItem(it.zohoId, it.name, it.phone) }
            } else {
                runCatching { accountRepo.getAccounts(1) }
                    .getOrNull()?.getOrNull()?.first
                    ?.let { _accountItems.value = it.map { a -> LookupItem(a.zohoId, a.name, a.phone) } }
            }

            val cachedContacts = ContactRepository.cache
            if (cachedContacts.isNotEmpty()) {
                _contactItems.value = cachedContacts.map { LookupItem(it.zohoId, it.fullName, it.email) }
            } else {
                runCatching { contactRepo.getContacts(1) }
                    .getOrNull()?.getOrNull()?.first
                    ?.let { _contactItems.value = it.map { c -> LookupItem(c.zohoId, c.fullName, c.email) } }
            }

            val cachedDeals = DealRepository.cache
            if (cachedDeals.isNotEmpty()) {
                _dealItems.value = cachedDeals.map { LookupItem(it.zohoId, it.dealName, it.accountName) }
            } else {
                runCatching { dealRepo.getDeals(1) }
                    .getOrNull()?.getOrNull()?.first
                    ?.let { _dealItems.value = it.map { d -> LookupItem(d.zohoId, d.dealName, d.accountName) } }
            }

            _lookupLoading.value = false
        }
    }

    fun saveQuote(
        zohoId        : String,
        subject       : String,
        accountName   : String,
        accountZohoId : String,
        contactName   : String,
        contactZohoId : String,
        dealName      : String,
        dealZohoId    : String,
        quoteStage    : String,
        validUntil    : String,
        description   : String,
        items         : List<QuoteItem>,
    ) {
        if (_saveState.value == SaveState.Saving) return
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val repo = QuoteRepository(api)
            repo.updateQuote(
                zohoId        = zohoId,
                subject       = subject,
                accountName   = accountName,
                accountZohoId = accountZohoId,
                contactName   = contactName,
                contactZohoId = contactZohoId,
                dealName      = dealName,
                dealZohoId    = dealZohoId,
                quoteStage    = quoteStage,
                validUntil    = validUntil,
                description   = description,
                items         = items,
            ).fold(
                onSuccess = { _saveState.value = SaveState.Success },
                onFailure = { e -> _saveState.value = SaveState.Error(e.message ?: "Save failed") },
            )
        }
    }

    fun clearError() { _saveState.value = SaveState.Idle }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuoteScreen(
    navController: NavController,
    quoteId: Int,
    vm: EditQuoteViewModel = viewModel(),
) {
    val original = QuoteRepository.cache.firstOrNull { it.id == quoteId }

    if (original == null) {
        Scaffold { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                Text("Quote not found", modifier = Modifier.padding(16.dp))
            }
        }
        return
    }

    var subject        by remember { mutableStateOf(original.subject) }
    var accountName    by remember { mutableStateOf(original.accountName) }
    var accountZohoId  by remember { mutableStateOf(original.accountZohoId) }
    var contactName    by remember { mutableStateOf(original.contactName) }
    var contactZohoId  by remember { mutableStateOf(original.contactZohoId) }
    var dealName       by remember { mutableStateOf(original.dealName) }
    var dealZohoId     by remember { mutableStateOf(original.dealZohoId) }
    var validUntil     by remember { mutableStateOf(original.validUntil) }
    var quoteStage     by remember { mutableStateOf(original.quoteStage) }
    var description    by remember { mutableStateOf(original.description) }
    var stageExpanded  by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }
    var editingIndex   by remember { mutableStateOf<Int?>(null) }

    val accountItems  by vm.accountItems.collectAsState()
    val contactItems  by vm.contactItems.collectAsState()
    val dealItems     by vm.dealItems.collectAsState()
    val lookupLoading by vm.lookupLoading.collectAsState()
    val saveState     by vm.saveState.collectAsState()
    val isSaving       = saveState == SaveState.Saving

    // ── FIX: use proper SnackbarHostState ─────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save outcomes via LaunchedEffect on ViewModel state
    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Success -> {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("quoteUpdated", true)
                navController.popBackStack()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                vm.clearError()
            }
            else -> Unit
        }
    }

    val stageOptions = listOf("Draft", "Delivered", "On Hold", "Confirmed", "Closed Accepted", "Closed Lost")
    val items = remember { mutableStateListOf<QuoteItem>().also { it.addAll(original.items) } }

    // ── Date Picker ───────────────────────────────────────────────────────────
    val initialMillis = runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(original.validUntil)?.time
    }.getOrNull() ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        validUntil = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    // ── Item Dialog ───────────────────────────────────────────────────────────
    if (showItemDialog) {
        val editing = editingIndex?.let { items.getOrNull(it) }
        var dBrand  by remember(editingIndex) { mutableStateOf(editing?.brand ?: "") }
        var dName   by remember(editingIndex) { mutableStateOf(editing?.productName?.takeIf { it != "Product name" } ?: "") }
        var dDesc   by remember(editingIndex) { mutableStateOf(editing?.description ?: "") }
        var dQty    by remember(editingIndex) { mutableStateOf((editing?.quantity ?: 1).toString()) }
        var dPrice  by remember(editingIndex) { mutableStateOf(if ((editing?.price ?: 0.0) == 0.0) "" else (editing?.price ?: 0.0).toString()) }

        AlertDialog(
            onDismissRequest = { showItemDialog = false; editingIndex = null },
            title = { Text(if (editing != null) "Edit Item" else "Add Item", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dBrand, onValueChange = { dBrand = it }, label = { Text("Brand") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dName,  onValueChange = { dName = it },  label = { Text("Product Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dDesc,  onValueChange = { dDesc = it },  label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dQty,   onValueChange = { dQty = it },   label = { Text("Quantity") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = dPrice, onValueChange = { dPrice = it }, label = { Text("Price") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty   = dQty.toIntOrNull() ?: 1
                    val price = dPrice.toDoubleOrNull() ?: 0.0
                    val idx   = editingIndex
                    if (idx != null) {
                        items[idx] = items[idx].copy(brand = dBrand, productName = dName.ifEmpty { "Product name" }, description = dDesc, quantity = qty, price = price)
                    } else {
                        val nextId = (items.maxOfOrNull { it.sNo } ?: 0) + 1
                        items.add(QuoteItem(nextId, brand = dBrand, productName = dName.ifEmpty { "Product name" }, description = dDesc, quantity = qty, price = price))
                    }
                    showItemDialog = false; editingIndex = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showItemDialog = false; editingIndex = null }) { Text("Cancel") } },
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        // ── FIX: use SnackbarHost with SnackbarHostState, not raw Snackbar ────
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Quote", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            vm.saveQuote(
                                zohoId        = original.zohoId,
                                subject       = subject,
                                accountName   = accountName,
                                accountZohoId = accountZohoId,
                                contactName   = contactName,
                                contactZohoId = contactZohoId,
                                dealName      = dealName,
                                dealZohoId    = dealZohoId,
                                quoteStage    = quoteStage,
                                validUntil    = validUntil,
                                description   = description,
                                items         = items.toList(),
                            )
                        },
                        enabled  = !isSaving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (isSaving)
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.surface,
                            )
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    EQFormField("Subject", subject, "Enter Quote title") { subject = it }
                    EQDivider()

                    LookupField(
                        label       = "Account Name",
                        value       = accountName,
                        placeholder = "Select Account",
                        items       = accountItems,
                        loading     = lookupLoading && accountItems.isEmpty(),
                        onSelect    = { item ->
                            accountName   = item.name
                            accountZohoId = item.zohoId
                        },
                    )
                    EQDivider()

                    LookupField(
                        label       = "Contact Name",
                        value       = contactName,
                        placeholder = "Select Contact",
                        items       = contactItems,
                        loading     = lookupLoading && contactItems.isEmpty(),
                        onSelect    = { item ->
                            contactName   = item.name
                            contactZohoId = item.zohoId
                        },
                    )
                    EQDivider()

                    LookupField(
                        label       = "Deal",
                        value       = dealName,
                        placeholder = "Link a Deal (optional)",
                        items       = dealItems,
                        loading     = lookupLoading && dealItems.isEmpty(),
                        onSelect    = { item ->
                            dealName   = item.name
                            dealZohoId = item.zohoId
                        },
                    )
                    EQDivider()

                    TextButton(
                        onClick        = { showDatePicker = true },
                        modifier       = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Valid Until", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
                            Text(
                                text     = validUntil.ifEmpty { "Select date" },
                                fontSize = 13.sp,
                                color    = if (validUntil.isEmpty()) CrmSubtext.copy(alpha = 0.7f) else CrmOnSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    EQDivider()

                    ExposedDropdownMenuBox(expanded = stageExpanded, onExpandedChange = { stageExpanded = !stageExpanded }) {
                        Row(
                            modifier          = Modifier.fillMaxWidth().menuAnchor().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Quote Stage", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
                            Text(quoteStage, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ExposedDropdownMenu(expanded = stageExpanded, onDismissRequest = { stageExpanded = false }) {
                            stageOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option, fontSize = 13.sp) }, onClick = { quoteStage = option; stageExpanded = false })
                            }
                        }
                    }
                    EQDivider()
                    EQFormField("Description", description, "Short description") { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Quoted Items")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(40.dp))
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text("PRICE",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp))
                        Spacer(Modifier.width(64.dp))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    items.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${item.sNo}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (item.brand.isNotEmpty()) Text(item.brand, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(item.productName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                if (item.description.isNotEmpty()) Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Qty: ${item.quantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹${String.format("%.2f", item.price)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                            IconButton(onClick = { editingIndex = index; showItemDialog = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { items.removeAt(index) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (index < items.lastIndex)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    if (items.isNotEmpty()) {
                        val subTotal = items.sumOf { it.price * it.quantity }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("₹${String.format("%.2f", subTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrmPrimary)
                        }
                    }

                    TextButton(onClick = { editingIndex = null; showItemDialog = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text("+ Add Item", color = CrmPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EQFormField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
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

@Composable
private fun EQDivider() = HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))