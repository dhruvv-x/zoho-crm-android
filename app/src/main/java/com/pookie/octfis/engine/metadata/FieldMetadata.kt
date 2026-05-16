package com.pookie.octfis.engine.metadata

import com.pookie.octfis.data.remote.dto.PickListValue

data class FieldMetadata(
    val apiName        : String,
    val label          : String,
    val type           : FieldType,
    val required       : Boolean          = false,
    val readOnly       : Boolean          = false,
    val maxLength      : Int?             = null,
    val sequence       : Int              = 0,
    val sectionName    : String           = "Details",
    val pickListValues : List<PickListValue> = emptyList(),
    val lookupModule   : String?          = null,
    val defaultValue   : String?          = null,
    val tooltip        : String?          = null,
)