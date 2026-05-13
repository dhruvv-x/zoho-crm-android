package com.pookie.octfis.data.remote

import com.pookie.octfis.data.remote.dto.AccountsResponse
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
}