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
import com.pookie.octfis.data.repository.ContactRepository
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    contactId: Int,
    vm: CreateContactViewModel = viewModel(),
) {
    val contact = ContactRepository.cache.firstOrNull { it.id == contactId }

    var firstName      by remember { mutableStateOf(contact?.firstName ?: "") }
    var lastName       by remember { mutableStateOf(contact?.lastName ?: "") }
    var phone          by remember { mutableStateOf(contact?.phone ?: "") }
    var email          by remember { mutableStateOf(contact?.email ?: "") }
    var accountName    by remember { mutableStateOf(contact?.accountName ?: "") }
    var title          by remember { mutableStateOf(contact?.title ?: "") }
    var department     by remember { mutableStateOf(contact?.department ?: "") }
    var selectedOwner  by remember { mutableStateOf(Pair("", contact?.contactOwner ?: "-None-")) }
    var leadSource     by remember { mutableStateOf(contact?.leadSource?.ifEmpty { "-None-" } ?: "-None-") }
    var description    by remember { mutableStateOf(contact?.description ?: "") }
    var mailingStreet  by remember { mutableStateOf(contact?.mailingStreet ?: "") }
    var mailingCity    by remember { mutableStateOf(contact?.mailingCity ?: "") }
    var mailingState   by remember { mutableStateOf(contact?.mailingState ?: "") }
    var mailingZip     by remember { mutableStateOf(contact?.mailingZip ?: "") }
    var mailingCountry by remember { mutableStateOf(contact?.mailingCountry ?: "") }

    val options        by vm.options.collectAsState()
    val optionsLoading by vm.optionsLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Contact", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
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
                    EditContactTextField("First Name",   firstName,   "Enter First Name")   { firstName = it }
                    EditContactDivider()
                    EditContactTextField("Last Name",    lastName,    "Enter Last Name")    { lastName = it }
                    EditContactDivider()
                    EditContactTextField("Phone",        phone,       "Enter Phone No")     { phone = it }
                    EditContactDivider()
                    EditContactTextField("Email",        email,       "Enter Email ID")     { email = it }
                    EditContactDivider()
                    EditContactTextField("Account Name", accountName, "Enter Company Name") { accountName = it }
                    EditContactDivider()
                    EditContactTextField("Title",        title,       "Enter Job Title")    { title = it }
                    EditContactDivider()
                    EditContactTextField("Department",   department,  "Enter Department")   { department = it }
                    EditContactDivider()
                    EditContactDropdown(
                        label   = "Contact Owner",
                        value   = selectedOwner.second,
                        options = options.owners.map { it.second },
                        loading = optionsLoading,
                    ) { name -> selectedOwner = options.owners.firstOrNull { it.second == name } ?: Pair("", name) }
                    EditContactDivider()
                    EditContactDropdown(
                        label   = "Lead Source",
                        value   = leadSource,
                        options = options.leadSources,
                        loading = optionsLoading,
                    ) { leadSource = it }
                    EditContactDivider()
                    EditContactTextField("Description",  description, "Short description")  { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            SectionHeader("Address")
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    EditContactTextField("Mailing Street",  mailingStreet,  "Plot no, Building name") { mailingStreet = it }
                    EditContactDivider()
                    EditContactTextField("Mailing City",    mailingCity,    "Enter City Name")        { mailingCity = it }
                    EditContactDivider()
                    EditContactTextField("Mailing State",   mailingState,   "Enter State")            { mailingState = it }
                    EditContactDivider()
                    EditContactTextField("Mailing ZIP",     mailingZip,     "Enter ZIP Code")         { mailingZip = it }
                    EditContactDivider()
                    EditContactTextField("Mailing Country", mailingCountry, "Enter Country")          { mailingCountry = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditContactTextField(
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
private fun EditContactDropdown(
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
private fun EditContactDivider() {
    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}