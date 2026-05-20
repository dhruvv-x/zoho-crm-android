package com.pookie.octfis.data.repository

import android.util.Log
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
                    accountZohoId   = zoho.accountName?.id.orEmpty(),
                    contactName     = zoho.contactName?.name.orEmpty(),
                    contactZohoId   = zoho.contactName?.id.orEmpty(),
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
            Pair(deals, response.info?.moreRecords ?: false)
        }

    suspend fun createDeal(
        dealName       : String,
        accountName    : String,
        accountZohoId  : String,
        contactName    : String,
        contactZohoId  : String,
        amount         : String,
        closingDate    : String,
        type           : String,
        email          : String,
        dealOwner      : String,
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Deal_Name", dealName)
            put("Stage", stage)
            if (closingDate.isNotBlank())                           put("Closing_Date",          closingDate)
            if (accountZohoId.isNotBlank())                         put("Account_Name",           mapOf("id" to accountZohoId))
            if (contactZohoId.isNotBlank())                         put("Contact_Name",           mapOf("id" to contactZohoId))
            amount.toDoubleOrNull()?.let {                          put("Amount",                 it) }
            if (type.isNotBlank() && type != "-None-")              put("Type",                   type)
            if (email.isNotBlank())                                 put("Email",                  email)
            if (description.isNotBlank())                           put("Description",            description)
            if (leadSource.isNotBlank() && leadSource != "-None-")  put("Lead_Source",            leadSource)
            if (leadSourceDrill.isNotBlank())                        put("Lead_Source_Drill_Down", leadSourceDrill)
            if (dealOwner.isNotBlank())                              put("Owner",                  mapOf("id" to dealOwner))
        }

        Log.d("DEAL_DEBUG", "PAYLOAD: $record")

        val response = api.createDeal(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") {
            val field = result?.details?.apiName ?: "unknown"
            error("${result?.message ?: "Create failed"} [field: $field]")
        }
        val newZohoId = result?.details?.id ?: error("No ID returned from Zoho")
        cache.add(Deal(
            id = cache.size + 1, zohoId = newZohoId, name = dealName, dealName = dealName,
            accountName = accountName, accountZohoId = accountZohoId,
            contactName = contactName, contactZohoId = contactZohoId,
            amount = amount, closingDate = closingDate,
            type = type.ifEmpty { "-None-" }, email = email,
            dealOwner = dealOwner.ifEmpty { "-None-" }, description = description,
            stage = stage.ifEmpty { "-None-" }, leadSource = leadSource.ifEmpty { "-None-" },
            leadSourceDrill = leadSourceDrill,
        ))
        newZohoId
    }

    suspend fun updateDeal(
        zohoId         : String,
        dealName       : String,
        accountName    : String,
        accountZohoId  : String,
        contactName    : String,
        contactZohoId  : String,
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
            put("Deal_Name", dealName)
            put("Stage", stage)
            if (closingDate.isNotBlank())                           put("Closing_Date",          closingDate)
            if (accountZohoId.isNotBlank())                         put("Account_Name",           mapOf("id" to accountZohoId))
            if (contactZohoId.isNotBlank())                         put("Contact_Name",           mapOf("id" to contactZohoId))
            amount.toDoubleOrNull()?.let {                          put("Amount",                 it) }
            if (email.isNotBlank())                                 put("Email",                  email)
            if (description.isNotBlank())                           put("Description",            description)
            if (type.isNotBlank() && type != "-None-")              put("Type",                   type)
            if (leadSource.isNotBlank() && leadSource != "-None-")  put("Lead_Source",            leadSource)
            if (leadSourceDrill.isNotBlank())                        put("Lead_Source_Drill_Down", leadSourceDrill)
            if (dealOwner.isNotBlank())                              put("Owner",                  mapOf("id" to dealOwner))
        }

        Log.d("DEAL_DEBUG", "UPDATE PAYLOAD: $record")

        val response = api.updateDeal(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") {
            val field = result?.details?.apiName ?: "unknown"
            error("${result?.message ?: "Update failed"} [field: $field]")
        }
        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) cache[idx] = cache[idx].copy(
            dealName = dealName, accountName = accountName, accountZohoId = accountZohoId,
            contactName = contactName, contactZohoId = contactZohoId,
            amount = amount, closingDate = closingDate, type = type.ifEmpty { "-None-" },
            email = email, dealOwner = dealOwner.ifEmpty { "-None-" }, description = description,
            stage = stage.ifEmpty { "-None-" }, leadSource = leadSource.ifEmpty { "-None-" },
            leadSourceDrill = leadSourceDrill,
        )
    }
}