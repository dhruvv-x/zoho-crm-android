package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AccountsResponse(
    @SerializedName("data") val data: List<ZohoAccount>?,
    @SerializedName("info") val info: PageInfo?,
)

data class PageInfo(
    @SerializedName("count")        val count: Int,
    @SerializedName("more_records") val moreRecords: Boolean,
    @SerializedName("page")         val page: Int,
    @SerializedName("per_page")     val perPage: Int,
)

data class ZohoAccount(
    @SerializedName("id")               val id: String,
    @SerializedName("Account_Name")     val accountName: String?,
    @SerializedName("Phone")            val phone: String?,
    @SerializedName("Website")          val website: String?,
    @SerializedName("Industry")         val industry: String?,
    @SerializedName("Account_Owner")    val accountOwner: ZohoOwner?,
    @SerializedName("Description")      val description: String?,
    @SerializedName("Billing_Street")   val billingStreet: String?,
    @SerializedName("Billing_City")     val billingCity: String?,
    @SerializedName("Billing_State")    val billingState: String?,
    @SerializedName("Billing_Code")     val billingCode: String?,
    @SerializedName("Billing_Country")  val billingCountry: String?,
    @SerializedName("GSTIN")            val gstin: String?,
    @SerializedName("Account_Number")   val accountNo: String?,
    @SerializedName("Lead_Source")      val leadSource: String?,
)

data class ZohoOwner(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)