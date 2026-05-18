// data/remote/ZohoApiService.kt
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

    @GET("Quotes/{id}")
    suspend fun getQuoteById(
        @Path("id") id: String,
        @Query("fields") fields: String = "Subject,Account_Name,Contact_Name,Quote_Stage,Valid_Till,Description,Grand_Total,Sub_Total,Discount,Tax,Quote_Owner,Product_Details",
    ): QuotesResponse

    @POST("Quotes")
    suspend fun createQuote(@Body body: Map<String, @JvmSuppressWildcards Any>): CreateRecordResponse

    @PUT("Quotes/{id}")
    suspend fun updateQuote(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

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

    @GET("settings/modules")
    suspend fun getModules(): ModulesResponse

    @GET("settings/layouts")
    suspend fun getLayouts(@Query("module") module: String): LayoutsResponse

    @GET("settings/related_lists")
    suspend fun getRelatedLists(@Query("module") module: String): RelatedListsResponse

    @GET("users")
    suspend fun getUsers(@Query("type") type: String = "AllUsers"): UsersResponse

    // ── Generic dynamic endpoints ─────────────────────────────────────────────

    @GET("{module}")
    suspend fun listRecords(
        @Path("module")      module   : String,
        @Query("page")       page     : Int    = 1,
        @Query("per_page")   perPage  : Int    = 50,
        @Query("sort_by")    sortBy   : String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): Map<String, @JvmSuppressWildcards Any>

    @POST("{module}")
    suspend fun createRecord(
        @Path("module") module: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    @PUT("{module}/{id}")
    suspend fun updateRecord(
        @Path("module") module: String,
        @Path("id")     id    : String,
        @Body body: Map<String, @JvmSuppressWildcards Any>,
    ): CreateRecordResponse

    @GET("{module}/{id}")
    suspend fun getRecord(
        @Path("module") module: String,
        @Path("id")     id    : String,
    ): Map<String, @JvmSuppressWildcards Any>

    @DELETE("{module}/{id}")
    suspend fun deleteRecord(
        @Path("module") module: String,
        @Path("id")     id    : String,
    ): CreateRecordResponse

    // ── Search ────────────────────────────────────────────────────────────────
    // Nullable — Zoho returns HTTP 204 (no body) when search has zero results
    @GET("{module}/search")
    suspend fun searchRecords(
        @Path("module")    module : String,
        @Query("word")     word   : String,
        @Query("per_page") perPage: Int = 10,
    ): Map<String, @JvmSuppressWildcards Any>?

    // ── Related records ───────────────────────────────────────────────────────
    @GET("{parentModule}/{parentId}/{relatedModule}")
    suspend fun listRelatedRecords(
        @Path("parentModule")  parentModule : String,
        @Path("parentId")      parentId     : String,
        @Path("relatedModule") relatedModule: String,
        @Query("page")         page         : Int = 1,
        @Query("per_page")     perPage      : Int = 10,
    ): Map<String, @JvmSuppressWildcards Any>?
}