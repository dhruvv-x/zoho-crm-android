package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.remote.ZohoApiService

class QuoteRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Quote>()
    }

    private fun mapQuote(index: Int, page: Int, zoho: com.pookie.octfis.data.remote.dto.ZohoQuote) = Quote(
        id          = ((page - 1) * 100) + index + 1,
        zohoId      = zoho.id,
        name        = zoho.subject.orEmpty().ifEmpty { "(No Subject)" },
        subject     = zoho.subject.orEmpty(),
        accountName = zoho.accountName?.name.orEmpty(),
        contactName = zoho.contactName?.name.orEmpty(),
        quoteStage  = zoho.quoteStage.orEmpty().ifEmpty { "Draft" },
        validUntil  = zoho.validUntil.orEmpty(),
        description = zoho.description.orEmpty(),
        grandTotal  = zoho.grandTotal ?: 0.0,
        subTotal    = zoho.subTotal ?: 0.0,
        discount    = zoho.discount ?: 0.0,
        tax         = zoho.tax ?: 0.0,
        items       = zoho.quotedItems?.mapIndexed { i, item ->
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
        mapQuote(localId - 1, 1, zoho).copy(id = localId)
    }

    suspend fun createQuote(
        subject    : String,
        accountName: String,
        contactName: String,
        quoteStage : String,
        validUntil : String,
        description: String,
        items      : List<QuoteItem>,
    ): Result<Unit> = runCatching {
        val subTotal    = items.sumOf { it.price * it.quantity }
        val productDetails = buildProductDetails(items)

        val record = buildMap<String, Any> {
            put("Subject",     subject.ifBlank { "(No Subject)" })
            put("Quote_Stage", quoteStage.ifBlank { "Draft" })
            put("Sub_Total",   subTotal)
            put("Grand_Total", subTotal)
            if (accountName.isNotBlank())      put("Account_Name",    mapOf("name" to accountName))
            if (contactName.isNotBlank())      put("Contact_Name",    mapOf("name" to contactName))
            if (validUntil.isNotBlank())       put("Valid_Till",      validUntil)
            if (description.isNotBlank())      put("Description",     description)
            if (productDetails.isNotEmpty())   put("Product_Details", productDetails)
        }

        val response = api.createQuote(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") error(result?.message ?: "Create failed")

        val newZohoId = result.details?.id.orEmpty()
        val newId     = (cache.maxOfOrNull { it.id } ?: 0) + 1
        cache.add(
            Quote(
                id          = newId,
                zohoId      = newZohoId,
                name        = subject.ifEmpty { "(No Subject)" },
                subject     = subject,
                accountName = accountName,
                contactName = contactName,
                validUntil  = validUntil,
                quoteStage  = quoteStage,
                description = description,
                subTotal    = subTotal,
                grandTotal  = subTotal,
                items       = items,
            )
        )
    }

    suspend fun updateQuote(
        zohoId     : String,
        subject    : String,
        quoteStage : String,
        validUntil : String,
        description: String,
        items      : List<QuoteItem>,
    ): Result<Unit> = runCatching {
        val subTotal       = items.sumOf { it.price * it.quantity }
        val productDetails = buildProductDetails(items)

        val record = buildMap<String, Any> {
            put("Subject",     subject.ifBlank { "(No Subject)" })
            put("Quote_Stage", quoteStage.ifBlank { "Draft" })
            put("Sub_Total",   subTotal)
            put("Grand_Total", subTotal)
            if (validUntil.isNotBlank())     put("Valid_Till",      validUntil)
            if (description.isNotBlank())    put("Description",     description)
            if (productDetails.isNotEmpty()) put("Product_Details", productDetails)
        }

        val response = api.updateQuote(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") error(result?.message ?: "Update failed")

        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) {
            cache[idx] = cache[idx].copy(
                name        = subject.ifEmpty { "(No Subject)" },
                subject     = subject,
                quoteStage  = quoteStage,
                validUntil  = validUntil,
                description = description,
                subTotal    = subTotal,
                grandTotal  = subTotal,
                items       = items,
            )
        }
    }

    private fun buildProductDetails(items: List<QuoteItem>) = items.map { item ->
        buildMap<String, Any> {
            put("product",    mapOf("name" to item.productName))
            put("quantity",   item.quantity.toDouble())
            put("unit_price", item.price)
            put("total",      item.price * item.quantity)
            if (item.description.isNotBlank()) put("description", item.description)
        }
    }
}