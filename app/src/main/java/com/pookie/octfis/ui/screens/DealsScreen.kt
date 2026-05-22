package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.components.CrmFilterSheet
import com.pookie.octfis.ui.components.FilterChipRow
import com.pookie.octfis.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── View mode toggle ──────────────────────────────────────────────────────────
private enum class DealViewMode { LIST, KANBAN }

// ── Stage → color mapping (same vibe as Zoho) ────────────────────────────────
private val stageColors = listOf(
    Color(0xFFB35C00), // burnt orange
    Color(0xFFD94F6B), // pink-red
    Color(0xFF7B3FA0), // purple
    Color(0xFF2E7D32), // green
    Color(0xFF1565C0), // blue
    Color(0xFF00838F), // teal
    Color(0xFFF57C00), // amber
    Color(0xFF558B2F), // olive green
    Color(0xFF6A1B9A), // deep purple
    Color(0xFF00695C), // dark teal
)

private fun colorForStage(stage: String, allStages: List<String>): Color {
    val idx = allStages.indexOf(stage).coerceAtLeast(0)
    return stageColors[idx % stageColors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    navController: NavController,
    vm: DealsViewModel = viewModel(),
) {
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute   = navBackStack?.destination?.route
    val uiState       by vm.uiState.collectAsState()
    val searchQuery   by vm.searchQuery.collectAsState()
    val filterState   by vm.filterState.collectAsState()
    val stages        by vm.stages.collectAsState()
    val accountNames  by vm.accountNames.collectAsState()
    val listState      = rememberLazyListState()
    var searchActive   by remember { mutableStateOf(false) }
    var filterOpen     by remember { mutableStateOf(false) }
    val isKanban       by vm.isKanban.collectAsState()
    val viewMode        = if (isKanban) DealViewMode.KANBAN else DealViewMode.LIST
    val focusRequester = remember { FocusRequester() }
    val isRefreshing   = uiState is DealsUiState.Loading

    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) { if (nearBottom && !searchActive && viewMode == DealViewMode.LIST) vm.loadNextPage() }
    LaunchedEffect(searchActive) { if (searchActive) focusRequester.requestFocus() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Filter sheet ──────────────────────────────────────────────────────────
    if (filterOpen) {
        CrmFilterSheet(
            title     = "Filter Deals",
            onDismiss = { filterOpen = false },
            onClear   = { vm.clearFilter(); filterOpen = false },
        ) {
            if (stages.isNotEmpty()) {
                FilterChipRow(
                    label    = "Stage",
                    options  = stages,
                    selected = filterState.stage,
                    onSelect = { vm.setFilter(filterState.copy(stage = it)) },
                )
            }
            if (accountNames.isNotEmpty()) {
                FilterChipRow(
                    label    = "Account",
                    options  = accountNames,
                    selected = filterState.accountName,
                    onSelect = { vm.setFilter(filterState.copy(accountName = it)) },
                )
            }
            FilterChipRow(
                label    = "Closing Date",
                options  = ClosingDateFilter.values().map { it.label },
                selected = filterState.closingDate.label,
                onSelect = { label ->
                    val picked = ClosingDateFilter.values().firstOrNull { it.label == label } ?: ClosingDateFilter.ALL
                    vm.setFilter(filterState.copy(closingDate = if (filterState.closingDate == picked) ClosingDateFilter.ALL else picked))
                },
            )
        }
    }

    Scaffold(
        bottomBar = { CrmBottomBar(navController, currentRoute) },
        floatingActionButton = {
            if (!searchActive) {
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.CreateDeal.route) },
                    containerColor = CrmPrimary,
                    contentColor   = Color.White,
                    shape          = CircleShape,
                ) { Icon(Icons.Default.Add, "Create Deal") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (searchActive) {
                    IconButton(onClick = { searchActive = false; vm.setSearch("") }) {
                        Icon(Icons.Default.ArrowBack, "Close Search", tint = CrmOnSurface)
                    }
                    TextField(
                        value         = searchQuery,
                        onValueChange = { vm.setSearch(it) },
                        placeholder   = { Text("Search deals…", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {}),
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.setSearch("") }) {
                            Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    IconButton(onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = CrmOnSurface)
                    }
                    Text("Deals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    if (uiState is DealsUiState.Success) {
                        Text(
                            text     = "${(uiState as DealsUiState.Success).deals.size} loaded",
                            fontSize = 11.sp,
                            color    = CrmSubtext,
                        )
                    }
                    // ── View toggle ───────────────────────────────────────────
                    IconButton(onClick = { vm.toggleViewMode() }) {
                        Icon(
                            imageVector = if (viewMode == DealViewMode.LIST) Icons.Default.ViewKanban else Icons.Default.ViewList,
                            contentDescription = if (viewMode == DealViewMode.LIST) "Switch to Kanban" else "Switch to List",
                            tint = CrmPrimary,
                        )
                    }
                    IconButton(onClick = { searchActive = true }) {
                        Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box {
                        IconButton(onClick = { filterOpen = true }) {
                            Icon(Icons.Default.FilterList, "Filter", tint = if (filterState.isActive) CrmPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (filterState.isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CrmPrimary)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                            )
                        }
                    }
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = { vm.load() },
                modifier     = Modifier.fillMaxSize(),
            ) {
                when (val s = uiState) {
                    is DealsUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CrmPrimary)
                                Spacer(Modifier.height(12.dp))
                                Text("Loading from Zoho CRM…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                    }
                    is DealsUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudOff, null, tint = CrmError, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(s.message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = CrmPrimary)) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is DealsUiState.Success -> {
                        if (s.deals.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        } else {
                            when (viewMode) {
                                DealViewMode.LIST   -> DealsListView(s, searchQuery, filterState, listState, navController)
                                DealViewMode.KANBAN -> DealsKanbanView(s.deals, stages, navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── LIST VIEW (original, untouched) ──────────────────────────────────────────
@Composable
private fun DealsListView(
    s            : DealsUiState.Success,
    searchQuery  : String,
    filterState  : DealFilterState,
    listState    : androidx.compose.foundation.lazy.LazyListState,
    navController: NavController,
) {
    LazyColumn(
        state          = listState,
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(s.deals) { _, deal ->
            DealRow(deal) {
                navController.navigate(Screen.DealDetail.createRoute(deal.id))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        }
        if (s.hasMore && searchQuery.isBlank() && !filterState.isActive) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CrmPrimary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

// ── KANBAN VIEW ───────────────────────────────────────────────────────────────
@Composable
private fun DealsKanbanView(
    deals        : List<Deal>,
    allStages    : List<String>,
    navController: NavController,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today         = remember { LocalDate.now() }

    // Group deals by stage; preserve stage order from allStages
    // If a deal's stage is not in allStages (e.g. "-None-"), put it in a fallback bucket
    val stageOrder = if (allStages.isEmpty()) {
        deals.map { it.stage }.filter { it.isNotBlank() && it != "-None-" }.distinct()
    } else allStages

    val grouped: Map<String, List<Deal>> = buildMap {
        stageOrder.forEach { stage -> put(stage, deals.filter { it.stage == stage }) }
        val others = deals.filter { it.stage == "-None-" || it.stage.isBlank() }
        if (others.isNotEmpty()) put("-None-", others)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        grouped.entries.forEach { (stage, stageDeals) ->
            val stageColor = colorForStage(stage, stageOrder)

            // Total amount for this stage
            val totalAmount = stageDeals.sumOf {
                it.amount.replace(",", "").replace("₹", "").trim().toDoubleOrNull() ?: 0.0
            }

            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
            ) {
                // ── Column header ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(stageColor)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = stage,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                modifier   = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(6.dp))
                            // Deal count badge
                            Box(
                                modifier         = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text      = "${stageDeals.size}",
                                    color     = Color.White,
                                    fontSize  = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        if (totalAmount > 0.0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text      = "₹ ${"%.2f".format(totalAmount)}",
                                color     = Color.White.copy(alpha = 0.92f),
                                fontSize  = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // ── Cards column ──────────────────────────────────────────────
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(stageDeals) { _, deal ->
                        KanbanDealCard(
                            deal          = deal,
                            stageColor    = stageColor,
                            today         = today,
                            dateFormatter = dateFormatter,
                            onClick       = { navController.navigate(Screen.DealDetail.createRoute(deal.id)) },
                        )
                    }
                }
            }
        }
    }
}

// ── Kanban card ───────────────────────────────────────────────────────────────
@Composable
private fun KanbanDealCard(
    deal         : Deal,
    stageColor   : Color,
    today        : LocalDate,
    dateFormatter: DateTimeFormatter,
    onClick      : () -> Unit,
) {
    val isOverdue = remember(deal.closingDate) {
        if (deal.closingDate.isBlank()) false
        else runCatching {
            LocalDate.parse(deal.closingDate, dateFormatter).isBefore(today)
        }.getOrDefault(false)
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        // Colored left accent bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stageColor)
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                // Deal name
                Text(
                    text       = deal.dealName.ifEmpty { "(No Name)" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )
                // Owner
                if (deal.dealOwner.isNotBlank() && deal.dealOwner != "-None-") {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text     = deal.dealOwner,
                        fontSize = 11.sp,
                        color    = CrmSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Account name
                if (deal.accountName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = deal.accountName,
                        fontSize = 11.sp,
                        color    = CrmPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Amount
                if (deal.amount.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text       = "₹ ${deal.amount}",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                }
                // Closing date
                if (deal.closingDate.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text     = deal.closingDate,
                        fontSize = 11.sp,
                        color    = if (isOverdue) CrmError else CrmSubtext,
                    )
                }
            }
        }
    }
}

// ── LIST ROW (original, untouched) ────────────────────────────────────────────
@Composable
private fun DealRow(deal: Deal, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(CrmPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Handshake, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(deal.dealName.ifEmpty { "(No Name)" }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            if (deal.accountName.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(deal.accountName, fontSize = 11.sp, color = CrmPrimary)
                }
                Spacer(Modifier.height(1.dp))
            }
            Text(deal.stage.ifEmpty { "-None-" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}