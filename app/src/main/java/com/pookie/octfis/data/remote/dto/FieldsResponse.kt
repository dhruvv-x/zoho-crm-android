package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// GET /settings/fields?module=Accounts
data class FieldsResponse(
    @SerializedName("fields") val fields: List<ZohoField>?,
)

data class ZohoField(
    @SerializedName("api_name")         val apiName: String,
    @SerializedName("field_label")      val fieldLabel: String,
    @SerializedName("pick_list_values") val pickListValues: List<PickListValue>?,
)

data class PickListValue(
    @SerializedName("display_value") val displayValue: String,
    @SerializedName("actual_value")  val actualValue: String,
)

// GET /users?type=AllUsers
data class UsersResponse(
    @SerializedName("users") val users: List<ZohoUser>?,
)

data class ZohoUser(
    @SerializedName("id")        val id: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email")     val email: String?,
)

// POST/PUT response (create or update any record)
data class CreateRecordResponse(
    @SerializedName("data") val data: List<CreateRecordResult>?,
)

data class CreateRecordResult(
    @SerializedName("code")    val code: String?,
    // FIX: expanded to capture both success id AND error field name from Zoho
    @SerializedName("details") val details: CreateRecordDetails?,
    @SerializedName("message") val message: String?,
    @SerializedName("status")  val status: String?,
)

data class CreateRecordDetails(
    // Success case: Zoho returns the new record id
    @SerializedName("id") val id: String?,
    // Error case: Zoho returns which field caused INVALID_DATA
    @SerializedName("api_name")           val apiName: String?,
    @SerializedName("expected_data_type") val expectedDataType: String?,
    @SerializedName("index")             val index: Int?,
)