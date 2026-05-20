package com.pookie.octfis.data.remote

import com.pookie.octfis.data.remote.dto.*
import retrofit2.http.*

interface ZohoApiService {

    // ── Accounts ──────────────────────────────────────────────────────────────

    @GET("Accounts")
    suspend fun getAccounts(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): AccountsResponse

    @GET("Accounts/{id}")
    suspend fun getAccountById(@Path("id") id: String): AccountsResponse

    @GET("Accounts/{accountId}/Contacts")
    suspend fun getAccountContacts(
        @Path("accountId") accountId: String,
        @Query("per_page") perPage: Int = 200,
    ): ContactsResponse

    @POST("Accounts")
    suspend fun createAccount(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    // ── Contacts ──────────────────────────────────────────────────────────────

    @GET("Contacts")
    suspend fun getContacts(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): ContactsResponse

    @POST("Contacts")
    suspend fun createContact(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Contacts/{id}")
    suspend fun updateContact(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    // ── Deals ─────────────────────────────────────────────────────────────────

    @GET("Deals")
    suspend fun getDeals(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): DealsResponse

    @POST("Deals")
    suspend fun createDeal(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Deals/{id}")
    suspend fun updateDeal(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    // ── Quotes ────────────────────────────────────────────────────────────────

    @GET("Quotes")
    suspend fun getQuotes(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): QuotesResponse

    // FIXED: Removed ?fields= param entirely.
    // When fields= is specified, Zoho treats Product_Details as a subform and
    // silently excludes it from the response — so product names never arrive.
    // Without the fields param, Zoho returns all fields including Product_Details.
    @GET("Quotes/{id}")
    suspend fun getQuoteById(@Path("id") id: String): QuotesResponse

    @POST("Quotes")
    suspend fun createQuote(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Quotes/{id}")
    suspend fun updateQuote(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    // ── Products ──────────────────────────────────────────────────────────────

    @GET("Products")
    suspend fun getProducts(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 200,
        @Query("sort_by")    sortBy: String = "Product_Name",
        @Query("sort_order") sortOrder: String = "asc",
        @Query("fields")     fields: String = "Product_Name,Unit_Price,Product_Code",
    ): ProductsResponse

    // ── Tasks ─────────────────────────────────────────────────────────────────

    @GET("Tasks")
    suspend fun getTasks(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Due_Date",
        @Query("sort_order") sortOrder: String = "asc",
    ): TasksResponse

    @GET("Tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): TasksResponse

    @POST("Tasks")
    suspend fun createTask(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    @DELETE("Tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): CreateRecordResponse

    // ── Events ────────────────────────────────────────────────────────────────

    @GET("Events")
    suspend fun getEvents(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Start_DateTime",
        @Query("sort_order") sortOrder: String = "asc",
    ): EventsResponse

    @GET("Events/{id}")
    suspend fun getEventById(@Path("id") id: String): EventsResponse

    @POST("Events")
    suspend fun createEvent(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    @DELETE("Events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): CreateRecordResponse

    // ── Calls ─────────────────────────────────────────────────────────────────

    @GET("Calls")
    suspend fun getCalls(
        @Query("page")       page: Int = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Call_Start_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): CallsResponse

    @GET("Calls/{id}")
    suspend fun getCallById(@Path("id") id: String): CallsResponse

    @POST("Calls")
    suspend fun createCall(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Calls")
    suspend fun updateCall(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @DELETE("Calls/{id}")
    suspend fun deleteCall(@Path("id") id: String): CreateRecordResponse

    // ── Settings ──────────────────────────────────────────────────────────────

    @GET("settings/fields")
    suspend fun getFields(@Query("module") module: String): FieldsResponse

    @GET("users")
    suspend fun getUsers(@Query("type") type: String = "AllUsers"): UsersResponse
}