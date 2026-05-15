package com.pookie.octfis.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.pookie.octfis.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

class ZohoAuthManager(
    private val context: Context,
    private val tokenStore: TokenStore,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(ZohoConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private val http = OkHttpClient()

    // ── PKCE ──────────────────────────────────────────────────────────────────

    private fun generateVerifier(): String {
        val b = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun challenge(verifier: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(d, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    // ── Step 1: Open Zoho login page ──────────────────────────────────────────

    fun launchAuthFlow(activityContext: Context) {
        val verifier = generateVerifier()
        prefs.edit().putString(ZohoConstants.KEY_CODE_VERIFIER, verifier).apply()
        Log.d("OctfisAuth", "launchAuthFlow — verifier saved, launching browser")

        val url = Uri.parse(ZohoConstants.AUTH_BASE_URL + "auth").buildUpon()
            .appendQueryParameter("response_type",         "code")
            .appendQueryParameter("client_id",             BuildConfig.ZOHO_CLIENT_ID)
            .appendQueryParameter("scope",                 ZohoConstants.SCOPE)
            .appendQueryParameter("redirect_uri",          ZohoConstants.REDIRECT_URI)
            .appendQueryParameter("access_type",           "offline")
            .appendQueryParameter("code_challenge",        challenge(verifier))
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

        Log.d("OctfisAuth", "auth url: $url")
        CustomTabsIntent.Builder().build().launchUrl(activityContext, url)
    }

    // ── Step 2: Exchange auth code for tokens ─────────────────────────────────

    suspend fun handleCallback(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val code = uri.getQueryParameter("code")
        Log.d("OctfisAuth", "handleCallback — code present: ${code != null}, code prefix: ${code?.take(15)}")

        if (code == null) return@withContext false

        val verifier = prefs.getString(ZohoConstants.KEY_CODE_VERIFIER, null)
        Log.d("OctfisAuth", "handleCallback — verifier present: ${verifier != null}")

        if (verifier == null) return@withContext false

        val body = FormBody.Builder()
            .add("grant_type",    "authorization_code")
            .add("client_id",    BuildConfig.ZOHO_CLIENT_ID)
            .add("redirect_uri", ZohoConstants.REDIRECT_URI)
            .add("code",         code)
            .add("code_verifier", verifier)
            .build()

        runCatching {
            val req = Request.Builder().url(ZohoConstants.AUTH_BASE_URL + "token").post(body).build()
            Log.d("OctfisAuth", "token request url: ${req.url}")
            val res  = http.newCall(req).execute()
            val raw  = res.body!!.string()
            Log.d("OctfisAuth", "token response [${res.code}]: $raw")

            val json = JSONObject(raw)

            if (json.has("error")) {
                Log.e("OctfisAuth", "Zoho token error: ${json.getString("error")}")
                return@runCatching false
            }

            tokenStore.save(
                json.getString("access_token"),
                json.getString("refresh_token"),
                json.getLong("expires_in"),
            )
            Log.d("OctfisAuth", "tokens saved successfully ✅")
            true
        }.onFailure { e ->
            Log.e("OctfisAuth", "token exchange exception: ${e.message}", e)
        }.getOrDefault(false)
    }

    // ── Step 3: Return a valid token, refreshing if needed ────────────────────
    //
    // BUG FIX: old logic called isExpired() first — but isExpired() returns true
    // when no token exists at all (expiresAt defaults to 0L), causing it to
    // attempt a refresh on a fresh/cleared install and fail with "Not authenticated".
    //
    // Correct order:
    //   1. Check we have a refresh token at all — if not, user is logged out.
    //   2. If access token exists and is not expired, return it immediately.
    //   3. Otherwise refresh using the refresh token.

    suspend fun getValidToken(): String? = withContext(Dispatchers.IO) {
        // No refresh token → not logged in at all
        val refreshToken = tokenStore.getRefreshToken()
            ?: return@withContext null

        // Access token still valid → return it directly
        val accessToken = tokenStore.getAccessToken()
        if (accessToken != null && !tokenStore.isExpired()) {
            Log.d("OctfisAuth", "getValidToken — returning cached access token")
            return@withContext accessToken
        }

        // Token expired (or missing) → refresh it
        Log.d("OctfisAuth", "getValidToken — token expired, refreshing")
        val body = FormBody.Builder()
            .add("grant_type",    "refresh_token")
            .add("client_id",    BuildConfig.ZOHO_CLIENT_ID)
            .add("refresh_token", refreshToken)
            .build()

        runCatching {
            val res = http.newCall(
                Request.Builder().url(ZohoConstants.AUTH_BASE_URL + "token").post(body).build()
            ).execute()
            val raw  = res.body!!.string()
            Log.d("OctfisAuth", "refresh response [${res.code}]: $raw")
            val json = JSONObject(raw)

            if (json.has("error")) {
                Log.e("OctfisAuth", "refresh error: ${json.getString("error")}")
                return@runCatching null
            }

            val newToken = json.getString("access_token")
            tokenStore.saveAccessOnly(newToken, json.getLong("expires_in"))
            Log.d("OctfisAuth", "token refreshed successfully ✅")
            newToken
        }.onFailure { e ->
            Log.e("OctfisAuth", "refresh exception: ${e.message}", e)
        }.getOrNull()
    }

    suspend fun logout() = tokenStore.clear()
}