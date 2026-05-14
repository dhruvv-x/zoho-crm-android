package com.pookie.octfis.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.Meeting
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingListScreen(
    navController: NavController,
    vm: MeetingsViewModel = viewModel(),
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val uiState      by vm.uiState.collectAsState()
    val searchQuery  by vm.searchQuery.collectAsState()
    val actionState  by vm.actionState.collectAsState()
    val listState     = rememberLazyListState()
    var searchActive  by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val snackbarHost   = remember { SnackbarHostState() }

    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) { if (nearBottom && !searchActive) vm.loadNextPage() }
    LaunchedEffect(searchActive) { if (searchActive) focusRequester.requestFocus() }
    LaunchedEffect(actionState) {
        if (actionState is MeetingActionState.Error) {
            snackbarHost.showSnackbar((actionState as MeetingActionState.Error).message)
            vm.resetActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar    = { CrmBottomBar(navController, currentRoute) },
        floatingActionButton = {
            if (!searchActive) {
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.CreateMeeting.route) },
                    containerColor = CrmPrimary,
                    contentColor   = Color.White,
                    shape          = CircleShape,
                ) { Icon(Icons.Default.Add, "Create Meeting") }
            }
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (searchActive) {
                    IconButton(onClick = { searchActive = false; vm.setSearch("") }) {
                        Icon(Icons.Default.ArrowBack, "Close Search", tint = CrmOnSurface)
                    }
                    TextField(
                        value         = searchQuery,
                        onValueChange = { vm.setSearch(it) },
                        placeholder   = { Text("Search meetings…", fontSize = 14.sp, color = CrmSubtext) },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f).focusRequester(focusRequester),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {}),
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.setSearch("") }) {
                            Icon(Icons.Default.Close, "Clear", tint = CrmSubtext)
                        }
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Groups, null, tint = CrmPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Meetings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    if (uiState is MeetingsUiState.Success) {
                        Text(
                            text     = "${(uiState as MeetingsUiState.Success).meetings.size} loaded",
                            fontSize = 11.sp,
                            color    = CrmSubtext,
                        )
                    }
                    IconButton(onClick = { searchActive = true }) {
                        Icon(Icons.Default.Search, "Search", tint = CrmSubtext)
                    }
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = CrmSubtext)
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────────
            when (val s = uiState) {
                is MeetingsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CrmPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading from Zoho CRM…", color = CrmSubtext, fontSize = 13.sp)
                        }
                    }
                }

                is MeetingsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudOff, null, tint = CrmError, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(s.message, color = CrmSubtext, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = CrmPrimary)) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is MeetingsUiState.Success -> {
                    if (s.meetings.isEmpty() && searchQuery.isNotBlank()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results for \"$searchQuery\"", color = CrmSubtext, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            state          = listState,
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            itemsIndexed(s.meetings, key = { _, m -> m.id }) { _, meeting ->
                                MeetingSwipeRow(
                                    meeting  = meeting,
                                    onDelete = { vm.deleteMeeting(meeting.id) },
                                    onClick  = { navController.navigate(Screen.MeetingDetail.createRoute(meeting.id)) },
                                    onEdit   = { navController.navigate(Screen.EditMeeting.createRoute(meeting.id)) },
                                )
                                HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)
                            }
                            if (s.hasMore && searchQuery.isBlank()) {
                                item {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CrmPrimary, strokeWidth = 2.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingSwipeRow(
    meeting : Meeting,
    onDelete: () -> Unit,
    onClick : () -> Unit,
    onEdit  : () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) showDeleteDialog = true
            false
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Meeting?") },
            text    = { Text("\"${meeting.title}\" will be permanently deleted from Zoho CRM.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = CrmError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    SwipeToDismissBox(
        state             = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) CrmError else Color.Transparent,
                label = "swipe-bg",
            )
            Box(
                Modifier.fillMaxSize().background(color).padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        },
    ) {
        MeetingRow(meeting, onClick, onEdit)
    }
}

@Composable
private fun MeetingRow(meeting: Meeting, onClick: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(CrmSurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Groups, null, tint = CrmPrimary, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(meeting.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CrmOnSurface)
            Spacer(Modifier.height(2.dp))
            if (meeting.startDateTime.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = CrmSubtext, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(meeting.startDateTime.take(16).replace("T", " "), fontSize = 11.sp, color = CrmSubtext)
                }
            }
            if (meeting.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = CrmSubtext, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(meeting.location, fontSize = 11.sp, color = CrmPrimary)
                }
            }
        }

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit", tint = CrmSubtext, modifier = Modifier.size(18.dp))
        }
    }
}