package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Contact
import com.pookie.octfis.data.remote.ZohoApiService

class ContactRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Contact>()
    }

    suspend fun getContacts(page: Int = 1): Result<Pair<List<Contact>, Boolean>> =
        runCatching {
            val response = api.getContacts(page = page, perPage = 100)
            val contacts = response.data?.mapIndexed { index, zoho ->
                Contact(
                    id             = ((page - 1) * 100) + index + 1,
                    zohoId         = zoho.id,
                    firstName      = zoho.firstName.orEmpty(),
                    lastName       = zoho.lastName.orEmpty(),
                    fullName       = zoho.fullName
                        ?: "${zoho.firstName.orEmpty()} ${zoho.lastName.orEmpty()}".trim().ifEmpty { "(No Name)" },
                    phone          = zoho.phone.orEmpty(),
                    mobile         = zoho.mobile.orEmpty(),
                    email          = zoho.email.orEmpty(),
                    accountName    = zoho.accountName?.name.orEmpty(),
                    title          = zoho.title.orEmpty(),
                    department     = zoho.department.orEmpty(),
                    leadSource     = zoho.leadSource.orEmpty(),
                    contactOwner   = zoho.contactOwner?.name.orEmpty(),
                    description    = zoho.description.orEmpty(),
                    mailingStreet  = zoho.mailingStreet.orEmpty(),
                    mailingCity    = zoho.mailingCity.orEmpty(),
                    mailingState   = zoho.mailingState.orEmpty(),
                    mailingZip     = zoho.mailingZip.orEmpty(),
                    mailingCountry = zoho.mailingCountry.orEmpty(),
                )
            } ?: emptyList()

            if (page == 1) cache.clear()
            cache.addAll(contacts)

            val hasMore = response.info?.moreRecords ?: false
            Pair(contacts, hasMore)
        }
}