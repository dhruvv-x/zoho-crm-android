// data/remote/dto/RelatedListsResponse.kt
package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── GET /settings/related_lists?module=X ──────────────────────────────────────

data class RelatedListsResponse(
    @SerializedName("related_lists") val relatedLists: List<ZohoRelatedList>?,
)

data class ZohoRelatedList(
    // The Zoho API name of the related module, e.g. "Contacts", "Deals"
    @SerializedName("api_name")       val apiName      : String,
    // The display label shown to the user, e.g. "Contacts", "Open Deals"
    @SerializedName("display_label")  val displayLabel : String?,
    // The module name used for record fetching (may differ from api_name in some modules)
    @SerializedName("module")         val module       : String?,
    // Sequence number for ordering sections
    @SerializedName("sequence_number") val sequence    : Int = 999,
    // "default" | "custom"
    @SerializedName("type")           val type         : String? = null,
    // Whether this related list is visible/accessible
    @SerializedName("visible")        val visible      : Boolean = true,
)