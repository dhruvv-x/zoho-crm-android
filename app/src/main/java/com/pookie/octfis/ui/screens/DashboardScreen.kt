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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.FakeData
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*

private enum class DashTab(val label: String, val icon: ImageVector) {
    TodayActivity("Today",    Icons.Default.Today),
    Calls        ("Calls",    Icons.Default.Call),
    Meetings     ("Meetings", Icons.Default.Groups),
    Task         ("Tasks",    Icons.Default.TaskAlt),
}

@Composable
fun DashboardScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(DashTab.TodayActivity) }
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

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
                SummaryCard("Calls",    "0", Icons.Default.Call,    Modifier.weight(1f))
                SummaryCard("Meetings", "0", Icons.Default.Groups,  Modifier.weight(1f))
                SummaryCard("Tasks",    "0", Icons.Default.TaskAlt, Modifier.weight(1f))
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (selectedTab) {
                    DashTab.TodayActivity -> {
                        ProperTable("My Today Calls",    listOf("Time", "Subject"), FakeData.todayCalls.map    { listOf(it.time, it.subject) })
                        ProperTable("My Today Meetings", listOf("Time", "Subject"), FakeData.todayMeetings.map { listOf(it.time, it.subject) })
                        ProperTable("My Today Tasks",    listOf("Time", "Subject"), FakeData.todayTasks.map    { listOf(it.time, it.subject) })
                    }
                    DashTab.Calls -> {
                        ProperTable("My Today Calls",     listOf("Time", "Subject"), FakeData.todayCalls.map    { listOf(it.time, it.subject) })
                        ProperTable("My Upcoming Calls",  listOf("Time", "Subject"), FakeData.upcomingCalls.map { listOf(it.time, it.subject) })
                        ProperTable("My This Week Calls", listOf("Time", "Subject"), FakeData.thisWeekCalls.map { listOf(it.time, it.subject) })
                    }
                    DashTab.Meetings -> {
                        ProperTable("My Today Meetings", listOf("Time", "Subject"), FakeData.todayMeetings.map { listOf(it.time, it.subject) })
                    }
                    DashTab.Task -> {
                        ProperTable("My Today Tasks", listOf("Time", "Subject"), FakeData.todayTasks.map { listOf(it.time, it.subject) })
                    }
                }
            }
        }
    }
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
            Text(count,  color = Color.White, fontWeight = FontWeight.Bold,   fontSize = 20.sp)
            Text(label,  color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}

// ── Proper Table with borders ─────────────────────────────────────────────────

@Composable
private fun ProperTable(
    title  : String,
    headers: List<String>,
    rows   : List<List<String>>,
) {
    Column {
        // Title
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text       = title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = CrmOnSurface,
            )
            Text(
                text     = "${rows.size} records",
                fontSize = 11.sp,
                color    = CrmSubtext,
            )
        }

        // Table
        Card(
            shape     = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            modifier  = Modifier.fillMaxWidth(),
        ) {
            Column {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrmPrimary),
                ) {
                    headers.forEachIndexed { index, header ->
                        Box(
                            modifier = Modifier
                                .weight(if (index == 0) 1f else 2f)
                                .then(
                                    if (index > 0) Modifier.border(
                                        width = 0.5.dp,
                                        color = Color.White.copy(alpha = 0.3f),
                                    ) else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text       = header,
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp,
                            )
                        }
                    }
                }

                // Body rows
                if (rows.isEmpty()) {
                    // Empty state
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
                                        .then(
                                            if (colIdx > 0) Modifier.border(
                                                width = 0.5.dp,
                                                color = CrmDivider,
                                            ) else Modifier
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                ) {
                                    Text("—", fontSize = 12.sp, color = CrmSubtext)
                                }
                            }
                        }
                    }

                    // No records label
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .background(CrmBackground)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text      = "No records found",
                            fontSize  = 12.sp,
                            color     = CrmSubtext,
                            textAlign = TextAlign.Center,
                        )
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
                                        .then(
                                            if (colIdx > 0) Modifier.border(
                                                width = 0.5.dp,
                                                color = CrmDivider,
                                            ) else Modifier
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                ) {
                                    Text(cell, fontSize = 12.sp, color = CrmOnSurface)
                                }
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