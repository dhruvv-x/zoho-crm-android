package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun DealDetailScreen(navController: NavController, dealId: Int) {
    val deal = FakeData.deals.firstOrNull { it.id == dealId }
        ?: FakeData.deals.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Deal Detail",
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
                    FormRow("Deal Name",    deal.dealName.ifEmpty    { "Deal Name" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Name", deal.accountName.ifEmpty { "Enter Company name" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact Name", deal.contactName.ifEmpty { "Enter Customer Name" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Amount",       deal.amount.ifEmpty      { "Enter Deal Amount" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Closing Date", deal.closingDate.ifEmpty { "Enter Deal Closing Date" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Type",         deal.type)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Email",        deal.email.ifEmpty       { "Enter Email ID" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Deal Owner",   deal.dealOwner)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",  deal.description.ifEmpty { "Short description" })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Additional Information ────────────────────────────────────
            SectionHeader("Additional Information")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Stage",       deal.stage)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Lead Source", deal.leadSource)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}