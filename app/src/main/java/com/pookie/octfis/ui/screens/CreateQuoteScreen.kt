package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteScreen(navController: NavController) {

    var subject      by remember { mutableStateOf("") }
    var accountName  by remember { mutableStateOf("") }
    var contactName  by remember { mutableStateOf("") }
    var validUntil   by remember { mutableStateOf("10/05/2026") }
    var quoteStage   by remember { mutableStateOf("Draft") }
    var description  by remember { mutableStateOf("") }
    var stageExpanded by remember { mutableStateOf(false) }

    val stageOptions = listOf("Draft", "Delivered", "On Hold", "Confirmed", "Closed Accepted", "Closed Lost")

    val items = remember {
        mutableStateListOf(
            QuoteItem(1, price = 10.99),
            QuoteItem(2, price = 8.99),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Create Quote",
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
                    // Subject
                    QuoteFormField("Subject", subject, "Enter Quote title") { subject = it }
                    QuoteDivider()

                    // Account Name
                    QuoteFormField("Account Name", accountName, "Select Company Name") { accountName = it }
                    QuoteDivider()

                    // Contact Name
                    QuoteFormField("Contact Name", contactName, "Select Contact Person") { contactName = it }
                    QuoteDivider()

                    // Valid Until — trailing arrow
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text     = "Valid Until",
                            fontSize = 13.sp,
                            color    = CrmSubtext,
                            modifier = Modifier.width(130.dp),
                        )
                        Text(
                            text     = validUntil,
                            fontSize = 13.sp,
                            color    = CrmOnSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector        = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint               = CrmSubtext,
                            modifier           = Modifier.size(14.dp),
                        )
                    }
                    QuoteDivider()

                    // Quote Stage — dropdown
                    ExposedDropdownMenuBox(
                        expanded        = stageExpanded,
                        onExpandedChange = { stageExpanded = !stageExpanded },
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text     = "Quote Stage",
                                fontSize = 13.sp,
                                color    = CrmSubtext,
                                modifier = Modifier.width(130.dp),
                            )
                            Text(
                                text     = quoteStage,
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
                        ExposedDropdownMenu(
                            expanded        = stageExpanded,
                            onDismissRequest = { stageExpanded = false },
                        ) {
                            stageOptions.forEach { option ->
                                DropdownMenuItem(
                                    text    = { Text(option, fontSize = 13.sp) },
                                    onClick = {
                                        quoteStage    = option
                                        stageExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    QuoteDivider()

                    // Description — trailing arrow
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text     = "Description",
                            fontSize = 13.sp,
                            color    = CrmSubtext,
                            modifier = Modifier.width(130.dp),
                        )
                        Text(
                            text     = description.ifEmpty { "Short description" },
                            fontSize = 13.sp,
                            color    = if (description.isEmpty()) CrmSubtext.copy(alpha = 0.7f) else CrmOnSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector        = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint               = CrmSubtext,
                            modifier           = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Quoted Items ──────────────────────────────────────────────
            SectionHeader("Quoted Items")

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("S.NO",         fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.width(48.dp))
                        Text("Product Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface, modifier = Modifier.weight(1f))
                        Text("PRICE",        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmOnSurface)
                    }
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)

                    // Item rows
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text     = "${item.sNo}",
                                fontSize = 13.sp,
                                color    = CrmOnSurface,
                                modifier = Modifier.width(48.dp).padding(top = 2.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.brand,       fontSize = 11.sp, color = CrmSubtext)
                                Text(item.productName, fontSize = 13.sp, color = CrmOnSurface, fontWeight = FontWeight.Medium)
                                Text(item.description, fontSize = 11.sp, color = CrmSubtext)
                                Text("Quantity: ${String.format("%02d", item.quantity)}", fontSize = 11.sp, color = CrmSubtext)
                            }
                            Text(
                                text     = "$${String.format("%.2f", item.price)}",
                                fontSize = 13.sp,
                                color    = CrmOnSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        if (index < items.lastIndex)
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // + Add Item
                    TextButton(
                        onClick  = {
                            val nextId = (items.maxOfOrNull { it.sNo } ?: 0) + 1
                            items.add(QuoteItem(nextId))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text     = "+ Add Item",
                            color    = CrmPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuoteFormField(
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
private fun QuoteDivider() {
    HorizontalDivider(
        color     = CrmDivider,
        thickness = 0.5.dp,
        modifier  = Modifier.padding(horizontal = 16.dp),
    )
}