package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
fun CreateDealScreen(
    navController: NavController,
    vm: CreateDealViewModel = viewModel(),
) {
    var dealName        by remember { mutableStateOf("") }
    var accountName     by remember { mutableStateOf("") }
    var accountZohoId   by remember { mutableStateOf("") }   // ← NEW
    var contactName     by remember { mutableStateOf("") }
    var contactZohoId   by remember { mutableStateOf("") }   // ← NEW
    var amount          by remember { mutableStateOf("") }
    var closingDate     by remember { mutableStateOf("") }
    var type            by remember { mutableStateOf("-None-") }
    var email           by remember { mutableStateOf("") }
    var selectedOwner   by remember { mutableStateOf(Pair("", "-None-")) }
    var description     by remember { mutableStateOf("") }
    var stage           by remember { mutableStateOf("-None-") }
    var leadSource      by remember { mutableStateOf("-None-") }
    var leadSourceDrill by remember { mutableStateOf("") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()
    val createState    by vm.createState.collectAsState()
    val accountItems   by vm.accountItems.collectAsState()   // ← NEW
    val contactItems   by vm.contactItems.collectAsState()   // ← NEW

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createState) {
        if (createState is CreateDealState.Saved) {
            vm.resetState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(createState) {
        if (createState is CreateDealState.Error) {
            snackbarHostState.showSnackbar((createState as CreateDealState.Error).message)
            vm.resetState()
        }
    }

    val isSaving = createState is CreateDealState.Saving

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Create Deal",
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
                    Button(
                        onClick = {
                            vm.save(
                                dealName        = dealName,
                                accountName     = accountName,
                                accountZohoId   = accountZohoId,   // ← NEW
                                contactName     = contactName,
                                contactZohoId   = contactZohoId,   // ← NEW
                                amount          = amount,
                                closingDate     = closingDate,
                                type            = type,
                                email           = email,
                                ownerEntry      = selectedOwner,
                                description     = description,
                                stage           = stage,
                                leadSource      = leadSource,
                                leadSourceDrill = leadSourceDrill,
                            )
                        },
                        enabled  = !isSaving,
                        colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                        shape    = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (isSaving)
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.surface,
                            )
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
                    DealFormField("Deal Name", dealName, "Deal Name") { dealName = it }
                    DealDivider()

                    // ← FIXED: was plain text, now LookupField (captures Zoho ID)
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
                    DealDivider()

                    // ← FIXED: was plain text, now LookupField (captures Zoho ID)
                    LookupField(
                        label       = "Contact Name",
                        value       = contactName,
                        placeholder = "Select Contact",
                        items       = contactItems,
                        loading     = optionsLoading && contactItems.isEmpty(),
                        onSelect    = { item ->
                            contactName   = item.name
                            contactZohoId = item.zohoId
                        },
                    )
                    DealDivider()

                    DealFormField("Amount",       amount,      "Enter Deal Amount") { amount = it }
                    DealDivider()
                    DealFormField("Closing Date", closingDate, "YYYY-MM-DD")        { closingDate = it }
                    DealDivider()
                    DealPicklistField("Type", type, options.types, optionsLoading)  { type = it }
                    DealDivider()
                    DealFormField("Email",        email,       "Enter Email ID")    { email = it }
                    DealDivider()
                    DealPicklistField(
                        label   = "Deal Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    DealDivider()
                    DealFormField("Description", description, "Short description") { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Additional Information")

            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    DealPicklistField("Stage",           stage,      options.stages,      optionsLoading) { stage = it }
                    DealDivider()
                    DealPicklistField("Lead Source",      leadSource, options.leadSources, optionsLoading) { leadSource = it }
                    DealDivider()
                    DealFormField("Lead Source Drill", leadSourceDrill, "Enter Source Reference") { leadSourceDrill = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DealFormField(
    label        : String,
    value        : String,
    placeholder  : String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 13.sp, color = CrmSubtext, modifier = Modifier.width(130.dp))
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(text = placeholder, color = CrmSubtext.copy(alpha = 0.7f), fontSize = 13.sp) },
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
private fun DealPicklistField(
    label   : String,
    value   : String,
    options : List<String>,
    loading : Boolean,
    onSelect: (String) -> Unit,
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
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CrmSubtext)
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
private fun DealDivider() {
    HorizontalDivider(
        color     = CrmDivider,
        thickness = 0.5.dp,
        modifier  = Modifier.padding(horizontal = 16.dp),
    )
}