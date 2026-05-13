package com.pookie.octfis.data.remote

import com.pookie.octfis.data.remote.dto.AccountsResponse
import com.pookie.octfis.data.remote.dto.ContactsResponse
import com.pookie.octfis.data.remote.dto.DealsResponse
import com.pookie.octfis.data.remote.dto.QuotesResponse
import com.pookie.octfis.data.remote.dto.TasksResponse
import com.pookie.octfis.data.remote.dto.EventsResponse
import com.pookie.octfis.data.remote.dto.CallsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ZohoApiService {

    @GET("Accounts")
    suspend fun getAccounts(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): AccountsResponse

    @GET("Contacts")
    suspend fun getContacts(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): ContactsResponse

    @GET("Deals")
    suspend fun getDeals(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): DealsResponse

    @GET("Quotes")
    suspend fun getQuotes(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 100,
        @Query("sort_by")    sortBy: String = "Modified_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): QuotesResponse

    @GET("Tasks")
    suspend fun getTasks(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Due_Date",
        @Query("sort_order") sortOrder: String = "asc",
    ): TasksResponse

    @GET("Events")
    suspend fun getEvents(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Start_DateTime",
        @Query("sort_order") sortOrder: String = "asc",
    ): EventsResponse

    @GET("Calls")
    suspend fun getCalls(
        @Query("page")       page: Int    = 1,
        @Query("per_page")   perPage: Int = 50,
        @Query("sort_by")    sortBy: String = "Call_Start_Time",
        @Query("sort_order") sortOrder: String = "desc",
    ): CallsResponse
}