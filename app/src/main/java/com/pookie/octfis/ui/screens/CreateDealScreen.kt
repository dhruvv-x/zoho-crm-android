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
import androidx.navigation.NavController
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDealScreen(navController: NavController) {

    var dealName        by remember { mutableStateOf("") }
    var accountName     by remember { mutableStateOf("") }
    var contactName     by remember { mutableStateOf("") }
    var amount          by remember { mutableStateOf("") }
    var closingDate     by remember { mutableStateOf("") }
    var type            by remember { mutableStateOf("-None-") }
    var email           by remember { mutableStateOf("") }
    var dealOwner       by remember { mutableStateOf("-None-") }
    var description     by remember { mutableStateOf("") }
    var stage           by remember { mutableStateOf("-None-") }
    var leadSource      by remember { mutableStateOf("-None-") }
    var leadSourceDrill by remember { mutableStateOf("") }

    Scaffold(
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
                    DealFormField("Deal Name",     dealName,    "Deal Name")               { dealName = it }
                    DealDivider()
                    DealFormField("Account Name",  accountName, "Enter Company name")      { accountName = it }
                    DealDivider()
                    DealFormField("Contact Name",  contactName, "Enter Customer Name")     { contactName = it }
                    DealDivider()
                    DealFormField("Amount",        amount,      "Enter Deal Amount")       { amount = it }
                    DealDivider()
                    DealFormField("Closing Date",  closingDate, "Enter Deal Closing Date") { closingDate = it }
                    DealDivider()
                    DealDropdownField("Type",       type)       { type = it }
                    DealDivider()
                    DealFormField("Email",         email,       "Enter Email ID")          { email = it }
                    DealDivider()
                    DealDropdownField("Deal Owner", dealOwner)  { dealOwner = it }
                    DealDivider()
                    DealFormField("Description",   description, "Short description")       { description = it }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Additional Information ────────────────────────────────────
            SectionHeader("Additional Information")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    DealDropdownField("Stage",      stage)      { stage = it }
                    DealDivider()
                    DealDropdownField("Lead Source", leadSource) { leadSource = it }
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
private fun DealDropdownField(
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
private fun DealDivider() {
    HorizontalDivider(
        color     = CrmDivider,
        thickness = 0.5.dp,
        modifier  = Modifier.padding(horizontal = 16.dp),
    )
}