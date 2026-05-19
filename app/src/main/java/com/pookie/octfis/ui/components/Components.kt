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
import androidx.navigation.compose.currentBackStackEntryAsState
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

/**
 * Standard Zoho modules matched by API name (case-insensitive).
 * Returns null if not a known standard module so the caller
 * can fall through to knownCustomIcon() or prefix-based fallback.
 */
private fun knownModuleIcon(apiName: String): ImageVector? =
    when (apiName.trim().lowercase().replace(" ", "_")) {
        "accounts"                          -> Icons.Default.Domain
        "contacts"                          -> Icons.Default.PermContactCalendar
        "deals", "potentials"               -> Icons.Default.Work
        "leads"                             -> Icons.Default.PersonSearch
        "quotes"                            -> Icons.Default.RequestQuote
        "tasks"                             -> Icons.Default.AssignmentTurnedIn
        "meetings", "events"                -> Icons.Default.Groups
        "calls"                             -> Icons.Default.Phone
        "products"                          -> Icons.Default.Inventory2
        "invoices"                          -> Icons.Default.Receipt
        "purchase_orders", "purchaseorders" -> Icons.Default.LocalShipping
        "sales_orders", "salesorders"       -> Icons.Default.ShoppingBag
        "campaigns"                         -> Icons.Default.Campaign
        "cases"                             -> Icons.Default.Headset
        "solutions"                         -> Icons.Default.Lightbulb
        "vendors"                           -> Icons.Default.Storefront
        "price_books", "pricebooks"         -> Icons.Default.LocalOffer
        "services"                          -> Icons.Default.MiscellaneousServices
        "appointments"                      -> Icons.Default.CalendarMonth
        "documents"                         -> Icons.Default.Article
        "reports"                           -> Icons.Default.BarChart
        "analytics"                         -> Icons.Default.Analytics
        "forecasts"                         -> Icons.Default.TrendingUp
        "projects"                          -> Icons.Default.AccountTree
        "feeds"                             -> Icons.Default.DynamicFeed
        "salesinbox"                        -> Icons.Default.AllInbox
        "social"                            -> Icons.Default.Share
        "visits"                            -> Icons.Default.Place
        "commandcenter"                     -> Icons.Default.Hub
        "approvals"                         -> Icons.Default.HowToVote
        "google_adwords", "googleadwords"   -> Icons.Default.Search
        "hubspot0"                          -> Icons.Default.Hub
        "contracts"                         -> Icons.Default.Gavel
        else                                -> null
    }

/**
 * Your org's custom modules, web tabs, and linking modules —
 * matched by their display (plural) label (case-insensitive).
 * Returns null if unrecognised so the caller falls through to
 * the prefix-based type detection.
 */
private fun knownCustomIcon(pluralLabel: String): ImageVector? =
    when (pluralLabel.trim().lowercase()) {
        // ── Custom modules ────────────────────────────────────────────────
        "productions"               -> Icons.Default.Build
        "clinical notes"            -> Icons.Default.LocalHospital
        "accounts2"                 -> Icons.Default.Domain
        "contact vs"                -> Icons.Default.CompareArrows
        "buyer11"                   -> Icons.Default.ShoppingCart
        "avs leads"                 -> Icons.Default.PersonSearch
        "indiamart logs"            -> Icons.Default.History
        "exporters india logs"      -> Icons.Default.ImportExport
        "twilio history"            -> Icons.Default.History
        "twilio messages"           -> Icons.Default.Sms
        "zohosign recipients"       -> Icons.Default.Draw
        "countries"                 -> Icons.Default.Public
        "states"                    -> Icons.Default.LocationCity
        "cities"                    -> Icons.Default.LocationCity
        "zohosign documents"        -> Icons.Default.Draw
        "zohosign document events"  -> Icons.Default.EventNote
        "insurance"                 -> Icons.Default.HealthAndSafety
        "targeted products"         -> Icons.Default.Inventory2
        "purchase requisitions"     -> Icons.Default.AddShoppingCart
        "doctor"                    -> Icons.Default.LocalHospital
        "chittals"                  -> Icons.Default.ReceiptLong
        "chittal details rsm"       -> Icons.Default.ReceiptLong
        "pmjby insurances"          -> Icons.Default.HealthAndSafety
        "architects"                -> Icons.Default.Architecture
        "purchase"                  -> Icons.Default.ShoppingCart
        "piller"                    -> Icons.Default.Straighten
        "p deals"                   -> Icons.Default.Work
        "p vendors"                 -> Icons.Default.Storefront
        "p products"                -> Icons.Default.Inventory2
        "dealers"                   -> Icons.Default.Store
        "victory"                   -> Icons.Default.EmojiEvents
        "cush"                      -> Icons.Default.Inventory2
        "demo"                      -> Icons.Default.Science
        "faw"                       -> Icons.Default.Widgets
        "fap"                       -> Icons.Default.Widgets
        "wishdata"                  -> Icons.Default.Bookmarks
        "odden"                     -> Icons.Default.Widgets
        "specification"             -> Icons.Default.Description
        "my jobs"                   -> Icons.Default.HowToVote
        // ── Linking modules ───────────────────────────────────────────────
        "products x chittals",
        "p product x vendor",
        "contacts x accounts"       -> Icons.Default.Link
        // ── Web tabs ──────────────────────────────────────────────────────
        "shopify"                   -> Icons.Default.ShoppingBag
        "twilio inbox"              -> Icons.Default.AllInbox
        "routeiq"                   -> Icons.Default.Route
        "leadchain"                 -> Icons.Default.PersonSearch
        "mapsly"                    -> Icons.Default.Map
        "zoho analytics"            -> Icons.Default.Analytics
        "track my location",
        "tag my location"           -> Icons.Default.LocationOn
        "youtube"                   -> Icons.Default.VideoLibrary
        "zoho form"                 -> Icons.Default.ListAlt
        "octfis"                    -> Icons.Default.Apps
        "testing"                   -> Icons.Default.BugReport
        "school forms"              -> Icons.Default.School
        "allforms"                  -> Icons.Default.ListAlt
        "map"                       -> Icons.Default.Map
        "zoho books"                -> Icons.Default.MenuBook
        "lead dashboard"            -> Icons.Default.Dashboard
        "whatsapp"                  -> Icons.Default.Chat
        else                        -> null
    }

// Fallback tint colors by module type
private val customModuleColor  = Color(0xFF5F6368) // gray  — unknown CustomModule*
private val webTabColor        = Color(0xFF0077B6) // blue  — unknown WebTab*
private val linkingModuleColor = Color(0xFF2D9D78) // teal  — unknown LinkingModule*

/**
 * Returns the icon for any module:
 *   1. Standard Zoho module  → matched by apiName
 *   2. Known org module      → matched by pluralLabel
 *   3. WebTab* / LinkingModule* / CustomTab* → type-based fallback icon
 *   4. Everything else       → Widgets
 */
fun moduleIcon(apiName: String, sequence: Int = 0, pluralLabel: String = ""): ImageVector {
    knownModuleIcon(apiName)?.let { return it }
    knownCustomIcon(pluralLabel)?.let { return it }
    return when {
        apiName.startsWith("WebTab",        ignoreCase = true) -> Icons.Default.Language
        apiName.startsWith("LinkingModule", ignoreCase = true) -> Icons.Default.Link
        apiName.startsWith("CustomTab",     ignoreCase = true) -> Icons.Default.Language
        else                                                    -> Icons.Default.Widgets
    }
}

/**
 * Returns the tint color for any module icon.
 * Standard and named org modules → CrmPrimary (blue).
 * Unknown types → color by type (gray / blue / teal).
 */
fun moduleIconColor(apiName: String, sequence: Int = 0, pluralLabel: String = ""): Color {
    if (knownModuleIcon(apiName) != null || knownCustomIcon(pluralLabel) != null) return CrmPrimary
    return when {
        apiName.startsWith("WebTab",        ignoreCase = true) -> webTabColor
        apiName.startsWith("LinkingModule", ignoreCase = true) -> linkingModuleColor
        else                                                    -> customModuleColor
    }
}

// ─── Route helper ─────────────────────────────────────────────────────────────

private fun moduleRoute(apiName: String): String =
    Screen.ModuleList.createRoute(apiName)

// ─── CrmBottomBar ─────────────────────────────────────────────────────────────
//
// KEY INSIGHT: NavBackStackEntry.destination.route always returns the TEMPLATE
// string ("module/{moduleName}/list"), never the filled value. To know which
// module is active we must read NavBackStackEntry.arguments instead.
// The argument key "moduleName" is declared in NavGraph for all Module* routes.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmBottomBar(
    navController: NavController,
    currentRoute : String?,   // still accepted so callers need no changes
    vm           : NavBarViewModel = hiltViewModel(),
) {
    val navBackStack by navController.currentBackStackEntryAsState()

    // destination.route = template e.g. "module/{moduleName}/list"
    val routeTemplate = navBackStack?.destination?.route

    // arguments holds the FILLED values e.g. arguments["moduleName"] = "Accounts"
    val activeModuleName: String? = navBackStack?.arguments?.getString("moduleName")
    val allModules    by vm.modules.collectAsState()
    var showMoreSheet by remember { mutableStateOf(false) }

    // Deduplicate by apiName — Zoho can occasionally return the same module twice
    // (e.g. Accounts at sequence 1 and again at a higher sequence).
    val resolvedModules = (allModules ?: fallbackNavModules)
        .distinctBy { it.apiName }

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
            selected = routeTemplate == homeRoute,
            onClick  = {
                if (routeTemplate != homeRoute) {
                    navController.navigate(homeRoute) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            inclusive = false
                        }
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
            // activeModuleName comes from navBackStack.arguments — the actual
            // filled value ("Accounts"), not the template ("{moduleName}").
            // This also stays highlighted when inside detail/edit of that module.
            val selected = activeModuleName == module.apiName
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (activeModuleName != module.apiName) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = {
                    Icon(
                        imageVector        = moduleIcon(module.apiName, module.sequence, module.pluralLabel),
                        contentDescription = module.pluralLabel,
                    )
                },
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
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                items(resolvedModules) { module ->
                    val route = moduleRoute(module.apiName)
                    ListItem(
                        headlineContent = { Text(module.pluralLabel) },
                        leadingContent  = {
                            Icon(
                                imageVector        = moduleIcon(module.apiName, module.sequence, module.pluralLabel),
                                contentDescription = null,
                                tint               = moduleIconColor(module.apiName, module.sequence, module.pluralLabel),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMoreSheet = false
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                        inclusive = false
                                    }
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
    BottomNavItem("Accounts", Icons.Default.Domain,    Screen.ModuleList.createRoute("Accounts")),
    BottomNavItem("Contacts", Icons.Default.PermContactCalendar, Screen.ModuleList.createRoute("Contacts")),
    BottomNavItem("Deals",    Icons.Default.Work,      Screen.ModuleList.createRoute("Deals")),
    BottomNavItem("Quotes",   Icons.Default.RequestQuote, Screen.ModuleList.createRoute("Quotes")),
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