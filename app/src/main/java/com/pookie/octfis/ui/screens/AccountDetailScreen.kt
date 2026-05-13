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
import com.pookie.octfis.data.repository.AccountRepository
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(navController: NavController, accountId: Int) {
    // Try real cached data first, fall back to FakeData
    val account = AccountRepository.cache.firstOrNull { it.id == accountId }
        ?: FakeData.accounts.firstOrNull { it.id == accountId }
        ?: FakeData.accounts.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = account.name.ifEmpty { "Account Detail" },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Account Name",  account.name.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account No",    account.accountNo.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Phone",         account.phone.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Website",       account.website.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Industry",      account.industry)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("GST Treatment", account.gstTreatment)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("GSTIN",         account.gstin.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Lead Source",   account.leadSource)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Owner", account.accountOwner)
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",   account.description.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormRow("Billing Street",  account.billingStreet.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Billing City",    account.billingCity.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Billing State",   account.billingState.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Billing Code",    account.billingCode.ifEmpty { "—" })
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Billing Country", account.billingCountry.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}