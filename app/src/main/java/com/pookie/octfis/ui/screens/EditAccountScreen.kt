package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.AccountRepository
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    navController: NavController,
    zohoId: String,
    vm: CreateAccountViewModel = viewModel(),
) {
    val account = AccountRepository.cache.firstOrNull { it.zohoId == zohoId }

    var accountName    by remember { mutableStateOf(account?.name ?: "") }
    var phone          by remember { mutableStateOf(account?.phone ?: "") }
    var website        by remember { mutableStateOf(account?.website ?: "") }
    var industry       by remember { mutableStateOf(account?.industry?.ifEmpty { "-None-" } ?: "-None-") }
    var gstTreatment   by remember { mutableStateOf(account?.gstTreatment?.ifEmpty { "-None-" } ?: "-None-") }
    var gstin          by remember { mutableStateOf(account?.gstin ?: "") }
    var leadSource     by remember { mutableStateOf(account?.leadSource?.ifEmpty { "-None-" } ?: "-None-") }
    var selectedOwner  by remember { mutableStateOf(Pair("", account?.accountOwner ?: "-None-")) }
    var description    by remember { mutableStateOf(account?.description ?: "") }
    var billingStreet  by remember { mutableStateOf(account?.billingStreet ?: "") }
    var billingCity    by remember { mutableStateOf(account?.billingCity ?: "") }
    var billingState   by remember { mutableStateOf(account?.billingState ?: "") }
    var billingCode    by remember { mutableStateOf(account?.billingCode ?: "") }
    var billingCountry by remember { mutableStateOf(account?.billingCountry ?: "") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Account", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick  = { navController.popBackStack() },
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                    EditAccountTextField("Account Name",  accountName,  "Enter Company name") { accountName = it }
                    EditAccountDivider()
                    EditAccountTextField("Phone",         phone,        "Enter Phone no")     { phone = it }
                    EditAccountDivider()
                    EditAccountTextField("Website",       website,      "www.example.com")    { website = it }
                    EditAccountDivider()
                    EditAccountDropdown("Industry",      industry,      options.industries,    optionsLoading) { industry = it }
                    EditAccountDivider()
                    EditAccountDropdown("GST Treatment", gstTreatment, options.gstTreatments, optionsLoading) { gstTreatment = it }
                    EditAccountDivider()
                    EditAccountTextField("GSTIN",         gstin,        "Enter GST Number")   { gstin = it }
                    EditAccountDivider()
                    EditAccountDropdown("Lead Source",   leadSource,   options.leadSources,   optionsLoading) { leadSource = it }
                    EditAccountDivider()
                    EditAccountDropdown(
                        label   = "Account Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    EditAccountDivider()
                    EditAccountTextField("Description",   description,  "Short description")  { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    EditAccountTextField("Billing Street",   billingStreet,  "Plot no, Building name") { billingStreet = it }
                    EditAccountDivider()
                    EditAccountTextField("Billing City",     billingCity,    "Enter City Name")        { billingCity = it }
                    EditAccountDivider()
                    EditAccountTextField("Billing State",    billingState,   "Enter State")            { billingState = it }
                    EditAccountDivider()
                    EditAccountTextField("Billing Code",     billingCode,    "PIN / ZIP code")         { billingCode = it }
                    EditAccountDivider()
                    EditAccountTextField("Billing Country",  billingCountry, "Enter Country")          { billingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditAccountTextField(
    label: String, value: String, placeholder: String, onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountDropdown(
    label: String, value: String, options: List<String>, loading: Boolean, onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { if (!loading) expanded = it },
        modifier         = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = CrmSubtext)
            } else {
                Text(
                    text     = value,
                    fontSize = 13.sp,
                    color    = if (value == "-None-") CrmSubtext else CrmOnSurface,
                    modifier = Modifier.weight(1f),
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(option, fontSize = 14.sp) },
                    onClick = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun EditAccountDivider() {
    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}