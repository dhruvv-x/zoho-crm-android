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
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    navController: NavController,
    vm: CreateAccountViewModel = viewModel(),
) {
    var accountName    by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var website        by remember { mutableStateOf("") }
    var industry       by remember { mutableStateOf("-None-") }
    var gstTreatment   by remember { mutableStateOf("-None-") }
    var gstin          by remember { mutableStateOf("") }
    var leadSource     by remember { mutableStateOf("-None-") }
    var selectedOwner  by remember { mutableStateOf(Pair("", "-None-")) }
    var description    by remember { mutableStateOf("") }
    var billingStreet  by remember { mutableStateOf("") }
    var billingStreet2 by remember { mutableStateOf("") }
    var billingCity    by remember { mutableStateOf("") }
    var billingState   by remember { mutableStateOf("") }
    var billingCode    by remember { mutableStateOf("") }
    var billingCountry by remember { mutableStateOf("") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val createState    by vm.createState.collectAsState()
    val snackbarHost    = remember { SnackbarHostState() }

    LaunchedEffect(createState) {
        when (val s = createState) {
            is CreateAccountState.Saved  -> navController.popBackStack()
            is CreateAccountState.Error  -> { snackbarHost.showSnackbar(s.message); vm.resetState() }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val saving = createState is CreateAccountState.Saving
                    Button(
                        onClick = {
                            if (!saving) vm.save(
                                name           = accountName,
                                phone          = phone,
                                website        = website,
                                industry       = industry,
                                gstTreatment   = gstTreatment,
                                gstin          = gstin,
                                leadSource     = leadSource,
                                ownerEntry     = selectedOwner,
                                description    = description,
                                billingStreet  = billingStreet,
                                billingStreet2 = billingStreet2,
                                billingCity    = billingCity,
                                billingState   = billingState,
                                billingCode    = billingCode,
                                billingCountry = billingCountry,
                            )
                        },
                        enabled  = !saving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (saving)
                            CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                        else
                            Text("Save", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
       containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    CrmTextField("Account Name", accountName, "Enter Company name") { accountName = it }
                    FieldDivider()
                    CrmTextField("Phone",        phone,        "Enter Phone no")     { phone = it }
                    FieldDivider()
                    CrmTextField("Website",      website,      "www.example.com")    { website = it }
                    FieldDivider()
                    CrmDropdown("Industry",      industry,      options.industries,    optionsLoading) { industry = it }
                    FieldDivider()
                    CrmDropdown("GST Treatment", gstTreatment, options.gstTreatments, optionsLoading) { gstTreatment = it }
                    FieldDivider()
                    CrmTextField("GSTIN",        gstin,        "Enter GST Number")   { gstin = it }
                    FieldDivider()
                    CrmDropdown("Lead Source",   leadSource,   options.leadSources,   optionsLoading) { leadSource = it }
                    FieldDivider()
                    CrmDropdown(
                        label   = "Account Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    FieldDivider()
                    CrmTextField("Description",  description,  "Short description")  { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    CrmTextField("Billing Street",   billingStreet,  "Plot no, Building name") { billingStreet = it }
                    FieldDivider()
                    CrmTextField("Billing Street 2", billingStreet2, "Landmark")               { billingStreet2 = it }
                    FieldDivider()
                    CrmTextField("Billing City",     billingCity,    "Enter City Name")        { billingCity = it }
                    FieldDivider()
                    CrmTextField("Billing State",    billingState,   "Enter State")            { billingState = it }
                    FieldDivider()
                    CrmTextField("Billing Code",     billingCode,    "PIN / ZIP code")         { billingCode = it }
                    FieldDivider()
                    CrmTextField("Billing Country",  billingCountry, "Enter Country")          { billingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CrmTextField(
    label: String, value: String, placeholder: String, onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 13.sp) },
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
private fun CrmDropdown(
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
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(130.dp))
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun FieldDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}