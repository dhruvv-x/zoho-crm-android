package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// GET /settings/fields?module=Accounts
data class FieldsResponse(
    @SerializedName("fields") val fields: List<ZohoField>?,
)

data class ZohoField(
    @SerializedName("api_name")         val apiName        : String,
    @SerializedName("field_label")      val fieldLabel     : String,
    @SerializedName("data_type")        val dataType       : String  = "text",
    @SerializedName("json_type")        val jsonType       : String? = null,
    @SerializedName("length")           val length         : Int?    = null,
    @SerializedName("mandatory")        val mandatory      : Boolean = false,
    @SerializedName("read_only")        val readOnly       : Boolean = false,
    @SerializedName("sequence_number")  val sequenceNumber : Int     = 0,
    @SerializedName("tooltip")          val tooltip        : Tooltip? = null,
    @SerializedName("pick_list_values") val pickListValues : List<PickListValue>? = null,
    @SerializedName("lookup")           val lookup         : LookupMeta? = null,
)

data class Tooltip(
    @SerializedName("name") val name: String?,
)

data class LookupMeta(
    @SerializedName("module") val module: String?,
)

data class PickListValue(
    @SerializedName("display_value") val displayValue: String,
    @SerializedName("actual_value")  val actualValue : String,
)

// GET /users?type=AllUsers
data class UsersResponse(
    @SerializedName("users") val users: List<ZohoUser>?,
)

data class ZohoUser(
    @SerializedName("id")        val id      : String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email")     val email   : String?,
)

// POST /Accounts response
data class CreateRecordResponse(
    @SerializedName("data") val data: List<CreateRecordResult>?,
)

data class CreateRecordResult(
    @SerializedName("code")    val code   : String?,
    @SerializedName("details") val details: CreateRecordDetails?,
    @SerializedName("message") val message: String?,
    @SerializedName("status")  val status : String?,
)

data class CreateRecordDetails(
    @SerializedName("id") val id: String?,
)