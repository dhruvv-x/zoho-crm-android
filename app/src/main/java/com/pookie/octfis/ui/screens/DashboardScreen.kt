package com.pookie.octfis.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.launch

private enum class DashTab(val label: String, val icon: ImageVector) {
    TodayActivity("Today",    Icons.Default.Today),
    Calls        ("Calls",    Icons.Default.Call),
    Meetings     ("Meetings", Icons.Default.Groups),
    Task         ("Tasks",    Icons.Default.TaskAlt),
}

@Composable
fun DashboardScreen(
    navController : NavController,
    onToggleTheme : () -> Unit = {},
    isDark        : Boolean    = false,
    vm            : DashboardViewModel = viewModel(),
    searchVm      : MasterSearchViewModel = viewModel(),
) {
    var selectedTab    by remember { mutableStateOf(DashTab.TodayActivity) }
    var searchActive   by remember { mutableStateOf(false) }
    val navBackStack   by navController.currentBackStackEntryAsState()
    val currentRoute   = navBackStack?.destination?.route
    val uiState        by vm.uiState.collectAsState()
    val searchState    by searchVm.state.collectAsState()
    val scope          = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val todayCallCount    = (uiState as? DashboardUiState.Success)?.data?.todayCalls?.size ?: 0
    val todayMeetingCount = (uiState as? DashboardUiState.Success)?.data?.todayMeetings?.size ?: 0
    val todayTaskCount    = (uiState as? DashboardUiState.Success)?.data?.todayTasks?.size ?: 0

    Scaffold(
        bottomBar      = { CrmBottomBar(navController, currentRoute) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Main dashboard column ──────────────────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top Bar ───────────────────────────────────────────────
                var overflowExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrmPrimary)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = "Activity Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color.White,
                        modifier   = Modifier.weight(1f),
                    )
                    IconButton(onClick = { searchActive = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    // ── 3-dot overflow menu ───────────────────────────────
                    Box {
                        IconButton(onClick = { overflowExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded         = overflowExpanded,
                            onDismissRequest = { overflowExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector        = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint               = MaterialTheme.colorScheme.onSurface,
                                            modifier           = Modifier.size(18.dp),
                                        )
                                        Text(if (isDark) "Light Mode" else "Dark Mode")
                                    }
                                },
                                onClick = {
                                    overflowExpanded = false
                                    onToggleTheme()
                                },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Default.Logout,
                                            contentDescription = null,
                                            tint               = MaterialTheme.colorScheme.error,
                                            modifier           = Modifier.size(18.dp),
                                        )
                                        Text("Logout", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    overflowExpanded = false
                                    scope.launch { ZohoServiceLocator.getTokenStore().clear() }
                                    navController.navigate(Screen.SignIn.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                            )
                        }
                    }
                }

                // ── Summary Cards ─────────────────────────────────────────
                if (selectedTab == DashTab.TodayActivity) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrmPrimary)
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryCard("Calls",    "$todayCallCount",    Icons.Default.Call,    Modifier.weight(1f))
                        SummaryCard("Meetings", "$todayMeetingCount", Icons.Default.Groups,  Modifier.weight(1f))
                        SummaryCard("Tasks",    "$todayTaskCount",    Icons.Default.TaskAlt, Modifier.weight(1f))
                    }
                }

                // ── Tab Row ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DashTab.entries.forEach { tab ->
                        val sel = selectedTab == tab
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (sel) CrmPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (sel) CrmPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector        = tab.icon,
                                contentDescription = null,
                                tint               = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(13.dp),
                            )
                            Text(
                                text       = tab.label,
                                fontSize   = 12.sp,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

                // ── Dashboard Content ─────────────────────────────────────
                when (val s = uiState) {
                    is DashboardUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CrmPrimary)
                                Spacer(Modifier.height(12.dp))
                                Text("Loading activities…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                    }
                    is DashboardUiState.Error -> {
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
                    is DashboardUiState.Success -> {
                        val data = s.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            when (selectedTab) {
                                DashTab.TodayActivity -> {
                                    ProperTable(
                                        title      = "My Calls",
                                        headers    = listOf("Time", "Subject"),
                                        rows       = data.todayCalls.map { listOf(formatTime(it.startTime), it.subject) },
                                        rowIds     = data.todayCalls.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.CallDetail.createRoute(id)) },
                                    )
                                    ProperTable(
                                        title      = "My Meetings",
                                        headers    = listOf("Time", "Subject"),
                                        rows       = data.todayMeetings.map { listOf(formatTime(it.startDateTime), it.title) },
                                        rowIds     = data.todayMeetings.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.MeetingDetail.createRoute(id)) },
                                    )
                                    ProperTable(
                                        title      = "My Tasks",
                                        headers    = listOf("Due Date", "Subject"),
                                        rows       = data.todayTasks.map { listOf(it.dueDate, it.subject) },
                                        rowIds     = data.todayTasks.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) },
                                    )
                                }
                                DashTab.Calls -> {
                                    ProperTable(
                                        title      = "All Calls",
                                        headers    = listOf("Time", "Subject"),
                                        rows       = data.allCalls.map { listOf(formatTime(it.startTime), it.subject) },
                                        rowIds     = data.allCalls.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.CallDetail.createRoute(id)) },
                                    )
                                }
                                DashTab.Meetings -> {
                                    ProperTable(
                                        title      = "All Meetings",
                                        headers    = listOf("Time", "Subject"),
                                        rows       = data.allMeetings.map { listOf(formatTime(it.startDateTime), it.title) },
                                        rowIds     = data.allMeetings.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.MeetingDetail.createRoute(id)) },
                                    )
                                }
                                DashTab.Task -> {
                                    ProperTable(
                                        title      = "All Tasks",
                                        headers    = listOf("Due Date", "Subject"),
                                        rows       = data.allTasks.map { listOf(it.dueDate, it.subject) },
                                        rowIds     = data.allTasks.map { it.id },
                                        onRowClick = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Master Search Overlay ──────────────────────────────────────
            AnimatedVisibility(
                visible = searchActive,
                enter   = fadeIn() + slideInVertically { -40 },
                exit    = fadeOut() + slideOutVertically { -40 },
            ) {
                MasterSearchOverlay(
                    state         = searchState,
                    focusRequester= focusRequester,
                    onQueryChange = searchVm::onQueryChange,
                    onClose       = {
                        searchActive = false
                        searchVm.clear()
                    },
                    onResultClick = { result ->
                        searchActive = false
                        searchVm.clear()
                        navigateToResult(navController, result)
                    },
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            }
        }
    }
}

// ── Navigate to result ────────────────────────────────────────────────────────

private fun navigateToResult(nav: NavController, result: SearchResult) {
    when (result.module) {
        SearchModule.ACCOUNT -> nav.navigate(Screen.AccountDetail.createRoute(result.id))
        SearchModule.CONTACT -> nav.navigate(Screen.ContactDetail.createRoute(result.id.toIntOrNull() ?: 0))
        SearchModule.DEAL    -> nav.navigate(Screen.DealDetail.createRoute(result.id.toIntOrNull() ?: 0))
        SearchModule.QUOTE   -> nav.navigate(Screen.QuoteDetail.createRoute(result.id.toIntOrNull() ?: 0))
        SearchModule.TASK    -> nav.navigate(Screen.TaskDetail.createRoute(result.id))
        SearchModule.MEETING -> nav.navigate(Screen.MeetingDetail.createRoute(result.id))
        SearchModule.CALL    -> nav.navigate(Screen.CallDetail.createRoute(result.id))
    }
}

// ── Master Search Overlay Composable ─────────────────────────────────────────

@Composable
private fun MasterSearchOverlay(
    state         : MasterSearchUiState,
    focusRequester: FocusRequester,
    onQueryChange : (String) -> Unit,
    onClose       : () -> Unit,
    onResultClick : (SearchResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Search Bar ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmPrimary)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close search", tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (state.query.isEmpty()) {
                    Text(
                        text  = "Search accounts, contacts, deals…",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                }
                BasicTextField(
                    value         = state.query,
                    onValueChange = onQueryChange,
                    singleLine    = true,
                    textStyle     = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush   = SolidColor(Color.White),
                    modifier      = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            if (state.query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                }
            }
        }

        // ── Module filter hint bar ────────────────────────────────────────
        if (state.query.isBlank()) {
            SearchHintGrid()
        }

        // ── Loading indicator ─────────────────────────────────────────────
        if (state.isSearching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color    = CrmPrimary,
            )
        }

        // ── Results ───────────────────────────────────────────────────────
        when {
            state.query.isNotBlank() && state.results.isEmpty() && !state.isSearching -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff, null,
                            modifier = Modifier.size(56.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No results for \"${state.query}\"",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                        Text(
                            "Try a different name, email, stage, or amount",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            state.results.isNotEmpty() -> {
                // Group results by module
                val grouped = state.results.groupBy { it.module }
                Text(
                    text     = "${state.results.size} result${if (state.results.size != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (module, items) ->
                        // Module header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector        = moduleIcon(module),
                                    contentDescription = null,
                                    tint               = CrmPrimary,
                                    modifier           = Modifier.size(14.dp),
                                )
                                Text(
                                    text       = module.label + "s",
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = CrmPrimary,
                                )
                                Text(
                                    text     = "(${items.size})",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        // Result rows
                        items(items) { result ->
                            SearchResultRow(result = result, onClick = { onResultClick(result) })
                            HorizontalDivider(
                                color     = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Hint grid shown when query is empty ──────────────────────────────────────

@Composable
private fun SearchHintGrid() {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Search across all modules",
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Type any name, email, phone, stage, amount, or date.",
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        val modules = listOf(
            SearchModule.ACCOUNT to "Company names, GSTIN, city",
            SearchModule.CONTACT to "Name, email, phone",
            SearchModule.DEAL    to "Deal name, stage, amount",
            SearchModule.QUOTE   to "Subject, stage, total",
            SearchModule.TASK    to "Subject, status, priority",
            SearchModule.MEETING to "Title, date",
            SearchModule.CALL    to "Subject, type, status",
        )
        modules.forEach { (mod, hint) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector        = moduleIcon(mod),
                    contentDescription = null,
                    tint               = CrmPrimary,
                    modifier           = Modifier.size(18.dp),
                )
                Column {
                    Text(mod.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(hint, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Single result row ─────────────────────────────────────────────────────────

@Composable
private fun SearchResultRow(result: SearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Module icon circle
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(50))
                .background(CrmPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = moduleIcon(result.module),
                contentDescription = null,
                tint               = CrmPrimary,
                modifier           = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = result.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.subtitle.isNotBlank()) {
                Text(
                    text     = result.subtitle,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (result.badge.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CrmPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text     = result.badge,
                    fontSize = 10.sp,
                    color    = CrmPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Module → icon mapping ─────────────────────────────────────────────────────

private fun moduleIcon(module: SearchModule): ImageVector = when (module) {
    SearchModule.ACCOUNT -> Icons.Default.Business
    SearchModule.CONTACT -> Icons.Default.Person
    SearchModule.DEAL    -> Icons.Default.Handshake
    SearchModule.QUOTE   -> Icons.Default.Receipt
    SearchModule.TASK    -> Icons.Default.TaskAlt
    SearchModule.MEETING -> Icons.Default.Groups
    SearchModule.CALL    -> Icons.Default.Call
}

// ── Helpers (unchanged) ───────────────────────────────────────────────────────

private fun formatTime(raw: String): String {
    if (raw.isBlank()) return "—"
    val tIndex = raw.indexOf('T')
    if (tIndex == -1) return "—"
    val timePart = raw.substring(tIndex + 1).take(5)
    return if (timePart.length == 5) timePart else "—"
}

@Composable
private fun SummaryCard(label: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProperTable(
    title     : String,
    headers   : List<String>,
    rows      : List<List<String>>,
    rowIds    : List<String> = emptyList(),
    onRowClick: ((String) -> Unit)? = null,
) {
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${rows.size} records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            shape     = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier  = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(CrmPrimary)) {
                    headers.forEachIndexed { index, header ->
                        Box(
                            modifier = Modifier
                                .weight(if (index == 0) 1f else 2f)
                                .then(if (index > 0) Modifier.border(0.5.dp, Color.White.copy(alpha = 0.3f)) else Modifier)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(header, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
                if (rows.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("No records found", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                } else {
                    rows.forEachIndexed { rowIdx, row ->
                        val rowId = rowIds.getOrNull(rowIdx).orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (rowIdx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                                .then(if (onRowClick != null && rowId.isNotEmpty()) Modifier.clickable { onRowClick(rowId) } else Modifier),
                        ) {
                            row.forEachIndexed { colIdx, cell ->
                                Box(
                                    modifier = Modifier
                                        .weight(if (colIdx == 0) 1f else 2f)
                                        .then(if (colIdx > 0) Modifier.border(0.5.dp, CrmDivider) else Modifier)
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                ) {
                                    Text(cell, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                        if (rowIdx < rows.lastIndex)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}