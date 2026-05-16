// ui/screens/RecordListScreen.kt
package com.pookie.octfis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.RawRecord
import com.pookie.octfis.engine.list.RecordListSkeleton
import com.pookie.octfis.engine.list.RecordListUiState
import com.pookie.octfis.engine.list.RecordListViewModel
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.util.ConnectivityObserver

// ── Error classifier ──────────────────────────────────────────────────────────

private enum class ErrorKind { NETWORK, AUTH, UNKNOWN }

private fun classifyError(message: String): ErrorKind = when {
    message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect",      ignoreCase = true) ||
            message.contains("timeout",                ignoreCase = true) ||
            message.contains("SocketTimeout",          ignoreCase = true) ||
            message.contains("UnknownHost",            ignoreCase = true) ||
            message.contains("Network",                ignoreCase = true) -> ErrorKind.NETWORK

    message.contains("401", ignoreCase = true) ||
            message.contains("403", ignoreCase = true) ||
            message.contains("unauthorized", ignoreCase = true) ||
            message.contains("token",        ignoreCase = true) -> ErrorKind.AUTH

    else -> ErrorKind.UNKNOWN
}

private data class ErrorDisplay(
    val icon    : ImageVector,
    val title   : String,
    val subtitle: String,
)

private fun errorDisplay(kind: ErrorKind): ErrorDisplay = when (kind) {
    ErrorKind.NETWORK -> ErrorDisplay(
        icon     = Icons.Default.WifiOff,
        title    = "No connection",
        subtitle = "Check your internet and try again.",
    )
    ErrorKind.AUTH -> ErrorDisplay(
        icon     = Icons.Default.Lock,
        title    = "Session expired",
        subtitle = "Your session has expired. Please sign in again.",
    )
    ErrorKind.UNKNOWN -> ErrorDisplay(
        icon     = Icons.Default.ErrorOutline,
        title    = "Something went wrong",
        subtitle = "We couldn't load this data. Please try again.",
    )
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(
    message : String,
    onRetry : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kind    = classifyError(message)
    val display = errorDisplay(kind)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                imageVector        = display.icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text      = display.title,
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = display.subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Retry")
            }
        }
    }
}

// ── Offline banner ────────────────────────────────────────────────────────────

@Composable
internal fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF59E0B))   // amber-400
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = Icons.Default.WifiOff,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = "You're offline — showing cached data",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

// ── RecordListScreen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    navController        : NavController,
    moduleName           : String,
    viewModel            : RecordListViewModel,
    connectivityObserver : ConnectivityObserver,
    primaryField         : String  = "Name",
    secondaryField       : String? = null,
    avatarField          : String? = null,
    avatarColor          : Color   = MaterialTheme.colorScheme.primary,
    onRecordClick        : (zohoId: String) -> Unit = {},
    showBackButton       : Boolean = false,
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val vmPrimary   by viewModel.primaryField.collectAsStateWithLifecycle()
    val vmSecondary by viewModel.secondaryField.collectAsStateWithLifecycle()

    val resolvedPrimary   = if (primaryField  != "Name") primaryField  else vmPrimary
    val resolvedSecondary = if (secondaryField != null)  secondaryField else vmSecondary
    val resolvedAvatar    = avatarField ?: resolvedPrimary

    val listState      = rememberLazyListState()
    var searchActive   by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Offline state — starts true to avoid flicker on first frame
    val isOnline by connectivityObserver.isOnline.collectAsStateWithLifecycle(initialValue = true)

    val isRefreshing = uiState is RecordListUiState.Loading &&
            (uiState as? RecordListUiState.Success)?.records?.isNotEmpty() == true

    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) {
        if (nearBottom && !searchActive) viewModel.loadNextPage()
    }
    LaunchedEffect(searchActive) {
        if (searchActive) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            Column {
                if (searchActive) {
                    TopAppBar(
                        title = {
                            TextField(
                                value         = searchQuery,
                                onValueChange = { viewModel.setSearch(it) },
                                placeholder   = { Text("Search $moduleName…") },
                                singleLine    = true,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor   = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor   = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch  = { }),
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                searchActive = false
                                viewModel.setSearch("")
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                            }
                        },
                    )
                } else {
                    TopAppBar(
                        title = { Text(moduleName) },
                        navigationIcon = if (showBackButton) {
                            {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        } else ({}),
                        actions = {
                            if (uiState is RecordListUiState.Error) {
                                IconButton(onClick = { viewModel.refresh() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                }
                            }
                            IconButton(onClick = { searchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                    )
                }

                // Offline banner animates below the top bar
                AnimatedVisibility(
                    visible = !isOnline,
                    enter   = expandVertically(),
                    exit    = shrinkVertically(),
                ) {
                    OfflineBanner()
                }
            }
        },
        floatingActionButton = {
            if (uiState !is RecordListUiState.Error) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.ModuleCreate.createRoute(moduleName))
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add $moduleName")
                }
            }
        },
    ) { padding ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = { viewModel.refresh() },
            modifier     = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {

                is RecordListUiState.Loading -> {
                    RecordListSkeleton(rowCount = 12)
                }

                is RecordListUiState.Error -> {
                    ErrorState(
                        message  = state.message,
                        onRetry  = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is RecordListUiState.Success -> {
                    if (state.records.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Default.Inbox,
                                    contentDescription = null,
                                    modifier           = Modifier.size(64.dp),
                                    tint               = MaterialTheme.colorScheme.outlineVariant,
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text  = "No $moduleName found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state          = listState,
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            itemsIndexed(
                                items = state.records,
                                key   = { _, r -> r.id },
                            ) { _, record ->
                                RecordListItem(
                                    record         = record,
                                    primaryField   = resolvedPrimary,
                                    secondaryField = resolvedSecondary,
                                    avatarField    = resolvedAvatar,
                                    avatarColor    = avatarColor,
                                    onClick        = { onRecordClick(record.id) },
                                )
                                HorizontalDivider(
                                    modifier  = Modifier.padding(start = 72.dp),
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }

                            if (state.hasMore) {
                                item {
                                    Box(
                                        modifier         = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

// ── List item ─────────────────────────────────────────────────────────────────

@Composable
private fun RecordListItem(
    record        : RawRecord,
    primaryField  : String,
    secondaryField: String?,
    avatarField   : String,
    avatarColor   : Color,
    onClick       : () -> Unit,
) {
    val primary   = record.fields[primaryField]?.toString()?.ifBlank { "—" } ?: "—"
    val secondary = secondaryField?.let {
        record.fields[it]?.toString()?.ifBlank { null }
    }
    val initials = primary
        .trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifEmpty { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = initials,
                color      = Color.White,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = primary,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (secondary != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = secondary,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}