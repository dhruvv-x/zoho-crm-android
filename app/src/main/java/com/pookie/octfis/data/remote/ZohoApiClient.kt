package com.pookie.octfis.data.remote

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pookie.octfis.data.remote.dto.FlexibleReminder
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type

object ZohoApiClient {

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

        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { authManager.getValidToken() }
                ?: throw IOException("Not authenticated — please sign in with Zoho CRM")

            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Zoho-oauthtoken $token")
                .build()

            val response: Response = chain.proceed(req)

            if (response.code == 401) {
                response.close()
                throw IOException("HTTP 401 — token rejected. Please sign out and sign in again.")
            }

            response
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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