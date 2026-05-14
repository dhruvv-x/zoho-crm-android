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
import androidx.navigation.NavController
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.repository.QuoteRepository
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuoteScreen(navController: NavController, quoteId: Int) {
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
    var contactName    by remember { mutableStateOf(original.contactName) }
    var validUntil     by remember { mutableStateOf(original.validUntil) }
    var quoteStage     by remember { mutableStateOf(original.quoteStage) }
    var description    by remember { mutableStateOf(original.description) }
    var stageExpanded  by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showItemDialog by remember { mutableStateOf(false) }
    var editingIndex   by remember { mutableStateOf<Int?>(null) }

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
                            val subTotal = items.sumOf { it.price * it.quantity }
                            val cacheIdx = QuoteRepository.cache.indexOfFirst { it.id == quoteId }
                            if (cacheIdx >= 0) {
                                QuoteRepository.cache[cacheIdx] = original.copy(
                                    name        = subject.ifEmpty { "(No Subject)" },
                                    subject     = subject,
                                    accountName = accountName,
                                    contactName = contactName,
                                    validUntil  = validUntil,
                                    quoteStage  = quoteStage,
                                    description = description,
                                    subTotal    = subTotal,
                                    grandTotal  = subTotal,
                                    items       = items.toList(),
                                )
                            }
                            navController.popBackStack()
                        },
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    EQFormField("Subject",      subject,     "Enter Quote title")     { subject = it }
                    EQDivider()
                    EQFormField("Account Name", accountName, "Select Company Name")   { accountName = it }
                    EQDivider()
                    EQFormField("Contact Name", contactName, "Select Contact Person") { contactName = it }
                    EQDivider()

                    TextButton(
                        onClick        = { showDatePicker = true },
                        modifier       = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Valid Until", fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
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
                            Text("Quote Stage", fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
                            Text(quoteStage, fontSize = 13.sp, color = CrmOnSurface, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, tint = CrmSubtext)
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
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(40.dp))
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.weight(1f))
                        Text("PRICE",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(60.dp))
                        Spacer(Modifier.width(64.dp))
                    }
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)

                    items.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${item.sNo}", fontSize = 13.sp, color = CrmOnSurface, modifier = Modifier.width(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (item.brand.isNotEmpty()) Text(item.brand, fontSize = 11.sp, color = CrmSubtext)
                                Text(item.productName, fontSize = 13.sp, color = CrmOnSurface, fontWeight = FontWeight.Medium)
                                if (item.description.isNotEmpty()) Text(item.description, fontSize = 11.sp, color = CrmSubtext)
                                Text("Qty: ${item.quantity}", fontSize = 11.sp, color = CrmSubtext)
                            }
                            Text("₹${String.format("%.2f", item.price)}", fontSize = 13.sp, color = CrmOnSurface, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                            IconButton(onClick = { editingIndex = index; showItemDialog = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { items.removeAt(index) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (index < items.lastIndex)
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    if (items.isNotEmpty()) {
                        val subTotal = items.sumOf { it.price * it.quantity }
                        HorizontalDivider(color = CrmDivider, thickness = 1.dp)
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrmOnSurface)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EQFormField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
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

@Composable
private fun EQDivider() = HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))