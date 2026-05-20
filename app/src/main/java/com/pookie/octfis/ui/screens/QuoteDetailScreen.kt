package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.QuoteRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

class QuoteDetailViewModel : ViewModel() {
    private val repo = QuoteRepository(ZohoServiceLocator.getApiService())

    private val _quote   = MutableStateFlow<Quote?>(null)
    private val _loading = MutableStateFlow(true)
    private val _error   = MutableStateFlow<String?>(null)

    val quote:   StateFlow<Quote?>   = _quote.asStateFlow()
    val loading: StateFlow<Boolean>  = _loading.asStateFlow()
    val error:   StateFlow<String?>  = _error.asStateFlow()

    fun load(zohoId: String) {
        if (zohoId.isBlank()) { _loading.value = false; return }
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            repo.getQuoteById(zohoId)
                .onSuccess { _quote.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    navController: NavController,
    quoteId: Int,
    vm: QuoteDetailViewModel = viewModel(),
) {
    val zohoId  = remember { QuoteRepository.cache.firstOrNull { it.id == quoteId }?.zohoId.orEmpty() }
    val quote   by vm.quote.collectAsState()
    val loading by vm.loading.collectAsState()
    val error   by vm.error.collectAsState()

    // Initial load
    LaunchedEffect(zohoId) { vm.load(zohoId) }

    // Re-fetch when returning from EditQuoteScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val quoteUpdated = savedStateHandle
        ?.getStateFlow("quoteUpdated", false)
        ?.collectAsState()

    LaunchedEffect(quoteUpdated?.value) {
        if (quoteUpdated?.value == true) {
            savedStateHandle?.set("quoteUpdated", false)
            vm.load(zohoId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = quote?.subject?.ifEmpty { "Quote Detail" } ?: "Quote Detail",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.EditQuote.createRoute(quoteId))
                    }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text(
                    text     = error ?: "Error",
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                quote == null -> Text(
                    text     = "Quote not found",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                else -> QuoteDetailContent(quote!!)
            }
        }
    }
}

@Composable
private fun QuoteDetailContent(quote: Quote) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader("Key Information")
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                FormRow("Subject",      quote.subject)
                FormRow("Account",      quote.accountName)
                FormRow("Contact",      quote.contactName)
                FormRow("Deal",         quote.dealName)
                FormRow("Valid Until",  quote.validUntil)
                FormRow("Stage",        quote.quoteStage)
                FormRow("Description",  quote.description)
            }
        }

        if (quote.items.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionHeader("Quoted Items")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(40.dp))
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("PRICE",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                    quote.items.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${item.sNo}", fontSize = 13.sp, modifier = Modifier.width(40.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    if (item.materialThickness.isNotEmpty())
                                        Text(item.materialThickness, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.material.isNotEmpty())
                                        Text(item.material, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Qty: ${item.quantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                "₹${String.format("%.2f", item.price)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = CrmOnSurface,
                                modifier = Modifier.width(80.dp),
                            )
                        }
                        if (index < quote.items.lastIndex)
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    val grandTotal = quote.items.sumOf { it.price * it.quantity }
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", grandTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrmPrimary)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}