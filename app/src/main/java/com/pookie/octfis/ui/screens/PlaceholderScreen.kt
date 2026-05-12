package com.pookie.octfis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pookie.octfis.ui.components.CrmBottomBar
import com.pookie.octfis.ui.theme.CrmBackground
import com.pookie.octfis.ui.theme.CrmPrimary
import com.pookie.octfis.ui.theme.CrmSubtext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(navController: NavController, title: String) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title  = {
                    Text(
                        text       = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 17.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar      = { CrmBottomBar(navController, currentRoute) },
        containerColor = CrmBackground,
    ) { padding ->
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector        = Icons.Default.Construction,
                    contentDescription = null,
                    tint               = CrmPrimary,
                    modifier           = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text     = "$title coming soon",
                    fontSize = 16.sp,
                    color    = CrmSubtext,
                )
            }
        }
    }
}