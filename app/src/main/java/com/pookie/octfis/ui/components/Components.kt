package com.pookie.octfis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*

// ─── Bottom Nav ───────────────────────────────────────────────────────────────

data class BottomNavItem(
    val label : String,
    val icon  : ImageVector,
    val route : String,
)

val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Default.Home,      Screen.Dashboard.route),
    BottomNavItem("Accounts", Icons.Default.Business,  Screen.Accounts.route),
    BottomNavItem("Contacts", Icons.Default.Contacts,  Screen.Contacts.route),
    BottomNavItem("Deals",    Icons.Default.Handshake, Screen.Deals.route),
    BottomNavItem("Quotes",   Icons.Default.Receipt,   Screen.Quotes.route),
)

@Composable
fun CrmBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = CrmPrimary,
                    selectedTextColor   = CrmPrimary,
                    unselectedIconColor = CrmSubtext,
                    unselectedTextColor = CrmSubtext,
                    indicatorColor      = Color.Transparent,
                )
            )
        }
    }
}

// ─── Activity Table ───────────────────────────────────────────────────────────

@Composable
fun ActivityTable(
    title   : String,
    rows    : List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text       = title,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 14.sp,
            color      = CrmOnSurface,
            modifier   = Modifier.padding(bottom = 6.dp),
        )
        Surface(
            shape          = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier       = Modifier.fillMaxWidth(),
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrmTableHeader)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text("Time",     color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("Subjects", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(2f))
                }
                // Body rows
                if (rows.isEmpty()) {
                    repeat(3) { idx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (idx % 2 == 0) CrmRowAlt else Color.White)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text("", modifier = Modifier.weight(1f))
                            Text("", modifier = Modifier.weight(2f))
                        }
                    }
                } else {
                    rows.forEachIndexed { idx, (time, subject) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (idx % 2 == 0) CrmRowAlt else Color.White)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(time,    fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(subject, fontSize = 12.sp, modifier = Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

// ─── Form Row ─────────────────────────────────────────────────────────────────

@Composable
fun FormRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = CrmSubtext,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            text     = value,
            fontSize = 13.sp,
            color    = CrmOnSurface,
            modifier = Modifier.weight(2f),
        )
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrmPrimary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text       = title,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
        )
    }
}