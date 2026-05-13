package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    navController: NavController,
    vm: DashboardViewModel = viewModel(),
) {
    var selectedTab by remember { mutableStateOf(DashTab.TodayActivity) }
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val uiState by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val callCount    = (uiState as? DashboardUiState.Success)?.data?.calls?.size ?: 0
    val meetingCount = (uiState as? DashboardUiState.Success)?.data?.meetings?.size ?: 0
    val taskCount    = (uiState as? DashboardUiState.Success)?.data?.tasks?.size ?: 0

    Scaffold(
        bottomBar      = { CrmBottomBar(navController, currentRoute) },
        containerColor = CrmBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────
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
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.load() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
                IconButton(onClick = {
                    scope.launch {
                        ZohoServiceLocator.getTokenStore().clear()
                    }
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                }
            }

            // ── Summary Cards ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrmPrimary)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryCard("Calls",    "$callCount",    Icons.Default.Call,    Modifier.weight(1f))
                SummaryCard("Meetings", "$meetingCount", Icons.Default.Groups,  Modifier.weight(1f))
                SummaryCard("Tasks",    "$taskCount",    Icons.Default.TaskAlt, Modifier.weight(1f))
            }

            // ── Tab Row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DashTab.entries.forEach { tab ->
                    val sel = selectedTab == tab
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) CrmPrimary else CrmBackground)
                            .border(1.dp, if (sel) CrmPrimary else CrmDivider, RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector        = tab.icon,
                            contentDescription = null,
                            tint               = if (sel) Color.White else CrmSubtext,
                            modifier           = Modifier.size(13.dp),
                        )
                        Text(
                            text       = tab.label,
                            fontSize   = 12.sp,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (sel) Color.White else CrmSubtext,
                        )
                    }
                }
            }

            HorizontalDivider(color = CrmDivider, thickness = 1.dp)

            // ── Content ───────────────────────────────────────────────────
            when (val s = uiState) {
                is DashboardUiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CrmPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading activities…", color = CrmSubtext, fontSize = 13.sp)
                        }
                    }
                }

                is DashboardUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudOff, null, tint = CrmError, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(s.message, color = CrmSubtext, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { vm.load() },
                                colors  = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
                            ) { Text("Retry") }
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
                                    "My Calls",
                                    listOf("Time", "Subject"),
                                    data.calls.map { listOf(formatTime(it.startTime), it.subject) }
                                )
                                ProperTable(
                                    "My Meetings",
                                    listOf("Time", "Subject"),
                                    data.meetings.map { listOf(formatTime(it.startDateTime), it.title) }
                                )
                                ProperTable(
                                    "My Tasks",
                                    listOf("Due Date", "Subject"),
                                    data.tasks.map { listOf(it.dueDate, it.subject) }
                                )
                            }
                            DashTab.Calls -> {
                                ProperTable(
                                    "All Calls",
                                    listOf("Time", "Subject"),
                                    data.calls.map { listOf(formatTime(it.startTime), it.subject) }
                                )
                            }
                            DashTab.Meetings -> {
                                ProperTable(
                                    "All Meetings",
                                    listOf("Time", "Subject"),
                                    data.meetings.map { listOf(formatTime(it.startDateTime), it.title) }
                                )
                            }
                            DashTab.Task -> {
                                ProperTable(
                                    "All Tasks",
                                    listOf("Due Date", "Subject"),
                                    data.tasks.map { listOf(it.dueDate, it.subject) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(raw: String): String {
    if (raw.isBlank()) return "—"
    return try {
        val t = raw.substringAfter("T").take(5)
        if (t.length == 5) t else raw.take(10)
    } catch (e: Exception) { raw.take(10) }
}

// ── Summary Card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    label   : String,
    count   : String,
    icon    : ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}

// ── Proper Table ──────────────────────────────────────────────────────────────

@Composable
private fun ProperTable(
    title  : String,
    headers: List<String>,
    rows   : List<List<String>>,
) {
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CrmOnSurface)
            Text("${rows.size} records", fontSize = 11.sp, color = CrmSubtext)
        }

        Card(
            shape     = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
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
                    repeat(3) { rowIdx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (rowIdx % 2 == 0) CrmRowAlt else Color.White)
                                .border(0.5.dp, CrmDivider),
                        ) {
                            headers.forEachIndexed { colIdx, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(if (colIdx == 0) 1f else 2f)
                                        .then(if (colIdx > 0) Modifier.border(0.5.dp, CrmDivider) else Modifier)
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                ) { Text("—", fontSize = 12.sp, color = CrmSubtext) }
                            }
                        }
                    }
                    Box(
                        modifier         = Modifier.fillMaxWidth().background(CrmBackground).padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No records found", fontSize = 12.sp, color = CrmSubtext, textAlign = TextAlign.Center)
                    }
                } else {
                    rows.forEachIndexed { rowIdx, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (rowIdx % 2 == 0) CrmRowAlt else Color.White),
                        ) {
                            row.forEachIndexed { colIdx, cell ->
                                Box(
                                    modifier = Modifier
                                        .weight(if (colIdx == 0) 1f else 2f)
                                        .then(if (colIdx > 0) Modifier.border(0.5.dp, CrmDivider) else Modifier)
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                ) { Text(cell, fontSize = 12.sp, color = CrmOnSurface) }
                            }
                        }
                        if (rowIdx < rows.lastIndex)
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}