package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.FakeData
import com.pookie.octfis.ui.components.ActivityTable
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*

private enum class DashTab { TodayActivity, Calls, Meetings, Task }

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
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CrmOnSurface)
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = "Activity Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                )
            }

            // ── Tab Row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DashTab.entries.forEach { tab ->
                    val sel = selectedTab == tab
                    Box(
                        modifier         = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) CrmTabSelected else Color.White)
                            .border(1.dp, if (sel) CrmTabSelected else CrmDivider, RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = tab.name.replace("TodayActivity", "Today Activity"),
                            fontSize   = 12.sp,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (sel) Color.White else CrmOnSurface,
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedTab) {
                    DashTab.TodayActivity -> {
                        ActivityTable("My Today Calls",     FakeData.todayCalls.map    { it.time to it.subject })
                        ActivityTable("My Today Meetings",  FakeData.todayMeetings.map { it.time to it.subject })
                        ActivityTable("My Today Tasks",     FakeData.todayTasks.map    { it.time to it.subject })
                    }
                    DashTab.Calls -> {
                        ActivityTable("My Today Calls",     FakeData.todayCalls.map    { it.time to it.subject })
                        ActivityTable("My Upcoming Calls",  FakeData.upcomingCalls.map { it.time to it.subject })
                        ActivityTable("My This Week Calls", FakeData.thisWeekCalls.map { it.time to it.subject })
                    }
                    DashTab.Meetings -> {
                        ActivityTable("My Today Meetings",  FakeData.todayMeetings.map { it.time to it.subject })
                    }
                    DashTab.Task -> {
                        ActivityTable("My Today Tasks",     FakeData.todayTasks.map    { it.time to it.subject })
                    }
                }
            }
        }
    }
}