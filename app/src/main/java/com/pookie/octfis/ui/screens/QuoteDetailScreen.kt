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
import androidx.navigation.NavController
import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.QuoteRepository
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(navController: NavController, quoteId: Int) {

    // zohoId from cache (fast lookup)
    val zohoId = remember { QuoteRepository.cache.firstOrNull { it.id == quoteId }?.zohoId.orEmpty() }

    // Full quote fetched from API (includes Quoted_Items)
    var quote by remember { mutableStateOf<Quote?>(QuoteRepository.cache.firstOrNull { it.id == quoteId }) }
    var loading by remember { mutableStateOf(true) }
    var error   by remember { mutableStateOf<String?>(null) }

    val repo = remember { QuoteRepository(ZohoServiceLocator.getApiService()) }

    LaunchedEffect(zohoId) {
        if (zohoId.isBlank()) { loading = false; return@LaunchedEffect }
        repo.getQuoteById(zohoId)
            .onSuccess { quote = it; loading = false }
            .onFailure { error = it.message; loading = false }
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
                    IconButton(onClick = { navController.navigate(Screen.EditQuote.createRoute(quoteId)) }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        when {
            loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CrmPrimary)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(error ?: "Error", color = CrmError, modifier = Modifier.padding(16.dp))
                }
            }
            quote == null -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    Text("Quote not found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            }
            else -> {
                val q = quote!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    SectionHeader("Key Information")
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                        Column {
                            FormRow("Subject",      q.subject.ifEmpty { "—" })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Account Name", q.accountName.ifEmpty { "—" })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Contact Name", q.contactName.ifEmpty { "—" })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Valid Until",  q.validUntil.ifEmpty { "—" })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Quote Stage",  q.quoteStage)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Description",  q.description.ifEmpty { "—" })
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    SectionHeader("Quoted Items")
                    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("S.NO",    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(40.dp))
                                Text("Product", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                Text("Qty",     fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(36.dp))
                                Text("Price",   fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                            if (q.items.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No items", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                q.items.forEachIndexed { index, item ->
                                    Row(
                                        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Text("${item.sNo}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(40.dp).padding(top = 2.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.productName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                            if (item.description.isNotEmpty())
                                                Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("${item.quantity}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(36.dp))
                                        Text("₹${"%.2f".format(item.price)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    }
                                    if (index < q.items.lastIndex)
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }

                            if (q.items.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                    if (q.subTotal > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Sub Total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("₹${"%.2f".format(q.subTotal)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    if (q.discount > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Discount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("- ₹${"%.2f".format(q.discount)}", fontSize = 13.sp, color = CrmError)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    if (q.tax > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Tax", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("₹${"%.2f".format(q.tax)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("₹${"%.2f".format(q.grandTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrmPrimary)
                                    }
                                }
                            }
                        } // end if (q.items.isNotEmpty())
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}