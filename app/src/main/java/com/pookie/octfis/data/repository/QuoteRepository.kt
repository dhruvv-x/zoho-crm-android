package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Quote
import com.pookie.octfis.data.model.QuoteItem
import com.pookie.octfis.data.remote.ZohoApiService

class QuoteRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Quote>()
    }

    suspend fun getQuotes(page: Int = 1): Result<Pair<List<Quote>, Boolean>> =
        runCatching {
            val response = api.getQuotes(page = page, perPage = 100)
            val quotes = response.data?.mapIndexed { index, zoho ->
                Quote(
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
            } ?: emptyList()

            if (page == 1) cache.clear()
            cache.addAll(quotes)

            val hasMore = response.info?.moreRecords ?: false
            Pair(quotes, hasMore)
        }
}