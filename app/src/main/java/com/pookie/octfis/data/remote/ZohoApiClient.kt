package com.pookie.octfis.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pookie.octfis.data.remote.dto.FlexibleReminder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type

object ZohoApiClient {

    // Serialized token refresh — only one coroutine refreshes at a time.
    // All others wait on the Mutex and then re-read the (now-fresh) token.
    private val refreshMutex = Mutex()

    // Handles Remind_At being either a String or an Object from Zoho — safely ignores strings
    private val flexibleReminderAdapter = object : JsonDeserializer<FlexibleReminder?> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): FlexibleReminder? {
            return if (json.isJsonObject) {
                val obj = json.asJsonObject
                FlexibleReminder(
                    period = obj.get("period")?.takeIf { !it.isJsonNull }?.asString,
                    unit   = obj.get("unit")?.takeIf { !it.isJsonNull }?.asString,
                )
            } else {
                null  // Zoho sent a string — ignore it
            }
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(FlexibleReminder::class.java, flexibleReminderAdapter)
        .create()

    fun create(authManager: ZohoAuthManager): ZohoApiService {

        // ── Step 1: Attach token to every request ─────────────────────────
        // Uses runBlocking ONLY to attach an already-valid cached token.
        // getValidToken() returns immediately from cache when not expired,
        // so this runBlocking is non-blocking in the 99% case.
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking {
                refreshMutex.withLock { authManager.getValidToken() }
            } ?: throw IOException("Not authenticated — please sign in with Zoho CRM")

            chain.proceed(
                chain.request().newBuilder()
                    .header("Authorization", "Zoho-oauthtoken $token")
                    .build()
            )
        }

        // ── Step 2: On 401, refresh once and retry ────────────────────────
        // OkHttp's Authenticator is the correct hook for reactive token refresh.
        // It is called only on 401, not on every request, keeping the hot path clean.
        val tokenAuthenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                // If we've already retried once (Authorization header present and still 401), give up.
                if (response.request.header("Authorization") != null &&
                    response.priorResponse?.code == 401
                ) {
                    return null  // Triggers IOException("Too many follow-up requests")
                }

                // Serialize refresh across concurrent 401s — only one thread refreshes,
                // the rest wait and then re-read the freshly-saved token.
                val freshToken = runBlocking {
                    refreshMutex.withLock { authManager.getValidToken() }
                } ?: return null  // null → OkHttp throws; caller surfaces it as an error

                return response.request.newBuilder()
                    .header("Authorization", "Zoho-oauthtoken $freshToken")
                    .build()
            }
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(ZohoConstants.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ZohoApiService::class.java)
    }
}