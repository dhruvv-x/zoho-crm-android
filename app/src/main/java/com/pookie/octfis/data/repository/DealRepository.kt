package com.pookie.octfis.data.repository

import android.util.Log
import com.pookie.octfis.data.model.Deal
import com.pookie.octfis.data.remote.ZohoApiService

class DealRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Deal>()

        // ── FIX: Zoho returns lookup field names as "<Display Name> - <ZohoId>"
        // e.g. Contact_Name.name = "Sapna Kachhadiya - 4475594000081653003"
        //      Account_Name.name = "Acme Corp - 4475594000073526055"
        // Strip the trailing " - <id>" so we store only the clean display name.
        // This prevents the dirty string from being sent back to Zoho as Deal_Name
        // via the Contact_Name name-fallback branch, which caused Zoho to overwrite
        // the Deal Name with the contact's display label.
        fun cleanLookupName(raw: String): String {
            // Match " - " followed by a long numeric ID at the end of the string
            val pattern = Regex("""\s-\s\d{10,}$""")
            return raw.replace(pattern, "").trim()
        }
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
                    accountName     = cleanLookupName(zoho.accountName?.name.orEmpty()),
                    accountZohoId   = zoho.accountName?.id.orEmpty(),
                    contactName     = cleanLookupName(zoho.contactName?.name.orEmpty()),
                    contactZohoId   = zoho.contactName?.id.orEmpty(),
                    amount          = zoho.amount?.let { "%.2f".format(it) }.orEmpty(),
                    closingDate     = zoho.closingDate.orEmpty(),
                    type            = zoho.type.orEmpty().ifEmpty { "-None-" },
                    email           = zoho.email.orEmpty(),
                    phone           = zoho.phone.orEmpty(),
                    dealOwner       = zoho.dealOwner?.name.orEmpty().ifEmpty { "-None-" },
                    dealOwnerId     = zoho.dealOwner?.id.orEmpty(),
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
        dealOwnerName  : String = "",
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Deal_Name", dealName)
            put("Stage", stage)
            if (closingDate.isNotBlank())                           put("Closing_Date",          closingDate)

            // Zoho requires {"id": "<zohoId>"} for lookup fields.
            // Always prefer id — name fallback uses cleanLookupName() as a safety net.
            when {
                accountZohoId.isNotBlank() -> put("Account_Name", mapOf("id" to accountZohoId))
                accountName.isNotBlank()   -> put("Account_Name", mapOf("name" to cleanLookupName(accountName)))
                // else: no account selected — omit the field entirely
            }

            when {
                contactZohoId.isNotBlank() -> put("Contact_Name", mapOf("id" to contactZohoId))
                contactName.isNotBlank()   -> put("Contact_Name", mapOf("name" to cleanLookupName(contactName)))
            }

            amount.toDoubleOrNull()?.let {                          put("Amount",                 it) }
            if (type.isNotBlank() && type != "-None-")              put("Type",                   type)
            if (email.isNotBlank())                                 put("Email",                  email)
            if (description.isNotBlank())                           put("Description",            description)
            if (leadSource.isNotBlank() && leadSource != "-None-")  put("Lead_Source",            leadSource)
            if (leadSourceDrill.isNotBlank())                        put("Lead_Source_Drill_Down", leadSourceDrill)
            if (dealOwner.isNotBlank() && dealOwner != "-None-" && dealOwner.toLongOrNull() != null) put("Owner", mapOf("id" to dealOwner))
        }

        Log.d("DEAL_DEBUG", "CREATE PAYLOAD: $record")

        val response = api.createDeal(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") {
            val field = result?.details?.apiName ?: "unknown"
            error("${result?.message ?: "Create failed"} [field: $field]")
        }
        val newZohoId = result?.details?.id ?: error("No ID returned from Zoho")
        cache.add(Deal(
            id = cache.size + 1, zohoId = newZohoId, name = dealName, dealName = dealName,
            accountName = cleanLookupName(accountName), accountZohoId = accountZohoId,
            contactName = cleanLookupName(contactName), contactZohoId = contactZohoId,
            amount = amount, closingDate = closingDate,
            type = type.ifEmpty { "-None-" }, email = email,
            dealOwner = dealOwnerName.ifEmpty { "-None-" }, dealOwnerId = dealOwner,
            description = description,
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
        dealOwnerName  : String,
        description    : String,
        stage          : String,
        leadSource     : String,
        leadSourceDrill: String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            put("Deal_Name", dealName)
            put("Stage", stage)
            if (closingDate.isNotBlank())                           put("Closing_Date",          closingDate)

            when {
                accountZohoId.isNotBlank() -> put("Account_Name", mapOf("id" to accountZohoId))
                accountName.isNotBlank()   -> put("Account_Name", mapOf("name" to cleanLookupName(accountName)))
            }

            when {
                contactZohoId.isNotBlank() -> put("Contact_Name", mapOf("id" to contactZohoId))
                contactName.isNotBlank()   -> put("Contact_Name", mapOf("name" to cleanLookupName(contactName)))
            }

            amount.toDoubleOrNull()?.let {                          put("Amount",                 it) }
            if (email.isNotBlank())                                 put("Email",                  email)
            if (description.isNotBlank())                           put("Description",            description)
            if (type.isNotBlank() && type != "-None-")              put("Type",                   type)
            if (leadSource.isNotBlank() && leadSource != "-None-")  put("Lead_Source",            leadSource)
            if (leadSourceDrill.isNotBlank())                        put("Lead_Source_Drill_Down", leadSourceDrill)
            if (dealOwner.isNotBlank() && dealOwner != "-None-" && dealOwner.toLongOrNull() != null) put("Owner", mapOf("id" to dealOwner))
        }

        Log.d("DEAL_DEBUG", "UPDATE PAYLOAD: $record")

        val response = api.updateDeal(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") {
            val field = result?.details?.apiName ?: "unknown"
            error("${result?.message ?: "Update failed"} [field: $field]")
        }

        val refreshed = runCatching { api.getDealById(zohoId) }.getOrNull()
            ?.data?.firstOrNull()

        Log.d("DEAL_DEBUG", "REFRESHED: dealName=${refreshed?.dealName} | ownerName=${refreshed?.dealOwner?.name} | ownerId=${refreshed?.dealOwner?.id}")

        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) {
            cache[idx] = if (refreshed != null) {
                cache[idx].copy(
                    name            = refreshed.dealName.orEmpty().ifEmpty { "(No Name)" },
                    dealName        = refreshed.dealName.orEmpty(),
                    accountName     = cleanLookupName(refreshed.accountName?.name.orEmpty()),
                    accountZohoId   = refreshed.accountName?.id.orEmpty(),
                    contactName     = cleanLookupName(refreshed.contactName?.name.orEmpty()),
                    contactZohoId   = refreshed.contactName?.id.orEmpty(),
                    amount          = refreshed.amount?.let { "%.2f".format(it) }.orEmpty(),
                    closingDate     = refreshed.closingDate.orEmpty(),
                    type            = refreshed.type.orEmpty().ifEmpty { "-None-" },
                    email           = refreshed.email.orEmpty(),
                    dealOwner       = refreshed.dealOwner?.name.orEmpty().ifEmpty { "-None-" },
                    dealOwnerId     = refreshed.dealOwner?.id.orEmpty(),
                    description     = refreshed.description.orEmpty(),
                    stage           = refreshed.stage.orEmpty().ifEmpty { "-None-" },
                    leadSource      = refreshed.leadSource.orEmpty().ifEmpty { "-None-" },
                    leadSourceDrill = refreshed.leadSourceDrill.orEmpty(),
                )
            } else {
                cache[idx].copy(
                    name            = dealName.ifEmpty { "(No Name)" },
                    dealName        = dealName,
                    accountName     = cleanLookupName(accountName), accountZohoId = accountZohoId,
                    contactName     = cleanLookupName(contactName), contactZohoId = contactZohoId,
                    amount          = amount, closingDate = closingDate, type = type.ifEmpty { "-None-" },
                    email           = email,
                    dealOwner       = dealOwnerName.ifEmpty { cache[idx].dealOwner },
                    dealOwnerId     = dealOwner.ifEmpty { cache[idx].dealOwnerId },
                    description     = description,
                    stage           = stage.ifEmpty { "-None-" }, leadSource = leadSource.ifEmpty { "-None-" },
                    leadSourceDrill = leadSourceDrill,
                )
            }
        }
    }
}