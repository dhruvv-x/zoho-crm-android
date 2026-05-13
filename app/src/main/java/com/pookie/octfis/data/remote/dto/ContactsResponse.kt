package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContactsResponse(
    @SerializedName("data") val data: List<ZohoContact>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoContact(
    @SerializedName("id")              val id: String,
    @SerializedName("First_Name")      val firstName: String?,
    @SerializedName("Last_Name")       val lastName: String?,
    @SerializedName("Full_Name")       val fullName: String?,
    @SerializedName("Phone")           val phone: String?,
    @SerializedName("Mobile")          val mobile: String?,
    @SerializedName("Email")           val email: String?,
    @SerializedName("Account_Name")    val accountName: ZohoContactAccount?,
    @SerializedName("Title")           val title: String?,
    @SerializedName("Department")      val department: String?,
    @SerializedName("Lead_Source")     val leadSource: String?,
    @SerializedName("Contact_Owner")   val contactOwner: ZohoOwner?,
    @SerializedName("Mailing_Street")  val mailingStreet: String?,
    @SerializedName("Mailing_City")    val mailingCity: String?,
    @SerializedName("Mailing_State")   val mailingState: String?,
    @SerializedName("Mailing_Zip")     val mailingZip: String?,
    @SerializedName("Mailing_Country") val mailingCountry: String?,
    @SerializedName("Description")     val description: String?,
)

data class ZohoContactAccount(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)