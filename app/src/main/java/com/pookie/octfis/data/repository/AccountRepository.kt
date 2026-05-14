package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Account
import com.pookie.octfis.data.model.Contact
import com.pookie.octfis.data.remote.ZohoApiService

class AccountRepository(private val api: ZohoApiService) {

    companion object {
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
                    industry       = zoho.industry.orEmpty(),
                    gstTreatment   = zoho.gstTreatment.orEmpty(),
                    accountOwner   = zoho.accountOwner?.name.orEmpty(),
                    leadSource     = zoho.leadSource.orEmpty(),
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

    suspend fun getAccountDetail(zohoId: String): Result<Account> =
        runCatching {
            val response = api.getAccountById(zohoId)
            val zoho = response.data?.firstOrNull()
                ?: error("Account not found: $zohoId")
            Account(
                id             = 0,
                zohoId         = zoho.id,
                accountNo      = zoho.accountNo.orEmpty(),
                name           = zoho.accountName.orEmpty(),
                phone          = zoho.phone.orEmpty(),
                website        = zoho.website.orEmpty(),
                industry       = zoho.industry.orEmpty(),
                gstTreatment   = zoho.gstTreatment.orEmpty(),
                accountOwner   = zoho.accountOwner?.name.orEmpty(),
                leadSource     = zoho.leadSource.orEmpty(),
                description    = zoho.description.orEmpty(),
                billingStreet  = zoho.billingStreet.orEmpty(),
                billingCity    = zoho.billingCity.orEmpty(),
                billingState   = zoho.billingState.orEmpty(),
                billingCode    = zoho.billingCode.orEmpty(),
                billingCountry = zoho.billingCountry.orEmpty(),
                gstin          = zoho.gstin.orEmpty(),
            )
        }

    suspend fun getAccountContacts(accountZohoId: String): Result<List<Contact>> =
        runCatching {
            val response = api.getAccountContacts(accountZohoId)
            response.data?.mapIndexed { i, zoho ->
                Contact(
                    id           = i + 1,
                    zohoId       = zoho.id,
                    firstName    = zoho.firstName.orEmpty(),
                    lastName     = zoho.lastName.orEmpty(),
                    fullName     = zoho.fullName
                        ?: "${zoho.firstName.orEmpty()} ${zoho.lastName.orEmpty()}".trim(),
                    phone        = zoho.phone.orEmpty(),
                    mobile       = zoho.mobile.orEmpty(),
                    email        = zoho.email.orEmpty(),
                    accountName  = zoho.accountName?.name.orEmpty(),
                    title        = zoho.title.orEmpty(),
                    department   = zoho.department.orEmpty(),
                    leadSource   = zoho.leadSource.orEmpty(),
                    contactOwner = zoho.contactOwner?.name.orEmpty(),
                    description  = zoho.description.orEmpty(),
                )
            } ?: emptyList()
        }

    suspend fun createAccount(
        name          : String,
        phone         : String,
        website       : String,
        industry      : String,
        gstTreatment  : String,
        gstin         : String,
        leadSource    : String,
        accountOwner  : String,
        description   : String,
        billingStreet : String,
        billingStreet2: String,
        billingCity   : String,
        billingState  : String,
        billingCode   : String,
        billingCountry: String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Account_Name", name)
            if (phone.isNotBlank())          put("Phone", phone)
            if (website.isNotBlank())        put("Website", website)
            if (industry.isNotBlank())       put("Industry", industry)
            if (gstTreatment.isNotBlank())   put("GST_Treatment", gstTreatment)
            if (gstin.isNotBlank())          put("GSTIN", gstin)
            if (leadSource.isNotBlank())     put("Lead_Source", leadSource)
            if (accountOwner.isNotBlank())   put("Owner", mapOf("id" to accountOwner))
            if (description.isNotBlank())    put("Description", description)
            val street = listOfNotNull(
                billingStreet.ifBlank { null },
                billingStreet2.ifBlank { null }
            ).joinToString("\n")
            if (street.isNotBlank())         put("Billing_Street", street)
            if (billingCity.isNotBlank())    put("Billing_City", billingCity)
            if (billingState.isNotBlank())   put("Billing_State", billingState)
            if (billingCode.isNotBlank())    put("Billing_Code", billingCode)
            if (billingCountry.isNotBlank()) put("Billing_Country", billingCountry)
        }
        val response = api.createAccount(mapOf("data" to listOf(record)))
        val result   = response.data?.firstOrNull()
        if (result?.status == "success") {
            result.details?.id ?: "created"
        } else {
            error(result?.message ?: "Create failed")
        }
    }
}