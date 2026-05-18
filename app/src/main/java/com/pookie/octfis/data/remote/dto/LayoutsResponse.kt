package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// GET /settings/layouts?module=X
data class LayoutsResponse(
    @SerializedName("layouts") val layouts: List<ZohoLayout>?,
)

data class ZohoLayout(
    @SerializedName("id")       val id      : String,
    @SerializedName("name")     val name    : String,
    @SerializedName("status")   val status  : Int = -1,
    @SerializedName("sections") val sections: List<ZohoLayoutSection>?,
)

data class ZohoLayoutSection(
    @SerializedName("name")            val name    : String,
    @SerializedName("sequence_number") val sequence: Int = 0,
    @SerializedName("fields")          val fields  : List<ZohoLayoutField>?,
)

data class ZohoLayoutField(
    @SerializedName("api_name")        val apiName        : String,
    @SerializedName("sequence_number") val sequenceNumber : Int = 0,
)