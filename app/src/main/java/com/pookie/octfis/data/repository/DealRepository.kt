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
                    id              = ((page - 1) * 100) + index + 1,
                    zohoId          = zoho.id,
                    name            = zoho.dealName.orEmpty().ifEmpty { "(No Name)" },
                    dealName        = zoho.dealName.orEmpty(),
                    accountName     = zoho.accountName?.name.orEmpty(),
                    contactName     = zoho.contactName?.name.orEmpty(),
                    amount          = zoho.amount?.let { "%.2f".format(it) }.orEmpty(),
                    closingDate     = zoho.closingDate.orEmpty(),
                    type            = zoho.type.orEmpty().ifEmpty { "-None-" },
                    email           = zoho.email.orEmpty(),
                    phone           = zoho.phone.orEmpty(),
                    dealOwner       = zoho.dealOwner?.name.orEmpty().ifEmpty { "-None-" },
                    description     = zoho.description.orEmpty(),
                    stage           = zoho.stage.orEmpty().ifEmpty { "-None-" },
                    leadSource      = zoho.leadSource.orEmpty().ifEmpty { "-None-" },
                    leadSourceDrill = zoho.leadSourceDrill.orEmpty(),
                )
            } ?: emptyList()

            if (page == 1) cache.clear()
            cache.addAll(deals)

            val hasMore = response.info?.moreRecords ?: false
            Pair(deals, hasMore)
        }

    suspend fun updateDeal(
        zohoId         : String,
        dealName       : String,
        accountName    : String,
        contactName    : String,
        amount         : String,
        closingDate    : String,
        type           : String,
        email          : String,
        dealOwner      : String,
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            put("Deal_Name",               dealName)
            if (accountName.isNotBlank())  put("Account_Name",  mapOf("name" to accountName))
            if (contactName.isNotBlank())  put("Contact_Name",  mapOf("name" to contactName))
            amount.toDoubleOrNull()?.let { put("Amount", it) }
            if (closingDate.isNotBlank())  put("Closing_Date",  closingDate)
            if (type.isNotBlank())         put("Type",          type)
            if (email.isNotBlank())        put("Email",         email)
            if (description.isNotBlank())  put("Description",   description)
            if (stage.isNotBlank())        put("Stage",         stage)
            if (leadSource.isNotBlank())   put("Lead_Source",   leadSource)
            if (leadSourceDrill.isNotBlank()) put("Lead_Source_Drill_Down", leadSourceDrill)
            if (dealOwner.isNotBlank())    put("Owner",         mapOf("id" to dealOwner))
        }
        val response = api.updateDeal(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") error(result?.message ?: "Update failed")

        // update local cache
        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) {
            cache[idx] = cache[idx].copy(
                dealName        = dealName,
                accountName     = accountName,
                contactName     = contactName,
                amount          = amount,
                closingDate     = closingDate,
                type            = type.ifEmpty { "-None-" },
                email           = email,
                dealOwner       = dealOwner.ifEmpty { "-None-" },
                description     = description,
                stage           = stage.ifEmpty { "-None-" },
                leadSource      = leadSource.ifEmpty { "-None-" },
                leadSourceDrill = leadSourceDrill,
            )
        }
    }
}