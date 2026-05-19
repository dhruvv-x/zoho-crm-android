// engine/metadata/MetadataEngine.kt
package com.pookie.octfis.engine.metadata

import com.pookie.octfis.data.remote.ZohoApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataEngine @Inject constructor(
    private val api: ZohoApiService,
) {
    private val cache = mutableMapOf<String, List<FieldMetadata>>()

    // Zoho system fields that must NEVER be editable, regardless of API readOnly flag
    private val immutableFields = setOf(
        "Created_Time", "Modified_Time",
        "Created_By",   "Modified_By",
        "id",
    )

    suspend fun getModuleMetadata(module: String): Result<List<FieldMetadata>> {
        cache[module]?.let { return Result.success(it) }

        return try {
            // ── Parallel fetch: fields + layouts ─────────────────────────────
            val (fieldsResponse, sectionMap) = coroutineScope {
                val fieldsDeferred  = async { api.getFields(module) }
                val layoutsDeferred = async {
                    runCatching { api.getLayouts(module) }.getOrNull()
                }

                val fields  = fieldsDeferred.await()
                val layouts = layoutsDeferred.await()

                // Build apiName → sectionName from the first active layout
                // (status == 0 means active in Zoho; fallback to first layout)
                val activeLayout = layouts?.layouts
                    ?.firstOrNull { it.status == 0 }
                    ?: layouts?.layouts?.firstOrNull()

                val map = mutableMapOf<String, String>()
                activeLayout?.sections?.forEach { section ->
                    section.fields?.forEach { layoutField ->
                        map[layoutField.apiName] = section.name
                    }
                }

                fields to map
            }

            // ── Build FieldMetadata list ──────────────────────────────────────
            val fields = fieldsResponse.fields
                ?.map { zohoField ->
                    FieldMetadata(
                        apiName        = zohoField.apiName,
                        label          = zohoField.fieldLabel,
                        type           = FieldTypeResolver.resolve(
                            zohoField.dataType,
                            zohoField.jsonType,
                        ),
                        required       = zohoField.mandatory,
                        // Force readOnly if Zoho says so OR it's a known system field
                        readOnly       = zohoField.readOnly || zohoField.apiName in immutableFields,
                        maxLength      = zohoField.length,
                        sequence       = zohoField.sequenceNumber,
                        sectionName    = sectionMap[zohoField.apiName] ?: "Details",
                        pickListValues = zohoField.pickListValues ?: emptyList(),
                        lookupModule   = zohoField.lookup?.module,
                        defaultValue   = zohoField.defaultValue,
                        tooltip        = zohoField.tooltip?.name,
                    )
                }
                ?.filter { field ->
                    // Only drop server-computed formula fields and truly unmapped types.
                    // readOnly fields (Created_Time, Modified_Time, Created_By, etc.)
                    // are intentionally kept — they are shown as locked display fields
                    // in edit mode and excluded from the API payload.
                    field.type != FieldType.FORMULA && field.type != FieldType.UNKNOWN
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