package com.pookie.octfis.data.remote

import com.pookie.octfis.data.remote.dto.*
import retrofit2.http.*

interface ZohoApiService {

    // ── LIST ─────────────────────────────────────────────────────────────────

    @GET("Accounts")
    suspend fun getAccounts(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): AccountsResponse

    @GET("Contacts")
    suspend fun getContacts(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): ContactsResponse

    @GET("Deals")
    suspend fun getDeals(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): DealsResponse

    @GET("Quotes")
    suspend fun getQuotes(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): QuotesResponse

    @GET("Tasks")
    suspend fun getTasks(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Due_Date",
        @Query("sort_order") sortOrder: String = "asc",
    ): TasksResponse

    @GET("Events")
    suspend fun getEvents(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Start_DateTime",
        @Query("sort_order") sortOrder: String = "asc",
    ): EventsResponse

    @GET("Calls")
    suspend fun getCalls(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Call_Start_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): CallsResponse

    // ── SINGLE RECORD ─────────────────────────────────────────────────────────

    @GET("Accounts/{id}")
    suspend fun getAccountById(@Path("id") id: String): AccountsResponse

    @GET("Accounts/{accountId}/Contacts")
    suspend fun getAccountContacts(
        @Path("accountId") accountId: String,
        @Query("per_page") perPage: Int = 200,
    ): ContactsResponse

    // ── SETTINGS ──────────────────────────────────────────────────────────────

    @GET("settings/fields")
    suspend fun getFields(@Query("module") module: String): FieldsResponse

    @GET("users")
    suspend fun getUsers(@Query("type") type: String = "AllUsers"): UsersResponse

    // ── CREATE ────────────────────────────────────────────────────────────────

    @POST("Accounts")
    suspend fun createAccount(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse
}