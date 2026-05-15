package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.components.CrmFilterSheet
import com.pookie.octfis.ui.components.FilterChipRow
import com.pookie.octfis.ui.theme.*

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
    val focusRequester = remember { FocusRequester() }
    val isRefreshing   = uiState is DealsUiState.Loading

    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) { if (nearBottom && !searchActive) vm.loadNextPage() }
    LaunchedEffect(searchActive) { if (searchActive) focusRequester.requestFocus() }

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
            // Closing Date filter
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
                    }
                }
            }
        }
    }
}

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