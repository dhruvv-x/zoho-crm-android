package com.pookie.octfis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.pookie.octfis.navigation.Screen
import com.pookie.octfis.ui.theme.*

@Composable
fun SignInScreen(navController: NavController) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Logo ──────────────────────────────────────────────────────────
        Text(
            text       = "🦅 OCTFIS\nTECHNO LLP",
            color      = CrmPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            textAlign  = TextAlign.Center,
            lineHeight = 26.sp,
        )

        Spacer(Modifier.height(36.dp))

        // ── Title ─────────────────────────────────────────────────────────
        Text(
            text       = "Login an account",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = CrmOnSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Enter your email and password to sign in for this app",
            fontSize  = 13.sp,
            color     = CrmSubtext,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        // ── Email ─────────────────────────────────────────────────────────
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            placeholder   = { Text("email@domain.com", color = CrmSubtext, fontSize = 14.sp) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape         = RoundedCornerShape(8.dp),
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = CrmPrimary,
                unfocusedBorderColor = CrmDivider,
            ),
        )

        Spacer(Modifier.height(12.dp))

        // ── Password ──────────────────────────────────────────────────────
        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            placeholder          = { Text("Enter your password", color = CrmSubtext, fontSize = 14.sp) },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape                = RoundedCornerShape(8.dp),
            modifier             = Modifier.fillMaxWidth(),
            colors               = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = CrmPrimary,
                unfocusedBorderColor = CrmDivider,
            ),
        )

        Spacer(Modifier.height(20.dp))

        // ── Continue Button ───────────────────────────────────────────────
        Button(
            onClick  = { navController.navigate(Screen.Dashboard.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrmPrimary),
        ) {
            Text("Continue", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(16.dp))

        // ── Divider ───────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CrmDivider)
            Text("  or  ", color = CrmSubtext, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = CrmDivider)
        }

        Spacer(Modifier.height(16.dp))

        // ── Zoho CRM Button ───────────────────────────────────────────────
        OutlinedButton(
            onClick  = { /* TODO: Zoho OAuth */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmOnSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrmDivider),
        ) {
            Text("⚙ ", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text("Continue with Zoho CRM", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Spacer(Modifier.height(28.dp))

        // ── Legal Text ────────────────────────────────────────────────────
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
            color     = CrmSubtext,
            textAlign = TextAlign.Center,
        )
    }
}