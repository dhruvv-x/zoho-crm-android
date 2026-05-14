package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(navController: NavController, zohoId: String) {

    val vm: AccountDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AccountDetailViewModel(zohoId) as T
        }
    )
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? AccountDetailUiState.Success)
                        ?.account?.name?.ifEmpty { "Account Detail" } ?: "Account Detail"
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.EditAccount.createRoute(zohoId))
                    }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = CrmBackground,
    ) { padding ->
        when (val s = uiState) {

            is AccountDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CrmPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text("Fetching from Zoho CRM…", color = CrmSubtext, fontSize = 13.sp)
                    }
                }
            }

            is AccountDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, null, tint = CrmError, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(s.message, color = CrmSubtext, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { vm.load() },
                            colors  = ButtonDefaults.buttonColors(containerColor = CrmPrimary)
                        ) { Text("Retry") }
                    }
                }
            }

            is AccountDetailUiState.Success -> {
                val account = s.account

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
                            FormRow("Industry",      account.industry.ifEmpty { "—" })
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("GST Treatment", account.gstTreatment.ifEmpty { "—" })
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("GSTIN",         account.gstin.ifEmpty { "—" })
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Lead Source",   account.leadSource.ifEmpty { "—" })
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            FormRow("Account Owner", account.accountOwner.ifEmpty { "—" })
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
    }
}