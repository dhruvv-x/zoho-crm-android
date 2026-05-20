package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
    @SerializedName("data") val data: List<ZohoProductRecord>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoProductRecord(
    @SerializedName("id")           val id: String,
    @SerializedName("Product_Name") val name: String?,
    @SerializedName("Unit_Price")   val unitPrice: Double?,
    @SerializedName("Product_Code") val code: String?,
    @SerializedName("Description")  val description: String?,
)