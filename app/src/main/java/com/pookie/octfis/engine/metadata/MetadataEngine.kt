// engine/metadata/MetadataEngine.kt
package com.pookie.octfis.engine.metadata

import com.pookie.octfis.data.remote.ZohoApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataEngine @Inject constructor(
    private val api: ZohoApiService,
) {
    private val cache = mutableMapOf<String, List<FieldMetadata>>()

    suspend fun getModuleMetadata(module: String): Result<List<FieldMetadata>> {
        cache[module]?.let { return Result.success(it) }

        return try {
            val response = api.getFields(module)
            val fields = response.fields
                ?.map { zohoField ->
                    FieldMetadata(
                        apiName        = zohoField.apiName,
                        label          = zohoField.fieldLabel,
                        type           = FieldTypeResolver.resolve(
                            zohoField.dataType,
                            zohoField.jsonType,
                        ),
                        required       = zohoField.mandatory,
                        readOnly       = zohoField.readOnly,
                        maxLength      = zohoField.length,
                        sequence       = zohoField.sequenceNumber,
                        sectionName    = "Details",
                        pickListValues = zohoField.pickListValues ?: emptyList(),
                        lookupModule   = zohoField.lookup?.module,
                        tooltip        = zohoField.tooltip?.name,
                    )
                }
                // FIXED: only skip purely cosmetic read-only non-mandatory fields
                // (formula fields, auto-number, system timestamps).
                // Mandatory read-only fields (Owner, some lookups) are kept so
                // the form can display and pre-fill them in edit mode.
                ?.filter { field ->
                    when {
                        field.type == FieldType.FORMULA  -> false   // always computed
                        field.type == FieldType.UNKNOWN  -> false   // unknown = can't render
                        field.readOnly && !field.required -> false   // cosmetic read-only
                        else                             -> true
                    }
                }
                ?.sortedBy { it.sequence }
                ?: emptyList()

            cache[module] = fields
            Result.success(fields)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun invalidate(module: String) { cache.remove(module) }
    fun invalidateAll() { cache.clear() }
}