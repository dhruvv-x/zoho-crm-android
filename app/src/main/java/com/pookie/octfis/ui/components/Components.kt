// ui/components/Components.kt
package com.pookie.octfis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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

/**
 * Thin @HiltViewModel that owns the ModuleEngine call.
 * Lives as long as the nav host — survives recomposition.
 * On success: emits the ordered ActiveModule list.
 * On failure: emits null → CrmBottomBar falls back to hardcoded tabs.
 */
@HiltViewModel
class NavBarViewModel @Inject constructor(
    private val moduleEngine: ModuleEngine,
) : ViewModel() {

    private val _modules = MutableStateFlow<List<ActiveModule>?>(null)
    val modules: StateFlow<List<ActiveModule>?> = _modules

    init {
        viewModelScope.launch {
            moduleEngine.getActiveModules()
                .onSuccess { _modules.value = it }
            // on failure leave null → fallback nav renders
        }
    }
}

// ─── Fallback static items (used when API hasn't loaded yet / fails) ──────────

private val fallbackNavModules = listOf(
    ActiveModule("Accounts", "Accounts", "Account",  1),
    ActiveModule("Contacts", "Contacts", "Contact",  2),
    ActiveModule("Deals",    "Deals",    "Deal",     3),
    ActiveModule("Quotes",   "Quotes",   "Quote",    4),
)

// ─── Icon mapping ─────────────────────────────────────────────────────────────

private fun moduleIcon(apiName: String): ImageVector = when (apiName) {
    "Accounts"      -> Icons.Default.Business
    "Contacts"      -> Icons.Default.Contacts
    "Deals",
    "Potentials"    -> Icons.Default.Handshake
    "Quotes"        -> Icons.Default.Receipt
    "Leads"         -> Icons.Default.PersonAdd
    "Products"      -> Icons.Default.Inventory
    "Invoices"      -> Icons.Default.Description
    "PurchaseOrders"-> Icons.Default.ShoppingCart
    "SalesOrders"   -> Icons.Default.ShoppingBag
    "Campaigns"     -> Icons.Default.Campaign
    "Cases"         -> Icons.Default.SupportAgent
    "Solutions"     -> Icons.Default.Lightbulb
    "Vendors"       -> Icons.Default.Store
    else            -> Icons.Default.Folder
}

// ─── Route helper ─────────────────────────────────────────────────────────────

/**
 * Maps a module's apiName to its bottom-nav route string.
 * Uses the unified ModuleList pattern — no hardcoded per-module routes.
 */
private fun moduleRoute(apiName: String): String =
    Screen.ModuleList.createRoute(apiName)

// ─── CrmBottomBar ─────────────────────────────────────────────────────────────

/**
 * Signature intentionally unchanged — all 9 existing call-sites compile as-is.
 *
 * Behaviour:
 *  1. Home tab is always pinned at position 0.
 *  2. Up to [ModuleEngine.MAX_NAV_TABS - 1] (= 4) dynamic module tabs follow,
 *     ordered by Zoho's sequence_number.
 *  3. While the API call is in-flight (modules == null) the fallback list
 *     renders so the nav is never empty or invisible.
 *  4. If the API call fails, modules stays null → fallback list is shown
 *     indefinitely (zero regression from old behaviour).
 */
@Composable
fun CrmBottomBar(
    navController: NavController,
    currentRoute : String?,
    vm           : NavBarViewModel = hiltViewModel(),
) {
    val dynamicModules by vm.modules.collectAsState()

    // Resolve the tab list — always has Home + up to 4 module tabs
    val moduleList = (dynamicModules ?: fallbackNavModules)
        .take(ModuleEngine.MAX_NAV_TABS - 1)   // cap at 4 module tabs

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

        // ── Dynamic module tabs ────────────────────────────────────────────
        moduleList.forEach { module ->
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
                icon  = { Icon(moduleIcon(module.apiName), contentDescription = module.pluralLabel) },
                label = { Text(module.pluralLabel, fontSize = 10.sp, maxLines = 1) },
                colors = navItemColors(),
            )
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

// bottomNavItems is kept so any remaining import references compile.
// CrmBottomBar no longer uses it — it drives from ActiveModule instead.
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

// ─── Filter Chip Row (horizontally scrollable) ────────────────────────────────

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