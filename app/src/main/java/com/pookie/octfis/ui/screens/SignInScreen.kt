package com.pookie.octfis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pookie.octfis.R
import com.pookie.octfis.data.remote.AuthState
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(navController: NavController) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    val loginSuccess by AuthState.loginSuccess.collectAsState()
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            AuthState.reset()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.SignIn.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ZohoServiceLocator.getTokenStore().isLoggedIn()) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.SignIn.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Logo ──────────────────────────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.octfis_logo),
            contentDescription = "Octfis Techno LLP",
            modifier           = Modifier
                .fillMaxWidth(0.72f)   // 72% of screen width — adjust to taste
                .aspectRatio(2.1f),    // matches the logo's natural width:height ratio
        )
        // ─────────────────────────────────────────────────────────────────

        Spacer(Modifier.height(36.dp))

        Text("Login an account", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Enter your email and password to sign in for this app",
            fontSize  = 13.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it },
            placeholder     = { Text("email@domain.com", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape           = RoundedCornerShape(8.dp),
            modifier        = Modifier.fillMaxWidth(),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = CrmPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            placeholder          = { Text("Enter your password", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape                = RoundedCornerShape(8.dp),
            modifier             = Modifier.fillMaxWidth(),
            colors               = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = CrmPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = { navController.navigate(Screen.Dashboard.route) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
        ) {
            Text("Continue", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            Text("  or  ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick  = {
                ZohoServiceLocator.getAuthManager().launchAuthFlow(context)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border   = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text("⚙ ", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text("Continue with Zoho CRM", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = buildAnnotatedString {
                append("By clicking continue, you agree to our ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = CrmPrimary)) {
                    append("Terms of Service")
                }
                append(" and ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = CrmPrimary)) {
                    append("Privacy Policy")
                }
            },
            fontSize  = 11.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}