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
import com.pookie.octfis.data.model.FakeData
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(navController: NavController, quoteId: Int) {
    val quote = FakeData.quotes.firstOrNull { it.id == quoteId }
        ?: FakeData.quotes.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Quote Detail",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: edit */ }) {
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint               = CrmPrimary,
                        )
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
            // ── Key Information ───────────────────────────────────────────
            SectionHeader("Key Information")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Subject",      quote.subject.ifEmpty     { "Enter Quote title" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Name", quote.accountName.ifEmpty { "Select Company Name" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact Name", quote.contactName.ifEmpty { "Select Contact Person" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Valid Until",  quote.validUntil)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Quote Stage",  quote.quoteStage)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",  quote.description.ifEmpty { "Short description" })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Quoted Items ──────────────────────────────────────────────
            SectionHeader("Quoted Items")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(48.dp))
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.weight(1f))
                        Text("PRICE",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface)
                    }
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)

                    // Item rows
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
                                modifier = Modifier.width(48.dp).padding(top = 2.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.brand,       fontSize = 11.sp, color = CrmSubtext)
                                Text(item.productName, fontSize = 13.sp, color = CrmOnSurface, fontWeight = FontWeight.Medium)
                                Text(item.description, fontSize = 11.sp, color = CrmSubtext)
                                Text("Quantity: ${String.format("%02d", item.quantity)}", fontSize = 11.sp, color = CrmSubtext)
                            }
                            Text(
                                text       = "$${String.format("%.2f", item.price)}",
                                fontSize   = 13.sp,
                                color      = CrmOnSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        if (index < quote.items.lastIndex)
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // + Add Item (read-only label, non-functional in detail view)
                    TextButton(
                        onClick  = { /* view only */ },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text       = "+ Add Item",
                            color      = CrmPrimary,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}