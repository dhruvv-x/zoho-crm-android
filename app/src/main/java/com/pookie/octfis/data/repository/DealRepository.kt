package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.data.remote.ZohoApiService

class DealRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Deal>()
    }

    suspend fun getDeals(page: Int = 1): Result<Pair<List<Deal>, Boolean>> =
        runCatching {
            val response = api.getDeals(page = page, perPage = 100)
            val deals = response.data?.mapIndexed { index, zoho ->
                Deal(
                    id             = ((page - 1) * 100) + index + 1,
                    zohoId         = zoho.id,
                    name           = zoho.dealName.orEmpty().ifEmpty { "(No Name)" },
                    dealName       = zoho.dealName.orEmpty(),
                    accountName    = zoho.accountName?.name.orEmpty(),
                    contactName    = zoho.contactName?.name.orEmpty(),
                    amount         = zoho.amount?.let { "%.2f".format(it) }.orEmpty(),
                    closingDate    = zoho.closingDate.orEmpty(),
                    type           = zoho.type.orEmpty().ifEmpty { "-None-" },
                    email          = zoho.email.orEmpty(),
                    phone          = zoho.phone.orEmpty(),
                    dealOwner      = zoho.dealOwner?.name.orEmpty().ifEmpty { "-None-" },
                    description    = zoho.description.orEmpty(),
                    stage          = zoho.stage.orEmpty().ifEmpty { "-None-" },
                    leadSource     = zoho.leadSource.orEmpty().ifEmpty { "-None-" },
                    leadSourceDrill= zoho.leadSourceDrill.orEmpty(),
                )
            } ?: emptyList()

            if (page == 1) cache.clear()
            cache.addAll(deals)

            val hasMore = response.info?.moreRecords ?: false
            Pair(deals, hasMore)
        }
}