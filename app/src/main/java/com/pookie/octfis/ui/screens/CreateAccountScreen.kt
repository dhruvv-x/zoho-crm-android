package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(navController: NavController) {

    var accountName    by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var website        by remember { mutableStateOf("") }
    var industry       by remember { mutableStateOf("-None-") }
    var gstTreatment   by remember { mutableStateOf("-None-") }
    var gstin          by remember { mutableStateOf("") }
    var leadSource     by remember { mutableStateOf("-None-") }
    var accountOwner   by remember { mutableStateOf("-None-") }
    var description    by remember { mutableStateOf("") }
    var billingStreet  by remember { mutableStateOf("") }
    var billingStreet2 by remember { mutableStateOf("") }
    var billingCity    by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Create Account",
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
            // ── Key Information ───────────────────────────────────────────
            SectionHeader("Key Information")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormField("Account Name",  accountName,    "Enter Company name") { accountName = it }
                    FieldDivider()
                    FormField("Phone",         phone,          "Enter Phone no")     { phone = it }
                    FieldDivider()
                    FormField("Website",       website,        "www.example.com")    { website = it }
                    FieldDivider()
                    DropdownField("Industry",       industry)      { industry = it }
                    FieldDivider()
                    DropdownField("GST Treatment",  gstTreatment)  { gstTreatment = it }
                    FieldDivider()
                    FormField("GSTIN",         gstin,          "Enter GST Number")  { gstin = it }
                    FieldDivider()
                    DropdownField("Lead Source",    leadSource)    { leadSource = it }
                    FieldDivider()
                    DropdownField("Account Owner",  accountOwner)  { accountOwner = it }
                    FieldDivider()
                    FormField("Description",   description,    "Short description") { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Address ───────────────────────────────────────────────────
            SectionHeader("Address")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    FormField("Billing Street",   billingStreet,  "Plot no, Building name") { billingStreet = it }
                    FieldDivider()
                    FormField("Billing Street 2", billingStreet2, "Landmark")               { billingStreet2 = it }
                    FieldDivider()
                    FormField("Billing City",     billingCity,    "Enter City Name")        { billingCity = it }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormField(
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
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = CrmSubtext,
            modifier = Modifier.width(130.dp),
        )
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    text     = placeholder,
                    color    = CrmSubtext.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                )
            },
            singleLine = true,
            modifier   = Modifier.weight(1f),
            colors     = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun DropdownField(
    label        : String,
    value        : String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = CrmSubtext,
            modifier = Modifier.width(130.dp),
        )
        Text(
            text     = value,
            fontSize = 13.sp,
            color    = CrmOnSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector        = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint               = CrmSubtext,
        )
    }
}

@Composable
private fun FieldDivider() {
    HorizontalDivider(
        color    = CrmDivider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}