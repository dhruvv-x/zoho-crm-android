package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.ui.components.LookupItem
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.CrmPrimary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

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
                    _products.value = list.map { p ->
                        LookupItem(
                            zohoId   = p.id,
                            name     = p.name.orEmpty(),
                            subtitle = if ((p.unitPrice ?: 0.0) > 0.0)
                                "₹${String.format("%.2f", p.unitPrice)}"
                            else
                                p.code.orEmpty(),
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
    val nextSno  = navController.previousBackStackEntry
        ?.savedStateHandle?.get<Int>("nextItemSno") ?: 1

    var productName       by remember { mutableStateOf("") }
    var productZohoId     by remember { mutableStateOf("") }
    var materialThickness by remember { mutableStateOf("") }
    var material          by remember { mutableStateOf("") }
    var quantity          by remember { mutableStateOf("1") }
    var price             by remember { mutableStateOf("") }

    val allProducts by vm.products.collectAsState()
    val loading     by vm.loading.collectAsState()

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
                            val item = QuoteItem(
                                sNo               = nextSno,
                                productName       = productName,
                                productZohoId     = productZohoId,
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

                    // ── Inline product search ──────────────────────────────
                    InlineProductSearch(
                        value         = productName,
                        allProducts   = allProducts,
                        loading       = loading,
                        onSelect      = { item ->
                            productName   = item.name
                            productZohoId = item.zohoId
                            // auto-fill price from unit price if still empty
                            if (price.isBlank() && item.subtitle.startsWith("₹")) {
                                price = item.subtitle.removePrefix("₹").trim()
                            }
                        },
                        onClear       = {
                            productName   = ""
                            productZohoId = ""
                        },
                        onQueryChange = { query ->
                            // If user edits after selecting, clear the zohoId
                            // so we don't send a stale product ID
                            productName   = query
                            productZohoId = ""
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

// ── Inline product search field ───────────────────────────────────────────────
// Type to filter — matching results appear as a flat list below the field.
// No dialog, no popup — everything stays inline in the form.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InlineProductSearch(
    value         : String,
    allProducts   : List<LookupItem>,
    loading       : Boolean,
    onSelect      : (LookupItem) -> Unit,
    onClear       : () -> Unit,
    onQueryChange : (String) -> Unit,
) {
    // Show suggestions only when the user is actively typing and nothing is confirmed yet
    var showSuggestions by remember { mutableStateOf(false) }

    val filtered = remember(value, allProducts) {
        if (value.isBlank()) emptyList()
        else allProducts.filter {
            it.name.contains(value, ignoreCase = true) ||
                    it.subtitle.contains(value, ignoreCase = true)
        }.take(6) // cap at 6 to avoid huge inline list
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Input row ─────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = "Product Name",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(150.dp).padding(top = 14.dp),
            )

            if (loading) {
                // While products are loading show a subtle indicator
                Box(
                    modifier          = Modifier.weight(1f).padding(top = 14.dp),
                    contentAlignment  = Alignment.CenterStart,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CrmPrimary)
                        Text("Loading products…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                TextField(
                    value         = value,
                    onValueChange = { query ->
                        onQueryChange(query)
                        showSuggestions = query.isNotBlank()
                    },
                    placeholder   = {
                        Text(
                            "Type to search product",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    trailingIcon  = {
                        if (value.isNotEmpty()) {
                            IconButton(onClick = { onClear(); showSuggestions = false }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    colors        = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            }
        }

        // ── Inline suggestions ────────────────────────────────────────────
        // Rendered directly below the input row — no dialog, no z-layer popup
        if (showSuggestions && filtered.isNotEmpty()) {
            Surface(
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .zIndex(1f),
                shape         = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                color         = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
            ) {
                Column {
                    filtered.forEachIndexed { index, item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    showSuggestions = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text       = item.name,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color      = MaterialTheme.colorScheme.onSurface,
                            )
                            if (item.subtitle.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text     = item.subtitle,
                                    fontSize = 12.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (index < filtered.lastIndex)
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outline,
                                modifier  = Modifier.padding(horizontal = 8.dp),
                            )
                    }
                }
            }
        }

        // "No results" hint when user typed something but nothing matched
        if (showSuggestions && filtered.isEmpty() && value.isNotBlank() && !loading) {
            Text(
                text     = "No products found for \"$value\"",
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 166.dp, top = 4.dp, bottom = 4.dp),
            )
        }
    }
}

// ── Shared form helpers ───────────────────────────────────────────────────────

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