package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DealsResponse(
    @SerializedName("data") val data: List<ZohoDeal>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoDeal(
    @SerializedName("id")               val id: String,
    @SerializedName("Deal_Name")        val dealName: String?,
    @SerializedName("Account_Name")     val accountName: ZohoDealAccount?,
    @SerializedName("Contact_Name")     val contactName: ZohoDealContact?,
    @SerializedName("Amount")           val amount: Double?,
    @SerializedName("Closing_Date")     val closingDate: String?,
    @SerializedName("Deal_Owner")       val dealOwner: ZohoOwner?,
    @SerializedName("Stage")            val stage: String?,
    @SerializedName("Lead_Source")      val leadSource: String?,
    @SerializedName("Type")             val type: String?,
    @SerializedName("Description")      val description: String?,
    @SerializedName("Email")            val email: String?,
    @SerializedName("Phone")            val phone: String?,
    @SerializedName("Lead_Source_Drill_Down") val leadSourceDrill: String?,
)

data class ZohoDealAccount(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)

data class ZohoDealContact(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)