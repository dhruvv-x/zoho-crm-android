package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.Account
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*

@Composable
fun AccountsScreen(
    navController: NavController,
    vm: AccountsViewModel = viewModel(),
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val uiState      by vm.uiState.collectAsState()
    val listState     = rememberLazyListState()

    // Trigger next page when near the bottom
    val nearBottom by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 8 && total > 0
        }
    }
    LaunchedEffect(nearBottom) {
        if (nearBottom) vm.loadNextPage()
    }

    Scaffold(
        bottomBar = { CrmBottomBar(navController, currentRoute) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.CreateAccount.route) },
                containerColor = CrmPrimary,
                contentColor   = Color.White,
                shape          = CircleShape,
            ) { Icon(Icons.Default.Add, "Create Account") }
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Top Bar
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Menu, "Menu", tint = CrmOnSurface)
                Spacer(Modifier.width(12.dp))
                Text("Accounts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                if (uiState is AccountsUiState.Success) {
                    Text(
                        text     = "${(uiState as AccountsUiState.Success).accounts.size} loaded",
                        fontSize = 11.sp,
                        color    = CrmSubtext,
                    )
                }
                IconButton(onClick = { vm.load() }) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = CrmSubtext)
                }
            }

            when (val s = uiState) {

                is AccountsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CrmPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading from Zoho CRM…", color = CrmSubtext, fontSize = 13.sp)
                        }
                    }
                }

                is AccountsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                is AccountsUiState.Success -> {
                    LazyColumn(
                        state          = listState,
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(s.accounts) { _, account ->
                            AccountRow(account) {
                                navController.navigate(Screen.AccountDetail.createRoute(account.id))
                            }
                            HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)
                        }
                        if (s.hasMore) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(24.dp),
                                        color       = CrmPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountRow(account: Account, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(CrmAccent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text       = account.name.ifEmpty { "(No Name)" },
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = CrmOnSurface,
            )
            Spacer(Modifier.height(2.dp))
            if (account.accountNo.isNotEmpty()) {
                Text(account.accountNo, fontSize = 11.sp, color = CrmPrimary)
                Spacer(Modifier.height(1.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = CrmSubtext, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = account.phone.ifEmpty { "No phone" },
                    fontSize = 12.sp,
                    color    = CrmSubtext,
                )
            }
        }
    }
}