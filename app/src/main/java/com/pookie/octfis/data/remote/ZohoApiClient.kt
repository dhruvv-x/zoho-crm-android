package com.pookie.octfis.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object ZohoApiClient {

    fun create(authManager: ZohoAuthManager): ZohoApiService {

        val authInterceptor = Interceptor { chain ->
            // FIX: throw early instead of sending "Zoho-oauthtoken null" → 401
            val token = runBlocking { authManager.getValidToken() }
                ?: throw IOException("Not authenticated — please sign in with Zoho CRM")

            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Zoho-oauthtoken $token")
                .build()

            val response: Response = chain.proceed(req)

            // FIX: surface 401 as a clear exception so AccountsViewModel shows it
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZohoApiService::class.java)
    }
}