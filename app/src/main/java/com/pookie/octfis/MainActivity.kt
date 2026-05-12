package com.pookie.octfis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.pookie.octfis.navigation.NavGraph
import com.pookie.octfis.ui.theme.OctfisCRMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OctfisCRMTheme {
                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}