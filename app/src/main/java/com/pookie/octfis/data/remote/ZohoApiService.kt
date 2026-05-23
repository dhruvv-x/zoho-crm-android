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
        @Query("fields")     fields: String = "id,First_Name,Last_Name,Full_Name,Phone,Mobile,Email,Account_Name,Title,Department,Lead_Source,Contact_Owner,Description,Mailing_Street,Mailing_City,Mailing_State,Mailing_Zip,Mailing_Country",
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

    @GET("Deals/{id}")
    suspend fun getDealById(@Path("id") id: String): DealsResponse

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

    // ── Events (Meetings) ─────────────────────────────────────────────────────

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

    @GET("Contacts/{contactId}/Calls")
    suspend fun getCallsForContact(
        @Path("contactId") contactId: String,
        @Query("page")     page: Int = 1,
        @Query("per_page") perPage: Int = 50,
    ): CallsResponse

    @POST("Calls")
    suspend fun createCall(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    // ✅ FIX: was @PUT("Calls") with no {id} — Zoho requires the record ID in the path
    @PUT("Calls/{id}")
    suspend fun updateCall(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    @DELETE("Calls/{id}")
    suspend fun deleteCall(@Path("id") id: String): CreateRecordResponse

    // ── Settings ──────────────────────────────────────────────────────────────

    @GET("settings/fields")
    suspend fun getFields(@Query("module") module: String): FieldsResponse

    @GET("users")
    suspend fun getUsers(@Query("type") type: String = "AllUsers"): UsersResponse
}