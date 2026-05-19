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

    suspend fun createContact(
        firstName     : String,
        lastName      : String,
        phone         : String,
        email         : String,
        accountName   : String,
        title         : String,
        department    : String,
        contactOwner  : String,
        leadSource    : String,
        description   : String,
        mailingStreet : String,
        mailingCity   : String,
        mailingState  : String,
        mailingZip    : String,
        mailingCountry: String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Last_Name", lastName)
            if (firstName.isNotBlank())      put("First_Name",      firstName)
            if (phone.isNotBlank())          put("Phone",            phone)
            if (email.isNotBlank())          put("Email",            email)
            if (title.isNotBlank())          put("Title",            title)
            if (department.isNotBlank())     put("Department",       department)
            if (leadSource.isNotBlank() && leadSource != "-None-") put("Lead_Source", leadSource)
            if (description.isNotBlank())    put("Description",      description)
            if (mailingStreet.isNotBlank())  put("Mailing_Street",   mailingStreet)
            if (mailingCity.isNotBlank())    put("Mailing_City",     mailingCity)
            if (mailingState.isNotBlank())   put("Mailing_State",    mailingState)
            if (mailingZip.isNotBlank())     put("Mailing_Zip",      mailingZip)
            if (mailingCountry.isNotBlank()) put("Mailing_Country",  mailingCountry)
            // FIX: Do NOT send Account_Name as {"name": "..."} — Zoho requires the "id" key
            // inside any lookup object. Without an accountZohoId we simply omit the field.
            if (contactOwner.isNotBlank())   put("Owner", mapOf("id" to contactOwner))
        }
        val response = api.createContact(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") error(result?.message ?: "Create failed")

        val newZohoId = result?.details?.id
            ?: error("No ID returned from Zoho")

        cache.add(
            Contact(
                id             = cache.size + 1,
                zohoId         = newZohoId,
                firstName      = firstName,
                lastName       = lastName,
                fullName       = "$firstName $lastName".trim(),
                phone          = phone,
                email          = email,
                accountName    = accountName,
                title          = title,
                department     = department,
                leadSource     = leadSource,
                description    = description,
                mailingStreet  = mailingStreet,
                mailingCity    = mailingCity,
                mailingState   = mailingState,
                mailingZip     = mailingZip,
                mailingCountry = mailingCountry,
            )
        )

        newZohoId
    }

    suspend fun updateContact(
        zohoId        : String,
        contactId     : Int,
        firstName     : String,
        lastName      : String,
        phone         : String,
        email         : String,
        accountName   : String,
        title         : String,
        department    : String,
        contactOwner  : String,
        leadSource    : String,
        description   : String,
        mailingStreet : String,
        mailingCity   : String,
        mailingState  : String,
        mailingZip    : String,
        mailingCountry: String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            // Required by Zoho — always send
            put("Last_Name", lastName.ifBlank { "(No Name)" })

            if (firstName.isNotBlank())      put("First_Name",      firstName)
            if (phone.isNotBlank())          put("Phone",            phone)
            if (email.isNotBlank())          put("Email",            email)
            if (title.isNotBlank())          put("Title",            title)
            if (department.isNotBlank())     put("Department",       department)
            if (description.isNotBlank())    put("Description",      description)
            if (mailingStreet.isNotBlank())  put("Mailing_Street",   mailingStreet)
            if (mailingCity.isNotBlank())    put("Mailing_City",     mailingCity)
            if (mailingState.isNotBlank())   put("Mailing_State",    mailingState)
            if (mailingZip.isNotBlank())     put("Mailing_Zip",      mailingZip)
            if (mailingCountry.isNotBlank()) put("Mailing_Country",  mailingCountry)

            if (leadSource.isNotBlank() && leadSource != "-None-")
                put("Lead_Source", leadSource)

            // FIX: Do NOT send Account_Name as {"name": "..."} — Zoho requires the "id" key
            // inside any lookup object. Without an accountZohoId we simply omit the field.
            if (contactOwner.isNotBlank())   put("Owner", mapOf("id" to contactOwner))
        }

        val response = api.updateContact(zohoId, mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status != "success") error(result?.message ?: "Update failed")

        val idx = cache.indexOfFirst { it.id == contactId }
        if (idx >= 0) {
            cache[idx] = cache[idx].copy(
                firstName      = firstName,
                lastName       = lastName,
                fullName       = "$firstName $lastName".trim(),
                phone          = phone,
                email          = email,
                accountName    = accountName,
                title          = title,
                department     = department,
                leadSource     = leadSource,
                description    = description,
                mailingStreet  = mailingStreet,
                mailingCity    = mailingCity,
                mailingState   = mailingState,
                mailingZip     = mailingZip,
                mailingCountry = mailingCountry,
            )
        }
    }
}