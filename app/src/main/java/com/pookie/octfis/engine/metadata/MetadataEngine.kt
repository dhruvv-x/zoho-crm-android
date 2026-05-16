package com.pookie.octfis.engine.metadata

import com.pookie.octfis.data.remote.ZohoApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataEngine @Inject constructor(
    private val api: ZohoApiService,
) {
    // In-memory cache — survives config changes, cleared on process kill
    private val cache = mutableMapOf<String, List<FieldMetadata>>()

    suspend fun getModuleMetadata(module: String): Result<List<FieldMetadata>> {
        // 1. Return from cache if available
        cache[module]?.let { return Result.success(it) }

        // 2. Fetch from Zoho API
        return try {
            val response = api.getFields(module)
            val fields   = response.fields
                ?.map { zohoField ->
                    FieldMetadata(
                        apiName       = zohoField.apiName,
                        label         = zohoField.fieldLabel,
                        type          = FieldTypeResolver.resolve(
                            zohoField.dataType,
                            zohoField.jsonType,
                        ),
                        required      = zohoField.mandatory,
                        readOnly      = zohoField.readOnly,
                        maxLength     = zohoField.length,
                        sequence      = zohoField.sequenceNumber,
                        sectionName   = "Details",
                        pickListValues = zohoField.pickListValues ?: emptyList(),
                        lookupModule  = zohoField.lookup?.module,
                        tooltip       = zohoField.tooltip?.name,
                    )
                }
                ?.filter { !it.readOnly }        // skip formula/read-only in forms
                ?.sortedBy { it.sequence }        // respect Zoho field order
                ?: emptyList()

            cache[module] = fields
            Result.success(fields)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun invalidate(module: String) {
        cache.remove(module)
    }

    suspend fun invalidateAll() {
        cache.clear()
    }
}