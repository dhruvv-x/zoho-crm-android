// data/remote/dto/ModulesResponse.kt
package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── GET /settings/modules ─────────────────────────────────────────────────────

data class ModulesResponse(
    @SerializedName("modules") val modules: List<ZohoModule>?,
)

data class ZohoModule(
    @SerializedName("api_name")        val apiName        : String,
    @SerializedName("module_name")     val moduleName     : String,
    @SerializedName("plural_label")    val pluralLabel    : String?,
    @SerializedName("singular_label")  val singularLabel  : String?,
    @SerializedName("api_supported")   val apiSupported   : Boolean = false,
    @SerializedName("creatable")       val creatable      : Boolean = false,
    @SerializedName("editable")        val editable       : Boolean = false,
    @SerializedName("deletable")       val deletable      : Boolean = false,
    @SerializedName("viewable")        val viewable       : Boolean = false,
    @SerializedName("sequence_number") val sequenceNumber : Int     = 999,
    // "default" | "custom" | "activity" (Tasks / Events / Calls)
    @SerializedName("generated_type")  val generatedType  : String? = null,
)