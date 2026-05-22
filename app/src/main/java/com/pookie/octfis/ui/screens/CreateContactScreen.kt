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
import com.pookie.octfis.ui.components.LookupField
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContactScreen(
    navController: NavController,
    vm: CreateContactViewModel = viewModel(),
) {
    var firstName      by remember { mutableStateOf("") }
    var lastName       by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var mobile         by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var accountName    by remember { mutableStateOf("") }
    var accountZohoId  by remember { mutableStateOf("") }
    var title          by remember { mutableStateOf("") }
    var department     by remember { mutableStateOf("") }
    var selectedOwner  by remember { mutableStateOf(Pair("", "-None-")) }
    var leadSource     by remember { mutableStateOf("-None-") }
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
    val accountItems   by vm.accountItems.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createState) {
        when (val s = createState) {
            is CreateContactState.Saved -> navController.popBackStack()
            is CreateContactState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                vm.resetState()
            }
            else -> Unit
        }
    }

    val isSaving = createState is CreateContactState.Saving

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Contact", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            vm.save(
                                firstName      = firstName,
                                lastName       = lastName,
                                phone          = phone,
                                mobile         = mobile,
                                email          = email,
                                accountName    = accountName,
                                accountZohoId  = accountZohoId,
                                title          = title,
                                department     = department,
                                ownerEntry     = selectedOwner,
                                leadSource     = leadSource,
                                description    = description,
                                mailingStreet  = billingStreet,
                                mailingStreet2 = billingStreet2,
                                mailingCity    = billingCity,
                                mailingState   = billingState,
                                mailingZip     = billingCode,
                                mailingCountry = billingCountry,
                            )
                        },
                        enabled  = !isSaving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.surface,
                            )
                        } else {
                            Text("Save", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.SemiBold)
                        }
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
                    ContactTextField("First Name",  firstName,  "Enter First Name")  { firstName = it }
                    ContactDivider()
                    ContactTextField("Last Name",   lastName,   "Enter Last Name")   { lastName = it }
                    ContactDivider()
                    ContactTextField("Phone",       phone,      "Enter Phone No")    { phone = it }
                    ContactDivider()
                    ContactTextField("Mobile",      mobile,     "Enter Mobile No")   { mobile = it }
                    ContactDivider()
                    ContactTextField("Email",       email,      "Enter Email ID")    { email = it }
                    ContactDivider()

                    LookupField(
                        label       = "Account Name",
                        value       = accountName,
                        placeholder = "Select Account",
                        items       = accountItems,
                        loading     = optionsLoading && accountItems.isEmpty(),
                        onSelect    = { item ->
                            accountName   = item.name
                            accountZohoId = item.zohoId
                        },
                    )

                    ContactDivider()
                    ContactTextField("Title",       title,      "Enter Job Title")   { title = it }
                    ContactDivider()
                    ContactTextField("Department",  department, "Enter Department")  { department = it }
                    ContactDivider()
                    ContactDropdown(
                        label   = "Contact Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    ContactDivider()
                    ContactDropdown(
                        label   = "Lead Source",
                        value   = leadSource,
                        options = options.leadSources,
                        loading = optionsLoading,
                    ) { leadSource = it }
                    ContactDivider()
                    ContactTextField("Description", description, "Short description") { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    ContactTextField("Billing Street",   billingStreet,  "Plot no, Building name") { billingStreet = it }
                    ContactDivider()
                    ContactTextField("Billing Street 2", billingStreet2, "Landmark")               { billingStreet2 = it }
                    ContactDivider()
                    ContactTextField("Billing City",     billingCity,    "Enter City Name")        { billingCity = it }
                    ContactDivider()
                    ContactTextField("Billing State",    billingState,   "Enter State")            { billingState = it }
                    ContactDivider()
                    ContactTextField("Billing Code",     billingCode,    "Enter Postal Code")      { billingCode = it }
                    ContactDivider()
                    ContactTextField("Billing Country",  billingCountry, "Enter Country")          { billingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ContactTextField(
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
private fun ContactDropdown(
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
private fun ContactDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}