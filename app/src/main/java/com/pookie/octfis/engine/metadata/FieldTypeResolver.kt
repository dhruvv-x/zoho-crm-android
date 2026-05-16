package com.pookie.octfis.engine.metadata

object FieldTypeResolver {

    fun resolve(dataType: String, jsonType: String? = null): FieldType {
        return when (dataType.trim().lowercase()) {

            // Text
            "text"                          -> FieldType.TEXT
            "email"                         -> FieldType.EMAIL
            "phone"                         -> FieldType.PHONE
            "website", "url"                -> FieldType.URL
            "textarea"                      -> FieldType.TEXTAREA
            "rich_text", "richtext"         -> FieldType.RICH_TEXT

            // Numeric
            "integer"                       -> FieldType.INTEGER
            "double", "decimal"             -> FieldType.DECIMAL
            "currency"                      -> FieldType.CURRENCY
            "percent"                       -> FieldType.PERCENT

            // Selection
            "picklist"                      -> FieldType.PICKLIST
            "multiselectpicklist",
            "multi_select_picklist"         -> FieldType.MULTI_SELECT
            "boolean", "checkbox"           -> FieldType.BOOLEAN

            // Date
            "date"                          -> FieldType.DATE
            "datetime", "event_reminder"    -> FieldType.DATETIME

            // Relations
            "lookup"                        -> FieldType.LOOKUP
            "ownerlookup", "owner_lookup"   -> FieldType.OWNER

            // Formula / computed
            "formula"                       -> FieldType.FORMULA

            // Fallback — use jsonType as secondary hint
            else                            -> resolveFromJsonType(jsonType)
        }
    }

    private fun resolveFromJsonType(jsonType: String?): FieldType {
        return when (jsonType?.trim()?.lowercase()) {
            "string"  -> FieldType.TEXT
            "integer" -> FieldType.INTEGER
            "double"  -> FieldType.DECIMAL
            "boolean" -> FieldType.BOOLEAN
            else      -> FieldType.UNKNOWN
        }
    }
}