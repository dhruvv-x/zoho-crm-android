package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.QuoteRepository
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(navController: NavController, quoteId: Int) {
    val quote = QuoteRepository.cache.firstOrNull { it.id == quoteId }

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
                    IconButton(onClick = { /* TODO: edit */ }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        if (quote == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Quote not found", color = CrmSubtext, modifier = Modifier.padding(16.dp))
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
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Subject",      quote.subject.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Name", quote.accountName.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact Name", quote.contactName.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Valid Until",  quote.validUntil.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Quote Stage",  quote.quoteStage)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",  quote.description.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Quoted Items")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(40.dp))
                        Text("Product",      fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.weight(1f))
                        Text("Qty",          fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(36.dp))
                        Text("Price",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface)
                    }
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)

                    if (quote.items.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No items", fontSize = 13.sp, color = CrmSubtext)
                        }
                    } else {
                        quote.items.forEachIndexed { index, item ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text     = "${item.sNo}",
                                    fontSize = 13.sp,
                                    color    = CrmOnSurface,
                                    modifier = Modifier.width(40.dp).padding(top = 2.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontSize = 13.sp, color = CrmOnSurface, fontWeight = FontWeight.Medium)
                                    if (item.description.isNotEmpty())
                                        Text(item.description, fontSize = 11.sp, color = CrmSubtext)
                                }
                                Text(
                                    text     = "${item.quantity}",
                                    fontSize = 13.sp,
                                    color    = CrmOnSurface,
                                    modifier = Modifier.width(36.dp),
                                )
                                Text(
                                    text       = "₹${"%.2f".format(item.price)}",
                                    fontSize   = 13.sp,
                                    color      = CrmOnSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            if (index < quote.items.lastIndex)
                                HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    HorizontalDivider(color = CrmDivider, thickness = 1.dp)

                    // Totals section
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        if (quote.subTotal > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sub Total", fontSize = 13.sp, color = CrmSubtext)
                                Text("₹${"%.2f".format(quote.subTotal)}", fontSize = 13.sp, color = CrmOnSurface)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        if (quote.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", fontSize = 13.sp, color = CrmSubtext)
                                Text("- ₹${"%.2f".format(quote.discount)}", fontSize = 13.sp, color = CrmError)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        if (quote.tax > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tax", fontSize = 13.sp, color = CrmSubtext)
                                Text("₹${"%.2f".format(quote.tax)}", fontSize = 13.sp, color = CrmOnSurface)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrmOnSurface)
                            Text(
                                text       = "₹${"%.2f".format(quote.grandTotal)}",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color      = CrmPrimary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}