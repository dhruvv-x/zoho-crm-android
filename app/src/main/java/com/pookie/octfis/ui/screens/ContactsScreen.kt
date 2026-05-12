package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.data.model.Account
import com.pookie.octfis.data.model.FakeData
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.*

@Composable
fun ContactsScreen(navController: NavController) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Scaffold(
        bottomBar = { CrmBottomBar(navController, currentRoute) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.CreateContact.route) },
                containerColor = CrmPrimary,
                contentColor   = Color.White,
                shape          = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Contact")
            }
        },
        containerColor = CrmBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                    text       = "Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                )
            }

            // ── List ──────────────────────────────────────────────────────
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(FakeData.accounts) { _, contact ->
                    ContactRow(contact) {
                        /* TODO: navigate to ContactDetail */
                    }
                    HorizontalDivider(color = CrmDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Account, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Avatar ────────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CrmAccent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = contact.name.first().uppercaseChar().toString(),
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        // ── Name & Phone ──────────────────────────────────────────────────
        Column {
            Text(
                text       = contact.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = CrmOnSurface,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Phone,
                    contentDescription = null,
                    tint               = CrmSubtext,
                    modifier           = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = contact.phone,
                    fontSize = 12.sp,
                    color    = CrmSubtext,
                )
            }
        }
    }
}