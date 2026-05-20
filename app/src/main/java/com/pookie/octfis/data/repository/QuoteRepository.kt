package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.remote.ZohoApiService
import java.text.SimpleDateFormat
import java.util.Locale

class QuoteRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Quote>()

        fun toZohoDate(uiDate: String): String {
            if (uiDate.isBlank()) return ""
            return try {
                val from = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val to   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                from.isLenient = false
                to.format(from.parse(uiDate)!!)
            } catch (_: Exception) {
                uiDate
            }
        }

        fun toUiDate(zohoDate: String): String {
            if (zohoDate.isBlank()) return ""
            return try {
                val from = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val to   = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                from.isLenient = false
                to.format(from.parse(zohoDate)!!)
            } catch (_: Exception) {
                zohoDate
            }
        }
    }

    private fun mapQuote(index: Int, page: Int, zoho: com.pookie.octfis.data.remote.dto.ZohoQuote) = Quote(
        id            = ((page - 1) * 100) + index + 1,
        zohoId        = zoho.id,
        name          = zoho.subject.orEmpty().ifEmpty { "(No Subject)" },
        subject       = zoho.subject.orEmpty(),
        accountName   = zoho.accountName?.name.orEmpty(),
        accountZohoId = zoho.accountName?.id.orEmpty(),
        contactName   = zoho.contactName?.name.orEmpty(),
        contactZohoId = zoho.contactName?.id.orEmpty(),
        dealName      = zoho.dealName?.name.orEmpty(),
        dealZohoId    = zoho.dealName?.id.orEmpty(),
        quoteStage    = zoho.quoteStage.orEmpty().ifEmpty { "Draft" },
        validUntil    = toUiDate(zoho.validUntil.orEmpty()),
        description   = zoho.description.orEmpty(),
        grandTotal    = zoho.grandTotal ?: 0.0,
        subTotal      = zoho.subTotal ?: 0.0,
        discount      = zoho.discount ?: 0.0,
        tax           = zoho.tax ?: 0.0,
        items         = zoho.quotedItems?.mapIndexed { i, item ->
            QuoteItem(
                sNo         = i + 1,
                productName = item.product?.name.orEmpty().ifEmpty { "Product" },
                description = item.description.orEmpty(),
                quantity    = item.quantity?.toInt() ?: 1,
                price       = item.unitPrice ?: 0.0,
            )
        } ?: emptyList(),
    )

    suspend fun getQuotes(page: Int = 1): Result<Pair<List<Quote>, Boolean>> = runCatching {
        val response = api.getQuotes(page = page, perPage = 100)
        val quotes = response.data?.mapIndexed { index, zoho ->
            mapQuote(index, page, zoho)
        } ?: emptyList()

        if (page == 1) cache.clear()
        cache.addAll(quotes)

        Pair(quotes, response.info?.moreRecords ?: false)
    }

    suspend fun getQuoteById(zohoId: String): Result<Quote> = runCatching {
        val zoho = api.getQuoteById(zohoId).data?.firstOrNull()
            ?: error("Quote not found: $zohoId")
        val cacheIdx = cache.indexOfFirst { it.zohoId == zohoId }
        val localId  = if (cacheIdx >= 0) cache[cacheIdx].id else 0
        val mapped   = mapQuote(localId - 1, 1, zoho).copy(id = localId)
        if (cacheIdx >= 0) cache[cacheIdx] = mapped
        mapped
    }

    suspend fun createQuote(
        subject       : String,
        accountName   : String,
        accountZohoId : String = "",
        contactName   : String,
        contactZohoId : String = "",
        dealName      : String = "",
        dealZohoId    : String = "",
        quoteStage    : String,
        validUntil    : String,
        description   : String,
        items         : List<QuoteItem>,
    ): Result<Unit> = runCatching {
        val subTotal = items.sumOf { it.price * it.quantity }

        val record = buildMap<String, Any> {
            put("Subject",     subject.ifBlank { "(No Subject)" })
            put("Quote_Stage", quoteStage.ifBlank { "Draft" })

            // Always send totals computed from local items.
            // We intentionally do NOT send Product_Details because Zoho requires each
            // line item to reference a valid product id from the Products module.
            // Without a product lookup feature, sending Product_Details always causes
            // "required field not found [field: id]". Totals are sent manually instead.
            put("Sub_Total",   subTotal)
            put("Grand_Total", subTotal)

            // Lookup fields — ONLY send when we have the Zoho id.
            // Sending {"name": "..."} without "id" causes "required field not found [field: id]".
            if (accountZohoId.isNotBlank()) {
                put("Account_Name", mapOf("id" to accountZohoId))
            }
            if (contactZohoId.isNotBlank()) {
                put("Contact_Name", mapOf("id" to contactZohoId))
            }
            if (dealZohoId.isNotBlank()) {
                put("Deal_Name", mapOf("id" to dealZohoId))
            }

            val zohoDate = toZohoDate(validUntil)
            if (zohoDate.isNotBlank())    put("Valid_Till",  zohoDate)
            if (description.isNotBlank()) put("Description", description)
        }

        val response = api.createQuote(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()

        if (result?.status != "success") {
            val fieldHint = result?.details?.apiName?.let { " [field: $it]" } ?: ""
            error((result?.message ?: "Create failed") + fieldHint)
        }

        val newZohoId = result.details?.id.orEmpty()
        val newId     = (cache.maxOfOrNull { it.id } ?: 0) + 1
        cache.add(
            Quote(
                id            = newId,
                zohoId        = newZohoId,
                name          = subject.ifEmpty { "(No Subject)" },
                subject       = subject,
                accountName   = accountName,
                accountZohoId = accountZohoId,
                contactName   = contactName,
                contactZohoId = contactZohoId,
                dealName      = dealName,
                dealZohoId    = dealZohoId,
                validUntil    = validUntil,
                quoteStage    = quoteStage,
                description   = description,
                subTotal      = subTotal,
                grandTotal    = subTotal,
                items         = items,
            )
        )
    }

    suspend fun updateQuote(
        zohoId        : String,
        subject       : String,
        accountName   : String,
        accountZohoId : String = "",
        contactName   : String,
        contactZohoId : String = "",
        dealName      : String = "",
        dealZohoId    : String = "",
        quoteStage    : String,
        validUntil    : String,
        description   : String,
        items         : List<QuoteItem>,
    ): Result<Unit> = runCatching {
        val subTotal = items.sumOf { it.price * it.quantity }

        val record = buildMap<String, Any> {
            put("Subject",     subject.ifBlank { "(No Subject)" })
            put("Quote_Stage", quoteStage.ifBlank { "Draft" })

            // Always send totals computed from local items.
            // We intentionally do NOT send Product_Details — see createQuote comment above.
            put("Sub_Total",   subTotal)
            put("Grand_Total", subTotal)

            // Lookup fields — ONLY send when we have the Zoho id.
            if (accountZohoId.isNotBlank()) {
                put("Account_Name", mapOf("id" to accountZohoId))
            }
            if (contactZohoId.isNotBlank()) {
                put("Contact_Name", mapOf("id" to contactZohoId))
            }
            if (dealZohoId.isNotBlank()) {
                put("Deal_Name", mapOf("id" to dealZohoId))
            }

            val zohoDate = toZohoDate(validUntil)
            if (zohoDate.isNotBlank())    put("Valid_Till",  zohoDate)
            if (description.isNotBlank()) put("Description", description)
        }

        val response = api.updateQuote(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()

        if (result?.status != "success") {
            val fieldHint = result?.details?.apiName?.let { " [field: $it]" } ?: ""
            error((result?.message ?: "Update failed") + fieldHint)
        }

        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) {
            cache[idx] = cache[idx].copy(
                name          = subject.ifEmpty { "(No Subject)" },
                subject       = subject,
                accountName   = accountName,
                accountZohoId = accountZohoId,
                contactName   = contactName,
                contactZohoId = contactZohoId,
                dealName      = dealName,
                dealZohoId    = dealZohoId,
                quoteStage    = quoteStage,
                validUntil    = validUntil,
                description   = description,
                subTotal      = subTotal,
                grandTotal    = subTotal,
                items         = items,
            )
        }
    }
}