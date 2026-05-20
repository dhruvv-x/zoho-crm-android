package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.pookie.octfis.ui.components.LookupField
import com.pookie.octfis.ui.components.LookupItem
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.CrmPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────
// Fetches products from Zoho CRM Products module for the lookup field

class AddQuoteItemViewModel : ViewModel() {

    private val api = ZohoServiceLocator.getApiService()

    private val _products = MutableStateFlow<List<LookupItem>>(emptyList())
    val products: StateFlow<List<LookupItem>> = _products.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _loading.value = true
            runCatching { api.getProducts() }
                .getOrNull()
                ?.data
                ?.let { list ->
                    _products.value = list.map { product ->
                        LookupItem(
                            zohoId   = product.id,
                            name     = product.name.orEmpty(),
                            subtitle = if ((product.unitPrice ?: 0.0) > 0.0)
                                "₹${String.format("%.2f", product.unitPrice)}"
                            else
                                product.code.orEmpty(),
                        )
                    }
                }
            _loading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteItemScreen(
    navController: NavController,
    vm: AddQuoteItemViewModel = viewModel(),
) {
    val nextSno = navController.previousBackStackEntry
        ?.savedStateHandle?.get<Int>("nextItemSno") ?: 1

    // Product selected via lookup — stores both display name and Zoho ID
    var productName       by remember { mutableStateOf("") }
    var productZohoId     by remember { mutableStateOf("") }
    var materialThickness by remember { mutableStateOf("") }
    var material          by remember { mutableStateOf("") }
    var quantity          by remember { mutableStateOf("1") }
    var price             by remember { mutableStateOf("") }

    val products by vm.products.collectAsState()
    val loading  by vm.loading.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Add Item", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // FIXED: QuoteItem is now @Parcelize so SavedStateHandle won't crash
                            val item = QuoteItem(
                                sNo               = nextSno,
                                productName       = productName,
                                productZohoId     = productZohoId,   // pass Zoho product ID
                                materialThickness = materialThickness,
                                material          = material,
                                quantity          = quantity.toIntOrNull() ?: 1,
                                price             = price.toDoubleOrNull() ?: 0.0,
                            )
                            navController.previousBackStackEntry
                                ?.savedStateHandle?.set("newQuoteItem", item)
                            navController.popBackStack()
                        },
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Item Details")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    // S.NO — read only
                    AqiFormField(
                        label         = "S.NO",
                        value         = nextSno.toString(),
                        placeholder   = "",
                        enabled       = false,
                        onValueChange = {},
                    )
                    AqiDivider()

                    // FIXED: Product Name is now a LookupField backed by Zoho Products module
                    LookupField(
                        label       = "Product Name",
                        value       = productName,
                        placeholder = "Search product",
                        items       = products,
                        loading     = loading,
                        onSelect    = { item ->
                            productName   = item.name
                            productZohoId = item.zohoId
                            // Auto-fill price from product's unit price if not set yet
                            if (price.isBlank() && item.subtitle.startsWith("₹")) {
                                price = item.subtitle.removePrefix("₹").trim()
                            }
                        },
                    )
                    AqiDivider()

                    AqiFormField(
                        label         = "Material Thickness",
                        value         = materialThickness,
                        placeholder   = "e.g. 2mm",
                        onValueChange = { materialThickness = it },
                    )
                    AqiDivider()
                    AqiFormField(
                        label         = "Material",
                        value         = material,
                        placeholder   = "e.g. Steel",
                        onValueChange = { material = it },
                    )
                    AqiDivider()
                    AqiFormField(
                        label         = "Quantity",
                        value         = quantity,
                        placeholder   = "1",
                        keyboardType  = KeyboardType.Number,
                        onValueChange = { quantity = it },
                    )
                    AqiDivider()
                    AqiFormField(
                        label         = "List Price (₹)",
                        value         = price,
                        placeholder   = "0.00",
                        keyboardType  = KeyboardType.Decimal,
                        onValueChange = { price = it },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AqiFormField(
    label         : String,
    value         : String,
    placeholder   : String,
    enabled       : Boolean = true,
    keyboardType  : KeyboardType = KeyboardType.Text,
    onValueChange : (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(150.dp).padding(top = 16.dp),
        )
        TextField(
            value           = value,
            onValueChange   = onValueChange,
            enabled         = enabled,
            placeholder     = {
                Text(
                    placeholder,
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            singleLine      = true,
            modifier        = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors          = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun AqiDivider() = HorizontalDivider(
    color     = MaterialTheme.colorScheme.outline,
    thickness = 0.5.dp,
    modifier  = Modifier.padding(horizontal = 16.dp),
)