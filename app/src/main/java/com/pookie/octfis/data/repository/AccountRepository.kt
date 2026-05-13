package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Account
import com.pookie.octfis.data.remote.ZohoApiService

class AccountRepository(private val api: ZohoApiService) {

    companion object {
        // In-memory cache — lets AccountDetailScreen find real records
        val cache = mutableListOf<Account>()
    }

    suspend fun getAccounts(page: Int = 1): Result<Pair<List<Account>, Boolean>> =
        runCatching {
            val response = api.getAccounts(page = page, perPage = 100)
            val accounts = response.data?.mapIndexed { index, zoho ->
                Account(
                    id             = ((page - 1) * 100) + index + 1,
                    zohoId         = zoho.id,
                    accountNo      = zoho.accountNo.orEmpty(),
                    name           = zoho.accountName.orEmpty(),
                    phone          = zoho.phone.orEmpty(),
                    website        = zoho.website.orEmpty(),
                    industry       = zoho.industry.orEmpty().ifEmpty { "-None-" },
                    accountOwner   = zoho.accountOwner?.name.orEmpty().ifEmpty { "-None-" },
                    leadSource     = zoho.leadSource.orEmpty().ifEmpty { "-None-" },
                    description    = zoho.description.orEmpty(),
                    billingStreet  = zoho.billingStreet.orEmpty(),
                    billingCity    = zoho.billingCity.orEmpty(),
                    billingState   = zoho.billingState.orEmpty(),
                    billingCode    = zoho.billingCode.orEmpty(),
                    billingCountry = zoho.billingCountry.orEmpty(),
                    gstin          = zoho.gstin.orEmpty(),
                )
            } ?: emptyList()

            if (page == 1) cache.clear()
            cache.addAll(accounts)

            val hasMore = response.info?.moreRecords ?: false
            Pair(accounts, hasMore)
        }
}