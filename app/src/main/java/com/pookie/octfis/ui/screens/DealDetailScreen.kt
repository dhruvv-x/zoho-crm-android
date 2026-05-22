package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.FormRow
import com.pookie.octfis.ui.components.SectionHeader
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    navController: NavController,
    dealId       : Int,
    vm           : DealDetailViewModel = viewModel(),
) {
    // Init once
    LaunchedEffect(dealId) { vm.init(dealId) }

    val deal             by vm.deal.collectAsState()
    val stages           by vm.stages.collectAsState()
    val stageUpdateState by vm.stageUpdateState.collectAsState()

    // Snackbar for success / error feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(stageUpdateState) {
        when (val s = stageUpdateState) {
            is StageUpdateState.Success -> {
                snackbarHostState.showSnackbar("Stage updated successfully")
                vm.resetUpdateState()
            }
            is StageUpdateState.Error -> {
                snackbarHostState.showSnackbar("Error: ${s.message}")
                vm.resetUpdateState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = deal?.dealName?.ifEmpty { "Deal Detail" } ?: "Deal Detail",
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
                    IconButton(onClick = {
                        navController.navigate(Screen.EditDeal.createRoute(dealId))
                    }) {
                        Icon(Icons.Default.Edit, "Edit", tint = CrmPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (deal == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Deal not found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        val currentDeal = deal!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Stage Pipeline Bar ────────────────────────────────────────────
            if (stages.isNotEmpty()) {
                StageProgressBar(
                    stages           = stages,
                    currentStage     = currentDeal.stage,
                    isSaving         = stageUpdateState is StageUpdateState.Saving,
                    startDate        = currentDeal.closingDate, // used as label hint
                    onStageSelected  = { vm.updateStage(it) },
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Key Information ───────────────────────────────────────────────
            SectionHeader("Key Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    FormRow("Deal Name",    currentDeal.dealName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Account Name", currentDeal.accountName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Contact Name", currentDeal.contactName.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Amount",       currentDeal.amount.ifEmpty { "—" }.let { if (it != "—") "₹$it" else it })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Closing Date", currentDeal.closingDate.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Type",         currentDeal.type.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Email",        currentDeal.email.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Phone",        currentDeal.phone.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Deal Owner",   currentDeal.dealOwner.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Description",  currentDeal.description.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Additional Information ────────────────────────────────────────
            SectionHeader("Additional Information")
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    FormRow("Stage",       currentDeal.stage.ifEmpty { "—" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    FormRow("Lead Source", currentDeal.leadSource.ifEmpty { "—" })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Stage Progress Bar ─────────────────────────────────────────────────────────
@Composable
private fun StageProgressBar(
    stages          : List<String>,
    currentStage    : String,
    isSaving        : Boolean,
    startDate       : String,
    onStageSelected : (String) -> Unit,
) {
    val currentIdx  = stages.indexOfFirst { it == currentStage }.coerceAtLeast(0)
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {

            // ── START / CLOSING date row ──────────────────────────────────────
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("START", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrmSubtext)
                    Text(
                        text      = stages.firstOrNull() ?: "",
                        fontSize  = 10.sp,
                        color     = CrmSubtext,
                    )
                }
                if (startDate.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CLOSING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrmSubtext)
                        Text(startDate, fontSize = 10.sp, color = CrmSubtext)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Saving indicator ──────────────────────────────────────────────
            if (isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color    = CrmPrimary,
                )
                Spacer(Modifier.height(6.dp))
            }

            // ── Stage chips row ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left arrow hint
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint               = CrmSubtext,
                    modifier           = Modifier.size(20.dp),
                )

                stages.forEachIndexed { idx, stage ->
                    val isActive  = stage == currentStage
                    val isPast    = idx < currentIdx

                    StageChip(
                        label    = stage,
                        isActive = isActive,
                        isPast   = isPast,
                        enabled  = !isSaving,
                        onClick  = { onStageSelected(stage) },
                    )

                    // Chevron separator (not after last)
                    if (idx < stages.lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text  = "›",
                                color = CrmSubtext,
                                fontSize = 18.sp,
                            )
                        }
                    }
                }

                // Right arrow hint
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = CrmSubtext,
                    modifier           = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Single stage chip ──────────────────────────────────────────────────────────
@Composable
private fun StageChip(
    label   : String,
    isActive: Boolean,
    isPast  : Boolean,
    enabled : Boolean,
    onClick : () -> Unit,
) {
    val bgColor   = when {
        isActive -> CrmPrimary
        isPast   -> CrmPrimary.copy(alpha = 0.15f)
        else     -> Color.Transparent
    }
    val textColor = when {
        isActive -> Color.White
        isPast   -> CrmPrimary
        else     -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = when {
        isActive -> CrmPrimary
        isPast   -> CrmPrimary.copy(alpha = 0.4f)
        else     -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = label,
            color      = textColor,
            fontSize   = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )
    }
}