package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuotesResponse(
    @SerializedName("data") val data: List<ZohoQuote>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoQuote(
    @SerializedName("id")             val id: String,
    @SerializedName("Subject")        val subject: String?,
    @SerializedName("Account_Name")   val accountName: ZohoQuoteAccount?,
    @SerializedName("Contact_Name")   val contactName: ZohoQuoteContact?,
    @SerializedName("Quote_Stage")    val quoteStage: String?,
    @SerializedName("Valid_Till")     val validUntil: String?,
    @SerializedName("Description")    val description: String?,
    @SerializedName("Grand_Total")    val grandTotal: Double?,
    @SerializedName("Sub_Total")      val subTotal: Double?,
    @SerializedName("Discount")       val discount: Double?,
    @SerializedName("Tax")            val tax: Double?,
    @SerializedName("Quote_Owner")    val quoteOwner: ZohoOwner?,
    @SerializedName("Quoted_Items")   val quotedItems: List<ZohoQuotedItem>?,
)

data class ZohoQuoteAccount(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)

data class ZohoQuoteContact(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)

data class ZohoQuotedItem(
    @SerializedName("product")     val product: ZohoProduct?,
    @SerializedName("quantity")    val quantity: Double?,
    @SerializedName("unit_price")  val unitPrice: Double?,
    @SerializedName("total")       val total: Double?,
    @SerializedName("description") val description: String?,
)

data class ZohoProduct(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)