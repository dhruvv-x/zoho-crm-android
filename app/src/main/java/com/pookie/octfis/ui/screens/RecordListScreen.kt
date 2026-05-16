// ui/screens/RecordListScreen.kt
package com.pookie.octfis.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pookie.octfis.data.repository.RawRecord
import com.pookie.octfis.engine.list.RecordListUiState
import com.pookie.octfis.engine.list.RecordListViewModel
import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.navigation.Screen

/**
 * Generic record list screen — works for any Zoho module.
 *
 * @param moduleName        Zoho API module name, e.g. "Accounts", "Contacts"
 * @param primaryField      apiName of the field shown as the list item title (default "Name")
 * @param secondaryField    apiName of the field shown as subtitle (optional)
 * @param avatarField       apiName used to generate avatar initials (defaults to primaryField)
 * @param avatarColor       background color for the avatar circle
 * @param onRecordClick     called with the record's zohoId when a row is tapped
 * @param showBackButton    show ← arrow in the TopAppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    navController   : NavController,
    moduleName      : String,
    viewModel       : RecordListViewModel,
    primaryField    : String           = "Name",
    secondaryField  : String?          = null,
    avatarField     : String?          = null,
    avatarColor     : Color            = MaterialTheme.colorScheme.primary,
    onRecordClick   : (zohoId: String) -> Unit = {},
    showBackButton  : Boolean          = false,
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val fields      by viewModel.fields.collectAsStateWithLifecycle()

    val listState      = rememberLazyListState()
    var searchActive   by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val isRefreshing = uiState is RecordListUiState.Loading

    // Infinite scroll trigger
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
            if (searchActive) {
                // ── Search bar ────────────────────────────────────────────
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
                            colors        = TextFieldDefaults.colors(
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch  = { /* keep open */ }),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            searchActive = false
                            viewModel.setSearch("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                        }
                    },
                )
            } else {
                // ── Normal bar ────────────────────────────────────────────
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
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.DynamicCreate.createRoute(moduleName))
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add $moduleName")
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
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is RecordListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint   = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text  = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is RecordListUiState.Success -> {
                    if (state.records.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint     = MaterialTheme.colorScheme.outlineVariant,
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
                            state         = listState,
                            modifier      = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            itemsIndexed(
                                items = state.records,
                                key   = { _, r -> r.id },
                            ) { _, record ->
                                RecordListItem(
                                    record        = record,
                                    primaryField  = primaryField,
                                    secondaryField = secondaryField,
                                    avatarField   = avatarField ?: primaryField,
                                    avatarColor   = avatarColor,
                                    onClick       = { onRecordClick(record.id) },
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
                                        modifier        = Modifier
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
        // Avatar circle
        Box(
            modifier        = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text      = initials,
                color     = Color.White,
                fontSize  = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = primary,
                style     = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
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
            imageVector     = Icons.Default.ChevronRight,
            contentDescription = null,
            tint            = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}