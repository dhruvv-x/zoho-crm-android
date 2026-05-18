// ui/components/Components.kt
package com.pookie.octfis.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.pookie.octfis.engine.module.ActiveModule
import com.pookie.octfis.engine.module.ModuleEngine
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── NavBar ViewModel ─────────────────────────────────────────────────────────

@HiltViewModel
class NavBarViewModel @Inject constructor(
    private val moduleEngine: ModuleEngine,
) : ViewModel() {

    private val _modules = MutableStateFlow<List<ActiveModule>?>(null)
    val modules: StateFlow<List<ActiveModule>?> = _modules

    init {
        viewModelScope.launch {
            moduleEngine.getActiveModules()
                .onSuccess { modules ->
                    Log.d("OctfisNav", "Loaded ${modules.size} modules from Zoho API")
                    _modules.value = modules
                }
                .onFailure { error ->
                    // This log is critical — if you see it, check:
                    // 1. ZohoConstants.SCOPE includes ZohoCRM.settings.modules.READ
                    // 2. The user has re-authenticated after the scope was added
                    Log.e("OctfisNav", "getActiveModules() FAILED — falling back to static tabs", error)
                }
        }
    }
}

// ─── Fallback static items ────────────────────────────────────────────────────

private val fallbackNavModules = listOf(
    ActiveModule("Accounts", "Accounts", "Account", 1),
    ActiveModule("Contacts", "Contacts", "Contact", 2),
    ActiveModule("Deals",    "Deals",    "Deal",    3),
    ActiveModule("Quotes",   "Quotes",   "Quote",   4),
)

// ─── Icon mapping ─────────────────────────────────────────────────────────────

private fun moduleIcon(apiName: String): ImageVector = when (apiName) {
    "Accounts"       -> Icons.Default.Business
    "Contacts"       -> Icons.Default.Contacts
    "Deals",
    "Potentials"     -> Icons.Default.Handshake
    "Quotes"         -> Icons.Default.Receipt
    "Leads"          -> Icons.Default.PersonAdd
    "Products"       -> Icons.Default.Inventory
    "Invoices"       -> Icons.Default.Description
    "PurchaseOrders" -> Icons.Default.ShoppingCart
    "SalesOrders"    -> Icons.Default.ShoppingBag
    "Campaigns"      -> Icons.Default.Campaign
    "Cases"          -> Icons.Default.SupportAgent
    "Solutions"      -> Icons.Default.Lightbulb
    "Vendors"        -> Icons.Default.Store
    else             -> Icons.Default.Folder
}

// ─── Route helper ─────────────────────────────────────────────────────────────

private fun moduleRoute(apiName: String): String =
    Screen.ModuleList.createRoute(apiName)

// ─── CrmBottomBar ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmBottomBar(
    navController: NavController,
    currentRoute : String?,
    vm           : NavBarViewModel = hiltViewModel(),
) {
    val allModules    by vm.modules.collectAsState()
    var showMoreSheet by remember { mutableStateOf(false) }

    val resolvedModules = allModules ?: fallbackNavModules

    // First 4 modules go in the bottom bar; rest go in the "More" sheet
    val pinnedModules = resolvedModules.take(4)
    val moreModules   = resolvedModules.drop(4)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        // ── Home (pinned) ──────────────────────────────────────────────────
        val homeRoute = Screen.Dashboard.route
        NavigationBarItem(
            selected = currentRoute == homeRoute,
            onClick  = {
                if (currentRoute != homeRoute) {
                    navController.navigate(homeRoute) {
                        popUpTo(homeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            },
            icon   = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label  = { Text("Home", fontSize = 10.sp) },
            colors = navItemColors(),
        )

        // ── Top 4 dynamic module tabs ──────────────────────────────────────
        pinnedModules.forEach { module ->
            val route    = moduleRoute(module.apiName)
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon   = { Icon(moduleIcon(module.apiName), contentDescription = module.pluralLabel) },
                label  = { Text(module.pluralLabel, fontSize = 10.sp, maxLines = 1) },
                colors = navItemColors(),
            )
        }

        // ── "More" tab — only shown when there are extra modules ───────────
        if (moreModules.isNotEmpty()) {
            NavigationBarItem(
                selected = false,
                onClick  = { showMoreSheet = true },
                icon     = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                label    = { Text("More", fontSize = 10.sp) },
                colors   = navItemColors(),
            )
        }
    }

    // ── More modules bottom sheet ──────────────────────────────────────────
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor   = MaterialTheme.colorScheme.surface,
        ) {
            Text(
                text     = "All Modules",
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            )
            HorizontalDivider()
            LazyColumn(
                modifier            = Modifier.fillMaxWidth(),
                contentPadding      = PaddingValues(bottom = 32.dp),
            ) {
                items(resolvedModules) { module ->
                    val route = moduleRoute(module.apiName)
                    ListItem(
                        headlineContent = { Text(module.pluralLabel) },
                        leadingContent  = {
                            Icon(
                                imageVector        = moduleIcon(module.apiName),
                                contentDescription = null,
                                tint               = CrmPrimary,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMoreSheet = false
                                navController.navigate(route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = CrmPrimary,
    selectedTextColor   = CrmPrimary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor      = MaterialTheme.colorScheme.surfaceVariant,
)

// ─── BottomNavItem — kept for any code that still references it ───────────────

data class BottomNavItem(
    val label : String,
    val icon  : ImageVector,
    val route : String,
)

val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Default.Home,      Screen.Dashboard.route),
    BottomNavItem("Accounts", Icons.Default.Business,  Screen.ModuleList.createRoute("Accounts")),
    BottomNavItem("Contacts", Icons.Default.Contacts,  Screen.ModuleList.createRoute("Contacts")),
    BottomNavItem("Deals",    Icons.Default.Handshake, Screen.ModuleList.createRoute("Deals")),
    BottomNavItem("Quotes",   Icons.Default.Receipt,   Screen.ModuleList.createRoute("Quotes")),
)

// ─── Filter Bottom Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmFilterSheet(
    title    : String,
    onDismiss: () -> Unit,
    onClear  : () -> Unit,
    content  : @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text       = title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f),
                )
                TextButton(onClick = onClear) {
                    Text("Clear All", color = CrmPrimary, fontSize = 13.sp)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

// ─── Filter Chip Row ──────────────────────────────────────────────────────────

@Composable
fun FilterChipRow(
    label    : String,
    options  : List<String>,
    selected : String,
    onSelect : (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick  = { onSelect(if (selected == option) "" else option) },
                    label    = { Text(option, fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrmPrimary,
                        selectedLabelColor     = Color.White,
                    ),
                )
            }
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
            color      = MaterialTheme.colorScheme.onSurface,
            modifier   = Modifier.padding(bottom = 6.dp),
        )
        Surface(
            shape          = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier       = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrmTableHeader)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text("Time",     color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("Subjects", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(2f))
                }
                if (rows.isEmpty()) {
                    repeat(3) { idx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (idx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
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
                                .background(if (idx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
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
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            text     = value,
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurface,
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
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text       = title,
            color      = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
        )
    }
}