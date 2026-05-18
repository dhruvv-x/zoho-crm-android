// engine/metadata/FieldDisplayResolver.kt
package com.pookie.octfis.engine.metadata

object FieldDisplayResolver {

    private val NON_DISPLAY_TYPES = setOf(
        FieldType.LOOKUP,
        FieldType.OWNER,
        FieldType.FORMULA,
        FieldType.UNKNOWN,
        FieldType.BOOLEAN,
        FieldType.MULTI_SELECT,
        FieldType.RICH_TEXT,
    )

    private val SECONDARY_TYPE_PRIORITY = listOf(
        FieldType.EMAIL,
        FieldType.PHONE,
        FieldType.CURRENCY,
        FieldType.DECIMAL,
        FieldType.PERCENT,
        FieldType.DATE,
        FieldType.DATETIME,
        FieldType.PICKLIST,
        FieldType.TEXT,
    )

    /**
     * Returns the apiName of the best field to use as the record title.
     *
     * Resolution order:
     * 1. "Name"      — Accounts, Contacts, Deals, custom modules
     * 2. "Full_Name" — Leads (computed first+last, always populated)
     * 3. "Subject"   — Tasks, Quotes, Meetings
     * 4. First TEXT field ending with "_Name" or "_Title"
     * 5. First non-readOnly TEXT / EMAIL / PHONE field by sequence
     * 6. First displayable field
     * 7. Absolute fallback: first field regardless of type
     */
    fun resolvePrimaryApiName(meta: List<FieldMetadata>): String {
        val sorted      = meta.sortedBy { it.sequence }
        val displayable = sorted.filter { it.type !in NON_DISPLAY_TYPES }

        val primary =
            displayable.firstOrNull { it.apiName == "Name" }
                ?: displayable.firstOrNull { it.apiName == "Full_Name" }
                ?: displayable.firstOrNull { it.apiName == "Subject" }
                ?: displayable.firstOrNull {
                    it.type == FieldType.TEXT &&
                            (it.apiName.endsWith("_Name") || it.apiName.endsWith("_Title"))
                }
                ?: displayable.firstOrNull {
                    !it.readOnly &&
                            it.type in listOf(FieldType.TEXT, FieldType.EMAIL, FieldType.PHONE)
                }
                ?: displayable.firstOrNull()
                ?: sorted.firstOrNull()

        return primary?.apiName ?: "Name"
    }

    /**
     * Returns the apiName of the best field to use as the record subtitle
     * (secondary display field), or null if nothing suitable exists.
     *
     * Skips the field already chosen as primary.
     */
    fun resolveSecondaryApiName(
        meta          : List<FieldMetadata>,
        primaryApiName: String,
    ): String? {
        val sorted      = meta.sortedBy { it.sequence }
        val displayable = sorted.filter { it.type !in NON_DISPLAY_TYPES }

        return SECONDARY_TYPE_PRIORITY.firstNotNullOfOrNull { preferredType ->
            displayable.firstOrNull { field ->
                field.type == preferredType && field.apiName != primaryApiName
            }
        }?.apiName
    }
}